package de.idepe.cebithero.models;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface Mission {
    @GET("/api/missions")
    public void lastMission(Callback<MissionModel> cb);

    @GET("/api/missions/{id}")
    public void getMission(@Path("id") String id, Callback<MissionModel> cb);
}
