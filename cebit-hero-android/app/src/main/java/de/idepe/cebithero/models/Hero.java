package de.idepe.cebithero.models;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface Hero {
    @GET("/api/heroes")
    public void allHeroes(Callback<List<HeroModel>> cb);

    @GET("/api/heroes/{code}")
    public void getHero(@Path("code") String code, Callback<HeroModel> cb);

    @PUT("/api/heroes/{code}")
    public void updateHero(@Path("code") String code, @Body HeroModel hero, Callback<HeroModel> cb);
}
