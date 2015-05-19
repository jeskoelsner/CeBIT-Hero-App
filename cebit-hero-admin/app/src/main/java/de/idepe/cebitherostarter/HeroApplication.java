package de.idepe.cebitherostarter;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import de.idepe.cebitherostarter.models.Hero;
import de.idepe.cebitherostarter.models.HeroModel;
import de.idepe.cebitherostarter.models.Mission;
import de.idepe.cebitherostarter.models.MissionModel;
import retrofit.Callback;
import retrofit.RestAdapter;

public class HeroApplication extends Application implements IMqttActionListener, MqttCallback{

    //Mqtt
    private String host = "tcp://rabbit.livesapp.io:1883";
    private MqttAndroidClient client;
    private MqttConnectOptions options = new MqttConnectOptions();
    private MqttClientPersistence usePersistence = new MemoryPersistence();

    private RestAdapter restAdapter;
    private Hero heroService;
    private Mission missionService;

    private int defaultVolume;
    private Uri alarmSound;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private String clientID;

    @Override public void onCreate() {
        super.onCreate();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://heroadmin.livesapp.net")
                .build();
        heroService = restAdapter.create(Hero.class);
        missionService = restAdapter.create(Mission.class);
    }

    public void scoreHero(String code, Callback<HeroModel> cb){
        heroService.scoreHero(code, cb);
    }

    public void checkMission(String id, Callback<MissionModel> cb){
        missionService.getMission(id, cb);
    }

    public void createMission(MissionModel model, Callback<MissionModel> cb) {
        missionService.createMission(model, cb);
    }

    public String getAndroidClientId() {
        if (clientID == null) {
            String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (android_id.length() > 8) {
                android_id = android_id.substring(0, 8);
            }
            clientID = android_id;
        }
        return clientID;
    }

    public void startMQTT() {
        client = new MqttAndroidClient(this, host, "andi_" + getAndroidClientId(), usePersistence);
        if(!client.isConnected()){
            try {
                client.connect(options, null, this);
            } catch (MqttException e) {
                Log.e("MQTT", "\tCONNECTION ERROR: " + e.getMessage());
            }
            client.setCallback(this);
        }
    }

    public void stopMQTT(){
        if(client.isConnected()){
            try {
                client.disconnect();
            } catch (MqttException e) {
                Log.e("MQTT", "\tDISCONNECT ERROR: " + e.getMessage());
            }
        }
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        Log.v("MQTT", "\tCONNECTED!");
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        Log.e("MQTT", throwable.getMessage());
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Log.v("MQTT", "\tCONNECTION LOST!");
    }


    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        //DO NOTHING
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //TODO CHECK IF COMPLETE
    }
}
