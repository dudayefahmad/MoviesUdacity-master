package com.ahmaddudayef.movieudacity.data;

import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ahmad Dudayef on 6/8/2018.
 */

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.ahmaddudayef.movieudacity";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_MOVIE_NAME = "movieName";
        public static final String COLUMN_POSTER_PATH = "posterPath";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_USER_RATING = "userRating";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";

        public static Uri buildMovieUriWithMovieId(int id){
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
        }
    }
}
