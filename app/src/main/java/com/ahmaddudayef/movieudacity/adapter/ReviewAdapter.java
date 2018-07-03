package com.ahmaddudayef.movieudacity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahmaddudayef.movieudacity.R;
import com.ahmaddudayef.movieudacity.pojo.Review;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ahmad Dudayef on 12/5/2016.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final int CHARS_LIMIT = 250;
    private Context mContext;
    private ArrayList<Review> mReviewResultModels;

    public ReviewAdapter (Context context, ArrayList<Review> reviewResultModels){
        mContext = context;
        mReviewResultModels = reviewResultModels;
    }

    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review reviewData = mReviewResultModels.get(position);
        holder.author.setText(reviewData.getAuthor());

        String review = reviewData.getContent();
        if (review.length() > CHARS_LIMIT){
            setSpannableString(holder.content, review, true);
        } else {
            holder.content.setText(review);
        }
    }

    @Override
    public int getItemCount() {
        if (mReviewResultModels == null) return 0;
        else return mReviewResultModels.size();
    }

    private void setSpannableString(final TextView textView, final String text, final boolean isMore) {
        SpannableString spannableString;
        if (isMore) {
            String subText = text.substring(0, CHARS_LIMIT) + "...";
            spannableString = new SpannableString(subText + " " + mContext.getString(R.string.read_more));
        } else {
            spannableString = new SpannableString(text + " " + mContext.getString(R.string.read_less));
        }

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                setSpannableString(textView, text, !isMore);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(clickableSpan, spannableString.length() - 9, spannableString.length(), 0);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.author)
        TextView author;
        @BindView(R.id.content)
        TextView content;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setTag(itemView);
        }
    }
}
