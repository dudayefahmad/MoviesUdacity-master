package com.ahmaddudayef.movieudacity.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmaddudayef.movieudacity.BuildConfig;
import com.ahmaddudayef.movieudacity.R;
import com.ahmaddudayef.movieudacity.adapter.MovieAdapter;
import com.ahmaddudayef.movieudacity.data.MovieContract;
import com.ahmaddudayef.movieudacity.model.MovieURL;
import com.ahmaddudayef.movieudacity.pojo.MainModel;
import com.ahmaddudayef.movieudacity.pojo.MovieModel;
import com.ahmaddudayef.movieudacity.util.EndlessRecyclerViewScrollListener;
import com.ahmaddudayef.movieudacity.util.GridSpacingItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> ,MovieAdapter.ItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SORT_FAVORITES = 1;
    private static final int SORT_POPULAR = 2;
    private static final int SORT_TOP_RATED = 3;
    private static final int SORT_UPCOMING = 4;
    private static final int SORT_NOW_PLAYING = 5;

    public static final String[] MOVIE_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_NAME,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE
    };
    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_NAME = 1;
    public static final int INDEX_POSTER_PATH = 2;
    public static final int INDEX_OVERVIEW = 3;
    public static final int INDEX_USER_RATING = 4;
    public static final int INDEX_RELEASE_DATE = 5;

    private static final int ID_MOVIE_LOADER = 44;

    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_movie_empty) TextView mMovieEmptyTextView;
    @BindView(R.id.pb_movie_list) ProgressBar mMovieProgressBar;

    private MovieAdapter movieAdapter;
    private ArrayList<MovieModel> mMovieModels = new ArrayList<>();
    private ArrayList<MovieModel> mFavoriteMovieModels = new ArrayList<>();
    private ArrayList<MovieModel> mPopularMovieModels = new ArrayList<>();
    private ArrayList<MovieModel> mTopRatedMovieModels = new ArrayList<>();
    private ArrayList<MovieModel> mUpComingMovieModels = new ArrayList<>();
    private ArrayList<MovieModel> mNowPlayingMovieModels = new ArrayList<>();

    private MovieApiInterface mApiInterface;

    private int sort = SORT_POPULAR;
    private boolean disableMenu;
    private int popularPage = 2, topRatedPage = 2, upComingPage = 2, nowPlayingPage = 2;
    private static int totalPopularPage, totalTopRatedPage, totalUpComingPage, totalNowPlayingPage;

    private EndlessRecyclerViewScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            configureRecyclerView(mRecyclerView, 3);
        } else {
            configureRecyclerView(mRecyclerView, 2);
        }

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MovieURL.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mApiInterface = retrofit.create(MovieApiInterface.class);

        if (savedInstanceState != null){
            sort = savedInstanceState.getInt(getString(R.string.sort_key));
            mPopularMovieModels = savedInstanceState.getParcelableArrayList(getString(R.string.popular_movies_key));
            mTopRatedMovieModels = savedInstanceState.getParcelableArrayList(getString(R.string.top_rated_movies_key));
            mUpComingMovieModels = savedInstanceState.getParcelableArrayList(getString(R.string.up_coming_movies_key));
            mNowPlayingMovieModels = savedInstanceState.getParcelableArrayList(getString(R.string.now_playing_movies_key));
            popularPage = savedInstanceState.getInt(getString(R.string.popular_page_key));
            topRatedPage = savedInstanceState.getInt(getString(R.string.top_rated_page_key));
            upComingPage = savedInstanceState.getInt(getString(R.string.up_coming_page_key));
            nowPlayingPage = savedInstanceState.getInt(getString(R.string.now_playing_page_key));

            if (sort == SORT_POPULAR) {
                mMovieModels.addAll(mPopularMovieModels);
            } else if (sort == SORT_TOP_RATED) {
                if (mTopRatedMovieModels != null){
                    mMovieModels.addAll(mTopRatedMovieModels);
                }
            } else if (sort == SORT_UPCOMING) {
                if (mUpComingMovieModels != null){
                    mMovieModels.addAll(mUpComingMovieModels);
                }
            } else if (sort == SORT_NOW_PLAYING) {
                if (mNowPlayingMovieModels != null){
                    mMovieModels.addAll(mNowPlayingMovieModels);
                }
            } else if (sort == SORT_FAVORITES){
                getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
            }
        } else {
            callPopularMovies(1, true);
        }

        movieAdapter = new MovieAdapter(this, mMovieModels, this);
        mRecyclerView.setAdapter(movieAdapter);
    }

    private void callPopularMovies(int page, final boolean clearList){
        if (clearList) showLoading();

        Call<MainModel> popularMovies = mApiInterface.getPopularMovies(BuildConfig.MOVIE_DB_API_KEY, page);
        popularMovies.enqueue(new Callback<MainModel>() {
            @Override
            public void onResponse(Call<MainModel> call, retrofit2.Response<MainModel> response) {
                if (response.body() != null){
                    totalPopularPage = response.body().getTotalPages();
                    ArrayList<MovieModel> results = response.body().getResults();
                    if (results != null && !results.isEmpty()){
                        if (clearList) mPopularMovieModels.clear();
                        else popularPage++;
                        mPopularMovieModels.addAll(results);

                        mMovieModels.clear();
                        mMovieModels.addAll(mPopularMovieModels);
                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();

                        sort = SORT_POPULAR;
                    } else {
                        Toast.makeText(MainActivity.this, R.string.no_results, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.failed_fetch_popular, Toast.LENGTH_SHORT).show();
                }
                hideLoading();
            }

            @Override
            public void onFailure(Call<MainModel> call, Throwable t) {
                Log.e(TAG, getString(R.string.failed_fetch_popular), t.getCause());
                Toast.makeText(MainActivity.this, R.string.failed_fetch_popular, Toast.LENGTH_SHORT).show();
                hideLoading();
            }
        });
    }

    private void callTopRatedMovies(int page, final boolean clearList) {
        if (clearList) showLoading();

        Call<MainModel> topRatedMovies = mApiInterface.getTopRatedMovies(BuildConfig.MOVIE_DB_API_KEY, page);
        topRatedMovies.enqueue(new Callback<MainModel>() {
            @Override
            public void onResponse(Call<MainModel> call, retrofit2.Response<MainModel> response) {
                if (response.body() != null){
                    totalTopRatedPage = response.body().getTotalPages();
                    ArrayList<MovieModel> results = response.body().getResults();
                    if (results != null && !results.isEmpty()){
                        if (clearList) mTopRatedMovieModels.clear();
                        else topRatedPage++;
                        mTopRatedMovieModels.addAll(results);

                        mMovieModels.clear();
                        mMovieModels.addAll(mTopRatedMovieModels);
                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();

                        sort = SORT_TOP_RATED;
                    } else {
                        Toast.makeText(MainActivity.this, R.string.failed_fetch_top_rated, Toast.LENGTH_SHORT).show();
                    }
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<MainModel> call, Throwable t) {
                Log.e(TAG, getString(R.string.failed_fetch_top_rated), t.getCause());
                Toast.makeText(MainActivity.this, R.string.failed_fetch_top_rated, Toast.LENGTH_SHORT).show();
                hideLoading();
            }
        });
    }

    private void callUpComingMovies(int page, final boolean clearList) {
        if (clearList) showLoading();

        Call<MainModel> upComingMovies = mApiInterface.getUpComingMovies(BuildConfig.MOVIE_DB_API_KEY, page);
        upComingMovies.enqueue(new Callback<MainModel>() {
            @Override
            public void onResponse(Call<MainModel> call, retrofit2.Response<MainModel> response) {
                if (response.body() != null){
                    totalUpComingPage = response.body().getTotalPages();
                    ArrayList<MovieModel> results = response.body().getResults();
                    if (results != null && !results.isEmpty()){
                        if (clearList) mUpComingMovieModels.clear();
                        else upComingPage++;
                        mUpComingMovieModels.addAll(results);

                        mMovieModels.clear();
                        mMovieModels.addAll(mUpComingMovieModels);
                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();

                        sort = SORT_UPCOMING;
                    } else {
                        Toast.makeText(MainActivity.this, R.string.failed_fetch_up_coming, Toast.LENGTH_SHORT).show();
                    }
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<MainModel> call, Throwable t) {
                Log.e(TAG, getString(R.string.failed_fetch_up_coming), t.getCause());
                Toast.makeText(MainActivity.this, R.string.failed_fetch_up_coming, Toast.LENGTH_SHORT).show();
                hideLoading();
            }
        });
    }

    private void callNowPlayingMovies(int page, final boolean clearList) {
        if (clearList) showLoading();

        Call<MainModel> nowPlayingMovies = mApiInterface.getNowPlayingMovies(BuildConfig.MOVIE_DB_API_KEY, page);
        nowPlayingMovies.enqueue(new Callback<MainModel>() {
            @Override
            public void onResponse(Call<MainModel> call, retrofit2.Response<MainModel> response) {
                if (response.body() != null){
                    totalNowPlayingPage = response.body().getTotalPages();
                    ArrayList<MovieModel> results = response.body().getResults();
                    if (results != null && !results.isEmpty()){
                        if (clearList) mNowPlayingMovieModels.clear();
                        else nowPlayingPage++;
                        mNowPlayingMovieModels.addAll(results);

                        mMovieModels.clear();
                        mMovieModels.addAll(mNowPlayingMovieModels);
                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();

                        sort = SORT_NOW_PLAYING;
                    } else {
                        Toast.makeText(MainActivity.this, R.string.failed_fetch_now_playing, Toast.LENGTH_SHORT).show();
                    }
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<MainModel> call, Throwable t) {
                Log.e(TAG, getString(R.string.failed_fetch_now_playing), t.getCause());
                Toast.makeText(MainActivity.this, R.string.failed_fetch_now_playing, Toast.LENGTH_SHORT).show();
                hideLoading();
            }
        });
    }

    private void hideLoading() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mMovieEmptyTextView.setVisibility(View.GONE);
        mMovieProgressBar.setVisibility(View.GONE);

        disableMenu = false;
        invalidateOptionsMenu();
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.GONE);
        mMovieEmptyTextView.setVisibility(View.GONE);
        mMovieProgressBar.setVisibility(View.VISIBLE);

        disableMenu = true;
        invalidateOptionsMenu();
    }

    private void configureRecyclerView(RecyclerView recyclerView, int spanCount) {
        GridLayoutManager glm = new GridLayoutManager(MainActivity.this, spanCount);
        recyclerView.setLayoutManager(glm);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, 0, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mScrollListener = new EndlessRecyclerViewScrollListener(glm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (sort == SORT_POPULAR) {
                    if (popularPage <= totalPopularPage){
                        callPopularMovies(popularPage, false);
                    }
                } else if (sort == SORT_TOP_RATED) {
                    if (topRatedPage <= totalTopRatedPage){
                        callTopRatedMovies(topRatedPage, false);
                    }
                } else if (sort == SORT_UPCOMING) {
                    if (upComingPage <= totalUpComingPage){
                        callUpComingMovies(upComingPage, false);
                    }
                } else if (sort == SORT_NOW_PLAYING) {
                    if (nowPlayingPage <= totalNowPlayingPage){
                        callNowPlayingMovies(nowPlayingPage, false);
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        menu.findItem(R.id.movie_popular).setEnabled(!disableMenu);
        menu.findItem(R.id.movie_top_rated).setEnabled(!disableMenu);
        menu.findItem(R.id.movie_up_coming).setEnabled(!disableMenu);
        menu.findItem(R.id.movie_now_playing).setEnabled(!disableMenu);
        menu.findItem(R.id.favorites).setEnabled(!disableMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.favorites:
                if (sort != SORT_FAVORITES) {
                    getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
                }
                return true;
            case R.id.movie_popular:
                if (mPopularMovieModels.size() > 0){
                    if (sort != SORT_POPULAR) {
                        mMovieModels.clear();
                        mMovieModels.addAll(mPopularMovieModels);

                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();
                        sort = SORT_POPULAR;
                    }
                    hideLoading();
                } else {
                    callPopularMovies(1, true);
                }
                return true;
            case R.id.movie_top_rated:
                if (mTopRatedMovieModels.size() > 0){
                    if (sort != SORT_TOP_RATED){
                        mMovieModels.clear();
                        mMovieModels.addAll(mTopRatedMovieModels);

                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();
                        sort = SORT_TOP_RATED;
                    }
                    hideLoading();
                } else {
                    callTopRatedMovies(1, true);
                }
                return true;
            case R.id.movie_up_coming:
                if (mUpComingMovieModels.size() > 0){
                    if (sort != SORT_UPCOMING){
                        mMovieModels.clear();
                        mMovieModels.addAll(mUpComingMovieModels);

                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();
                        sort = SORT_UPCOMING;
                    }
                    hideLoading();
                } else {
                    callUpComingMovies(1, true);
                }
                return true;
            case R.id.movie_now_playing:
                if (mNowPlayingMovieModels.size() > 0){
                    if (sort != SORT_NOW_PLAYING){
                        mMovieModels.clear();
                        mMovieModels.addAll(mNowPlayingMovieModels);

                        movieAdapter.notifyDataSetChanged();
                        mScrollListener.resetState();
                        sort = SORT_NOW_PLAYING;
                    }
                    hideLoading();
                } else {
                    callNowPlayingMovies(1, true);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.sort_key), sort);
        outState.putParcelableArrayList(getString(R.string.popular_movies_key), mPopularMovieModels);
        outState.putParcelableArrayList(getString(R.string.top_rated_movies_key), mTopRatedMovieModels);
        outState.putParcelableArrayList(getString(R.string.up_coming_movies_key), mUpComingMovieModels);
        outState.putParcelableArrayList(getString(R.string.now_playing_movies_key), mNowPlayingMovieModels);
        outState.putInt(getString(R.string.popular_page_key), popularPage);
        outState.putInt(getString(R.string.top_rated_page_key), topRatedPage);
        outState.putInt(getString(R.string.up_coming_page_key), upComingPage);
        outState.putInt(getString(R.string.now_playing_page_key), nowPlayingPage);
    }

    boolean internet_connection(){
        //Check if connected to internet, output accordingly
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public void setOnItemClickListener(View view, int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        if (sort == SORT_POPULAR) {
            intent.putExtra(getString(R.string.detail_key), mPopularMovieModels.get(position));
        } else if (sort == SORT_TOP_RATED) {
            intent.putExtra(getString(R.string.detail_key), mTopRatedMovieModels.get(position));
        } else if (sort == SORT_UPCOMING) {
            intent.putExtra(getString(R.string.detail_key), mUpComingMovieModels.get(position));
        } else if (sort == SORT_NOW_PLAYING) {
            intent.putExtra(getString(R.string.detail_key), mNowPlayingMovieModels.get(position));
        } else if (sort == SORT_FAVORITES) {
            intent.putExtra(getString(R.string.detail_key), mFavoriteMovieModels.get(position));
        }
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showLoading();
        switch (id){
            case ID_MOVIE_LOADER:
                Uri movieQueryUri = MovieContract.MovieEntry.CONTENT_URI;
                String sortOrder = MovieContract.MovieEntry._ID + " DESC";

                return new CursorLoader(this, movieQueryUri, MOVIE_PROJECTION, null, null, sortOrder);
            default:
                throw new RuntimeException(getString(R.string.loader_not_implemented) + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0){
            showEmptyFavoritesMessage();
        } else {
            convertToMovieModel(data);
            mMovieModels.clear();
            mMovieModels.addAll(mFavoriteMovieModels);

            movieAdapter.notifyDataSetChanged();
            hideLoading();
        }

        sort = SORT_FAVORITES;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieModels.clear();
        movieAdapter.notifyDataSetChanged();
    }

    private void convertToMovieModel(Cursor data) {
        mFavoriteMovieModels.clear();
        for (int i = 0; i < data.getCount(); i++){
            data.moveToPosition(i);
            MovieModel model = new MovieModel();
            model.setId(data.getInt(INDEX_MOVIE_ID));
            model.setOriginalTitle(data.getString(INDEX_MOVIE_NAME));
            model.setPosterPath(data.getString(INDEX_POSTER_PATH));
            model.setOverview(data.getString(INDEX_OVERVIEW));
            model.setVoteAverage(data.getDouble(INDEX_USER_RATING));
            model.setReleaseDate(data.getString(INDEX_RELEASE_DATE));
            mFavoriteMovieModels.add(model);
        }
    }

    private void showEmptyFavoritesMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mMovieProgressBar.setVisibility(View.GONE);
        mMovieEmptyTextView.setVisibility(View.VISIBLE);

        disableMenu = false;
        invalidateOptionsMenu();
    }

    public interface MovieApiInterface {
        @GET("movie/popular")
        Call<MainModel> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

        @GET("movie/top_rated")
        Call<MainModel> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int page);

        @GET("movie/upcoming")
        Call<MainModel> getUpComingMovies(@Query("api_key") String apiKey, @Query("page") int page);

        @GET("movie/now_playing")
        Call<MainModel> getNowPlayingMovies(@Query("api_key") String apiKey, @Query("page") int page);
    }
}
