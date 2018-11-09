package com.asoss.a3drender.app.CoreModules;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    ProgressBar progressBar;
    TextView progress_status;

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

        progressBar=findViewById(R.id.progressBar);

        YoYo.with(Techniques.FadeInRight)
                .duration(2500)
                .playOn(findViewById(R.id.txtvw_title));

        YoYo.with(Techniques.ZoomInDown)
                .duration(1500)
                .playOn(findViewById(R.id.card_view));

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
                        progressBar.setProgress(progressStatus);
                       // progress_status.setText((String.valueOf(progressStatus))+" %");
                    });
                }

                handler.post(() -> {

                    progressBar.setVisibility(View.GONE);
                   // Constants.globalStartIntent(SplashActivity.this, RenderFile.class, null);

                });
            }

            private int doSomeWork() {
                try {
                    // ---simulate doing some work---
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
