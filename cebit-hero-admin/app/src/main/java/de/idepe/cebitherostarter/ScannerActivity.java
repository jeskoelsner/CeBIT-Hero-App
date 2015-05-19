package de.idepe.cebitherostarter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import de.idepe.cebitherostarter.models.HeroModel;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ScannerActivity extends ActionBarActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private Activity mActivity;
    private HeroApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        List<BarcodeFormat> onlyQR = new ArrayList<BarcodeFormat>();
        onlyQR.add(BarcodeFormat.QR_CODE);

        mScannerView.setFormats(onlyQR);

        setContentView(mScannerView);

        mActivity = this;
        mApplication = ((HeroApplication) getApplication());
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String code = null;

        try {
            code = rawResult.getText().split("/")[3];

            mScannerView.stopCamera();

            mApplication.scoreHero(code, new Callback<HeroModel>() {
                @Override
                public void success(HeroModel heroModel, Response response) {
                    switch ( response.getStatus() ) {
                        case HttpURLConnection.HTTP_NO_CONTENT:
                            Toast.makeText(mActivity, "Zu spät", Toast.LENGTH_SHORT).show();
                            break;
                        case HttpURLConnection.HTTP_ACCEPTED:
                            Toast.makeText(mActivity, heroModel.getName() + " " + heroModel.getScore(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    switch ( error.getResponse().getStatus() ) {
                        case HttpURLConnection.HTTP_NOT_MODIFIED:
                            Toast.makeText(mActivity, "bereits bepunktet", Toast.LENGTH_SHORT).show();
                            break;
                        case HttpURLConnection.HTTP_NOT_FOUND:
                            Toast.makeText(mActivity, "Spieler oder Mission nicht gefunden", Toast.LENGTH_SHORT).show();
                            break;
                        case HttpURLConnection.HTTP_GONE:
                            Toast.makeText(mActivity, "Mission bereits beendet", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        } catch( Exception e ) {
            Toast.makeText(mActivity, "invalid code", Toast.LENGTH_SHORT).show();
        }

        mScannerView.startCamera();
    }
}
