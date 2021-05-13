package com.svalero.beers.service;

import com.svalero.beers.Constantes;
import com.svalero.beers.domain.Beer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import java.util.List;

public class BeersService {

    private BeersApiService apiService;

    public BeersService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constantes.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        apiService = retrofit.create(BeersApiService.class);
    }


    // ENDPOINTS
    public Observable<List<Beer>> getAllBeers() {
        return apiService.getAllBeers();
    }

}
