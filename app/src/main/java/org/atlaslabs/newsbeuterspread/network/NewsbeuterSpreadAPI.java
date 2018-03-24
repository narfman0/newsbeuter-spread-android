package org.atlaslabs.newsbeuterspread.network;

import io.reactivex.Single;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NewsbeuterSpreadAPI {
    @GET("api/item/")
    Single<ItemsResponse> getUnread();

    @DELETE("api/item/{id}/")
    Single<ItemDeleteResponse> markRead(
            @Path("id") int id);
}