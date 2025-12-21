/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
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

package com.eup.codeopsstudio;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.ActivityMainBinding;
import com.eup.codeopsstudio.observers.ContextualObserver;
import com.eup.codeopsstudio.ui.onboarding.LandingFragment;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.viewmodel.MainViewModel;

/**
 * Primary container activity hosting either {@link MainFragment} for regular use or {@link
 * LandingFragment} for first-time onboarding.
 *
 * @author Etido Peter
 */
public class MainActivity extends AppCompatActivity {

  public static final String TAG = MainActivity.class.getSimpleName();

  private MainViewModel mainViewModel;
  private ContextualObserver lifecycleObserver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    BaseUtil.enforceEdgeToEdge(getWindow(), true);
    setContentView(binding.getRoot());

    mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    lifecycleObserver = new ContextualObserver(this, getActivityResultRegistry(), this);
    getLifecycle().addObserver(lifecycleObserver);

    if (PreferencesUtils.isAppFirstLaunch()) {
      loadFragment(LandingFragment.newInstance(), LandingFragment.TAG);
    } else {
      handleNotificationIntent(getIntent());
      loadFragment(MainFragment.newInstance(), MainFragment.TAG);
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleNotificationIntent(intent);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
      mainViewModel.requestCloseDrawer();
      return true;
    }
    return super.onKeyDown(keyCode, keyEvent);
  }

  private void handleNotificationIntent(Intent intent) {
    if (intent == null) {
      ILog.info(TAG, "NotificationIntent is null");
      return;
    }
    mainViewModel.setIntentBundle(intent);
  }

  public void loadFragment(Fragment fragment, String tag) {
    loadFragment(fragment, tag, false);
  }

  public void loadFragment(Fragment fragment, String tag, boolean animate) {
    FragmentManager fm = getSupportFragmentManager();
    if (fm.findFragmentByTag(tag) == null) {
      FragmentTransaction transaction = fm.beginTransaction();
      transaction.replace(R.id.fragment_container, fragment, tag);
      if (animate) {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      }
      transaction.commit();
    }
  }

  public ContextualObserver getLifecycleObserver() {
    return this.lifecycleObserver;
  }
}
