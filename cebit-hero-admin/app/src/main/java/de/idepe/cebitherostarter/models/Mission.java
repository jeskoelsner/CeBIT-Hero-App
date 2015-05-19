package de.idepe.cebitherostarter.models;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface Mission {
    @GET("/api/missions")
    public void lastMission(Callback<MissionModel> cb);

    @POST("/api/missions")
    public void createMission(@Body MissionModel mission, Callback<MissionModel> cb);

    @GET("/api/missions/{id}")
    public void getMission(@Path("id") String id, Callback<MissionModel> cb);
}
