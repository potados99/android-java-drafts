package org.potados.study_in_java;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitSingleton {
    private static Retrofit instance = null;

    public static Retrofit get() {
        if (instance == null) {
            OkHttpClient.Builder client = new OkHttpClient.Builder();

            instance = new Retrofit.Builder()
                    .baseUrl("http://www.potados.gq")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client.build())
                    .build();
        }

        return instance;
    }
}
