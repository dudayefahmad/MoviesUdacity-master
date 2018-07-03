package com.ahmaddudayef.movieudacity.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahmaddudayef.movieudacity.R;
import com.ahmaddudayef.movieudacity.activity.DetailActivity;
import com.ahmaddudayef.movieudacity.fragment.TrailerFragment;
import com.ahmaddudayef.movieudacity.pojo.Trailer;

import java.util.ArrayList;

/**
 * Created by Ahmad Dudayef on 12/5/2016.
 */
public class TrailerAdapter extends FragmentPagerAdapter{

    private ArrayList<Trailer> results;
    private Context context;

    public TrailerAdapter(Context context, FragmentManager manager, ArrayList<Trailer> results) {
        super(manager);
        this.context = context;
        this.results = results;
    }


    @Override
    public Fragment getItem(int position) {
        return TrailerFragment.newInstance(context, results.get(position).getKey());
    }

    @Override
    public int getCount() {
        if (results == null) return 0;
        else return results.size();
    }
}
