package org.atlaslabs.newsbeuterspread.network;

import io.reactivex.annotations.Nullable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestUtil {
    public static NewsbeuterSpreadAPI createAPI(String baseURL, @Nullable String username, @Nullable String password) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (username != null && password != null)
            httpClient.addInterceptor(new BasicAuthInterceptor(username, password));
        return new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(NewsbeuterSpreadAPI.class);
    }
}