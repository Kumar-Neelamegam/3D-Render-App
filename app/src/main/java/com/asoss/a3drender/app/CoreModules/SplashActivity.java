package com.asoss.a3drender.app.CoreModules;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.asoss.a3drender.app.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Kumar Neelamegam
 * Created a splash screen activity
 * 09-11-2018
 */
public class SplashActivity extends AppCompatActivity {

    //***************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            GetInitialize();

            Controllisteners();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
//***************************************************************************************

    private void GetInitialize() {

        YoYo.with(Techniques.FadeInRight)
                .duration(2500)
                .playOn(findViewById(R.id.txtvw_title));

        YoYo.with(Techniques.ZoomInDown)
                .duration(1500)
                .playOn(findViewById(R.id.card_view));

    }

//***************************************************************************************

    private void Controllisteners() {


    }

//***************************************************************************************


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



//***************************************************************************************



}//END
