
package com.ahmaddudayef.movieudacity.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewMovie {

    @SerializedName("id")
    private int id;
    @SerializedName("page")
    private int page;
    @SerializedName("results")
    private ArrayList<Review> results;
    @SerializedName("total_pages")
    private int totalPages;
    @SerializedName("total_results")
    private int totalResults;

    public ReviewMovie() {
        // Constructor
    }

    public int getId() {
        return id;
    }

    public int getPage() {
        return page;
    }

    public ArrayList<Review> getResults() {
        return results;
    }

    public void setResults(ArrayList<Review> results) {
        this.results = results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

}
