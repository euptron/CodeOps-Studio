/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.file;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.eup.codeopsstudio.common.util.FileUtil;
import com.eup.codeopsstudio.logging.Logger;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutDialogProgressBinding;
import com.eup.codeopsstudio.res.databinding.LayoutDialogTextInputBinding;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import org.apache.commons.io.FileUtils;

/**
 * A class for managing files globally.
 *
 * <p>All file transactions are carried out using this class.
 *
 * @since 28-7-2023
 */
public class FileManager {

  private Context context;
  private String SPACE = " ";
  private Logger logger;
  public static final String LOG_TAG = "FileManager";

  public FileManager(Context context, FragmentActivity activity) {
    this.context = context;
    logger = new Logger(Logger.LogClass.IDE);
    logger.attach(activity);
  }

  public static boolean isNonOpenableFile(File file) {
    return !file.getName()
        .endsWith(
            ".*\\.(wav|pdf|amr|bin|ttf|png|jpe?g|bmp|mp4|mp3|m4a|iso|so|zip|jar|dex|odex|vdex|7z|apk|apks|xapk)$");
  }

  public static String getFileName(String path) {
    return new File(path).getName();
  }

  public static String getFilePath(String path) {
    return new File(path).getAbsolutePath();
  }

  public interface TaskListener {
    void onTaskComplete(Object object);
  }

  public static final Comparator<File> FILE_FIRST_ORDER =
      (file1, file2) -> {
        if (file1.isFile() && file2.isDirectory()) {
          return 1;
        } else if (file2.isFile() && file1.isDirectory()) {
          return -1;
        } else {
          return String.CASE_INSENSITIVE_ORDER.compare(file1.getName(), file2.getName());
        }
      };

  public static class SortFileName implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
      return f1.getName().compareTo(f2.getName());
    }
  }

  public static class SortFolder implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
      if (f1.isDirectory() == f2.isDirectory()) return 0;
      else if (f1.isDirectory() && !f2.isDirectory()) return -1;
      else return 1;
    }
  }

  public void startFileTask(
      final FileAction fileAction, final File file, final TaskListener listener) {
    String title = "";
    String message = "";
    String hint = "";
    String fileName = "";
    String positiveButtonLabel = "";
    String negativeButtonLabel = "";

    if (fileAction == FileAction.CREATE_FILE) {
      title = context.getString(R.string.new_file);
      message =
          context.getString(R.string.prompt_contains_slashes)
              + "\n\n"
              + context.getString(R.string.destination_path, file.getAbsolutePath());
      hint = context.getString(R.string.prompt_file_name);
      positiveButtonLabel = context.getString(R.string.create);
      negativeButtonLabel = context.getString(R.string.cancel);
    } else if (fileAction == FileAction.CREATE_FOLDER) {
      title = context.getString(R.string.new_folder);
      message = context.getString(R.string.prompt_contains_slashes);
      hint = context.getString(R.string.prompt_folder_name);
      positiveButtonLabel = context.getString(R.string.create);
      negativeButtonLabel = context.getString(R.string.cancel);
    } else if (fileAction == FileAction.DELETE_FILE) {
      title = context.getString(R.string.confirm_delete);
      message =
          context.getString(
              R.string.msg_confirm_delete, file.getName() + "[" + file.getAbsolutePath() + "]");
      positiveButtonLabel = context.getString(R.string.yes);
      negativeButtonLabel = context.getString(R.string.no);
    } else if (fileAction == FileAction.DELETE_FOLDER) {
      title = context.getString(R.string.confirm_delete);
      message =
          context.getString(
              R.string.msg_confirm_delete, file.getName() + "[" + file.getAbsolutePath() + "]");
      positiveButtonLabel = context.getString(R.string.yes);
      negativeButtonLabel = context.getString(R.string.no);
    } else if (fileAction == FileAction.RENAME_FILE) {
      fileName = file.getName();
      title = context.getString(R.string.rename);
      message = context.getString(R.string.msg_rename, context.getString(R.string.file));
      hint = context.getString(R.string.prompt_new_name);
      positiveButtonLabel = context.getString(R.string.rename);
      negativeButtonLabel = context.getString(R.string.cancel);
    } else if (fileAction == FileAction.RENAME_FOLDER) {
      fileName = file.getName();
      title = context.getString(R.string.rename);
      message = context.getString(R.string.msg_rename, context.getString(R.string.folder));
      hint = context.getString(R.string.prompt_new_name);
      positiveButtonLabel = context.getString(R.string.yes);
      negativeButtonLabel = context.getString(R.string.cancel);
    }
    LayoutDialogTextInputBinding binding =
        LayoutDialogTextInputBinding.inflate(LayoutInflater.from(context));
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(title);
    builder.setMessage(message);
    if (fileAction == FileAction.CREATE_FILE
        || fileAction == FileAction.CREATE_FOLDER
        || fileAction == FileAction.RENAME_FILE
        || fileAction == FileAction.RENAME_FOLDER) {
      builder.setView(binding.getRoot());
      binding.tilName.setHint(hint);
      binding.tilName.getEditText().setText(fileName);
    }

    builder.setPositiveButton(
        positiveButtonLabel,
        (dialog, which) -> {
          executeFileTask(
              fileAction, binding.tilName.getEditText().getText().toString(), file, listener);
        });

    builder.setNegativeButton(negativeButtonLabel, null);
    builder.show();
  }

  @SuppressWarnings("deprecation")
  private void executeFileTask(
      final FileAction fileAction,
      final String newFileName,
      final File file,
      final TaskListener listener) {
    new AsyncTask<Void, Integer, Object>() {

      @Override
      protected void onPreExecute() {
        if (fileAction == FileAction.DELETE_FILE) {
          showProgressDialog(fileAction, file);
        } else if (fileAction == FileAction.DELETE_FOLDER) {
          showProgressDialog(fileAction, file);
        }
      }

      @Override
      protected Object doInBackground(Void... params) {
        if (fileAction == FileAction.CREATE_FILE) {
          return createFile(file, newFileName);
        } else if (fileAction == FileAction.CREATE_FOLDER) {
          return createFolder(file, newFileName);
        } else if (fileAction == FileAction.DELETE_FILE) {
          return deleteFile(file);
        } else if (fileAction == FileAction.DELETE_FOLDER) {
          return deleteFolder(file);
        } else if (fileAction == FileAction.RENAME_FILE) {
          return renameFile(file, newFileName);
        } else if (fileAction == FileAction.RENAME_FOLDER) {
          return renameFolder(file, newFileName);
        }
        return null;
      }

      @Override
      protected void onPostExecute(Object result) {
        if (fileAction == FileAction.DELETE_FILE) {
          hideProgressDialog();
        } else if (fileAction == FileAction.DELETE_FOLDER) {
          hideProgressDialog();
        }
        if (result != null && result instanceof Boolean) {
          listener.onTaskComplete((Boolean) result);
        } else if (result != null && result instanceof File) {
          if (result != null && result instanceof File) {
            listener.onTaskComplete((File) result);
          }
        }
      }
    }.execute();
  }

  private void showProgressDialog(FileAction fileAction, File file) {
    LayoutDialogProgressBinding binding =
        LayoutDialogProgressBinding.inflate(LayoutInflater.from(context));
    
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(getProgressDialogTitle(fileAction, file));
    builder.setView(binding.getRoot());
    builder.setCancelable(false);
    alertDialog = builder.show();
  }

  private void hideProgressDialog() {
    if (alertDialog != null && alertDialog.isShowing()) {
      alertDialog.dismiss();
    }
  }

  private String getProgressDialogTitle(FileAction fileAction, File file) {
    if (fileAction == FileAction.DELETE_FILE) {
      return context.getString(R.string.msg_deleting_action, SPACE + file.getName());
    } else if (fileAction == FileAction.DELETE_FOLDER)
      return context.getString(R.string.msg_deleting_action, SPACE + file.getName());
    return context.getString(R.string.processing);
  }

  private AlertDialog alertDialog;

  private boolean deleteFile(File file) {
    if (!file.isFile()) return false;
    return FileUtils.deleteQuietly(file);
  }

  private boolean deleteFolder(File file) {
    try {
      if (!file.isDirectory()) return false;
      FileUtils.deleteDirectory(file);
      return true;
    } catch (IOException e) {
      logger.e(
          LOG_TAG,
          context.getString(R.string.failed_to_delete_folder)
              + " ["
              + context.getString(R.string.cause)
              + "] "
              + e.getMessage());
      return false;
    }
  }

  private File renameFile(File file, String newName) {
    if (FileUtil.rename(file, newName))
      return new File(file.getParent() + File.separator + newName);
    else {
      logger.e(
          LOG_TAG,
          context.getString(R.string.failed_to_rename_file) + " [" + file.getAbsolutePath() + "] ");
      return null;
    }
  }

  private File renameFolder(File folder, String newFolderName) {
    if (FileUtil.rename(folder, newFolderName))
      return new File(folder.getParent() + File.separator + newFolderName);
    else {
      logger.e(
          LOG_TAG,
          context.getString(R.string.failed_to_rename_folder)
              + " ["
              + folder.getAbsolutePath()
              + "] ");
      return null;
    }
  }

  private File createFile(File currentDir, String fileName) {
    File newFile = new File(currentDir, fileName);
    try {
      if (!newFile.exists()) newFile.createNewFile();
      return newFile;
    } catch (IOException e) {
      logger.e(
          LOG_TAG,
          context.getString(R.string.failed_to_create_file)
              + " ["
              + context.getString(R.string.cause)
              + "] "
              + e.getMessage());
      return null;
    }
  }

  private File createFolder(File currentDir, String folderName) {
    File newFolder = new File(currentDir, folderName);
    try {
      if (!newFolder.exists()) newFolder.mkdir();
      return newFolder;
    } catch (Exception e) {
      logger.e(
          LOG_TAG,
          context.getString(R.string.failed_to_create_folder)
              + " ["
              + context.getString(R.string.cause)
              + "] "
              + e.getMessage());
      return null;
    }
  }
}
