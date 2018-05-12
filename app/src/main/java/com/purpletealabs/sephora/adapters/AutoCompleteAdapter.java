package com.purpletealabs.sephora.adapters;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;


import com.purpletealabs.sephora.R;
import com.purpletealabs.sephora.dataSource.BooksRepository;
import com.purpletealabs.sephora.dtos.Book;
import com.purpletealabs.sephora.dtos.SearchBooksResponseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> mData;
    Context context;

    LifecycleOwner lifecycleOwner;

    public AutoCompleteAdapter(Context context, int textViewResourceId, LifecycleOwner lifecycleOwner) {
        super(context, R.layout.search_item);
        mData = new ArrayList<>();
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public int getCount() {

        return mData.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.search_item, parent, false);
        }


        TextView name = (TextView) convertView.findViewById(R.id.autoCompleteItem);
        name.setText(mData.get(position));


        return convertView;
    }


    @Nullable
    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    final CountDownLatch latch = new CountDownLatch(1);
                    BooksRepository booksRepository = BooksRepository.getInstance();
                    LiveData<SearchBooksResponseModel> liveData = booksRepository.getBooks(constraint.toString());
                    liveData.observe(lifecycleOwner, new Observer<SearchBooksResponseModel>() {
                        @Override
                        public void onChanged(@Nullable SearchBooksResponseModel searchBooksResponseModel) {
                            List<Book> bookList = searchBooksResponseModel.getBooks();
                            mData.clear();
                            if (bookList != null) {
                                for (Book b : bookList) {
                                    mData.add(b.getVolumeInfo().getTitle());
                                }
                            }

                            filterResults.values = mData;
                            filterResults.count = mData.size();
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if (contraint == null) {
                    if (results != null && results.count > 0) {

                        notifyDataSetChanged();
                    }
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }


}