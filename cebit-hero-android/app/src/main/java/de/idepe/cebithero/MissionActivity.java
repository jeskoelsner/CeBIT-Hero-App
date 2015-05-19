package de.idepe.cebithero;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cocoahero.android.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.mapbox.mapboxsdk.views.MapView;
import com.norbsoft.typefacehelper.TypefaceHelper;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.idepe.cebithero.models.HeroModel;
import de.idepe.cebithero.models.MissionModel;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MissionActivity extends ActionBarActivity {
    @InjectView(R.id.mapview) MapView mMapView;
    @InjectView(R.id.action_bar) Toolbar mToolbar;
    @InjectView(R.id.winOverlay) RelativeLayout mWinOverlay;

    private Activity mActivity;
    private HeroApplication mApplication;
    private LatLng startLocation = new LatLng(52.326, 9.809);

    private UserLocationOverlay userLocationOverlay;
    private GpsLocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        mActivity = this;
        mApplication = ((HeroApplication) getApplication());
        ButterKnife.inject(mActivity);
        TypefaceHelper.typeface(this, mApplication.getRobotoTypeface());

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.custom_actionbar_layout);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        //TODO SET LOCATION
        double passedLatitude = getIntent().getDoubleExtra("latitude", 52.326);
        double passedLongitude = getIntent().getDoubleExtra("longitude", 9.809);
        startLocation = new LatLng(passedLatitude, passedLongitude);

        Marker mission = new Marker("GO THERE!", "Mission meeting place", startLocation);
        mission.setIcon(new Icon(getResources().getDrawable(R.drawable.flag)));
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
                    if(mApplication.getMission() == null){
                        finish();
                    }else{
                        mApplication.updateLocation(newLocation, new Callback<HeroModel>() {
                            @Override
                            public void success(HeroModel heroModel, Response response) {
                                popupReady(heroModel.getLat(), heroModel.getLng(), startLocation.getLatitude(), startLocation.getLongitude());
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                //DO NOTHING!
                            }
                        });
                    }
                }
            }
        };
        userLocationOverlay.enableMyLocation(locationProvider);
        userLocationOverlay.enableFollowLocation();

        mMapView.getOverlays().add(userLocationOverlay);

        mWinOverlay.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        askLeave();
    }

    public static double calculateDistance(double lat1, double long1, double lat2, double long2) {
        double earthRadius = 6371000; // in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(long2 - long1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        return dist;
    }

    private void popupReady(double lat1, double long1, double lat2, double long2){
        if(mWinOverlay.getVisibility() == View.GONE && calculateDistance(lat1, long1, lat2, long2) <= 50){
            mWinOverlay.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.winOverlay})
    public void askLeave() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.mission_leave_title))
                .content(Html.fromHtml(getString(R.string.mission_leave)))
                .positiveText(R.string.mission_leave_yes)
                .negativeText(R.string.mission_leave_no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationProvider.stopLocationProvider();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mission, menu);
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

    @Override
    public void onResume() {
        super.onResume();

        locationProvider.setLocationUpdateMinDistance(5);
        locationProvider.setLocationUpdateMinTime(10000);
        locationProvider.startLocationProvider(userLocationOverlay);

        // Load GeoJSON
        try {
            FeatureCollection features = DataLoadingUtils.loadGeoJSONFromAssets(mActivity, "cebit.geojson");
            ArrayList<Object> uiObjects = DataLoadingUtils.createUIObjectsFromGeoJSONObjects(features, null);

            for (Object obj : uiObjects) {
                if (obj instanceof Marker) {
                    mMapView.addMarker((Marker) obj);
                } else if (obj instanceof PathOverlay) {
                    PathOverlay tmp = (PathOverlay) obj;
                    tmp.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);

                    Paint style = tmp.getPaint();
                    style.setStyle(Paint.Style.FILL_AND_STROKE);
                    style.setARGB(110, 68, 132, 246);
                    style.setAntiAlias(false);
                    style.setDither(false);
                    style.setStrokeWidth(1);

                    tmp.setPaint(style);
                    mMapView.getOverlays().add(tmp);
                }
            }
            if (uiObjects.size() > 0) {
                mMapView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
