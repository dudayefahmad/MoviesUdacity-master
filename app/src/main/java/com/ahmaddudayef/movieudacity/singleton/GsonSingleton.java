package com.ahmaddudayef.movieudacity.singleton;

import com.google.gson.Gson;

/**
 * Created by Ahmad Dudayef on 12/1/2016.
 */
public class GsonSingleton {
    static Gson gson;
    public static Gson getGson(){
        if (null == gson){
            return new Gson();
        }
        return gson;
    }
}
