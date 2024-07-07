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
 
   package com.eup.codeopsstudio;

import com.eup.codeopsstudio.common.Constants;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import com.eup.codeopsstudio.databinding.ActivityCrashBinding;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.util.Wizard;

public class CrashActivity extends AppCompatActivity {

  private ActivityCrashBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityCrashBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setSupportActionBar(binding.topAppBar);
    getSupportActionBar().setTitle(R.string.app_crashed);

    var error = new StringBuilder();
    error.append(getString(R.string.msg_app_crashed) + "\n\n");
    error.append(getString(R.string.msg_crash_report) + ":" + "\n");
    error.append(getString(R.string.app_version) + ":" + Wizard.getAppVersionName(this) + "\n");
    error.append(getString(R.string.stacktrace) + ":" + "\n");
    error.append(getIntent().getStringExtra("Error"));
    error.append(Constants.NEXT_LINE.repeat(2));
    error.append(getIntent().getStringExtra("Date"));
    error.append(Constants.NEXT_LINE.repeat(4));
    binding.result.setText(error.toString());
    binding.result.setTextIsSelectable(true);
    // support clickable links
    binding.result.setMovementMethod(LinkMovementMethod.getInstance());
    binding.rab.setOnClickListener(v -> restartApp());
  }

  @Override
  public void onBackPressed() {
    finishAffinity();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    var close = menu.add(getString(R.string.close));
    close.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    close.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_close));
    close.setContentDescription(getString(R.string.close_app));
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getTitle().equals(getString(R.string.close))) {
      finishAffinity();
      return true;
    }
    return false;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  private void restartApp() {
    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
    if (intent != null) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(Intent.makeRestartActivityTask(intent.getComponent()));
      finish();
      // Ensure complete restart
      Process.killProcess(Process.myPid());
      System.exit(0);
    }
  }
}
