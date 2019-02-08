package com.asoss.a3drender.app.CoreModules;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.asoss.a3drender.app.ImageProcessing.PreviewActivity;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.RenderUtils.ModelActivity;
import com.asoss.a3drender.app.Utilities.BottomDialog;
import com.celites.androidexternalfilewriter.AppExternalFileWriter;
import com.imagepicker.FilePickUtils;
import com.imagepicker.LifeCycleCallBackManager;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader;
import org.andresoviedo.util.android.*;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.imagepicker.FilePickUtils.CAMERA_PERMISSION;
import static com.imagepicker.FilePickUtils.STORAGE_PERMISSION_IMAGE;


/**
 * AUTHOR   :   MUTHUKUMAR NEELAMEGAM
 * EMAIL    :   kumar.neelamegam17@gmail.com
 */
public class Dashboard extends AppCompatActivity {


    /**
     * Declaration
     */
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private static final String SUPPORTED_FILE_TYPES_REGEX = "(?i).*\\.(obj|stl|dae)";

    private static final String REPO_URL = "https://github.com/andresoviedo/android-3D-model-viewer/raw/master/models/index";
    private static final int REQUEST_INTERNET_ACCESS = 1001;
    private static final int REQUEST_CODE_OPEN_FILE = 1101;
    private static final int REQUEST_CODE_OPEN_MATERIAL = 1102;
    private static final int REQUEST_CODE_OPEN_TEXTURE = 1103;


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



    private static final int MAX_STEP = 4;

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    public RelativeLayout parentlayout;


    private String title_array[] = {
            "Partscout",
            "STL (STereoLithography) File Support",
            "Transformations: scaling, rotation, translation",
            "Other Features",
    };
    private String description_array[] = {
            "useful to render 3d models",
            "support stl files to render 3d models",
            "enables to manipulate 3d models",
            "wireframe, point mode, pinch and zoom",
    };
    private int about_images_array[] = {
            R.drawable.eye,
            R.drawable.ic_vector_stlfiles,
            R.drawable.ic_vector_transform,
            R.drawable.ic_vector_features
    };
    private int color_array[] = {
            R.color.colorPrimary,
            R.color.md_green_500,
            R.color.md_deep_purple_500,
            R.color.md_deep_orange_500
    };


    // Custom handler: org/andresoviedo/app/util/url/android/Handler.class
    static {
        System.setProperty("java.protocol.handler.pkgs", "org.andresoviedo.util.android");
        URL.setURLStreamHandlerFactory(new AndroidURLStreamHandlerFactory());
    }


    /**************************************************************************************************************************
     * Oncreate
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);

        try {
            GET_INITIALIZE(savedInstanceState);
            CONTROLLISTENERS();
            CLEAN_ALL_LOCALFILES();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void CLEAN_ALL_LOCALFILES() {

        try {
            //File external writer
            AppExternalFileWriter appExternalFileWriter=new AppExternalFileWriter(this);
            //deleting stl old files
            File desfolder = new File(Environment.getExternalStorageDirectory().getPath()+"/"+this.getString(R.string.app_name)+"-stl");
            appExternalFileWriter.deleteDirectory(desfolder);

            //deleting processed old images
            desfolder = new File(Environment.getExternalStorageDirectory().getPath()+"/"+this.getString(R.string.app_name)+"-img");
            appExternalFileWriter.deleteDirectory(desfolder);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }


    /**************************************************************************************************************************
     * Initializing the components
     */
    private void GET_INITIALIZE(Bundle savedInstanceState) {

        try {
            ButterKnife.bind(this);

            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

            spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
            spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_action_home));
            spaceNavigationView.addSpaceItem(new SpaceItem("OTHERS", R.drawable.ic_action_load));
            spaceNavigationView.shouldShowFullBadgeText(true);
            spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);

            viewPager = findViewById(R.id.view_pager);
            // adding bottom dots
            bottomProgressDots(0);

            myViewPagerAdapter = new MyViewPagerAdapter();
            viewPager.setAdapter(myViewPagerAdapter);
            viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
            parentlayout = findViewById(R.id.parent_layout);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void CONTROLLISTENERS() {

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                // Toast.makeText(Dashboard.this,"onCentreButtonClick", Toast.LENGTH_SHORT).show();
                //OpenCamera();
                // Constants.globalStartIntent(Dashboard.this, CropActivity.class, null);
                // showPictureDialog();

                showImagePickerDialog(onFileChoose);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {

                if (itemIndex == 1)//Load model from sd card
                {
                    loadModel();
                }
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {

                if (itemIndex == 1)//Load model from sd card
                {
                    loadModel();
                }
            }
        });


    }


    /**************************************************************************************************************************
     /**
     * DISPLAYING INTRO - APP FEATURES
     */
    LinearLayout dotsLayout;

    private void bottomProgressDots(int current_index) {
        dotsLayout = findViewById(R.id.layoutDots);
        ImageView[] dots = new ImageView[MAX_STEP];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            int width_height = 15;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
            params.setMargins(5, 5, 5, 5);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.shape_circle);
            dots[i].setColorFilter(getResources().getColor(R.color.overlay_dark_30), PorterDuff.Mode.SRC_IN);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[current_index].setImageResource(R.drawable.shape_circle);
            dots[current_index].setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_IN);
        }
        dotsLayout.setBackgroundColor(getResources().getColor(color_array[current_index]));

    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            bottomProgressDots(position);

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.item_stepper_wizard_color, container, false);
            ((TextView) view.findViewById(R.id.title)).setText(title_array[position]);
            ((TextView) view.findViewById(R.id.description)).setText(description_array[position]);
            ((ImageView) view.findViewById(R.id.image)).setImageResource(about_images_array[position]);
            view.findViewById(R.id.lyt_parent).setBackgroundColor(getResources().getColor(color_array[position]));

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return title_array.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    /**************************************************************************************************************************
     */
    private FilePickUtils filePickUtils;
    private BottomDialog bottomDialog;


    /**************************************************************************************************************************
     *
     * SELECTING IMAGES FROM CAMERA AND GALLERY
     *
     */
    int flag=1; // 1 = camera, 2 = browse

    private FilePickUtils.OnFileChoose onFileChoose = new FilePickUtils.OnFileChoose() {
        @Override
        public void onFileChoose(String s, int i, int i1) {
            bottomDialog.dismiss();

            Intent nextdraw = new Intent(Dashboard.this, PreviewActivity.class);
            nextdraw.putExtra("ImageUrl", s);
            if(flag==0)
            {
                nextdraw.putExtra("OptionType", "1");
            }else
            {
                nextdraw.putExtra("OptionType", "2");
            }

            startActivity(nextdraw);

        }

    };

    public void showImagePickerDialog(FilePickUtils.OnFileChoose onFileChoose) {
        filePickUtils = new FilePickUtils(this, onFileChoose);
        lifeCycleCallBackManager = filePickUtils.getCallBackManager();
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_options, null);
        bottomDialog = new BottomDialog(Dashboard.this);
        bottomDialog.setContentView(bottomSheetView);
        final TextView tvCamera = bottomSheetView.findViewById(R.id.tvCamera);
        final TextView tvGallery = bottomSheetView.findViewById(R.id.tvGallery);
        tvCamera.setOnClickListener(onCameraListener);
        tvGallery.setOnClickListener(onGalleryListener);
        bottomDialog.show();
    }

    private View.OnClickListener onCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            filePickUtils.requestImageCamera(CAMERA_PERMISSION, false, false);
            bottomDialog.dismiss();
            flag=1;
        }
    };

    private View.OnClickListener onGalleryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            filePickUtils.requestImageGallery(STORAGE_PERMISSION_IMAGE, false, false);
            bottomDialog.dismiss();
            flag=2;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (lifeCycleCallBackManager != null) {
            lifeCycleCallBackManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**************************************************************************************************************************
     * STL FILE PROCESSING AND ITS METHODS
     */

    private void loadModel() {

        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_options, null);
        bottomDialog = new BottomDialog(Dashboard.this);
        bottomDialog.setContentView(bottomSheetView);
        final TextView tvOption1 = bottomSheetView.findViewById(R.id.tvEmbeddedModels);
        final TextView tvOption2 = bottomSheetView.findViewById(R.id.tvRepository);
        final TextView tvOption3 = bottomSheetView.findViewById(R.id.tvExternalStorage);
        final TextView tvOption4 = bottomSheetView.findViewById(R.id.tvContentProvider);


        tvOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomDialog.dismiss();
                loadModelFromAssets();

            }
        });

        tvOption3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomDialog.dismiss();
                loadModelFromSdCard();

            }
        });


        //  tvOption2.setOnClickListener(v -> loadModelFromRepository());


        // tvOption4.setOnClickListener(v -> loadModelFromContentProvider());

        bottomDialog.show();


    }

    private void loadModelFromAssets() {
        AssetUtils.createChooserDialog(this, "Select file", null, "models", "(?i).*\\.(obj|stl|dae)",
                (String file) -> {
                    if (file != null) {
                        ContentUtils.provideAssets(this);
                        launchModelRendererActivity(Uri.parse("assets://" + getPackageName() + "/" + file));
                    }
                });
    }

    private void loadModelFromRepository() {
        if (!AndroidUtils.checkPermission(this, Manifest.permission.INTERNET, REQUEST_INTERNET_ACCESS)) {
            return;
        }
        new LoadRepoIndexTask().execute();
    }

    class LoadRepoIndexTask extends AsyncTask<Void, Integer, List<String>> {

        private final ProgressDialog dialog;

        public LoadRepoIndexTask() {
            this.dialog = new ProgressDialog(Dashboard.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            return ContentUtils.getIndex(REPO_URL);
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (strings == null) {
                Toast.makeText(Dashboard.this, "Couldn't load repo index", Toast.LENGTH_LONG).show();
                return;
            }
            ContentUtils.createChooserDialog(Dashboard.this, "Select file", null,
                    strings, SUPPORTED_FILE_TYPES_REGEX,
                    (String file) -> {
                        if (file != null) {
                            launchModelRendererActivity(Uri.parse(file));
                        }
                    });
        }
    }

    private void loadModelFromSdCard() {
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

    private void loadModelFromContentProvider() {
        loadModelParameters.clear();
        ContentUtils.clearDocumentsProvided();
        askForFile(REQUEST_CODE_OPEN_FILE, "*/*");
    }

    private void askForFile(int requestCode, String mimeType) {
        Intent target = ContentUtils.createGetContentIntent(mimeType);
        Intent intent = Intent.createChooser(target, "Select file");
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Error. Please install a file content provider", Toast.LENGTH_LONG).show();
        }
    }

    private Uri getUserSelectedModel() {
        return (Uri) loadModelParameters.get("model");
    }

    private void askForRelatedFiles(int modelType) {
        loadModelParameters.put("type", modelType);
        switch (modelType) {
            case 0: // obj
                // check if model references material file
                String materialFile = WavefrontLoader.getMaterialLib(getUserSelectedModel());
                if (materialFile == null) {
                    launchModelRendererActivity(getUserSelectedModel());
                    break;
                }
                ContentUtils.showDialog(this, "Select material file", "This model references a " +
                                "material file (" + materialFile + "). Please select it", "OK",
                        "Cancel", (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_NEGATIVE:
                                    launchModelRendererActivity(getUserSelectedModel());
                                    break;
                                case DialogInterface.BUTTON_POSITIVE:
                                    loadModelParameters.put("file", materialFile);
                                    askForFile(REQUEST_CODE_OPEN_MATERIAL, "*/*");
                            }
                        });
                break;
            case 1: // stl
                launchModelRendererActivity(getUserSelectedModel());
                break;
            case 2: // dae
                // TODO: pre-process file to ask for referenced textures
                // XmlParser.parse(colladaFile)
                launchModelRendererActivity(getUserSelectedModel());
                break;
        }
    }


    /**************************************************************************************************************************
     * Sending image to server
     */

    BottomSheetDialog dialog;


    /**
     * activity results
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (lifeCycleCallBackManager != null) {
            lifeCycleCallBackManager.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }


        //Load stl, obj files
        ContentUtils.setThreadActivity(this);
        switch (requestCode) {


            case REQUEST_READ_EXTERNAL_STORAGE:
                loadModelFromSdCard();
                break;


            case REQUEST_INTERNET_ACCESS:
                loadModelFromRepository();
                break;


            case REQUEST_CODE_OPEN_FILE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                final Uri uri = data.getData();
                if (uri == null) {
                    return;
                }

                // save user selected model
                loadModelParameters.put("model", uri);

                // detect model type
                if (uri.toString().toLowerCase().endsWith(".obj")) {
                    askForRelatedFiles(0);
                } else if (uri.toString().toLowerCase().endsWith(".stl")) {
                    askForRelatedFiles(1);
                } else if (uri.toString().toLowerCase().endsWith(".dae")) {
                    askForRelatedFiles(2);
                } else {
                    // no model type from filename, ask user...
                    ContentUtils.showListDialog(this, "Select type", new String[]{"Wavefront (*.obj)", "Stereolithography (*" +
                            ".stl)", "Collada (*.dae)"}, (dialog, which) -> askForRelatedFiles(which));
                }
                break;


            case REQUEST_CODE_OPEN_MATERIAL:
                if (resultCode != RESULT_OK || data.getData() == null) {
                    launchModelRendererActivity(getUserSelectedModel());
                    break;
                }
                String filename = (String) loadModelParameters.get("file");
                ContentUtils.addUri(filename, data.getData());
                // check if material references texture file
                String textureFile = WavefrontLoader.getTextureFile(data.getData());
                if (textureFile == null) {
                    launchModelRendererActivity(getUserSelectedModel());
                    break;
                }
                ContentUtils.showDialog(this, "Select texture file", "This model references a " +
                                "texture file (" + textureFile + "). Please select it", "OK",
                        "Cancel", (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_NEGATIVE:
                                    launchModelRendererActivity(getUserSelectedModel());
                                    break;
                                case DialogInterface.BUTTON_POSITIVE:
                                    loadModelParameters.put("file", textureFile);
                                    askForFile(REQUEST_CODE_OPEN_TEXTURE, "image/*");
                            }
                        });
                break;


            case REQUEST_CODE_OPEN_TEXTURE:
                if (resultCode != RESULT_OK || data.getData() == null) {
                    launchModelRendererActivity(getUserSelectedModel());
                    break;
                }
                String textureFilename = (String) loadModelParameters.get("file");
                ContentUtils.addUri(textureFilename, data.getData());
                launchModelRendererActivity(getUserSelectedModel());

        }

    }



/*

    public String saveImage(Bitmap myBitmap) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            //myBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            Bitmap OutImage = Bitmap.createScaledBitmap(myBitmap, 1000, 1000, true);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), OutImage, "Title", null);

            File wallpaperDirectory = new File(Constants.DATABASE_FILE_PATH + File.separator + "Data" + File.separator);
            // have the object build the directory structure, if needed.
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            try {
                File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".png");
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

                return f.getAbsolutePath();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
*/

    /**************************************************************************************************************************
     */
/*
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
*/

    /**************************************************************************************************************************
     */
    private void launchModelRendererActivity(Uri uri) {

        Log.i("Menu", "Launching renderer for '" + uri + "'");
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        intent.putExtra("uri", uri.toString());
        intent.putExtra("immersiveMode", "true");
        intent.putExtra("verify", false);

        // content provider case
        if (!loadModelParameters.isEmpty()) {
            intent.putExtra("type", loadModelParameters.get("type").toString());
            loadModelParameters.clear();
        }

        startActivity(intent);
    }


    /**************************************************************************************************************************
     */
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Constants.ExitSweetDialog(Dashboard.this, Dashboard.class);
    }


    /**************************************************************************************************************************
     */
}//END
