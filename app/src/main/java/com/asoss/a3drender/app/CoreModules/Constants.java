package com.asoss.a3drender.app.CoreModules;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.asoss.a3drender.app.GlobalObjects.DataObjects;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.Utilities.CustomDialog;
import com.asoss.a3drender.app.Utilities.CustomIntent;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Constants {

    /*******************************************************************************************************************
     */

    public Constants constants;

    public static String DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + R.string.app_name;
    public static String Preferred_Language_Key = "Preferred_Language_Key";
    public static String Preferred_Language_Code = "Preferred_Language_Code";
    public static String Preferred_Language_Status = "Preferred_Language_Status";
    public static ArrayList<DataObjects> dataObjectsItems;
    public static String ServerFilePath="";

    public static String Server_IP = "192.168.0.7";
    public static int Server_Port = 5005;

    public Constants getInstance() {
        if (constants == null) {
            constants = new Constants();
            return constants;
        }

        return constants;
    }

    /*******************************************************************************************************************
     */


    public static void globalStartIntent(Context context, Class classes, Bundle bundle, int id) {
        ((Activity) context).finish();
        Intent intent = new Intent(context, classes);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        CustomIntent.customType(context, id);
        context.startActivity(intent);

    }

    /*******************************************************************************************************************
     */

    public static void globalStartIntent2(Context context, Class classes, Bundle bundle) {

        Intent intent = new Intent(context, classes);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        CustomIntent.customType(context, 4);
        context.startActivity(intent);

    }

    /*******************************************************************************************************************
     */


    public static void ExitSweetDialog(final Context ctx, final Class<?> className) {

        new CustomDialog(ctx)
                .setLayoutColor(R.color.md_green_500)
                .setImage(R.drawable.ic_exit_to_app_black_24dp)
                .setTitle(ctx.getResources().getString(R.string.message_title))
                .setDescription(ctx.getResources().getString(R.string.are_you_sure_want_to_exit))
                .setPossitiveButtonTitle("YES")
                .setNegativeButtonTitle("NO")
                .setOnPossitiveListener(((Activity) ctx)::finishAffinity);


    }

    /*******************************************************************************************************************
     */

    static String TAG = "PARTSCOUT";

    public static void Logger(String Message) {
        Log.e(TAG, Message);
    }


    public static void SnackBar(Context ctx, String Message, View parentLayout, int id) {

        Snackbar mSnackBar = Snackbar.make(parentLayout, Message, Snackbar.LENGTH_LONG);
        View view = mSnackBar.getView();
        view.setPadding(5, 5, 5, 5);

        if (id == 1)//Positive
        {
            view.setBackgroundColor(ctx.getResources().getColor(R.color.colorPrimary));
        } else if (id == 2)//Negative
        {
            view.setBackgroundColor(ctx.getResources().getColor(R.color.md_deep_orange_300));
        } else//Negative
        {
            view.setBackgroundColor(ctx.getResources().getColor(R.color.md_deep_orange_300));
        }


        TextView mainTextView = (view).findViewById(R.id.snackbar_text);
        mainTextView.setAllCaps(true);
        mainTextView.setTextSize(16);
        mainTextView.setTextColor(ctx.getResources().getColor(R.color.md_white_1000));
        mSnackBar.setDuration(3000);
        mSnackBar.show();

    }

    /*******************************************************************************************************************
     */

    public static void changeStatusBarColour(Context ctx) {
        if (Build.VERSION.SDK_INT >= 21) {
            ((Activity) ctx).getWindow().setNavigationBarColor(ContextCompat.getColor(ctx, R.color.colorPrimaryDark)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            ((Activity) ctx).getWindow().setStatusBarColor(ContextCompat.getColor(ctx, R.color.colorPrimary)); //status bar or the time bar at the top
        }
    }

    /*******************************************************************************************************************
     */


    public static boolean CheckNetwork(Context ctx) {
        ConnectivityManager cn = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf = cn.getActiveNetworkInfo();
        if (nf != null && nf.isConnected() == true) {

            return true;
        } else {

            return false;
        }
    }

    /*******************************************************************************************************************
     */
    public static void ShowInternetDialog(Context ctx) {

        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_no_item_internet_image);

        ProgressBar progress_bar;
        LinearLayout lyt_no_connection;
        AppCompatButton bt_retry;

        progress_bar = dialog.findViewById(R.id.progress_bar);
        lyt_no_connection = dialog.findViewById(R.id.lyt_no_connection);
        bt_retry = (AppCompatButton) dialog.findViewById(R.id.bt_retry);

        progress_bar.setVisibility(View.GONE);
        lyt_no_connection.setVisibility(View.VISIBLE);

        bt_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                progress_bar.setVisibility(View.VISIBLE);
                lyt_no_connection.setVisibility(View.GONE);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Constants.CheckNetwork(ctx)) {
                            dialog.dismiss();
                        } else {
                            progress_bar.setVisibility(View.GONE);
                            lyt_no_connection.setVisibility(View.VISIBLE);
                        }
                    }
                }, 1000);
            }
        });

        dialog.show();

    }
    /*******************************************************************************************************************
     * Save bitmap to storage
     */
    public static void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString()+"/Partscout";
        File myDir = new File(root);
        myDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "grabcutt"+ timeStamp +".png";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ServerFilePath = fname;
        Log.e("File Path: ", root+File.separator+fname);

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /*******************************************************************************************************************
     */
}//END
