package com.asoss.a3drender.app.CoreModules;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.asoss.a3drender.app.R;

public class Initialization extends AppCompatActivity {

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


    }

    //***************************************************************************************

    private void Controllisteners() {


    }

    //***************************************************************************************


}//END
