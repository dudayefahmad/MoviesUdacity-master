package com.ahmaddudayef.movieudacity.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ahmaddudayef.movieudacity.R;

public class MovieProvider extends ContentProvider {
    public static final int CODE_MOVIE_FAVORITES = 100;
    public static final int CODE_MOVIE_WITH_ID = 101;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private MovieDBHelper mOpenHelper;

    private Context context;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE, CODE_MOVIE_FAVORITES);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", CODE_MOVIE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        mOpenHelper = new MovieDBHelper(getContext());
        context = getContext();
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)){
            case CODE_MOVIE_FAVORITES:
                db.beginTransaction();
                int rowInserted = 0;
                try {
                    for (ContentValues value : values){
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1){
                            rowInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowInserted > 0){
                    context.getContentResolver().notifyChange(uri, null);
                }
                return rowInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        switch (sUriMatcher.match(uri)){
            case CODE_MOVIE_FAVORITES:
                Uri resultUri = null;
                long id = mOpenHelper.getWritableDatabase().insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (id != -1){
                    resultUri = MovieContract.MovieEntry.buildMovieUriWithMovieId(values.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                }
                if (resultUri != null) {
                    context.getContentResolver().notifyChange(resultUri, null);
                }
                return resultUri;
            default:
                throw new UnsupportedOperationException(context.getString(R.string.content_provider_unknown_uri) + uri);

        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Cursor cursor;

        switch (sUriMatcher.match(uri)){
            case CODE_MOVIE_WITH_ID: {
                String movieId = uri.getLastPathSegment();
                String[] selectionArgurments = new String[]{movieId};

                cursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArgurments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_MOVIE_FAVORITES: {
                cursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException(context.getString(R.string.content_provider_unknown_uri) + uri);
        }

        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRowDeleted;
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE_FAVORITES:
                numRowDeleted = mOpenHelper.getWritableDatabase().delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(context.getString(R.string.content_provider_unknown_uri) + uri);
        }

        if (numRowDeleted != 0){
            context.getContentResolver().notifyChange(uri, null);
        }
        return numRowDeleted;
    }

    @Override
    public String getType(Uri uri) {
        throw new RuntimeException(context.getString(R.string.content_provider_not_implemented));
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new RuntimeException(context.getString(R.string.content_provider_not_implemented));
    }
}
