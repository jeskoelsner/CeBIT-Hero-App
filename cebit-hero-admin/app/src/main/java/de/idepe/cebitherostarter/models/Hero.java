package de.idepe.cebitherostarter.models;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface Hero {
    @PUT("/api/heroes/{code}/0")
    public void scoreHero(@Path("code") String code, Callback<HeroModel> cb);

    @PUT("/api/heroes/{code}")
    public void updateHero(@Path("code") String code, @Body HeroModel hero, Callback<HeroModel> cb);
}
