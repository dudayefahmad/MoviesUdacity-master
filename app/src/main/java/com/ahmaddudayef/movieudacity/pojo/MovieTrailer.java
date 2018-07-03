
package com.ahmaddudayef.movieudacity.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieTrailer {

    @SerializedName("id")
    private int id;
    @SerializedName("results")
    private ArrayList<Trailer> results;

    public MovieTrailer() {
        // Constructor
    }

    public int getId() {
        return id;
    }

    public ArrayList<Trailer> getResults() {
        return results;
    }

    public void setResults(ArrayList<Trailer> results) {
        this.results = results;
    }

}
