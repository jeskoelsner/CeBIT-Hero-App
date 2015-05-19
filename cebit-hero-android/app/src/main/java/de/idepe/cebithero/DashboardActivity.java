package de.idepe.cebithero;

import android.app.Activity;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.norbsoft.typefacehelper.TypefaceHelper;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.idepe.cebithero.models.HeroModel;
import de.idepe.cebithero.models.MissionModel;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DashboardActivity extends ActionBarActivity implements OnShowcaseEventListener {
    @InjectView(R.id.action_bar) Toolbar mToolbar;
    @InjectView(R.id.dashboard_name) TextView mName;
    @InjectView(R.id.dashboard_score) TextView mScore;
    @InjectView(R.id.dashboard_subtext) TextView mSub;
    @InjectView(R.id.dashboard_timer) TextView mTimer;

    private Activity mActivity;
    private HeroApplication mApplication;

    private ShowcaseView mShowcaseView;
    private CountDownTimer mCountdown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mActivity = this;
        mApplication = ((HeroApplication) getApplication());
        ButterKnife.inject(mActivity);
        TypefaceHelper.typeface(this, mApplication.getRobotoTypeface());

        setSupportActionBar(mToolbar);
        mToolbar.setLogo(R.drawable.ic_toolbar);

        mApplication.startMQTT();
    }

    private void countDown(long time){
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        mCountdown = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                mTimer.setText(timeFormat.format(millisUntilFinished));
            }

            public void onFinish() {
                mTimer.setText("waiting...");
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final HeroModel user = mApplication.getUser();

        mApplication.checkLastMission(new Callback<MissionModel>() {
            @Override
            public void success(MissionModel missionModel, Response response) {
                countDown(missionModel.getCreated() + missionModel.getLifetime() - System.currentTimeMillis());
            }

            @Override
            public void failure(RetrofitError error) {
                countDown(TimeUnit.HOURS.toMillis(1));
            }
        });

        mApplication.checkUser(user.getCode(), new Callback<HeroModel>() {
            @Override
            public void success(HeroModel heroModel, Response response) {
                mName.setText(heroModel.getName());
                mScore.setText(heroModel.getScore() + " points");
            }

            @Override
            public void failure(RetrofitError error) {
                mName.setText(user.getName());
                mScore.setText(user.getScore() + " points");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mCountdown.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApplication.stopMQTT();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            mShowcaseView = createShowcaseView(
                    R.id.stepOne,
                    R.string.stepOneTitle,
                    R.string.stepOneMessage,
                    R.string.stepOneButton);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Showcase Events
     */

    private ShowcaseView createShowcaseView(int id, int title, int message, int buttontext){
        ShowcaseView tmp = new ShowcaseView.Builder(mActivity, true)
                .setShowcaseEventListener(this)
                .setStyle(R.style.Tutorial)
                .setContentTitle(Html.fromHtml(getString(title)))
                .setContentText(Html.fromHtml(getString(message)))
                .build();

        tmp.setButtonText(getString(buttontext));
        tmp.setId(id);

        return tmp;
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView scView) {
        scView.setBackgroundColor(R.color.tutorial_transparent);
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView scView) {
        int nextId = 0;
        int nextTitle = 0;
        int nextMessage = 0;
        int nextButton = 0;
        switch(scView.getId()){
            case R.id.stepOne:
                nextId = R.id.stepTwo;
                nextTitle = R.string.stepTwoTitle;
                nextMessage = R.string.stepTwoMessage;
                nextButton = R.string.stepTwoButton;
                break;
            case R.id.stepTwo:
                nextId = R.id.stepThree;
                nextTitle = R.string.stepThreeTitle;
                nextMessage = R.string.stepThreeMessage;
                nextButton = R.string.stepThreeButton;
                break;
            default:
                //Toast.makeText(getApplicationContext(), "ID is: " + scView.getId(), Toast.LENGTH_LONG).show();
                break;
        }

        if(nextId != 0){
            scView.clearAnimation();
            scView.removeAllViews();

            mShowcaseView = createShowcaseView(nextId, nextTitle, nextMessage, nextButton);
        }
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView scView) {
        //DO NOTHING
    }
}
