package com.eup.codeopsstudio.ui.onboarding.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eup.codeopsstudio.databinding.ItemOnboardingBinding;
import com.eup.codeopsstudio.ui.onboarding.model.OnboardingItem;
import java.util.List;

public class OnboardingAdapter
    extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

  private final List<OnboardingItem> onboardingItems;

  public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
    this.onboardingItems = onboardingItems;
  }

  @NonNull
  @Override
  public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemOnboardingBinding binding =
        ItemOnboardingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new OnboardingViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
    holder.bind(onboardingItems.get(position));
  }

  @Override
  public int getItemCount() {
    return onboardingItems.size();
  }

  static class OnboardingViewHolder extends RecyclerView.ViewHolder {

    private final ImageView imageView;
    private final TextView textHeader;
    private final TextView textTitle;
    private final TextView textDescription;

    public OnboardingViewHolder(@NonNull ItemOnboardingBinding binding) {
      super(binding.getRoot());
      imageView = binding.imageOnboarding;
      textHeader = binding.textHeader;
      textTitle = binding.textTitle;
      textDescription = binding.textDescription;
    }

    void bind(OnboardingItem onboardingItem) {
      imageView.setImageResource(onboardingItem.getImage());
      textHeader.setText(onboardingItem.getHeader());
      textTitle.setText(onboardingItem.getTitle());
      textDescription.setText(onboardingItem.getDescription());
    }
  }
}
