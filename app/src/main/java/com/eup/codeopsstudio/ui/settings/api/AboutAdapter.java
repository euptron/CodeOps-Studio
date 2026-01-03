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

package com.eup.codeopsstudio.ui.settings.api;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.eup.codeopsstudio.R;
import com.eup.codeopsstudio.common.Constants;
import com.eup.codeopsstudio.databinding.LayoutAboutAppItemBinding;
import com.eup.codeopsstudio.databinding.LayoutAboutNestedListItemBinding;
import com.eup.codeopsstudio.databinding.LayoutBundleReleaseItemBinding;
import com.eup.codeopsstudio.databinding.LayoutPrivacyItemBinding;
import com.eup.codeopsstudio.databinding.LayoutShareAppItemBinding;
import com.eup.codeopsstudio.util.BaseUtil;
import com.eup.codeopsstudio.util.Wizard;
import com.eup.libraries.sharelayout.ShareLayout;

import java.util.ArrayList;
import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // nested list item by hierarchy
    public static final int VISIT_WEBSITE = 1;
    public static final int SOCIALS = 2;
    public static final int OPEN_SOURCE_LICENCES = 3;
    public static final int CHECK_UPDATE = 4;
    public static final int DOCUMENTATION = 5;
    private static final int VIEW_TYPE_ABOUT_APP = 0;
    private static final int VIEW_TYPE_SHARE = 1;
    private static final int VIEW_TYPE_NESTED_LIST = 2;
    private static final int VIEW_TYPE_SERVICES = 3;
    private static final int VIEW_TYPE_BUNDLE_RELEASE = 4;
    private final Context context;
    private OnItemClickListener itemClickListener;

    public AboutAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // fall out
        return switch (getItemViewType(position)) {
            case
                VIEW_TYPE_ABOUT_APP -> new AboutAppViewHolder(LayoutAboutAppItemBinding.inflate(inflater, parent, false));
            case
                VIEW_TYPE_SHARE -> new ShareViewHolder(LayoutShareAppItemBinding.inflate(inflater
                , parent, false));
            case
                VIEW_TYPE_NESTED_LIST -> new NestedListViewHolder(LayoutAboutNestedListItemBinding.inflate(inflater, parent, false));
            case
                VIEW_TYPE_SERVICES -> new PrivacyViewHolder(LayoutPrivacyItemBinding.inflate(inflater, parent, false));
            case
                VIEW_TYPE_BUNDLE_RELEASE -> new ReleaseViewHolder(LayoutBundleReleaseItemBinding.inflate(inflater, parent, false));
            default -> throw new RuntimeException(
                "Invalid view type at position: " + position); // IllegalArgumentException
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_SHARE:
                bind((ShareViewHolder) holder);
                break;
            case VIEW_TYPE_NESTED_LIST:
                bind((NestedListViewHolder) holder);
                break;
            case VIEW_TYPE_SERVICES:
                bind((PrivacyViewHolder) holder);
                break;
            default: // fall out
        }
    }

    private void bind(ShareViewHolder shareHolder) {
        shareHolder.shareLayout.setOnItemClickListener(v -> BaseUtil.shareAppPlayStoreLink(context));
        shareHolder.shareLayout.setSharePromptText(Constants.CHECK_UPDATE_GITHUB_URL);
        shareHolder.shareLayout.getSharePrompt().setSingleLine(true);
        shareHolder.shareLayout.getSharePrompt().setEllipsize(TextUtils.TruncateAt.END);
    }

    private void bind(NestedListViewHolder nestedListViewHolder) {
        nestedListViewHolder.bind(getListItems());
        nestedListViewHolder.adapter.setOnItemClickListener((items, pos) -> {
            if (itemClickListener != null) itemClickListener.onItemClick(items, pos);
        });
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
        aboutItemsList.add(new AboutItems(context.getString(R.string.version),
            Wizard.getAppVersionName(context)));
        aboutItemsList.add(new AboutItems(context.getString(R.string.visit_website),
            context.getString(R.string.msg_get_latest_update)));
        aboutItemsList.add(new AboutItems(context.getString(R.string.social_media_handles),
            context.getString(R.string.msg_subscribe_socials)));
        aboutItemsList.add(new AboutItems(context.getString(R.string.open_source_licences),
            context.getString(R.string.msg_view_open_source_libs)));
        aboutItemsList.add(new AboutItems(context.getString(R.string.check_update),
            context.getString(R.string.msg_get_latest_release,
                context.getString(R.string.app_name))));
        aboutItemsList.add((new AboutItems(context.getString(R.string.documentation),
            context.getString(R.string.msg_see_doc, context.getString(R.string.app_name)))));
        return aboutItemsList;
    }

    private void bind(@NonNull PrivacyViewHolder privacyViewHolder) {
        privacyViewHolder.privacy.setOnClickListener(v -> BaseUtil.openUrlOutsideActivity(Constants.PRIVACY_POLICY_URL));
        privacyViewHolder.service_terms.setOnClickListener(v -> BaseUtil.openUrlOutsideActivity(Constants.TERMS_OF_SERVICE_URL));
    }

    @Override
    public int getItemViewType(int position) {
        return position % 5;
    }

    @Override
    public int getItemCount() {
        return 5; // view types (0-4)
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(AboutItems items, int position);
    }

    private class ReleaseViewHolder extends RecyclerView.ViewHolder {

        public ReleaseViewHolder(LayoutBundleReleaseItemBinding binding) {
            super(binding.getRoot());
            TextView release_info = binding.appBundleName;
            String appName = Wizard.getAppName(context);
            String appVersionName = Wizard.getAppVersionName(context);
            String releaseInfo = appName + " " + appVersionName;
            release_info.setText(releaseInfo);
        }
    }

    private static class AboutAppViewHolder extends RecyclerView.ViewHolder {

        public AboutAppViewHolder(@NonNull LayoutAboutAppItemBinding binding) {
            super(binding.getRoot());
        }
    }

    private static class ShareViewHolder extends RecyclerView.ViewHolder {
        private final ShareLayout shareLayout;

        public ShareViewHolder(LayoutShareAppItemBinding binding) {
            super(binding.getRoot());
            shareLayout = binding.shareLayout;
        }
    }

    private static class NestedListViewHolder extends RecyclerView.ViewHolder {
        private final AboutListAdapter adapter;

        public NestedListViewHolder(LayoutAboutNestedListItemBinding binding) {
            super(binding.getRoot());
            RecyclerView nestedRecyclerView = binding.recyclerview;
            adapter = new AboutListAdapter(new ArrayList<>()); // Initialize the mAdapter
            LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(itemView.getContext());
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            nestedRecyclerView.setLayoutManager(linearLayoutManager);
            nestedRecyclerView.setAdapter(adapter);
        }

        private void bind(List<AboutItems> item) {
            adapter.setData(item); // Update the data in the mAdapter async
        }
    }

    private static class PrivacyViewHolder extends RecyclerView.ViewHolder {
        private final TextView privacy;
        private final TextView service_terms;

        public PrivacyViewHolder(LayoutPrivacyItemBinding binding) {
            super(binding.getRoot());
            privacy       = binding.tvPrivacyPolicy;
            service_terms = binding.tvTermsOfService;
        }
    }
}
