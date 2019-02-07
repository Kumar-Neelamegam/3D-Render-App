package com.asoss.a3drender.app.RenderUtils;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import com.asoss.a3drender.app.Adapters.RecyclerViewHorizontalListAdapter;
import com.asoss.a3drender.app.CoreModules.Constants;
import com.asoss.a3drender.app.GlobalObjects.DataObjects;
import com.asoss.a3drender.app.GlobalObjects.ItemClickListener;
import com.asoss.a3drender.app.R;
import org.andresoviedo.util.android.ContentUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity represents the container for our 3D viewer.
 *
 * @author andresoviedo
 */
public class ModelActivity extends AppCompatActivity {


    /*******************************************************************************************************************
     */
    private static final int REQUEST_CODE_LOAD_TEXTURE = 1000;

    /**
     * Type of model if file name has no extension (provided though content provider)
     */
    private int paramType;
    /**
     * The file to load. Passed as input parameter
     */
    private Uri paramUri;
    /**
     * Enter into Android Immersive mode so the renderer is full screen or not
     */
    private boolean immersiveMode = true;
    /**
     * Background GL clear color. Default is light gray
     */
    private float[] backgroundColor = new float[]{0.2f, 0.2f, 0.2f, 1.0f};

    // private ModelSurfaceView gLView;

    private SceneLoader scene;

    private Handler handler;

    ModelSurfaceView gLView;

    boolean IsMatchFinder = false;

    /*******************************************************************************************************************
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get input parameters
        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getString("uri") != null) {
                this.paramUri = Uri.parse(b.getString("uri"));
            }
            this.paramType = b.getString("type") != null ? Integer.parseInt(b.getString("type")) : -1;
            this.immersiveMode = "true".equalsIgnoreCase(b.getString("immersiveMode"));
            this.IsMatchFinder = b.getBoolean("verify");
            try {
                String[] backgroundColors = b.getString("backgroundColor").split(" ");
                backgroundColor[0] = Float.parseFloat(backgroundColors[0]);
                backgroundColor[1] = Float.parseFloat(backgroundColors[1]);
                backgroundColor[2] = Float.parseFloat(backgroundColors[2]);
                backgroundColor[3] = Float.parseFloat(backgroundColors[3]);
            } catch (Exception ex) {
                // Assuming default background color
            }
        }
        Log.i("Renderer", "Params: uri '" + paramUri + "'");

        handler = new Handler(getMainLooper());

        // Create our 3D sceneario
        if (paramUri == null) {
            //  scene = new ExampleSceneLoader(this);// To load default spl
        } else {
            scene = new SceneLoader(this);
        }
        scene.init();


        gLView = new ModelSurfaceView(this);
        setContentView(gLView);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        //gLView = new ModelSurfaceView(this);

        //glsContainer= (ModelSurfaceView) this.findViewById(R.id.glSurface);
        //glsContainer=new ModelSurfaceView(this);
        //glsContainer.invalidate();

        if (IsMatchFinder == true) {
            LoadRecyclerList();
        }


        // Show the Up button in the action bar.
        setupActionBar();


        // TODO: Alert user when there is no multitouch support (2 fingers). He won't be able to rotate or zoom
        ContentUtils.printTouchCapabilities(getPackageManager());

        setupOnSystemVisibilityChangeListener();
    }

    /*******************************************************************************************************************
     */
    /**
     * Loading recyclerview  from the json response
     */
    RecyclerView stlFileListView;

    private void LoadRecyclerList() {

        stlFileListView = new RecyclerView(this);
        LoadReponseListUI(Constants.dataObjectsItems);

    }

    /**
     * Binding all the data from the response
     * Loading the stl files in view
     */
    private void LoadReponseListUI(ArrayList<DataObjects> dataObjects) {

        try {

            if (dataObjects != null && dataObjects.size() > 0) {
                // add a divider after each item for more clarity
                stlFileListView.addItemDecoration(new DividerItemDecoration(ModelActivity.this, LinearLayoutManager.HORIZONTAL));
                RecyclerViewHorizontalListAdapter stlviewadpter = new RecyclerViewHorizontalListAdapter(dataObjects, ModelActivity.this);
                LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(ModelActivity.this, LinearLayoutManager.HORIZONTAL, false);
                stlFileListView.setLayoutManager(horizontalLayoutManager);
                stlFileListView.setAdapter(stlviewadpter);
                stlFileListView.setItemAnimator(new DefaultItemAnimator());
                stlviewadpter.notifyDataSetChanged();
                stlviewadpter.setClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, List<DataObjects> horizontalStlList) {

                        // Toast.makeText(ModelActivity.this,  "Clicked : %d "+String.valueOf(position) , Toast.LENGTH_SHORT).show();
                        // Toast.makeText(ModelActivity.this,  "Clicked : %d "+  horizontalStlList.get(position).getFileLocation() , Toast.LENGTH_SHORT).show();

                        LoadStlFile(horizontalStlList.get(position).getFileLocation());

                    }
                });

                addContentView(stlFileListView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));



            } else {
                Constants.SnackBar(ModelActivity.this, "Data not available..", gLView, 2);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Constants.SnackBar(ModelActivity.this, "Network Error..", gLView, 2);
        }

    }

    private void LoadStlFile(String FileName) {

        this.paramUri = Uri.fromFile(new File(FileName));
        scene = new SceneLoader(this);
        scene.init();

    }


    /*******************************************************************************************************************
     */
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.model, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupOnSystemVisibilityChangeListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        //@commented for testing @kumar
       /* getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // The system bars are visible. Make any desired
                if (immersiveMode) hideSystemUIDelayed(5000);
            }
        });*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
       /* if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (immersiveMode) hideSystemUIDelayed(5000);
            }
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.model_toggle_wireframe:
                scene.toggleWireframe();
                break;
            case R.id.model_toggle_boundingbox:
                scene.toggleBoundingBox();
                break;
            case R.id.model_toggle_textures:
                scene.toggleTextures();
                break;
            case R.id.model_toggle_animation:
                scene.toggleAnimation();
                break;
            case R.id.model_toggle_collision:
                scene.toggleCollision();
                break;
            case R.id.model_toggle_lights:
                scene.toggleLighting();
                break;
            case R.id.model_load_texture:
                Intent target = ContentUtils.createGetContentIntent("image/*");
                Intent intent = Intent.createChooser(target, "Select a file");
                try {
                    startActivityForResult(intent, REQUEST_CODE_LOAD_TEXTURE);
                } catch (ActivityNotFoundException e) {
                    // The reason for the existence of aFileChooser
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSystemUIDelayed(long millis) {
        handler.postDelayed(this::hideSystemUI, millis);
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hideSystemUIKitKat();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            hideSystemUIJellyBean();
        }
    }

    // This snippet hides the system bars.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUIKitKat() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void hideSystemUIJellyBean() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public Uri getParamUri() {
        return paramUri;
    }

    public int getParamType() {
        return paramType;
    }

    public float[] getBackgroundColor() {
        return backgroundColor;
    }

    public SceneLoader getScene() {
        return scene;
    }

    public ModelSurfaceView getGLView() {

        return gLView;
    }

    /*******************************************************************************************************************
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_LOAD_TEXTURE:
                // The URI of the selected file
                final Uri uri = data.getData();
                if (uri != null) {
                    Log.i("ModelActivity", "Loading texture '" + uri + "'");
                    try {
                        ContentUtils.setThreadActivity(this);
                        scene.loadTexture(null, uri);
                    } catch (IOException ex) {
                        Log.e("ModelActivity", "Error loading texture: " + ex.getMessage(), ex);
                        Toast.makeText(this, "Error loading texture '" + uri + "'. " + ex
                                .getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        ContentUtils.setThreadActivity(null);
                    }
                }
        }
    }

    /*******************************************************************************************************************
     */
}//END
