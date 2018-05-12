package com.purpletealabs.sephora.activities;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.purpletealabs.sephora.R;
import com.purpletealabs.sephora.adapters.AutoCompleteAdapter;
import com.purpletealabs.sephora.adapters.BooksAdapter;
import com.purpletealabs.sephora.dataSource.BooksRepository;
import com.purpletealabs.sephora.databinding.ActivityBookSearchBinding;
import com.purpletealabs.sephora.dtos.Book;
import com.purpletealabs.sephora.dtos.SearchBooksResponseModel;
import com.purpletealabs.sephora.viewmodels.BookSearchActivityViewModel;
import com.purpletealabs.sephora.adapters.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.List;

public class BookSearchActivity extends AppCompatActivity implements LifecycleOwner {

    private BookSearchActivityViewModel mViewModel;

    //This scroll listener decides when to load more data
    public EndlessRecyclerViewScrollListener mScrollListener;

    ActivityBookSearchBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_book_search);
        setupActionBar(binding);
        mViewModel = ViewModelProviders.of(this).get(BookSearchActivityViewModel.class);

        mViewModel.isSearchInProgress.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {

                //Hide keyboard when it starts to search and reset state of scroll listener
                if (((ObservableBoolean) observable).get()) {
                    hideKeyboard();
                    mScrollListener.resetState();
                }
            }
        });

        binding.setViewmodel(mViewModel);
        initSearch();
        initViews();
    }

    public void initSearch(){
        binding.toolbar.searchView.setIconifiedByDefault(true);
        if (!TextUtils.isEmpty(mViewModel.mSearchTerm)) {
            binding.toolbar.searchView.setIconified(false);
            binding.toolbar.searchView.setQuery(mViewModel.mSearchTerm, false);
            binding.toolbar.searchView.clearFocus();
        }
        binding.toolbar.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.searchFor(query, false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mViewModel.searchFor(query, true);
                return true;
            }
        });



        AutoCompleteAdapter adapter = new AutoCompleteAdapter(this, android.R.layout.simple_list_item_1, this);
        binding.autoCompleteSearch.setAdapter(adapter);


    }

    private void setupActionBar(ActivityBookSearchBinding binding){
        setSupportActionBar(binding.toolbar.searchToolbar);
    }
    LiveData<SearchBooksResponseModel> liveData;
    private void initViews() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        binding.rvBooks.setLayoutManager(layoutManager);

        //More data will be loaded once user is 20 items away from reaching last item
        int visibleThreshold = 20;

        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, visibleThreshold) {
            @Override
            public void onLoadMore(int page) {
                mViewModel.loadMore(page);
            }
        };
        binding.rvBooks.addOnScrollListener(mScrollListener);
        binding.rvBooks.setAdapter(new BooksAdapter(mViewModel.mBooks));

    }


//    public void observeAutoCompleteData(){
//
//        liveData.observe(this, new Observer<SearchBooksResponseModel>() {
//            @Override
//            public void onChanged(@Nullable SearchBooksResponseModel searchBooksResponseModel) {
//                if(searchBooksResponseModel != null) {
//                    List<Book> bookList = searchBooksResponseModel.getBooks();
//                    if (bookList != null) {
//                        for (Book b : bookList) {
//                            arrayList.add(b.getVolumeInfo().getTitle());
//                        }
//                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BookSearchActivity.this, android.R.layout.simple_list_item_1, arrayList);
//                        binding.autoCompleteSearch.setAdapter(arrayAdapter);
//                    }
//                }
//            }
//        });
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_search, menu);
//        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
//        searchView.setIconifiedByDefault(true);
//        if (!TextUtils.isEmpty(mViewModel.mSearchTerm)) {
//            searchView.setIconified(false);
//            searchView.setQuery(mViewModel.mSearchTerm, false);
//            searchView.clearFocus();
//        }
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                mViewModel.searchFor(query, false);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                mViewModel.searchFor(query, true);
//                return true;
//            }
//        });
//        return true;
//    }

    @Override
    protected void onDestroy() {
        //Called to cancel pending executions if any
        mViewModel.destroy();
        super.onDestroy();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}
