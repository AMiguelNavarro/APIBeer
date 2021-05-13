package com.svalero.beers.service;

import com.svalero.beers.domain.Beer;
import retrofit2.http.GET;
import rx.Observable;

import java.util.List;

public interface BeersApiService {

    /**
     * Devuelve todas las cervezas de la API
     * @return
     */
    @GET("beers")
    Observable<List<Beer>> getAllBeers();

}
