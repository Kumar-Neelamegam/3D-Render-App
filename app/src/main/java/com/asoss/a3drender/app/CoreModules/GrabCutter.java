package com.asoss.a3drender.app.CoreModules;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.asoss.a3drender.app.GlobalObjects.Point;
import com.asoss.a3drender.app.ImageProcessing.PreviewActivity;
import com.asoss.a3drender.app.R;
import com.imagepicker.pdfpicker.Constant;
import org.opencv.core.Mat;


import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class GrabCutter {


    //**********************************************************************************************

    String TAG = "GrabCutter";

    // Used to load the 'native-lib' library on application startup.
    static {

        System.loadLibrary("grabcut");
        System.loadLibrary("opencv_java3");
    }

    public native Point[] runGrabCutFirstTime(long a);

    public native Point[] runGrabCutSecondTime(Point[] a);

    public native Point[] runGrabCutForCorrection(Point[] correctionContours, Point startPoint, Point endPoint);


    //**********************************************************************************************
    /**
     * This method used as below
     * Find the contours from the source image
     * Draw the contours
     * Muthukumar N
     * kumar.asoss18@gmail.com
     * References: C++ files (Asoss GmbH)
     */
    Mat temp;
    Point[] FirstTimeResult;
    Point[] SecondTimeResult;
    Point[] correctionResult;

    Context currentCtx;
    Bitmap SourceImage;
    ImageView OutputImgView;

    public GrabCutter(Context context, Bitmap sourceImg, ImageView OutputView) {

        this.currentCtx = context;
        this.SourceImage = sourceImg;
        this.OutputImgView = OutputView;
    }

    public void Main_GrubCut() {


        try {

            //Passing bitmap image as mat to JNI grubcutter
            temp = new Mat();
            //src_bmp = BitmapFactory.decodeResource(currentCtx.getResources(), R.drawable.test3);
            Utils.bitmapToMat(SourceImage, temp);


            runGrabCutFirstTimeJava();//step 1

            runGrabCutSecondTimeJava();//step 2

            //runGrabCutForCorrectionJava();//step 3

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void runGrabCutFirstTimeJava() {
        //1. runGrabCutFirstTime -----------------------------------------------------
        FirstTimeResult = runGrabCutFirstTime(temp.getNativeObjAddr());
        //DrawContours(temp, FirstTimeResult);
    }


    private void runGrabCutSecondTimeJava() {
        //2. runGrabCutSecondTime ----------------------------------------------------
        SecondTimeResult = runGrabCutSecondTime(FirstTimeResult);
        DrawContours(temp, SecondTimeResult);
    }


    private void runGrabCutForCorrectionJava() {
        //3. runGrabCutForCorrection -------------------------------------------------
        Point startPoint = new Point();
        startPoint.x = -0.8f;
        startPoint.y = -0.9f;
        Point stopPoint = new Point();
        stopPoint.y = 0.15f;
        stopPoint.x = 0.16f;

        //correctionResult = runGrabCutForCorrection(SecondTimeResult, startPoint, stopPoint);
        //DrawContours(temp, correctionResult);
    }

    //**********************************************************************************************

    /**
     * This method is to draw the contours over the images
     *
     * @param temp - source mat
     */

    float mDips = 1;
    float mMul = 1;

    public void DrawContours(Mat temp, Point[] Result) {


        for (int i = 0; i < Result.length; i++) {

            //Result[i].x = (float) ((Result[i].x + 0.66) * 321.0 / 1.225);//static
            //Result[i].y = (float) ((Result[i].y + 0.5) * 262);//static


            float calculate = (float) SourceImage.getWidth() / (float) SourceImage.getHeight();
            Log.e("Width: ", String.valueOf(SourceImage.getWidth()));
            Log.e("Height: ", String.valueOf(SourceImage.getHeight()));
            Log.e("Avg: ", String.valueOf(calculate));

            double convert_pixel = (double) calculate * 0.5;

            Result[i].x = (float) ((Result[i].x + convert_pixel) * SourceImage.getWidth() / calculate); // == X - Pixel
            Result[i].y = (float) ((Result[i].y + 0.5) * SourceImage.getHeight()); // == Y - Pixel
        }

        List<org.opencv.core.Point> srcPts = new ArrayList<>();
        for (Point point : Result) {
            srcPts.add(new org.opencv.core.Point(point.getX(), point.getY()));
        }


        MatOfPoint sourceMat = new MatOfPoint();
        sourceMat.fromList(srcPts);
        sourceMat.toList();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contours.add(sourceMat);


        Mat alpha = new Mat(temp.size(), CvType.CV_8UC1, new Scalar(0));

        // draw contours:
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

            //Imgproc.drawContours(temp, contours, contourIdx, new Scalar(255),Core.FILLED, 8, new Mat(), Integer.MAX_VALUE,new org.opencv.core.Point());
            Imgproc.drawContours(temp, contours, contourIdx, new Scalar(255), 2);

        }

        // create a blank temp bitmap:
        Bitmap output_bmp = Bitmap.createBitmap(SourceImage.getWidth(), SourceImage.getHeight(), SourceImage.getConfig());
        Utils.matToBitmap(temp, output_bmp);

        // sampleimgvw.setImageBitmap(output_bmp);
        removeBackground(temp);


        //removeBackground(temp);


    }


    public void removeBackground(final Mat img) {


        /*final ProgressDialog progressDialog = new ProgressDialog(currentCtx);
        progressDialog.setTitle("Information");
        progressDialog.setMessage("Bitte warten.. Image processing.. ");
        progressDialog.setCancelable(false);
        progressDialog.show();*/

        ShowDialog();


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //GrabCut part
                // Mat img = new Mat();
                //Utils.bitmapToMat(bitmap, img);

                int r = img.rows();
                int c = img.cols();
                org.opencv.core.Point p1 = new org.opencv.core.Point(c/150 , r /150);
                org.opencv.core.Point p2 = new org.opencv.core.Point(c - c / 10, r - r / 10);

                Rect rect = new Rect(p1, p2);
                Mat mask = new Mat();
                Mat fgdModel = new Mat();
                Mat bgdModel = new Mat();
                Mat imgC3 = new Mat();

                Imgproc.cvtColor(img, imgC3, Imgproc.COLOR_RGBA2RGB);

                Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 2, Imgproc.GC_INIT_WITH_RECT);

                Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
                Core.compare(mask, source/* GC_PR_FGD */, mask, Core.CMP_EQ);


                Mat foreground = new Mat(img.size(), CvType.CV_8UC1, new Scalar(255, 255, 255));
                img.copyTo(foreground, mask);

                // convert matrix to output bitmap
                SourceImage = Bitmap.createBitmap((int) foreground.size().width, (int) foreground.size().height, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(foreground, SourceImage);

                handler.sendMessage(new Message());
            }

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    dialog.dismiss();

                    OutputImgView.setImageBitmap(SourceImage);//remove background

                    //Bitmap transparentBg = transparentBG(temp);

                    //OutputImgView.setImageBitmap(transparentBg);//transparent background

                    Bitmap bm=((BitmapDrawable)OutputImgView.getDrawable()).getBitmap();

                    Constants.saveImage(bm);
                }
            };

        });

        //start thread
        thread.start();

    }


    public Bitmap transparentBG(Mat src) {

        Mat dst = new Mat(src.size(), CvType.CV_8UC4);  //(src.rows,src.cols,CV_8UC4);
        Mat tmp = new Mat();
        Mat thr = new Mat();

        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(tmp, thr, 100, 255, Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 hierarchy = new MatOfInt4();


        int largest_contour_index = 0;
        int largest_area = 0;

        Mat alpha = new Mat(src.size(), CvType.CV_8UC1, new Scalar(0));

        Imgproc.findContours(tmp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); // Find the contours in the image
        for (int i = 0; i < contours.size(); i++) // iterate through each contour.
        {
            double a = Imgproc.contourArea(contours.get(i), false);  //  Find the area of contour
            if (a > largest_area) {
                largest_area = (int) a;
                largest_contour_index = i;                //Store the index of largest contour
            }
        }

        Imgproc.drawContours(alpha, contours, largest_contour_index, new Scalar(255), Core.FILLED, 8, hierarchy, Integer.MAX_VALUE, new org.opencv.core.Point());


        List<Mat> rgb = new ArrayList<Mat>(3);
        List<Mat> rgba = new ArrayList<Mat>(4);

        Core.split(src, rgb);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);

        Core.merge(rgba, dst);

        Bitmap output = Bitmap.createBitmap(SourceImage.getWidth(), SourceImage.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, output);
        return output;

    }


    BottomSheetDialog dialog;
    private void ShowDialog() {

        LayoutInflater inflater = (LayoutInflater) currentCtx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, null);
        dialog = new BottomSheetDialog(currentCtx);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
    }

    //**********************************************************************************************


}//end
