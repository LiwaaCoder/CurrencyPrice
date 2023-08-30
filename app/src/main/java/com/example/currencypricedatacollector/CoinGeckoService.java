package com.example.currencypricedatacollector;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CoinGeckoService {

    @GET("simple/price")
    Call<Object> getCryptoPrice(@Query("ids") String ids, @Query("vs_currencies") String vsCurrencies);
}
