package com.purpletealabs.sephora.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.databinding.library.baseAdapters.BR;
import com.purpletealabs.sephora.R;

import com.purpletealabs.sephora.activities.dummy.DummyContent;
import com.purpletealabs.sephora.adapters.AutoCompleteAdapter;
import com.purpletealabs.sephora.adapters.BooksAdapter;
import com.purpletealabs.sephora.adapters.EndlessRecyclerViewScrollListener;
import com.purpletealabs.sephora.adapters.SimpleItemRecyclerViewAdapter;
import com.purpletealabs.sephora.databinding.ActivityBookSearchBinding;
import com.purpletealabs.sephora.databinding.ActivityItemListBinding;
import com.purpletealabs.sephora.databinding.LayoutMobileBinding;
import com.purpletealabs.sephora.databinding.LayoutTabletBinding;
import com.purpletealabs.sephora.interfaces.OnListItemClick;
import com.purpletealabs.sephora.viewmodels.BookSearchActivityViewModel;
import com.purpletealabs.sephora.viewmodels.BookViewModel;

import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements OnListItemClick {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    LayoutMobileBinding layoutMobileBinding;
    LayoutTabletBinding layoutTabletBinding;
    private BookSearchActivityViewModel mViewModel;
    public EndlessRecyclerViewScrollListener mScrollListener;
    boolean isTablet;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTablet = getResources().getBoolean(R.bool.is_tablet);
        mViewModel = ViewModelProviders.of(this).get(BookSearchActivityViewModel.class);
        if (isTablet) {
            layoutTabletBinding = DataBindingUtil.setContentView(this, R.layout.layout_tablet);
            setSupportActionBar(layoutTabletBinding.toolbar.searchToolbar);
            layoutTabletBinding.setViewmodel(mViewModel);
        } else {
            layoutMobileBinding = DataBindingUtil.setContentView(this, R.layout.layout_mobile);
            setSupportActionBar(layoutMobileBinding.toolbar.searchToolbar);
            layoutMobileBinding.setViewmodel(mViewModel);
        }


        mViewModel.isSearchInProgress.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {

                if (((ObservableBoolean) observable).get()) {
                    hideKeyboard();
                    mScrollListener.resetState();
                }
            }
        });
//
//        binding.setViewmodel(mViewModel);


        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        if (isTablet) {
            initTabletViews();
        } else {
            initMobileViews();
        }
    }

    public void initTabletViews() {

        layoutTabletBinding.toolbar.searchView.setIconifiedByDefault(true);
        if (!TextUtils.isEmpty(mViewModel.mSearchTerm)) {
            layoutTabletBinding.toolbar.searchView.setIconified(false);
            layoutTabletBinding.toolbar.searchView.setQuery(mViewModel.mSearchTerm, false);
            layoutTabletBinding.toolbar.searchView.clearFocus();
        }
        layoutTabletBinding.toolbar.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        //More data will be loaded once user is 20 items away from reaching last item
        int visibleThreshold = 20;

        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, visibleThreshold) {
            @Override
            public void onLoadMore(int page) {
                mViewModel.loadMore(page);
            }
        };
        layoutTabletBinding.itemList.addOnScrollListener(mScrollListener);
        layoutTabletBinding.itemList.setAdapter(new SimpleItemRecyclerViewAdapter(mViewModel.mBooks, this));


    }

    public void initMobileViews() {

        layoutMobileBinding.toolbar.searchView.setIconifiedByDefault(true);
        if (!TextUtils.isEmpty(mViewModel.mSearchTerm)) {
            layoutMobileBinding.toolbar.searchView.setIconified(false);
            layoutMobileBinding.toolbar.searchView.setQuery(mViewModel.mSearchTerm, false);
            layoutMobileBinding.toolbar.searchView.clearFocus();
        }
        layoutMobileBinding.toolbar.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        layoutMobileBinding.itemList.setLayoutManager(layoutManager);

        //More data will be loaded once user is 20 items away from reaching last item
        int visibleThreshold = 20;

        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, visibleThreshold) {
            @Override
            public void onLoadMore(int page) {
                mViewModel.loadMore(page);
            }
        };
        layoutMobileBinding.itemList.addOnScrollListener(mScrollListener);
        layoutMobileBinding.itemList.setAdapter(new SimpleItemRecyclerViewAdapter(mViewModel.mBooks, this));


    }


    @Override
    public void onItemClick(BookViewModel bookViewModel) {
        Log.e("bookViewModel", bookViewModel.getBookTitle());
        if (isTablet) {
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM, bookViewModel.getBookTitle());
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, ItemDetailActivity.class);
            intent.putExtra(ItemDetailFragment.ARG_ITEM, bookViewModel.getBookTitle());
            startActivity(intent);
        }
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
