package com.ahmaddudayef.movieudacity.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmaddudayef.movieudacity.BuildConfig;
import com.ahmaddudayef.movieudacity.R;
import com.ahmaddudayef.movieudacity.activity.DetailActivity;
import com.ahmaddudayef.movieudacity.pojo.MovieModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ahmad Dudayef on 11/30/2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{

    private Context context;
    private ArrayList<MovieModel> mMovieModels;
    private ItemClickListener itemClickListener;


    public MovieAdapter (Context context, ArrayList<MovieModel> movieModels, ItemClickListener itemClickListener){
        this.context = context;
        this.mMovieModels = movieModels;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MovieModel movieModel = mMovieModels.get(position);

        holder.mNameTextView.setText(movieModel.getOriginalTitle());

        String year = movieModel.getReleaseDate().substring(0, 4);
        holder.mYearTextView.setText(year);

        String rating = String.valueOf(movieModel.getVoteAverage()) + context.getString(R.string.max_rating);
        holder.mRatingTextView.setText(rating);

        String posterPath = BuildConfig.MOVIE_DB_POSTER_URL + movieModel.getPosterPath();
        Picasso.with(context)
                .load(posterPath)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.mPosterImageView);
    }

    @Override
    public int getItemCount() {
        if (mMovieModels == null) return 0;
        else return mMovieModels.size();
    }

    public interface ItemClickListener {
        void setOnItemClickListener(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_movie_poster)
        ImageView mPosterImageView;
        @BindView(R.id.tv_movie_name)
        TextView mNameTextView;
        @BindView(R.id.tv_movie_year)
        TextView mYearTextView;
        @BindView(R.id.tv_movie_rating)
        TextView mRatingTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.setOnItemClickListener(view, getAdapterPosition());
            }
        }
    }
}
