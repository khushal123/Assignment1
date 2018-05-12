package com.purpletealabs.sephora.dataSource;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.purpletealabs.sephora.apis.GoogleBooksServiceFactory;
import com.purpletealabs.sephora.apis.IBooksService;
import com.purpletealabs.sephora.dtos.SearchBooksResponseModel;
import com.purpletealabs.sephora.utils.AppExecutors;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksRepository implements BooksDataSource {
    private volatile static BooksRepository INSTANCE = null;

    //Remote data source to search books in
    private final BooksDataSource mRemoteDataSource;

    //Here we can have multiple different data sources
    private BooksRepository(BooksDataSource dataSource) {
        mRemoteDataSource = dataSource;
    }

    private BooksRepository() {
        mRemoteDataSource = null;
    }

    public static BooksRepository getInstance(BooksDataSource remoteDataSource) {
        if (INSTANCE == null) {
            synchronized (BooksRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BooksRepository(remoteDataSource);
                }
            }
        }
        return INSTANCE;
    }

    public static BooksRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (BooksRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BooksRepository();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public void searchBooks(String searchTerm, int page, @NonNull final SearchBooksCallback callback) {
        mRemoteDataSource.searchBooks(searchTerm, page, new SearchBooksCallback() {
            @Override
            public void onSearchBooksResult(SearchBooksResponseModel result) {
                callback.onSearchBooksResult(result);
            }

            @Override
            public void onSearchBooksFailure() {
                callback.onSearchBooksFailure();
            }
        });
    }

    public LiveData<SearchBooksResponseModel> getBooks(String searchTerm) {
        final MutableLiveData<SearchBooksResponseModel> mutableLiveData = new MutableLiveData<>();
        IBooksService service = GoogleBooksServiceFactory.newServiceInstance(new AppExecutors().networkIO());
        Call<SearchBooksResponseModel> call = service.serarchBooks(searchTerm, 0, 10);
        call.enqueue(new Callback<SearchBooksResponseModel>() {
            @Override
            public void onResponse(Call<SearchBooksResponseModel> call, final Response<SearchBooksResponseModel> response) {
                Log.e("response", new Gson().toJson(response.body()));
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        SearchBooksResponseModel searchBooksResponseModel = response.body();
                        mutableLiveData.setValue(searchBooksResponseModel);
                    } else {
                        mutableLiveData.setValue(null);
                    }

            }

            @Override
            public void onFailure(Call<SearchBooksResponseModel> call, Throwable t) {
                Log.e("failure", Log.getStackTraceString(t));
                mutableLiveData.setValue(null);
            }
        });
        return mutableLiveData;
    }


    @Override
    public void cancelPendingExecutions() {
        if(mRemoteDataSource != null){
            mRemoteDataSource.cancelPendingExecutions();
        }

    }
}