package com.ahmaddudayef.movieudacity.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmaddudayef.movieudacity.BuildConfig;
import com.ahmaddudayef.movieudacity.R;
import com.ahmaddudayef.movieudacity.adapter.ReviewAdapter;
import com.ahmaddudayef.movieudacity.adapter.TrailerAdapter;
import com.ahmaddudayef.movieudacity.data.MovieContract;
import com.ahmaddudayef.movieudacity.model.MovieURL;
import com.ahmaddudayef.movieudacity.pojo.MovieModel;
import com.ahmaddudayef.movieudacity.pojo.MovieTrailer;
import com.ahmaddudayef.movieudacity.pojo.Review;
import com.ahmaddudayef.movieudacity.pojo.ReviewMovie;
import com.ahmaddudayef.movieudacity.pojo.Trailer;
import com.ahmaddudayef.movieudacity.singleton.GsonSingleton;
import com.ahmaddudayef.movieudacity.util.EndlessRecyclerViewScrollListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapsing_tollbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.image_movie_backdrop) ImageView img_backdrop;
    @BindView(R.id.poster_movie) ImageView img_poster;
    @BindView(R.id.title_movie) TextView title;
    @BindView(R.id.rating_movie) TextView rating;
    @BindView(R.id.release_date_movie) TextView release_date;
    @BindView(R.id.description_movie) TextView description;
    @BindView(R.id.bt_add_favorite) ImageView btnFavorite;

    @BindView(R.id.view_pager_trailer_detail) ViewPager viewPagerTrailer;
    @BindView(R.id.tab_trailer_detail) TabLayout tabLayoutTrailer;
    @BindView(R.id.pb_trailer_detail) ProgressBar progressBarTrailer;
    @BindView(R.id.recyclerview_review) RecyclerView mRecyclerViewReview;
    @BindView(R.id.tv_review_empty) TextView reviewEmptyTextView;
    @BindView(R.id.pb_review_detail) ProgressBar reviewProgressBar;
    @BindView(R.id.nested_view) NestedScrollView nestedScrollView;

    private static final String POSTER_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String POSTER_IMAGE_SIZE = "w185";
    private static final String POSTER_IMAGE_SIZE2 = "w500";

    Context context;

    private ReviewAdapter reviewAdapter;
    private MovieModel mMovieModel;
    private ArrayList<Review> mReviewModels = new ArrayList<>();
    private ArrayList<Trailer> mTrailerModels = new ArrayList<>();

    private DetailApiInterface mDetailApiInterface;

    private boolean isFavorite = false;
    private int reviewPage = 2;
    private static int totalReviewPages;
    private EndlessRecyclerViewScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        nestedScrollView.setFillViewport(true);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.MOVIE_DB_API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build();

        mDetailApiInterface = retrofit.create(DetailApiInterface.class);

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.detail_key))){
            mMovieModel = intent.getParcelableExtra(getString(R.string.detail_key));

            isFavorite = isFavoriteMovie();
            initView(mMovieModel);
            initRecyclerView(mRecyclerViewReview);

            if (savedInstanceState != null){
                mTrailerModels = savedInstanceState.getParcelableArrayList(getString(R.string.trailer_key));
                configureViewPagerWithTabLayout(mTrailerModels);

                mReviewModels = savedInstanceState.getParcelableArrayList(getString(R.string.review_key));
                reviewPage = savedInstanceState.getInt(getString(R.string.review_page_key));
            } else {
                callTrailers(mMovieModel.getId());
                callReviews(mMovieModel.getId(), 1, true);
            }

            reviewAdapter = new ReviewAdapter(DetailActivity.this, mReviewModels);
            mRecyclerViewReview.setAdapter(reviewAdapter);

        } else {
            Toast.makeText(this, getString(R.string.failed_show_detail), Toast.LENGTH_LONG).show();
        }

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorite){
                    deleteFromDatabase();
                    Toast.makeText(DetailActivity.this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                    btnFavorite.setImageResource(R.drawable.ic_favorite_border_blue);
                } else {
                    insertToDatabase();
                    Toast.makeText(DetailActivity.this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                    btnFavorite.setImageResource(R.drawable.ic_favorite_blue);
                }
            }
        });
    }

    private void insertToDatabase() {
        ContentResolver detailContentResolver = getContentResolver();

        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovieModel.getId());
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_NAME, mMovieModel.getOriginalTitle());
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, mMovieModel.getPosterPath());
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovieModel.getOverview());
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, mMovieModel.getVoteAverage());
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovieModel.getReleaseDate());

        detailContentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);
    }

    private void deleteFromDatabase() {
        ContentResolver detailContentResolver = getContentResolver();
        String[] selectionArgumens = new String[]{String.valueOf(mMovieModel.getId())};
        detailContentResolver.delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ", selectionArgumens);
    }

    private void callTrailers(int id) {
        showTrailerLoading();

        Call<MovieTrailer> trailers = mDetailApiInterface.getTrailers(id, BuildConfig.MOVIE_DB_API_KEY);
        trailers.enqueue(new Callback<MovieTrailer>() {
            @Override
            public void onResponse(Call<MovieTrailer> call, retrofit2.Response<MovieTrailer> response) {
                if (response.body() != null){
                    ArrayList<Trailer> results = response.body().getResults();
                    if (results != null && !results.isEmpty()){
                        mTrailerModels.clear();

                        for (Trailer result : results){
                            if (result.getSite().equals(getString(R.string.youtube))) {
                                mTrailerModels.add(result);
                            }
                        }

                        configureViewPagerWithTabLayout(mTrailerModels);
                        hideTrailerLoading();
                    } else {
                        hideAllTrailerLayout();
                    }
                } else {
                    Toast.makeText(DetailActivity.this, R.string.failed_fetch_trailers, Toast.LENGTH_SHORT).show();
                    hideAllTrailerLayout();
                }
            }

            @Override
            public void onFailure(Call<MovieTrailer> call, Throwable t) {
                Log.e(TAG, getString(R.string.failed_fetch_trailers), t.getCause());
                Toast.makeText(DetailActivity.this, R.string.failed_fetch_trailers, Toast.LENGTH_SHORT).show();
                hideAllTrailerLayout();
            }
        });
    }

    private void hideAllTrailerLayout() {
        progressBarTrailer.setVisibility(View.GONE);
        viewPagerTrailer.setVisibility(View.INVISIBLE);
        tabLayoutTrailer.setVisibility(View.INVISIBLE);
    }

    private void hideTrailerLoading() {
        progressBarTrailer.setVisibility(View.GONE);
        viewPagerTrailer.setVisibility(View.VISIBLE);
        tabLayoutTrailer.setVisibility(View.VISIBLE);
    }

    private void showTrailerLoading() {
        progressBarTrailer.setVisibility(View.VISIBLE);
        viewPagerTrailer.setVisibility(View.INVISIBLE);
        tabLayoutTrailer.setVisibility(View.INVISIBLE);
    }

    private void configureViewPagerWithTabLayout(ArrayList<Trailer> results) {
        viewPagerTrailer.setAdapter(new TrailerAdapter(this ,getSupportFragmentManager(), results));
        tabLayoutTrailer.setupWithViewPager(viewPagerTrailer, true);
    }

    private void initRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(DetailActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (reviewPage <= totalReviewPages) {
                    callReviews(mMovieModel.getId(), reviewPage, false);
                }
            }
        };

        recyclerView.addOnScrollListener(mScrollListener);

//        mRecyclerViewTrailer.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerViewReview.setLayoutManager(new LinearLayoutManager(this));
//        trailerAdapter = new TrailerAdapter(null);
//        reviewAdapter = new ReviewAdapter(null);
//        mRecyclerViewTrailer.setAdapter(trailerAdapter);
//        mRecyclerViewReview.setAdapter(reviewAdapter);
    }

    private void callReviews(int id, int page, final boolean clearList) {
        if (clearList) showReviewLoading();

        Call<ReviewMovie> reviews = mDetailApiInterface.getReviews(id, BuildConfig.MOVIE_DB_API_KEY, page);
        reviews.enqueue(new Callback<ReviewMovie>() {
            @Override
            public void onResponse(Call<ReviewMovie> call, retrofit2.Response<ReviewMovie> response) {
                if (response.body() != null){
                    totalReviewPages = response.body().getTotalPages();
                    ArrayList<Review> results = response.body().getResults();
                    if (results != null && !results.isEmpty()){
                        if (clearList) mReviewModels.clear();
                        else reviewPage++;

                        mReviewModels.addAll(results);
                        reviewAdapter.notifyDataSetChanged();

                        mScrollListener.resetState();
                        hideReviewLoading();
                    } else {
                        showReviewLoading();
                    }
                } else {
                    Toast.makeText(DetailActivity.this, R.string.failed_fetch_reviews, Toast.LENGTH_SHORT).show();
                    showReviewEmptyMessage();
                }
            }

            @Override
            public void onFailure(Call<ReviewMovie> call, Throwable t) {
                Log.e(TAG, getString(R.string.failed_fetch_reviews), t.getCause());
                Toast.makeText(DetailActivity.this, R.string.failed_fetch_reviews, Toast.LENGTH_SHORT).show();
                hideReviewLoading();
            }
        });

    }

    private void showReviewEmptyMessage() {
        mRecyclerViewReview.setVisibility(View.INVISIBLE);
        reviewProgressBar.setVisibility(View.GONE);
        reviewEmptyTextView.setVisibility(View.VISIBLE);
    }

    private void hideReviewLoading() {
        mRecyclerViewReview.setVisibility(View.VISIBLE);
        reviewProgressBar.setVisibility(View.GONE);
        reviewEmptyTextView.setVisibility(View.GONE);
    }

    private void showReviewLoading() {
        mRecyclerViewReview.setVisibility(View.INVISIBLE);
        reviewProgressBar.setVisibility(View.VISIBLE);
        reviewEmptyTextView.setVisibility(View.GONE);
    }

    private boolean isFavoriteMovie() {
        ContentResolver detailContentResolver = getContentResolver();
        Cursor cursor = detailContentResolver.query(
                MovieContract.MovieEntry.buildMovieUriWithMovieId(mMovieModel.getId()),
                null,
                null,
                null,
                MovieContract.MovieEntry._ID + " ASC");

        if (cursor != null) {
            cursor.close();
            return cursor.getCount() > 0;
        } else {
            return false;
        }
    }

    private void initView(MovieModel mMovieModel) {
        collapsingToolbar.setTitle(mMovieModel.getTitle());
        toolbar.setTitle(mMovieModel.getTitle());

        title.setText(mMovieModel.getOriginalTitle());
        rating.setText(String.valueOf(mMovieModel.getVoteAverage()));
        release_date.setText(mMovieModel.getReleaseDate());
        description.setText(mMovieModel.getOverview());

        if (isFavorite) btnFavorite.setImageResource(R.drawable.ic_favorite_blue);
        else btnFavorite.setImageResource(R.drawable.ic_favorite_border_blue);

        Picasso.with(context)
                .load(POSTER_IMAGE_BASE_URL + POSTER_IMAGE_SIZE2 + mMovieModel.getBackdropPath())
                .into(img_backdrop);

        Picasso.with(context)
                .load(POSTER_IMAGE_BASE_URL + POSTER_IMAGE_SIZE + mMovieModel.getPosterPath())
                .into(img_poster);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.trailer_key), mTrailerModels);
        outState.putParcelableArrayList(getString(R.string.review_key), mReviewModels);
        outState.putInt(getString(R.string.review_page_key), reviewPage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public interface DetailApiInterface {
        @GET("movie/{id}/reviews")
        Call<ReviewMovie> getReviews(@Path("id") int id, @Query("api_key") String apiKey, @Query("page") int page);

        @GET("movie/{id}/videos")
        Call<MovieTrailer> getTrailers(@Path("id") int id, @Query("api_key") String apiKey);
    }
}
