package de.idepe.cebithero;

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

import com.norbsoft.typefacehelper.TypefaceCollection;
import com.norbsoft.typefacehelper.TypefaceHelper;

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

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.idepe.cebithero.helper.AdvancedPreferences;
import de.idepe.cebithero.models.Hero;
import de.idepe.cebithero.models.HeroModel;
import de.idepe.cebithero.models.Mission;
import de.idepe.cebithero.models.MissionModel;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HeroApplication extends Application implements IMqttActionListener, MqttCallback{
    //Typefaces
    private TypefaceCollection mRobotoTypeface;

    //Mqtt
    private String host = "tcp://rabbit.livesapp.io:1883";
    private MqttAndroidClient client;
    private MqttConnectOptions options = new MqttConnectOptions();
    private MqttClientPersistence usePersistence = new MemoryPersistence();

    private RestAdapter restAdapter;
    private Hero heroService;
    private Mission missionService;

    //Shared prefs
    private AdvancedPreferences sharedPrefs;

    private int defaultVolume;
    private Uri alarmSound;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private String clientID;

    @Override public void onCreate() {
        super.onCreate();

        mRobotoTypeface = new TypefaceCollection.Builder()
                .set(Typeface.NORMAL, Typeface.createFromAsset(getAssets(),
                        "fonts/roboto/roboto-regular.ttf"))
                .set(Typeface.BOLD, Typeface.createFromAsset(getAssets(),
                        "fonts/roboto/roboto-bold.ttf"))
                .set(Typeface.ITALIC, Typeface.createFromAsset(getAssets(),
                        "fonts/roboto/roboto-thin.ttf"))
                .set(Typeface.BOLD_ITALIC, Typeface.createFromAsset(getAssets(),
                        "fonts/roboto/roboto-light.ttf"))
                .create();

        TypefaceHelper.init(mRobotoTypeface);

        mMediaPlayer = new MediaPlayer();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
        defaultVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);

        options.setPassword("jackrabbit_9_emurgency.io".toCharArray());
        options.setUserName("bucksbunny_6_emurgency.io");
        options.setKeepAliveInterval(49);
        options.setConnectionTimeout(300);
        options.setCleanSession(false);

        sharedPrefs = new AdvancedPreferences(this, null, MODE_PRIVATE);

        restAdapter = new RestAdapter.Builder()

                .setEndpoint("http://heroadmin.livesapp.net")
                .build();
        heroService = restAdapter.create(Hero.class);
        missionService = restAdapter.create(Mission.class);
    }

    public void checkUser(String code, final Callback<HeroModel> cb) {
        heroService.getHero(code, new Callback<HeroModel>() {
            @Override
            public void success(HeroModel heroModel, Response response) {
                saveUser(heroModel);
                cb.success(heroModel, response);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.failure(error);
            }
        });
    }

    public void checkLastMission(final Callback<MissionModel> cb) {
        if(getMission() != null && System.currentTimeMillis() > getMission().getCreated() + getMission().getLifetime() + TimeUnit.HOURS.toMillis(1)){
            missionService.lastMission(new Callback<MissionModel>() {
                @Override
                public void success(MissionModel missionModel, Response response) {

                    if(System.currentTimeMillis() <= missionModel.getCreated() + missionModel.getLifetime() + TimeUnit.HOURS.toMillis(1)){
                        saveMission(missionModel);
                    }else{
                        saveMission(null);
                    }
                    cb.success(missionModel,response);
                }

                @Override
                public void failure(RetrofitError error) {
                    cb.failure(error);
                }
            });
        }
    }

    public void updateLocation(Location location, Callback<HeroModel> cb){
        HeroModel data = new HeroModel();
        data.setLat(location.getLatitude());
        data.setLng(location.getLongitude());
        heroService.updateHero(getUser().getCode(), data, cb);
    }

    public void saveUser(HeroModel hero) {
        sharedPrefs.putObject("currentUser", hero);
        sharedPrefs.commit();
    }

    public HeroModel getUser() {
        return sharedPrefs.getObject("currentUser", HeroModel.class);
    }

    public void saveMission(MissionModel hero) {
        sharedPrefs.putObject("currentMission", hero);
        sharedPrefs.commit();
    }

    public MissionModel getMission() {
        return sharedPrefs.getObject("currentMission", MissionModel.class);
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

    public TypefaceCollection getRobotoTypeface() {
        return mRobotoTypeface;
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        Log.v("MQTT", "\tCONNECTED!");
        try {
            client.subscribe("/hero/game/", 1 );
        } catch (MqttException e) {
            Log.v("MQTT", "\tSUBSCRIPTION ERROR: " + e.getMessage());
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        Log.e("MQTT", throwable.getMessage());
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Log.v("MQTT", "\tCONNECTION LOST!");
    }

    class StopTask extends TimerTask {

        @Override
        public void run() {
            stopSound();
        }

    }

    public void playSound(int volume, int length, boolean setDefault, boolean vibrate) {
        int flag = AudioManager.RINGER_MODE_NORMAL;

        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, flag);
        try {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.setDataSource(this, alarmSound);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                Timer timer = new Timer("timer", true);
                timer.schedule(new StopTask(), length * 1000);
            }

        } catch (IOException e) {
            Log.v("Sound", "Playing sound not possible");
        }

    }

    public void stopSound() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, defaultVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        String payload = new String(mqttMessage.getPayload());

        missionService.getMission(payload, new Callback<MissionModel>() {
            @Override
            public void success(MissionModel missionModel, Response response) {
                saveMission(missionModel);

                Intent intent = new Intent(getApplicationContext(), MissionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("latitude", missionModel.getLat());
                intent.putExtra("longitude", missionModel.getLng());
                startActivity(intent);
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO: retry 2x
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
