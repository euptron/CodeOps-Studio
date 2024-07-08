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
 
   package com.eup.codeopsstudio.ui.settings.api;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.eup.libraries.sharelayout.ShareLayout;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.LayoutAboutAppItemBinding;
import com.eup.codeopsstudio.databinding.LayoutAboutNestedListItemBinding;
import com.eup.codeopsstudio.databinding.LayoutBundleReleaseItemBinding;
import com.eup.codeopsstudio.databinding.LayoutPrivacyItemBinding;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.res.databinding.LayoutShareAppItemBinding;
import com.eup.codeopsstudio.util.BaseUtil;
import java.util.ArrayList;
import java.util.List;
import com.eup.codeopsstudio.util.Wizard;

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int VIEW_TYPE_ABOUT_APP = 0;
  private static final int VIEW_TYPE_SHARE = 1;
  private static final int VIEW_TYPE_NESTED_LIST = 2;
  private static final int VIEW_TYPE_SERVICES = 3;
  private static final int VIEW_TYPE_BUNDLE_RELEASE = 4;
  private final Context context;

  public AboutAdapter(Context context) {
    this.context = context;
  }

  public interface OnItemClickListener {
    void onItemClick(AboutItems items, int position);
  }

  private OnItemClickListener itemClickListener;

  public void setOnItemClickListener(OnItemClickListener listener) {
    itemClickListener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    if (viewType == VIEW_TYPE_ABOUT_APP)
      return new AboutAppViewHolder(LayoutAboutAppItemBinding.inflate(inflater, parent, false));
    else if (viewType == VIEW_TYPE_SHARE)
      return new ShareViewHolder(LayoutShareAppItemBinding.inflate(inflater, parent, false));
    else if (viewType == VIEW_TYPE_NESTED_LIST)
      return new NestedListViewHolder(
          LayoutAboutNestedListItemBinding.inflate(inflater, parent, false));
     else if (viewType == VIEW_TYPE_SERVICES)
      return new PrivacyViewHolder(LayoutPrivacyItemBinding.inflate(inflater, parent, false));
    else if (viewType == VIEW_TYPE_BUNDLE_RELEASE) {
      return new ReleaseViewHolder(LayoutBundleReleaseItemBinding.inflate(inflater, parent, false));
    }
    throw new IllegalArgumentException(
        "Invalid view type: " + viewType); // can this happen ¯⁠\⁠_⁠(⁠ツ⁠)⁠_⁠/⁠¯
  }

  // nested list item by hierarchy
  public static final int APP_VERSION = 0;
  public static final int VISIT_WEBSITE = 1;
  public static final int SOCIALS = 2;
  public static final int OPEN_SOURCE_LICENCES = 3;
  public static final int CHECK_UPDATE = 4;
  public static final int DOCUMENTATION = 5;

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    int viewType = getItemViewType(position);
    if (viewType == VIEW_TYPE_SHARE) {
      ShareViewHolder shareHolder = (ShareViewHolder) holder;
      // set on click listener for share layout dynamic button
      shareHolder.shareLayout.setOnItemClickListener(v -> BaseUtil.shareAppPlayStoreLink(context));
      shareHolder.shareLayout.setSharePromptText(Constants./*GOOGLE_PLAY_APP_URL*/CHECK_UPDATE_GITHUB_URL);
      shareHolder.shareLayout.getSharePrompt().setSingleLine(true);
      shareHolder.shareLayout.getSharePrompt().setEllipsize(TextUtils.TruncateAt.END);
    } else if (viewType == VIEW_TYPE_NESTED_LIST) {
      NestedListViewHolder nestedListViewHolder = (NestedListViewHolder) holder;
      nestedListViewHolder.bind(getListItems());
      nestedListViewHolder.adapter.setOnItemClickListener(
          (items, pos) -> {
            if (itemClickListener != null) itemClickListener.onItemClick(items, pos);
          });
    } else if (viewType == VIEW_TYPE_SERVICES) {
      PrivacyViewHolder privacyViewHolder = (PrivacyViewHolder) holder;
      privacyViewHolder.privacy.setOnClickListener(
        v -> {
         BaseUtil.openUrlOutsideActivity(Constants.PRIVACY_POLICY_URL);
      });
      privacyViewHolder.service_terms.setOnClickListener(
        v -> {
         BaseUtil.openUrlOutsideActivity(Constants.TERMS_OF_SERVICE_URL);
      });
    }
  }

  @Override
  public int getItemCount() {
    return 5; // view types (0-4)
  }

  /**
   * Returns multiple view types.
   *
   * @param position The view required view type.
   */
  @Override
  public int getItemViewType(int position) {
    if (position == 1) {
      return VIEW_TYPE_SHARE;
    } else if (position == 2) {
      return VIEW_TYPE_NESTED_LIST;
    } else if (position == 3) {
     return VIEW_TYPE_SERVICES;
    } else if (position == 4) {
      return VIEW_TYPE_BUNDLE_RELEASE;
    }
    return super.getItemViewType(position);
  }

  private class AboutAppViewHolder extends RecyclerView.ViewHolder {

    private final ImageView icon;
    private final TextView title, summary;

    public AboutAppViewHolder(LayoutAboutAppItemBinding binding) {
      super(binding.getRoot());
      this.icon = binding.icon;
      this.title = binding.appName;
      this.summary = binding.appSlogan;
    }

    private void bind() {
      // TODO: Dynamic loading of app name, icon and slogan
    }
  }

  private class ShareViewHolder extends RecyclerView.ViewHolder {
    private ShareLayout shareLayout;

    public ShareViewHolder(LayoutShareAppItemBinding binding) {
      super(binding.getRoot());
      shareLayout = binding.shareLayout;
    }
  }

  private class NestedListViewHolder extends RecyclerView.ViewHolder {
    private RecyclerView nestedRecyclerView;
    private AboutListAdapter adapter;

    public NestedListViewHolder(LayoutAboutNestedListItemBinding binding) {
      super(binding.getRoot());
      nestedRecyclerView = binding.recyclerview;
      adapter = new AboutListAdapter(new ArrayList<>()); // Initialize the adapter
      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext());
      linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
      nestedRecyclerView.setLayoutManager(linearLayoutManager);
      nestedRecyclerView.setAdapter(adapter);
    }

    private void bind(List<AboutItems> item) {
      adapter.setData(item); // Update the data in the adapter async
    }
  }

  private class PrivacyViewHolder extends RecyclerView.ViewHolder {
    private TextView privacy, service_terms;

    public PrivacyViewHolder(LayoutPrivacyItemBinding binding) {
      super(binding.getRoot());
      privacy = binding.tvPrivacyPolicy;
      service_terms = binding.tvTermsOfService;
    }
  }

  private class ReleaseViewHolder extends RecyclerView.ViewHolder {
    private TextView release_info;

    public ReleaseViewHolder(LayoutBundleReleaseItemBinding binding) {
      super(binding.getRoot());
      release_info = binding.appBundleName;
      release_info.setText(Wizard.getAppName(context) + " " + Wizard.getAppVersionName(context));
    }
  }

  /**
   * A list {@link AboutItems} of items populated into the @see #AboutListAdapter
   *
   * <p>This items are responsible for the list section in the @see #AboutFragment.
   *
   * @return aboutItemsList
   */
  private List<AboutItems> getListItems() {
    List<AboutItems> aboutItemsList = new ArrayList<>();
    aboutItemsList.add(
        new AboutItems(context.getString(R.string.version), Wizard.getAppVersionName(context)));
    aboutItemsList.add(
        new AboutItems(
            context.getString(R.string.visit_website),
            context.getString(R.string.msg_get_latest_update)));
    aboutItemsList.add(
        new AboutItems(
            context.getString(R.string.social_media_handles),
            context.getString(R.string.msg_subscribe_socials)));
    aboutItemsList.add(
        new AboutItems(
            context.getString(R.string.open_source_licences),
            context.getString(R.string.msg_view_open_source_libs)));
    aboutItemsList.add(
        new AboutItems(
            context.getString(R.string.check_update),
            context.getString(R.string.msg_get_latest_release)));
    aboutItemsList.add(
        (new AboutItems(
            context.getString(R.string.documentation), context.getString(R.string.msg_see_doc))));
    return aboutItemsList;
  }
}
