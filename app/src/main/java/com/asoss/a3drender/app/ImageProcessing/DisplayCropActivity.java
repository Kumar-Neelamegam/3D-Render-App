package com.asoss.a3drender.app.ImageProcessing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;
import com.asoss.a3drender.app.CoreModules.Constants;
import com.asoss.a3drender.app.CoreModules.RenderFile;
import com.asoss.a3drender.app.R;

public class DisplayCropActivity extends AppCompatActivity {


    ImageView im_crop;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cropimg);

        im_crop = (ImageView) findViewById(R.id.im_crop);
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        im_crop.setImageBitmap(bmp);
        Toast.makeText(this, "This cropped image will be transferred to server as binary...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {

        Constants.globalStartIntent(DisplayCropActivity.this, RenderFile.class, null,2);
        super.onBackPressed();
    }
}//END
