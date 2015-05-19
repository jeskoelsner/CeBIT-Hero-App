package de.idepe.cebithero;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.snowdream.android.app.AbstractUpdateListener;
import com.github.snowdream.android.app.DownloadListener;
import com.github.snowdream.android.app.DownloadManager;
import com.github.snowdream.android.app.DownloadTask;
import com.github.snowdream.android.app.UpdateFormat;
import com.github.snowdream.android.app.UpdateInfo;
import com.github.snowdream.android.app.UpdateManager;
import com.github.snowdream.android.app.UpdateOptions;
import com.github.snowdream.android.app.UpdatePeriod;
import com.norbsoft.typefacehelper.TypefaceHelper;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.idepe.cebithero.models.HeroModel;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends Activity implements OnShowcaseEventListener{
    @InjectView(R.id.help) ImageButton mHelpButton;
    @InjectView(R.id.qrCode) ImageButton mQrCodeButton;
    @InjectView(R.id.softwareagButton) ImageButton mSoftwareAGButton;
    @InjectView(R.id.livesappButton) ImageButton mLivesappButton;
    @InjectView(R.id.startButton) Button mStartButton;

    @InjectView(R.id.codeInput) EditText mCodeText;
    @InjectView(R.id.loginProgress) ProgressBar mProgressView;
    @InjectView(R.id.tandc) TextView mTCView;

    private Activity mActivity;
    private HeroApplication mApplication;
    private ShowcaseView mShowcaseView;

    private UpdateManager updateManager;
    private UpdateOptions updateOptions;

    private MaterialDialog.Builder updateDialogBuilder;
    private MaterialDialog updateDialog;

    private MaterialDialog.Builder downloadDialogBuilder;
    private MaterialDialog downloadDialog;

    private AbstractUpdateListener updateListener = new AbstractUpdateListener(){

        @Override
        public void onShowNoUpdateUI() {
            //DO NOTHING
        }

        @Override
        public void onShowUpdateUI(final UpdateInfo updateInfo) {
            final String downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            final String apkFile = downloadDir + File.separator + "hero.apk";

            //delete all temporary
            File tmp = new File( apkFile );
            tmp.delete();

            updateDialog = updateDialogBuilder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    DownloadManager manager = new DownloadManager(mActivity);
                    final DownloadTask downloadTask = new DownloadTask(mActivity);

                    downloadTask.setUrl(updateInfo.getApkUrl());
                    downloadTask.setPath(apkFile);

                    manager.start(downloadTask, new DownloadListener<Integer, DownloadTask>() {
                        @Override
                        public void onProgressUpdate(Integer... values) {
                            super.onProgressUpdate(values);
                            onShowUpdateProgressUI(updateInfo, downloadTask, values[0]);
                        }

                        @Override
                        public void onSuccess(DownloadTask downloadTask) {
                            super.onSuccess(downloadTask);
                            if (downloadTask != null && !TextUtils.isEmpty(downloadTask.getPath())) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setDataAndType(Uri.parse("file://" + downloadTask.getPath()), "application/vnd.android.package-archive");
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }
                        }
                    });

                    dialog.dismiss();
                    downloadDialog = downloadDialogBuilder.show();
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    informCancel(updateInfo);
                    dialog.dismiss();
                }
            }).show();
        }

        @Override
        public void onShowUpdateProgressUI(UpdateInfo updateInfo, DownloadTask downloadTask, int i) {
            if(i != 100){
                downloadDialog.setProgress(i);
            }else{
                downloadDialog.dismiss();
            }
        }

        @Override
        public void ExitApp() { /* Do nothing */ }
    };

    /**
     * Android Lifecycle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mActivity = this;

        mApplication = ((HeroApplication) getApplication());
        ButterKnife.inject(mActivity);
        TypefaceHelper.typeface(this, mApplication.getRobotoTypeface());

        updateManager = new UpdateManager(this);

        HeroModel savedUser = mApplication.getUser();
        mCodeText.setText(savedUser != null ? savedUser.getCode() : "");

        updateOptions = new UpdateOptions.Builder(this)
                .checkUrl("http://lab.guardiansystems.eu/hero/cebit-hero-updates/raw/master/update.xml")
                .updateFormat(UpdateFormat.XML)
                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
                .checkPackageName(true)
                .build();

        updateDialogBuilder = new MaterialDialog.Builder(this)
                .title(getString(R.string.new_update_available_title))
                .content(Html.fromHtml(getString(R.string.new_update_available)))
                .positiveText(R.string.new_update_available_yes)
                .negativeText(R.string.new_update_available_no);

        downloadDialogBuilder = new MaterialDialog.Builder(this)
                .title(R.string.new_update_available_progress_title)
                .content(R.string.new_update_available_progress)
                .progress(false, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateManager.check(this, updateOptions, updateListener);
    }

    /**
     * Butterknife UI Events
     */

    @OnClick({R.id.startButton})
    public void popupStart() {
        final String doubleCheck = mCodeText.getText().toString();
        mProgressView.setVisibility(View.VISIBLE);
        mApplication.checkUser(doubleCheck, new Callback<HeroModel>() {
            @Override
            public void success(HeroModel heroModel, Response response) {
                if (doubleCheck.equals(heroModel.getCode())) {
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                mProgressView.setVisibility(View.GONE);
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.getResponse().getStatus() == 404) {
                    new MaterialDialog.Builder(mActivity)
                            .title(getString(R.string.qr_invalid_title))
                            .content(Html.fromHtml(getString(R.string.qr_invalid)))
                            .positiveText(R.string.okay)
                            .show();
                }
                mProgressView.setVisibility(View.GONE);
            }
        });
    }

    @OnClick({R.id.qrCode})
    public void popupQr() {
        Toast.makeText(mActivity, "QR Scanner Deactivated, wait till CeBIT", Toast.LENGTH_LONG).show();
    }

    @OnClick({R.id.tandc})
    public void popupTandC() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.terms_and_conditions_title))
                .content(Html.fromHtml(getString(R.string.terms_and_conditions)))
                .positiveText(R.string.tutorialThreeButton)
                .show();
    }

    @OnClick({R.id.help})
    public void popupShowcaseView() {
        mShowcaseView = createShowcaseView(
                R.id.tutorialOne,
                R.string.tutorialOneTitle,
                R.string.tutorialOneMessage,
                R.string.tutorialOneButton);
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
            case R.id.tutorialOne:
                nextId = R.id.tutorialTwo;
                nextTitle = R.string.tutorialTwoTitle;
                nextMessage = R.string.tutorialTwoMessage;
                nextButton = R.string.tutorialTwoButton;
                break;
            case R.id.tutorialTwo:
                nextId = R.id.tutorialThree;
                nextTitle = R.string.tutorialThreeTitle;
                nextMessage = R.string.tutorialThreeMessage;
                nextButton = R.string.tutorialThreeButton;
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



