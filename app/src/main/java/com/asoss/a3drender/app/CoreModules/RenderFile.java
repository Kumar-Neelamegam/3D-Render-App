package com.asoss.a3drender.app.CoreModules;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.RenderUtils.ModelActivity;
import org.andresoviedo.util.android.AndroidUtils;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.android.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RenderFile extends AppCompatActivity
{


    private static final String REPO_URL = "https://github.com/andresoviedo/android-3D-model-viewer/raw/master/models/index";
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private static final int REQUEST_INTERNET_ACCESS = 1001;
    private static final int REQUEST_CODE_OPEN_FILE = 1101;
    private static final int REQUEST_CODE_OPEN_MATERIAL = 1102;
    private static final int REQUEST_CODE_OPEN_TEXTURE = 1103;
    private static final String SUPPORTED_FILE_TYPES_REGEX = "(?i).*\\.(obj|stl|dae)";


    private enum Action {
        LOAD_MODEL, GITHUB, SETTINGS, HELP, ABOUT, EXIT, UNKNOWN, DEMO
    }


    /**
     * Load file user data
     */
    private Map<String, Object> loadModelParameters = new HashMap<>();



    Button Btn_LoadModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);


        try {
            GET_INITIALIZE();
            CONTROLLISTENERS();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void CONTROLLISTENERS() {

        Btn_LoadModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               LoadFromSDCard();

            }
        });
    }

    private void LoadFromSDCard() {


        // check permission starting from android API 23 - Marshmallow
        if (!AndroidUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE)) {
            return;
        }
        FileUtils.createChooserDialog(this, "Select file", null, null, SUPPORTED_FILE_TYPES_REGEX,
                (File file) -> {
                    if (file != null) {
                        ContentUtils.setCurrentDir(file.getParentFile());
                        launchModelRendererActivity(Uri.parse("file://" + file.getAbsolutePath()));
                    }
                });

    }

    private void GET_INITIALIZE() {

        Btn_LoadModel = findViewById(R.id.btn_load_model);

    }


    private void launchModelRendererActivity(Uri uri) {
        Log.i("Menu", "Launching renderer for '" + uri + "'");
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        intent.putExtra("uri", uri.toString());
        intent.putExtra("immersiveMode", "true");

        // content provider case
        if (!loadModelParameters.isEmpty()) {
            intent.putExtra("type", loadModelParameters.get("type").toString());
            loadModelParameters.clear();
        }

        startActivity(intent);
    }

}
