package com.asoss.a3drender.app.ImageProcessing;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.asoss.a3drender.app.CoreModules.Constants;
import com.asoss.a3drender.app.CoreModules.RenderFile;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.Utilities.CropModel;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CropActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {


    ImageView im_crop_image_view;
    Path clipPath;
    Bitmap bmp;
    Bitmap alteredBitmap;
    Canvas canvas;
    Paint paint;
    float downx = 0;
    float downy = 0;
    float tdownx = 0;
    float tdowny = 0;
    float upx = 0;
    float upy = 0;
    long lastTouchDown = 0;
    int CLICK_ACTION_THRESHHOLD = 100;
    Display display;
    Point size;
    int screen_width, screen_height;
    FloatingActionButton btn_ok;
    ArrayList<CropModel> cropModelArrayList;
    float smallx, smally, largex, largey;
    Paint cpaint;
    Bitmap temporary_bitmap;
    private ProgressDialog pDialog;


    private final int requestCode = 20;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);


        init();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }


    }

    private void init() {

        //Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(photoCaptureIntent, requestCode);

        pDialog = new ProgressDialog(CropActivity.this);
        im_crop_image_view = (ImageView) findViewById(R.id.im_crop_image_view);
        cropModelArrayList = new ArrayList<>();
        btn_ok = (FloatingActionButton) findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);


        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;

        initcanvas();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(this.requestCode == requestCode && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap)data.getExtras().get("data");
            im_crop_image_view.setImageBitmap(bitmap);
            initcanvas();
        }
    }


    public void initcanvas() {

        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {
            String Str_OptionType = extras.getString("OptionType");
            String Str_ImageUrl = extras.getString("ImageUrl");

                Bitmap myBitmap = BitmapFactory.decodeFile(Str_ImageUrl);
                im_crop_image_view.setImageBitmap(myBitmap);
                bmp = myBitmap;

        }else
        {
            Drawable d = getResources().getDrawable(R.drawable.bolt);
            bmp = ((BitmapDrawable)d).getBitmap();

        }


        alteredBitmap = Bitmap.createBitmap(screen_width, screen_height, bmp.getConfig());
        canvas = new Canvas(alteredBitmap);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{15.0f, 15.0f}, 0));

        int cx = (screen_width - bmp.getWidth()) >> 1;
        int cy = (screen_height - bmp.getHeight()) >> 1;
        canvas.drawBitmap(bmp, cx, cy, null);
        im_crop_image_view.setImageBitmap(alteredBitmap);
        im_crop_image_view.setOnTouchListener(this);

    }


    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:


                downx = event.getX();
                downy = event.getY();
                clipPath = new Path();
                clipPath.moveTo(downx, downy);
                tdownx = downx;
                tdowny = downy;
                smallx = downx;
                smally = downy;
                largex = downx;
                largey = downy;
                lastTouchDown = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                upx = event.getX();
                upy = event.getY();
                cropModelArrayList.add(new CropModel(upx, upy));
                clipPath = new Path();
                clipPath.moveTo(tdownx,tdowny);
                for(int i = 0; i<cropModelArrayList.size();i++){
                    clipPath.lineTo(cropModelArrayList.get(i).getY(),cropModelArrayList.get(i).getX());
                }
                canvas.drawPath(clipPath, paint);
                im_crop_image_view.invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD) {

                    cropModelArrayList.clear();
                    initcanvas();

                    int cx = (screen_width - bmp.getWidth()) >> 1;
                    int cy = (screen_height - bmp.getHeight()) >> 1;
                    canvas.drawBitmap(bmp, cx, cy, null);
                    im_crop_image_view.setImageBitmap(alteredBitmap);

                } else {
                    if (upx != upy) {
                        upx = event.getX();
                        upy = event.getY();


                        canvas.drawLine(downx, downy, upx, upy, paint);
                        clipPath.lineTo(upx, upy);
                        im_crop_image_view.invalidate();

                        crop();
                    }

                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    public void crop() {

        clipPath.close();
        clipPath.setFillType(Path.FillType.INVERSE_WINDING);

        for(int i = 0; i<cropModelArrayList.size();i++){
            if(cropModelArrayList.get(i).getY()<smallx){

                smallx=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()<smally){

                smally=cropModelArrayList.get(i).getX();
            }
            if(cropModelArrayList.get(i).getY()>largex){

                largex=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()>largey){

                largey=cropModelArrayList.get(i).getX();
            }
        }

        temporary_bitmap = alteredBitmap;
        cpaint = new Paint();
        cpaint.setAntiAlias(true);
        cpaint.setColor(getResources().getColor(R.color.colorAccent));
        cpaint.setAlpha(100);
        canvas.drawPath(clipPath, cpaint);

        canvas.drawBitmap(temporary_bitmap, 0, 0, cpaint);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                save();

            default:
                break;
        }

    }


    private void save() {

        if(clipPath != null) {
            final int color = 0xff424242;
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPath(clipPath, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(alteredBitmap, 0, 0, paint);

            float w = largex - smallx;
            float h = largey - smally;
            alteredBitmap = Bitmap.createBitmap(alteredBitmap, (int) smallx, (int) smally, (int) w, (int) h);

        }else{
            alteredBitmap = bmp;
        }
        pDialog.show();

        Thread mThread = new Thread() {
            @Override
            public void run() {

                Bitmap bitmap = alteredBitmap;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
                byte[] byteArray = stream.toByteArray();
                pDialog.dismiss();

                Intent intent = new Intent(CropActivity.this, DisplayCropActivity.class);
                intent.putExtra("image",byteArray);
                startActivity(intent);
            }
        };
        mThread.start();

    }


    @Override
    public void onBackPressed() {

        Constants.globalStartIntent(CropActivity.this, RenderFile.class, null,1);


    }
}//END
