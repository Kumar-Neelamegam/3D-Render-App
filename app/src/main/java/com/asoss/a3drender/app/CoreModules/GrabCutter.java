package com.asoss.a3drender.app.CoreModules;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;

public class GrabCutter extends AppCompatActivity   {


    //**********************************************************************************************
    // Used to load the 'native-lib' library on application startup.
    static {

        System.loadLibrary("grubcut");
        System.loadLibrary("opencv_java3");
    }



}//end
