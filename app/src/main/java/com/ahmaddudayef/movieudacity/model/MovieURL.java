package com.ahmaddudayef.movieudacity.model;

import com.ahmaddudayef.movieudacity.BuildConfig;

/**
 * Created by Ahmad Dudayef on 11/30/2016.
 */
public class MovieURL {
    public static final String
            BASE_URL = "https://api.themoviedb.org/3/";

    public static String getMovie(String kategori){
        return BASE_URL + "movie/" + kategori
                + "?api_key=" + BuildConfig.MOVIE_DB_API_KEY;
    }

    public static String getTrailer(String id_movie){
        return BASE_URL + "movie/" + id_movie +"/videos"
                + "?api_key=" + BuildConfig.MOVIE_DB_API_KEY;
    }

    public static String getReview(String id_movie){
        return BASE_URL + "movie/" + id_movie + "/reviews"
                + "?api_key=" + BuildConfig.MOVIE_DB_API_KEY;
    }

}
