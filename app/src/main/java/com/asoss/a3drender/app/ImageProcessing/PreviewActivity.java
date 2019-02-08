package com.asoss.a3drender.app.ImageProcessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.*;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.asoss.a3drender.app.CoreModules.Constants;
import com.asoss.a3drender.app.CoreModules.GrabCutter;
import com.asoss.a3drender.app.GlobalObjects.DataObjects;
import com.asoss.a3drender.app.NetworkUtils.MatchClient;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.RenderUtils.ModelActivity;
import com.imagepicker.pdfpicker.Constant;

import java.util.ArrayList;

public class PreviewActivity extends AppCompatActivity {

    /*******************************************************************************************************************
     */
    @BindView(R.id.im_crop_image_view)
    ImageView imCropImageView;
    @BindView(R.id.btn_ok)
    FloatingActionButton btnOk;
    @BindView(R.id.parent_preview)
    RelativeLayout parent_layout;


    /*******************************************************************************************************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        try {

            GetInitialize();
            Controllisteners();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*******************************************************************************************************************
     */

    String OptionType="";

    private void GetInitialize() {

        try {

            ButterKnife.bind(this);

            // Show the Up button in the action bar.
            setupActionBar();


            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                Constants.ServerFilePath = extras.getString("ImageUrl");
                OptionType = extras.getString("OptionType");// 1 == camera, 2 == gallery
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = BitmapFactory.decodeFile(Constants.ServerFilePath, options);
                imCropImageView.setImageBitmap(bitmap);

                if(OptionType.toString().equals("1"))//Camera - do grabcutter
                {
                    //Do grabcutter tasks !
                    RunGrabCutter(bitmap);
                }


            }else{
                Constants.SnackBar(PreviewActivity.this, "No data found - Try later", parent_layout, 2);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*******************************************************************************************************************
     */
    private void RunGrabCutter(Bitmap source) {

        try {
            GrabCutter grabCutter=new GrabCutter(PreviewActivity.this, source, imCropImageView);
            grabCutter.Main_GrubCut();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*******************************************************************************************************************
     */
    private void Controllisteners() {

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendRequestToServer();

            }
        });
    }

    /*******************************************************************************************************************
     */
    private void SendRequestToServer() {


        //Testing by hardcode values
        //SendFileToServer(Environment.getExternalStorageDirectory().getPath() + "/DCIM/img_test/1-0102-998.04-0_01.png");
        //SendFileToServer(Str_ImageUrl);
        SendFileToServer(Constants.ServerFilePath);


    }

    /**************************************************************************************************************************
     * Sending image to server
     */

    BottomSheetDialog dialog;
    Handler mHandler;
    ArrayList<DataObjects> dataObjectsItems;

    private void ShowDialog() {
        View view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_dialog, null);
        dialog = new BottomSheetDialog(PreviewActivity.this);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void SendFileToServer(String filePath) {

        if (Constants.CheckNetwork(PreviewActivity.this)) {

            ShowDialog();

            Thread mThread = new Thread() {
                @Override
                public void run() {
                    try {


                        MatchClient matchClient = new MatchClient();

                        dataObjectsItems = matchClient.requestMatching(filePath, PreviewActivity.this, parent_layout, dialog);

                        mHandler.sendMessage(new Message());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mThread.start();
        } else {
            Constants.ShowInternetDialog(PreviewActivity.this);
        }


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                Constants.SnackBar(PreviewActivity.this, "Success..", parent_layout, 1);

                //LoadReponseListUI(dataObjectsItems);
                //Load List Activity
                if (dataObjectsItems != null && dataObjectsItems.size() > 0) {
                    launchModelRendererActivity();
                } else {
                    Constants.SnackBar(PreviewActivity.this, "No match found !", parent_layout, 2);
                }


            }
        };


    }

    /**************************************************************************************************************************
     */
    private void launchModelRendererActivity() {

        this.finish();
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        intent.putExtra("uri", "");
        intent.putExtra("immersiveMode", "true");
        intent.putExtra("verify", true);
        startActivity(intent);
        Constants.dataObjectsItems = dataObjectsItems;//Passing as global to next activity

    }

    /*******************************************************************************************************************
     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    /*******************************************************************************************************************
     */
    private void setupActionBar() {

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    /*******************************************************************************************************************
     */

}//END
