/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.eup.codeopsstudio.databinding.LayoutActionItemBinding;
import com.eup.codeopsstudio.models.ActionModel;
import java.util.List;

public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.VH> {

  private List<ActionModel> list;
  
    

  public ActionAdapter(List<ActionModel> list) {
    this.list = list;
  }

  /**
   * An interface that defines a click listener for an item
   *
   * @param item The item to be clicked
   * @param position The relative position of item
   */
  public interface OnItemClickListener {
    void onItemClick(ActionModel model);
  }

  private OnItemClickListener mListener;

  /**
   * A method that defines the {@link OnItemClickListener} interface
   *
   * @param listener The listener for an item clicked
   */
  public void setOnItemClickListener(OnItemClickListener listener) {
    mListener = listener;
  }

  private OnButtonClickListener buttonClickListener;

  /**
   * An interface that defines a click listener for an button
   *
   * @param view The view to be clicked
   */
  public interface OnButtonClickListener {
    void onButtonClick(View view);
  }

  /**
   * A method that defines the {@link OnButtonClickListener} interface
   *
   * @param listener The listener for a button clicked
   */
  public void setOnButtonClickListener(OnButtonClickListener listener) {
    buttonClickListener = listener;
  }

  @Override
  public VH onCreateViewHolder(ViewGroup parent, int viewType) {
    return new VH(
        LayoutActionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(VH holder, int position) {
    String title = list.get(position).getTitle();
    String summary = list.get(position).getSummary();
    String buttonText = list.get(position).getButtonText();

    holder.title.setText(title);
    holder.icon.setImageResource(list.get(position).getIcon());

    if (summary != null) {
      holder.summary.setVisibility(View.VISIBLE);
      holder.summary.setText(list.get(position).getSummary());
    } else {
      holder.summary.setVisibility(View.GONE);
    }

    if (buttonText != null) {
      holder.buttonText.setVisibility(View.VISIBLE);
      holder.buttonText.setText(buttonText);
      holder.buttonText.setOnClickListener(
          v -> {
            if (buttonClickListener != null) {
              buttonClickListener.onButtonClick(v);
            }
          });
    } else {
      holder.buttonText.setVisibility(View.GONE);
    }

    holder.itemView.setOnClickListener(
        v -> {
          if (mListener != null) {
            mListener.onItemClick(list.get(holder.getAdapterPosition()));
          }
        });
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public class VH extends RecyclerView.ViewHolder {

    TextView title, summary;
    MaterialButton buttonText;
    ImageView icon;

    public VH(LayoutActionItemBinding binding) {
      super(binding.getRoot());
      title = binding.title;
      summary = binding.summary;
      icon = binding.icon;
      buttonText = binding.buttonOpen;
    }
  }
}
