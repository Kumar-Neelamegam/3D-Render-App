package com.asoss.a3drender.app.CoreModules;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.asoss.a3drender.app.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Kumar Neelamegam
 * Created a splash screen activity
 * 09-11-2018
 */
public class SplashActivity extends CoreActivity {

    //***************************************************************************************
    //Declaration
    @BindView(R.id.txtvw_title) TextView txtvwTitle;
    @BindView(R.id.img_logo) AppCompatImageView imgLogo;
    @BindView(R.id.progressbar) ProgressBar progressbar;


    private int progress = 0;
    private int progressStatus = 0;
    private final Handler handler = new Handler();

    //***************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            isStoragePermissionGranted();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //***************************************************************************************
    @Override
    public void onPermissionsGranted(int requestCode) {

        try {

            GetInitialize();

            Controllisteners();

        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    //***************************************************************************************

    private void GetInitialize() {

        ButterKnife.bind(this);

        YoYo.with(Techniques.FadeInRight)
                .duration(1500)
                .playOn(findViewById(R.id.img_logo));


    }

//***************************************************************************************

    private void Controllisteners() {

        CallNextIntent();

    }


    @Override
    protected void bindViews() {

    }

    @Override
    protected void setListeners() {

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


//***************************************************************************************


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



//***************************************************************************************

    private void CallNextIntent() {

        new Thread(new Runnable() {
            public void run() {

                while (progressStatus < 100) {
                    progressStatus = doSomeWork();

                    handler.post(() -> {
                        progressbar.setProgress(progressStatus);
                    });
                }

                handler.post(() -> {

                    progressbar.setVisibility(View.GONE);
                    Constants.globalStartIntent(SplashActivity.this, RenderFile.class, null);

                });
            }

            private int doSomeWork() {
                try {

                    Thread.sleep(30L);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ++progress;
                return
                        progress;
            }
        }).start();

    }



//***************************************************************************************


}//END
