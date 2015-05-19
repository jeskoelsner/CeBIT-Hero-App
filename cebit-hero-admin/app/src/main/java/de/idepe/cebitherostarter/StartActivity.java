package de.idepe.cebitherostarter;

import android.app.Activity;
import android.graphics.PointF;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import butterknife.InjectView;
import de.idepe.cebitherostarter.models.MissionModel;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class StartActivity extends ActionBarActivity {
    @InjectView(R.id.mapview)
    MapView mMapView;

    private Activity mActivity;
    private HeroApplication mApplication;

    private UserLocationOverlay userLocationOverlay;
    private GpsLocationProvider locationProvider;

    private LatLng startLocation = new LatLng(52.326, 9.809);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mActivity = this;
        mApplication = ((de.idepe.cebitherostarter.HeroApplication) getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();

        double passedLatitude = getIntent().getDoubleExtra("latitude", 52.326);
        double passedLongitude = getIntent().getDoubleExtra("longitude", 9.809);
        startLocation = new LatLng(passedLatitude, passedLongitude);

        Marker mission = new Marker("GO THERE!", "Mission meeting place", startLocation);
        //mission.setIcon(new Icon(getResources().getDrawable(R.drawable.flag)));
        Log.v("Anchor", "Point: " + mission.getAnchor().toString());
        mission.setAnchor(new PointF(0.26f, 0.92f));

        mMapView.setCenter(startLocation);
        mMapView.setScrollableAreaLimit(new BoundingBox(52.332834, 9.816736, 52.315494, 9.792821));
        mMapView.setTileSource(new MapboxTileLayer("proxylittle.ledeo868"));
        mMapView.setUserLocationTrackingMode(UserLocationOverlay.TrackingMode.FOLLOW);
        mMapView.setUserLocationRequiredZoom(12);
        mMapView.setZoom(17);
        mMapView.addMarker(mission);
        locationProvider = new GpsLocationProvider(mActivity);
        locationProvider.setLocationUpdateMinDistance(5);
        locationProvider.setLocationUpdateMinTime(10000);

        userLocationOverlay = new UserLocationOverlay(locationProvider, mMapView) {
            private Location lastLocation;

            @Override
            public void onLocationChanged(Location location, GpsLocationProvider source) {
                super.onLocationChanged(location, source);

                if(lastLocation == null || !(lastLocation.getLatitude() == location.getLatitude() && lastLocation.getLongitude() == location.getLongitude())){
                    lastLocation = location;
                    final Location newLocation = location;

                    MissionModel model = new MissionModel();
                    model.setLng(newLocation.getLongitude());
                    model.setLat(newLocation.getLatitude());

                    mApplication.createMission(model, new Callback<MissionModel>() {
                        @Override
                        public void success(MissionModel missionModel, Response response) {

                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
