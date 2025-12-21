package com.eup.codeopsstudio.ui.onboarding;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.eup.codeopsstudio.MainActivity;
import com.eup.codeopsstudio.MainFragment;
import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.util.PreferencesUtils;
import com.eup.codeopsstudio.databinding.FragmentLandingBinding;
import com.eup.codeopsstudio.ui.onboarding.adapter.OnboardingAdapter;
import com.eup.codeopsstudio.ui.onboarding.model.OnboardingItem;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeOps Studio Onboarding
 *
 * @author Etido Peter
 */
public class LandingFragment extends Fragment {

  public static final String TAG = LandingFragment.class.getSimpleName();

  private OnboardingAdapter adapter;
  private FragmentLandingBinding binding;

  public static LandingFragment newInstance() {
    return new LandingFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentLandingBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    int systemBars =WindowInsets.Type.systemBars();
    BaseUtil.applyWindowInsetToPadding(
        view, false, true, false, true, systemBars, false);

    adapter = new OnboardingAdapter(buildOnboardingItems());
    binding.viewPager.setAdapter(adapter);
    
    binding.viewPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            int totalItems = adapter.getItemCount();
            binding.textProgress.setText(String.format("%d/%d", position + 1, totalItems));

            if (position == adapter.getItemCount() - 1) {
              binding.btnNext.setText(R.string.btn_get_started);
            } else {
              binding.btnNext.setText(R.string.btn_next);
            }
          }
        });

    binding.btnNext.setOnClickListener(
        v -> {
          if (binding.viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
          } else {
            PreferencesUtils.setAppFirstLaunchComplete();
            ((MainActivity) requireActivity())
                .loadFragment(MainFragment.newInstance(), MainFragment.TAG, true);
          }
        });
  }

  @Override
  public void onStart() {
    super.onStart();
    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  public void onStop() {
    super.onStop();
    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }

  private List<OnboardingItem> buildOnboardingItems() {
    List<OnboardingItem> onboardingItems = new ArrayList<>();
    onboardingItems.add(
        new OnboardingItem(
            R.drawable.onboarding_1,
            getString(R.string.onboarding_title_introducing),
            getString(R.string.onboarding_subtitle_codeops_studio),
            getString(R.string.onboarding_description_intro)));

    onboardingItems.add(
        new OnboardingItem(
            R.drawable.onboarding_2,
            getString(R.string.onboarding_title_sync_thoughts),
            getString(R.string.onboarding_subtitle_capture_ideas),
            getString(R.string.onboarding_description_capture_ideas)));

    onboardingItems.add(
        new OnboardingItem(
            R.drawable.onboarding_3,
            getString(R.string.onboarding_title_pro_tools),
            getString(R.string.onboarding_subtitle_everything_you_need),
            getString(R.string.onboarding_description_features)));

    onboardingItems.add(
        new OnboardingItem(
            R.drawable.onboarding_4,
            getString(R.string.onboarding_title_dedicated_to),
            getString(R.string.onboarding_subtitle_dr_peter_umoren),
            getString(R.string.onboarding_description_dedication)));

    onboardingItems.add(
        new OnboardingItem(
            R.drawable.onboarding_5,
            getString(R.string.onboarding_title_take_leap),
            getString(R.string.onboarding_subtitle_manifest_ideas),
            getString(R.string.onboarding_description_start_coding)));
    return onboardingItems;
  }
}
