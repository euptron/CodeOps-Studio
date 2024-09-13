/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024 EUP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
   package com.eup.codeopsstudio.git;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutLoggingSheetBinding;
import com.eup.codeopsstudio.logging.LogAdapter;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitRepository {

	public interface CloneListener {
		void onCloneSuccess(File file);

		void onCloneFailed(String e);

		void onUpdateMessage(String message);

		void onProgress(int progress);
	}
	
	public interface CloneCompleteListener {
		void onCloneCompleted(File file);
	}

	private CloneListener listener;
	private CloneCompleteListener cloneCompleteListener;
	private Context context;
	private Git git = null;
	private Logger logger;
	private LogAdapter logAdapter;
	private MainViewModel model;
	private AlertDialog alertDialog;
	private LifecycleOwner lifecycleOwner;
	private ActivityResultLauncher<Intent> mStartForResult;
	private LayoutDialogTextInputBinding dialogTextInputBinding;
	private LayoutLoggingSheetBinding layoutLoggingSheetBinding;
	private String username;
	// personal access token
	private String password;
	private static final String LOG_TAG = "Git Clone GUI";

	public GitRepository(Context context, LifecycleOwner lifecycleOwner) {
		this.context = context;
		this.lifecycleOwner = lifecycleOwner;
        logAdapter = new LogAdapter();
		logger = new Logger(Logger.LogClass.IDE);
		logger.attach(((ViewModelStoreOwner) context));
		mStartForResult = ((FragmentActivity) context)
				.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
					if (result.getResultCode() == Activity.RESULT_OK) {
						Intent intent = result.getData();
						if (intent != null) {
							Uri folderUri = intent.getData();
							if (folderUri != null) {
								onFolderSelected(folderUri);
							}
						}
					}
				});
	}
	
	public void setCloneCompletionListener(CloneCompleteListener cloneCompleteListener) {
     this.cloneCompleteListener = cloneCompleteListener;
    }

	public void initalize() {
	    logger.d(LOG_TAG, context.getString(R.string.initializing));
		dialogTextInputBinding = LayoutDialogTextInputBinding.inflate(LayoutInflater.from(context));
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
		builder.setTitle(R.string.clone_git_repo);

		builder.setView(dialogTextInputBinding.getRoot());
		dialogTextInputBinding.tilOther.setVisibility(View.VISIBLE);
		dialogTextInputBinding.tilName.setHint(context.getString(R.string.repository_url));

		dialogTextInputBinding.tilOther.setHint(context.getString(R.string.save_location));
		dialogTextInputBinding.tilOther.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
		dialogTextInputBinding.tilOther.setEndIconDrawable(R.drawable.ic_folder_outline);
		dialogTextInputBinding.tilOther.setEndIconOnClickListener(v -> {
			openFolder();
		});

		builder.setPositiveButton(context.getString(R.string.clone), (dialog, which) -> {
			String url = dialogTextInputBinding.tilName.getEditText().getText().toString();
			String localPath = dialogTextInputBinding.tilOther.getEditText().getText().toString();
			if ((url != null && localPath != null) && (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(localPath))) {
				String url2 = url.trim();
				url2 = url.trim();
				if (!url2.endsWith(".git")) {
					url2 += ".git";
				}
				cloneRepository(url2, localPath);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		alertDialog = builder.create();

		alertDialog.setOnShowListener(d -> {
			final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setEnabled(false);

			dialogTextInputBinding.tilOther.getEditText().addTextChangedListener(new TextWatcherAdapter() {
				@Override
				public void afterTextChanged(Editable editable) {
					String url = dialogTextInputBinding.tilName.getEditText().getText().toString();
					final File output = new File(editable.toString(), extractRepositoryNameFromURL(url));
					if ((output!= null && output.exists()) && (url != null && !TextUtils.isEmpty(url))) {
						positiveButton.setEnabled(false);
						dialogTextInputBinding.tilOther.setErrorEnabled(false);
						dialogTextInputBinding.tilOther.setError(context.getString(R.string.msg_repo_dir_already_exists));
					} else {
						positiveButton.setEnabled(true);
						if (dialogTextInputBinding.tilOther.isErrorEnabled()) {
							dialogTextInputBinding.tilOther.setErrorEnabled(false);
						}
					}
				}
			});
		});

		alertDialog.show();
	}

	private void cloneRepository(String url, String directory) {
		layoutLoggingSheetBinding = LayoutLoggingSheetBinding.inflate(LayoutInflater.from(context));

		BottomSheetDialog sheetDialog = new BottomSheetDialog(context);

		model = new ViewModelProvider(((ViewModelStoreOwner) context)).get(MainViewModel.class);
		sheetDialog.setContentView(layoutLoggingSheetBinding.getRoot());
		sheetDialog.setCancelable(false);
		layoutLoggingSheetBinding.title.setText(context.getString(R.string.cloning_repo));
        
		layoutLoggingSheetBinding.progressbar.setProgress(100);
		
		layoutLoggingSheetBinding.loggingList.setLayoutManager(new LinearLayoutManager(context));
		layoutLoggingSheetBinding.loggingList.setAdapter(logAdapter);

		model.getIDELogs().observe(lifecycleOwner, data -> {
			logAdapter.submitList(data);
			scrollToLastItem();
		});

		listener = new CloneListener() {
			@Override
			public void onCloneSuccess(File file) {
				if (file != null && file.exists()) {
                    cloneCompleteListener.onCloneCompleted(file);
				}
			}

			@Override
			public void onCloneFailed(String e) {
				ThreadUtils.runOnUiThread(() -> {
					new MaterialAlertDialogBuilder(context).setTitle(context.getString(R.string.msg_failed_to_clone_git_repo)).setMessage(e)
							.setPositiveButton(android.R.string.ok, null).setCancelable(false).show();
			     logger.e(LOG_TAG, context.getString(R.string.msg_failed_to_clone_git_repo) + " [" + context.getString(R.string.cause) + "] " + e);
				});
			}

			@Override
			public void onUpdateMessage(String message) {
				ThreadUtils.runOnUiThread(() -> {
					logger.d(LOG_TAG, message);
				});
			}

			@Override
			public void onProgress(int progress) {
				ThreadUtils.runOnUiThread(() -> {
					layoutLoggingSheetBinding.progressbar.setProgressCompat(progress, true);
				});
			}
		};

		final File output = new File(directory, extractRepositoryNameFromURL(url));

		logger.d(LOG_TAG, context.getString(R.string.cloning_into) + Constants.SPACE + output + "...");
		BatchProgressMonitor monitor = new BatchProgressMonitor(url);

		CompletableFuture<Git> task = AsyncTask.runProvideError(() -> {
			CloneCommand cloneCommand = Git.cloneRepository();
			cloneCommand.setURI(url).setDirectory(output).setProgressMonitor(monitor);
			if ((username != null && password != null)
					&& (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password))) {
				cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
			}
			git = cloneCommand.call();
			return git;
		});

		layoutLoggingSheetBinding.btnClose.setOnClickListener(v -> {
			task.cancel(true);
			clearLogs();
			sheetDialog.dismiss();
		});

		sheetDialog.show();

		task.whenComplete((result, throwable) -> {
			ThreadUtils.runOnUiThread(() -> {
				clearLogs();
				sheetDialog.dismiss();
				if (result != null && throwable == null) {
					result.close();
					logger.d(LOG_TAG, context.getString(R.string.msg_repo_cloned_successfully) + Constants.SPACE + output);
					listener.onCloneSuccess(output);
					return;
				}

				if (throwable instanceof InvalidRemoteException) {
					listener.onCloneFailed(context.getString(R.string.msg_invalid_remote) + "\n" + throwable.getMessage());
				} else if (throwable instanceof TransportException) {
					listener.onCloneFailed(throwable.getMessage());
				} else if (throwable instanceof GitAPIException) {
					listener.onCloneFailed(context.getString(R.string.msg_clone_failed) + "\n" + throwable.getMessage());
				} else if (throwable instanceof JGitInternalException) {
					if (throwable.getCause() instanceof NotSupportedException) {
						listener.onCloneFailed(context.getString(R.string.msg_invalid_remote));
					} else {
						listener.onCloneFailed(throwable.getMessage());
					}
				} else if (throwable instanceof OutOfMemoryError) {
					listener.onCloneFailed(context.getString(R.string.msg_out_of_memory) + "\n" + throwable.getMessage());
				} else {
					listener.onCloneFailed(throwable.getMessage());
				}
			});
		});
	}

	public void setAuthenticationDetails(String username, String password) {
	    if ((username != null && password != null)
					&& (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password))) {
		  this.username = username.trim();
		  this.password = password.trim();
		}
	}

	private void clearLogs() {
		if (logger != null) {
			logger.clear();
			logAdapter.notifyDataSetChanged();
		}
	}

	private void scrollToLastItem() {
		int itemCount = logAdapter.getItemCount();
		if (itemCount > 0) {
			layoutLoggingSheetBinding.loggingList.scrollToPosition(itemCount - 1);
		}
	}

	private String extractRepositoryNameFromURL(String url) {
		String repositoryName = "";
		int lastSlashIndex = url.lastIndexOf("/");

		if (lastSlashIndex >= 0 && lastSlashIndex < url.length() - 1) {
			repositoryName = url.substring(lastSlashIndex + 1);

			if (repositoryName.endsWith(".git")) {
				repositoryName = repositoryName.substring(0, repositoryName.length() - 4);
			}
		}
		return repositoryName;
	}

	private class BatchProgressMonitor extends BatchingProgressMonitor {

		private String url;

		public BatchProgressMonitor(String url) {
			this.url = url;
		}

		@Override
		protected void onUpdate(String taskName, int workCurr) {
			String msg = String.format("[%s] %s %d", url, taskName, workCurr);
			listener.onUpdateMessage(msg);
		}

		@Override
		protected void onEndTask(String taskName, int workCurr) {
			String msg = String.format("[%s] %s %d", url, taskName, workCurr);
			listener.onUpdateMessage(msg);
		}

		@Override
		protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
			String msg = String.format("[%s] %s (%d/%d) %d", url, taskName, workCurr, workTotal, percentDone);
			listener.onUpdateMessage(msg);
			listener.onProgress(percentDone);
		}

		@Override
		protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
			String msg = String.format("[%s] %s (%d/%d) %d", url, taskName, workCurr, workTotal, percentDone);
			listener.onUpdateMessage(msg);
		}
	}

	private void openFolder() {
		mStartForResult.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
	}

	private void onFolderSelected(Uri uri) {
		try {
			DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
			File file = new File(FileUtil.getPathFromUri(context, pickedDir.getUri()));
			String folderPath = file.getAbsolutePath();
			if (folderPath != null) {
				dialogTextInputBinding.tilOther.getEditText().setText(folderPath);
			}
			logger.d(LOG_TAG, context.getString(R.string.folder_selection_success));
		} catch (Exception e) {
			// TODO: Handle
			logger.e(LOG_TAG, context.getString(R.string.folder_selection_error) + " [" + context.getString(R.string.cause) + "] " + e);
		}
	}
}
