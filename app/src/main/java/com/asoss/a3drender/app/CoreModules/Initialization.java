package com.asoss.a3drender.app.CoreModules;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.Utilities.CustomIntent;
import coursebuddy.karthaalabs.com.Utilities.LocalSharedPreference;

public class Initialization extends AppCompatActivity {


    //Declaration

    LocalSharedPreference sharedPreference;

    RadioButton rdbtnEnglish;
    RadioButton rbtnGerman;
    FloatingActionButton submitPreference;

    //***************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize);

        try {

            GetInitialize();

            Controllisteners();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //***************************************************************************************


    private void GetInitialize() {

        rdbtnEnglish = findViewById(R.id.rdbtn_english);
        rbtnGerman = findViewById(R.id.rbtn_german);
        submitPreference = findViewById(R.id.submit_preference);


        sharedPreference = new LocalSharedPreference(Initialization.this);

        boolean status = sharedPreference.getBoolean(Constants.Preferred_Language_Status);
        if (status) {
            Constants.globalStartIntent(Initialization.this, SplashActivity.class, null, 1);
        }


    }

    //***************************************************************************************

    private void Controllisteners() {

        submitPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rdbtnEnglish.isChecked() == true || rbtnGerman.isChecked() == true) {
                    sharedPreference.setValue(Constants.Preferred_Language_Key, "LANGUAGE");
                    sharedPreference.setValue(Constants.Preferred_Language_Code, "EN");
                    sharedPreference.setBoolean(Constants.Preferred_Language_Status, true);
                    Constants.globalStartIntent(Initialization.this, SplashActivity.class, null, 1);

                } else if (rdbtnEnglish.isChecked() == false && rbtnGerman.isChecked() == false) {
                    Toast.makeText(Initialization.this, "Choose any language..", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    //***************************************************************************************


}//END
