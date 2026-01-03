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

package com.eup.codeopsstudio.ui.debug;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.ActivityCrashBinding;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.codeopsstudio.util.manager.ExitOnBackPressed;

/**
 * @author Etido Peter
 */
public class CrashActivity extends AppCompatActivity {

    private ActivityCrashBinding binding;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        var close = menu.add(getString(R.string.close));
        close.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        close.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_close));
        close.setContentDescription(getString(R.string.close_app));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        CharSequence title = item.getTitle();
        if (title != null && title.equals(getString(R.string.close))) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getOnBackPressedDispatcher().addCallback(this, new ExitOnBackPressed(this));

        setSupportActionBar(binding.topAppBar);
        var title = getString(R.string.app_crashed, getString(R.string.app_name));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }

        String error = getString(R.string.msg_app_crashed) + Constants.NEXT_LINE.repeat(2)
            + getString(R.string.msg_crash_report) + ":" + Constants.NEXT_LINE
            + getString(R.string.app_version) + ":" + Wizard.getAppVersionName(this)
            + Constants.NEXT_LINE + getString(R.string.stacktrace) + ":" + Constants.NEXT_LINE
            + getIntent().getStringExtra("error") + Constants.NEXT_LINE.repeat(4);
        binding.result.setText(error);
        binding.result.setTextIsSelectable(true);
        // support clickable links
        binding.result.setMovementMethod(LinkMovementMethod.getInstance());
        binding.rab.setOnClickListener(v -> restartApp());
    }

    private void restartApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(Intent.makeRestartActivityTask(intent.getComponent()));
            finish();
            // Ensure complete restart.
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
