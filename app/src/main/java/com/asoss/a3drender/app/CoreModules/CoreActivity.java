package com.asoss.a3drender.app.CoreModules;

import android.Manifest;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.Utilities.RuntimePermissionsActivity;

public abstract class CoreActivity extends RuntimePermissionsActivity implements  ActivityCompat.OnRequestPermissionsResultCallback{
    private Context context;
    private static final int REQUEST_PERMISSIONS = 20;


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void setContentView(int layoutResID) {
        try {
            super.setContentView(layoutResID);
            bindViews();
            setContext(this);
            setListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * method to bind all views related to resourceLayout
     */
    protected abstract void bindViews();

    /**
     * called to set view listener for views
     */
    protected abstract void setListeners();

    //***************************************************************************************************
    public void isStoragePermissionGranted() {

        CoreActivity.super.requestAppPermissions(new
                        String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET

                }, R.string
                        .runtime_permissions_txt
                , REQUEST_PERMISSIONS);


    }
    //***************************************************************************************************
//End
}
