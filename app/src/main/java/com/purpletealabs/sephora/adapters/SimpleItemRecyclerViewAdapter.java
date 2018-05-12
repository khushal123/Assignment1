package com.purpletealabs.sephora.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.purpletealabs.sephora.BR;
import com.purpletealabs.sephora.R;
import com.purpletealabs.sephora.activities.ItemDetailActivity;
import com.purpletealabs.sephora.activities.ItemDetailFragment;
import com.purpletealabs.sephora.activities.ItemListActivity;
import com.purpletealabs.sephora.activities.dummy.DummyContent;
import com.purpletealabs.sephora.databinding.ItemListContentBinding;
import com.purpletealabs.sephora.databinding.RowBookBinding;
import com.purpletealabs.sephora.interfaces.OnListItemClick;
import com.purpletealabs.sephora.viewmodels.BookViewModel;

import java.util.List;

public class SimpleItemRecyclerViewAdapter
        extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {


    private final ObservableArrayList<BookViewModel> mBooks;

    OnListItemClick onListItemClick;
    public SimpleItemRecyclerViewAdapter(ObservableArrayList<BookViewModel> mBooks,
                                        OnListItemClick onListItemClick) {
        this.mBooks = mBooks;
        this.onListItemClick = onListItemClick;

    }

//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_list_content, parent, false);
//        return new ViewHolder(view);
//    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemListContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        BookViewModel bookViewModel = mBooks.get(position);
        holder.getBinding().setViewmodel(bookViewModel);
        holder.getBinding().executePendingBindings();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookViewModel bookViewModel = mBooks.get(position);
                onListItemClick.onItemClick(bookViewModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemListContentBinding binding;
        ViewHolder(ItemListContentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemListContentBinding getBinding() {
            return binding;
        }
    }
}