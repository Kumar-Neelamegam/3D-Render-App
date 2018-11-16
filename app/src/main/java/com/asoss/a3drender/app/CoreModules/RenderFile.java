package com.asoss.a3drender.app.CoreModules;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.RenderUtils.ModelActivity;
import com.imagepicker.LifeCycleCallBackManager;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import org.andresoviedo.util.android.AndroidUtils;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.android.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RenderFile extends AppCompatActivity {


    //Declaration
    //***************************************************************************************

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private static final String SUPPORTED_FILE_TYPES_REGEX = "(?i).*\\.(obj|stl|dae)";


    private enum Action {
        LOAD_MODEL, GITHUB, SETTINGS, HELP, ABOUT, EXIT, UNKNOWN, DEMO
    }


    /**
     * Load file user data
     */
    private Map<String, Object> loadModelParameters = new HashMap<>();

    @BindView(R.id.bottom_navi)
    SpaceNavigationView spaceNavigationView;
    private LifeCycleCallBackManager lifeCycleCallBackManager;


    //***************************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);

        try {
            GET_INITIALIZE(savedInstanceState);
            CONTROLLISTENERS();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }



    //***************************************************************************************

    private void GET_INITIALIZE(Bundle savedInstanceState) {

        ButterKnife.bind(this);

        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_action_home));
        spaceNavigationView.addSpaceItem(new SpaceItem("LOAD", R.drawable.ic_action_load));
        spaceNavigationView.shouldShowFullBadgeText(true);
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);

    }

    //***************************************************************************************

    private void CONTROLLISTENERS() {

        // LoadFromSDCard();

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
               // Toast.makeText(RenderFile.this,"onCentreButtonClick", Toast.LENGTH_SHORT).show();
                OpenCamera();
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
               // Toast.makeText(RenderFile.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
                if(itemIndex==1)//Load model from sd card
                {
                    LoadFromSDCard();
                }
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
               // Toast.makeText(RenderFile.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
                if(itemIndex==1)//Load model from sd card
                {
                    LoadFromSDCard();
                }
            }
        });


    }


    private void OpenCamera() {



    }


    //***************************************************************************************

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

    //***************************************************************************************

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

    //***************************************************************************************

}//END
