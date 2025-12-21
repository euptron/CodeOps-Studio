package com.eup.codeopsstudio.git;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eup.codeopsstudio.MainActivity;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.logger.adapter.LogAdapter;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.TextWatcherAdapter;
import com.eup.codeopsstudio.databinding.LayoutDialogTextInputBinding;
import com.eup.codeopsstudio.databinding.LayoutLoggingSheetBinding;
import com.eup.codeopsstudio.git.listeners.CloneListener;
import com.eup.codeopsstudio.git.task.CloneTask;
import com.eup.codeopsstudio.logger.Logger;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.viewmodel.FileViewModel;
import com.eup.codeopsstudio.viewmodel.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the user interface for interacting with Git commands. This class provides methods to
 * display information to the user and potentially gather input, although its current implementation
 * is basic.
 *
 * @author Etido Peter
 */
public class GitUI {
  public static final String TAG = "GitUI";
  private final Context context;
  private final Logger logger;
  private final LogAdapter logAdapter;
  private final MainViewModel model;
  private final FileViewModel fileViewModel;
  private final LifecycleOwner lifecycleOwner;
  private final FragmentActivity activity;
  private LayoutLoggingSheetBinding logSheetBinding;
  private LayoutDialogTextInputBinding inputBinding;
  private CloneCompleteListener cloneCompleteListener;

  public GitUI(@NonNull Context context) {
    Objects.requireNonNull(context, "Context is null cannot init UI");
    this.context = context;
    this.activity = inFragmentActivity(context);
    Objects.requireNonNull(activity, "Context is not instance of activity");

    this.lifecycleOwner = activity;
    this.logAdapter = new LogAdapter();
    this.logger = new Logger(Logger.LogClass.IDE);
    if (context instanceof ViewModelStoreOwner currentVMScope) {
      this.logger.attach(currentVMScope);
    }
    this.model = new ViewModelProvider(activity).get(MainViewModel.class);
    this.fileViewModel = new ViewModelProvider(activity).get(FileViewModel.class);
  }

  private FragmentActivity inFragmentActivity(@NonNull Context c) {
    if (c instanceof FragmentActivity act) {
      return act;
    } else {
      return null;
    }
  }

  public void showCloneDialog(CloneCompleteListener listener) {
    this.cloneCompleteListener = listener;

    logger.d(TAG, context.getString(R.string.initializing));
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(R.string.clone_git_repo);
    inputBinding = LayoutDialogTextInputBinding.inflate(LayoutInflater.from(context));
    logSheetBinding = LayoutLoggingSheetBinding.inflate(LayoutInflater.from(context));
    setupInputFields();
    setupValidation(builder);

    AlertDialog dialog = builder.create();
    dialog.setOnShowListener(d -> configurePositiveButton(dialog));
    dialog.show();
    fileViewModel.monitorMessages(lifecycleOwner, this::logFileSelectionError);
    fileViewModel.observePickedFolders(lifecycleOwner, this::handlePickedFolder);
  }

  private void configurePositiveButton(@NonNull AlertDialog dialog) {
    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
    positiveButton.setEnabled(false);

    if (inputBinding.tilOther.getEditText() != null) {
      inputBinding
          .tilOther
          .getEditText()
          .addTextChangedListener(
              new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(@NonNull Editable editable) {
                  validatePathExistence(positiveButton, editable);
                }
              });
    }
    positiveButton.setOnClickListener(v -> startCloneOperation(dialog));
  }

  private boolean validatePathExistence(Button positiveButton, Editable editable) {
    String path = editable.toString();
    String url = getUrl();

    if (Wizard.isEmpty(url) || Wizard.isEmpty(path)) {
      return false;
    }

    File output = new File(path, RepoConfig.extractRepoNameFromUri(getUrl()));

    if (output.exists()) {
      positiveButton.setEnabled(false);
      inputBinding.tilOther.setError(context.getString(R.string.msg_repo_dir_already_exists));
      // invoked after error message is set so layout resize
      inputBinding.tilOther.setErrorEnabled(true);
      requestFocus(inputBinding.tilOther.getEditText());
      BaseUtil.toastLong(R.string.msg_repo_dir_already_exists);
      return true;
    } else {
      positiveButton.setEnabled(true);
      inputBinding.tilOther.setErrorEnabled(false);
      return false;
    }
  }

  @Nullable
  private String getUrl() {
    return inputBinding.tilName.getEditText() != null
        ? inputBinding.tilName.getEditText().getText().toString()
        : null;
  }

  private void requestFocus(@Nullable EditText editText) {
    if (editText != null) {
      editText.requestFocus();
    }
  }

  private void startCloneOperation(@NonNull AlertDialog dialog) {
    if (!isValidUrl()) {
      ILog.warning(TAG, "Failed to start clone operation, url is null");
      inputBinding.tilName.setError(context.getString(R.string.msg_repo_url_required));
      // invoked after error message is set so layout resize
      inputBinding.tilName.setErrorEnabled(true);
      requestFocus(inputBinding.tilOther.getEditText());
      BaseUtil.toastLong(R.string.msg_repo_url_required);
      return;
    }

    if (!isValidPath()) {
      ILog.warning(TAG, "Failed to start clone operation, path is null");
      inputBinding.tilOther.setError(context.getString(R.string.msg_repo_dir_required));
      // invoked after error message is set so layout resize
      inputBinding.tilOther.setErrorEnabled(true);
      requestFocus(inputBinding.tilOther.getEditText());
      BaseUtil.toastLong(R.string.msg_repo_dir_required);
      return;
    }

    String url = getUrl();
    if (url == null) {
      ILog.debug(TAG, "Aborting since url == null");
      return;
    }
    String directory = getPath();

    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
    EditText saveLocationEditText = inputBinding.tilOther.getEditText();

    if (saveLocationEditText == null) {
      ILog.debug(TAG, "Aborting since SaveLocation edittext == null");
      return;
    }
    Editable editable = saveLocationEditText.getText();

    if (validatePathExistence(positiveButton, editable)) return;

    if (!url.endsWith(".git")) {
      url += ".git";
    }

    var sheetDialog = new BottomSheetDialog(context);
    final var output = new File(directory, RepoConfig.extractRepoNameFromUri(url));
    sheetDialog.setContentView(logSheetBinding.getRoot());
    sheetDialog.setCancelable(false);
    logSheetBinding.title.setText(context.getString(R.string.cloning_repo));
    logSheetBinding.progressbar.setProgress(100);
    logSheetBinding.loggingList.setLayoutManager(new LinearLayoutManager(context));
    logSheetBinding.loggingList.setAdapter(logAdapter);
    sheetDialog.show();

    model
        .getIDELogs()
        .observe(
            lifecycleOwner,
            data -> {
              logAdapter.submitList(data);
              scrollToLastItem();
            });

    CloneListener listener =
        new CloneListener() {
          @Override
          public void onCloneComplete(File file) {
            if (file != null && file.exists()) {
              AsyncTask.runOnUiThread(() -> cloneCompleteListener.onCloneCompleted(file));
            }
          }

          @Override
          public void onCloneFailed(String e) {
            AsyncTask.runOnUiThread(
                () -> {
                  new MaterialAlertDialogBuilder(context)
                      .setTitle(context.getString(R.string.msg_failed_to_clone_git_repo))
                      .setMessage(e)
                      .setPositiveButton(android.R.string.ok, null)
                      .setCancelable(false)
                      .show();
                  logger.e(
                      TAG,
                      context.getString(R.string.msg_failed_to_clone_git_repo)
                          + " ["
                          + context.getString(R.string.cause)
                          + "] "
                          + e);
                });
          }

          @Override
          public void onProgress(int progress) {
            AsyncTask.runOnUiThread(
                () -> logSheetBinding.progressbar.setProgressCompat(progress, true));
          }

          @Override
          public void onUpdateMessage(String message) {
            AsyncTask.runOnUiThread(() -> logger.d(TAG, message));
          }
        };

    // TODO: Handle Authentication
    logger.d(TAG, context.getString(R.string.cloning_into) + output + " ...");

    // start clone
    String userName = "";
    String userEmail = "";
    char[] userPassword = new char[0];
    boolean isUserAdmin = true;
    UserConfig userConfig = new UserConfig(userName, userEmail, userPassword, isUserAdmin);
    RepoConfig repoConfig = new RepoConfig(userConfig);
    repoConfig.setRemoteURI(url);
    repoConfig.setLocalURI(output.getAbsolutePath());
    repoConfig.setName(RepoConfig.extractRepoNameFromUri(url));

    CloneTask cloneTask = new CloneTask(repoConfig, listener);
    cloneTask.setCloneType(CloneTask.CloneType.PUBLIC);
    CompletableFuture<Repository> task = AsyncTask.runProvideError(cloneTask);

    task.whenComplete(
        (result, throwable) ->
            AsyncTask.runOnUiThread(
                () -> {
                  clearLogs();
                  if (dialog.isShowing()) dialog.dismiss();

                  if (throwable != null) {
                    listener.onCloneFailed(throwable.getMessage());
                  } else if (result != null) {
                    var repoName = cloneTask.getRepoConfig().getName();
                    var repoLocalPath = output.getAbsolutePath();
                    var msg =
                        context.getString(R.string.msg_git_clone_success, repoName, repoLocalPath);
                    logger.d(TAG, msg);
                  }
                }));

    logSheetBinding.btnClose.setOnClickListener(
        v -> {
          cloneTask.cancel();
          task.cancel(true);
          clearLogs();
          sheetDialog.dismiss();
        });
  }

  private boolean isValidPath() {
    String path = getPath();
    return !Wizard.isEmpty(path);
  }

  @Nullable
  private String getPath() {
    return inputBinding.tilOther.getEditText() != null
        ? inputBinding.tilOther.getEditText().getText().toString()
        : null;
  }

  private boolean isValidUrl() {
    String url = getUrl();
    return !Wizard.isEmpty(url);
  }

  private void scrollToLastItem() {
    int itemCount = logAdapter.getItemCount();
    if (itemCount > 0) {
      logSheetBinding.loggingList.scrollToPosition(itemCount - 1);
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  private void clearLogs() {
    logger.clear();
    logAdapter.notifyDataSetChanged();
  }

  private void setupInputFields() {
    inputBinding.tilName.setHint(context.getString(R.string.repository_url));
    inputBinding.tilOther.setVisibility(View.VISIBLE);
    inputBinding.tilOther.setHint(context.getString(R.string.save_location));
    inputBinding.tilOther.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
    inputBinding.tilOther.setEndIconDrawable(R.drawable.ic_folder_outline);
    inputBinding.tilOther.setEndIconOnClickListener(v -> openFolderPicker());
  }

  private void openFolderPicker() {
    MainActivity mainActivity = (MainActivity) activity;
    mainActivity.getLifecycleObserver().pickFolder();
  }

  private void setupValidation(@NonNull MaterialAlertDialogBuilder builder) {
    builder.setView(inputBinding.getRoot());
    builder.setPositiveButton(R.string.clone, null);
    builder.setNegativeButton(android.R.string.cancel, null);
  }

  private void logFileSelectionError(@NonNull Pair<Exception, String> pair) {
    String message = pair.second;
    if (message == null) return;
    logger.e(
        TAG,
        context.getString(R.string.folder_selection_error)
            + " ["
            + context.getString(R.string.cause)
            + "] "
            + message);
  }

  private void handlePickedFolder(@NonNull File file) {
    String folderPath = file.getAbsolutePath();
    Objects.requireNonNull(inputBinding.tilOther.getEditText()).setText(folderPath);
    logger.d(TAG, context.getString(R.string.folder_selection_success));
  }

  public interface CloneCompleteListener {
    void onCloneCompleted(File file);
  }
}
