/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
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
 * If you have more questions, feel free to message Etido Peter if you have any
 * questions or need additional information. Email: euptron@gmail.com
 */

package com.eup.codeopsstudio.util;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.databinding.LayoutDialogProgressBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UninstallDialogFragment extends DialogFragment {

  public static final String TAG = "UninstallDialogFragment";
  private static final String ARG_PACKAGE_NAME = "package_name";
  private static final String ARG_PLUGIN_NAME = "plugin_name";

  private String pluginName;
  private AlertDialog dialog;
  private String targetPackage;
  private boolean isUninstalling;
  private LayoutDialogProgressBinding binding;
  private final Handler handler = new Handler(Looper.getMainLooper());

  private final BroadcastReceiver packageReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (intent == null) return;

          boolean success = intent.getBooleanExtra(PackageInstaller.EXTRA_STATUS, false);
          String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

          if (success) {
            handler.post(() -> onUninstallComplete(true, null));
          } else {
            handler.post(() -> onUninstallComplete(false, msg));
          }
        }
      };

  public static UninstallDialogFragment newInstance(String packageName, String pluginName) {
    UninstallDialogFragment fragment = new UninstallDialogFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PACKAGE_NAME, packageName);
    args.putString(ARG_PLUGIN_NAME, pluginName);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      targetPackage = args.getString(ARG_PACKAGE_NAME);
      pluginName = args.getString(ARG_PLUGIN_NAME);
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    binding = LayoutDialogProgressBinding.inflate(getLayoutInflater());

    dialog =
        new MaterialAlertDialogBuilder(requireContext())
            .setView(binding.getRoot())
            .setPositiveButton(R.string.yes, null)
            .setNegativeButton(R.string.no, null)
            .setCancelable(false)
            .create();
    binding.textView.setTextSize(12f);
    binding.textView.setMaxLines(3);
    binding.textView.setText(getString(R.string.msg_uninstall_confirm_message, pluginName));
    binding.progressBar.setVisibility(View.GONE);
    dialog.setOnShowListener(d -> configureButtons());
    return dialog;
  }

  private void configureButtons() {
    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
    Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

    positive.setOnClickListener(
        v -> {
          if (!isUninstalling) {
            startUninstall();
          } else {
            dismiss();
          }
        });

    negative.setOnClickListener(v -> dismiss());
  }

  private void startUninstall() {
    if (targetPackage == null || !Wizard.isPackageInstalled(requireContext(), targetPackage)) {
      setErrorState(getString(R.string.msg_error_unknown));
      return;
    }

    setUninstallingState();

    IntentFilter filter = new IntentFilter("PACKAGE_UNINSTALL_COMPLETE");
    requireContext().registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

    try {
      PackageInstaller packageInstaller =
          requireContext().getPackageManager().getPackageInstaller();

      Intent uninstallIntent = new Intent("PACKAGE_UNINSTALL_COMPLETE");
      PendingIntent pendingIntent =
          PendingIntent.getBroadcast(
              requireContext(),
              0,
              uninstallIntent,
              PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

      packageInstaller.uninstall(targetPackage, pendingIntent.getIntentSender());

      // Fallback timeout in case callback doesn't come
      handler.postDelayed(
          () -> {
            if (isUninstalling) {
              boolean stillInstalled = Wizard.isPackageInstalled(requireContext(), targetPackage);
              onUninstallComplete(
                  !stillInstalled, stillInstalled ? getString(R.string.msg_error_timeout) : null);
            }
          },
          30_000);

    } catch (Exception e) {
      onUninstallComplete(false, e.getMessage());
    }
  }

  private void setUninstallingState() {
    isUninstalling = true;
    binding.progressBar.setVisibility(View.VISIBLE);
    binding.textView.setText(getString(R.string.msg_uninstall_in_progress, pluginName));

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.cancel);
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
  }

  private void setSuccessState() {
    isUninstalling = false;
    binding.progressBar.setVisibility(View.GONE);
    binding.textView.setText(getString(R.string.msg_uninstall_success, pluginName));

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.done);
  }

  private void setErrorState(String message) {
    isUninstalling = false;
    binding.progressBar.setVisibility(View.GONE);
    binding.textView.setText(getString(R.string.msg_uninstall_error, message));

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.ok);
  }

  private void onUninstallComplete(boolean success, @Nullable String error) {
    handler.removeCallbacksAndMessages(null);
    unregisterReceiverSafely();

    if (success) {
      setSuccessState();
    } else {
      setErrorState(error != null ? error : getString(R.string.msg_error_unknown));
    }
  }

  private void unregisterReceiverSafely() {
    try {
      requireContext().unregisterReceiver(packageReceiver);
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    super.onDismiss(dialog);
    handler.removeCallbacksAndMessages(null);
    unregisterReceiverSafely();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
