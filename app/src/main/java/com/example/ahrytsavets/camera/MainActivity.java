package com.example.ahrytsavets.camera;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.findContours;



@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Object currentFrame;
    private Object previousFrame;
    private Size size = new Size(3, 3);
    private int sensivity = 30;
    private double maxArea = 100;
    private Scalar redColor = new Scalar(255, 0, 0);
    private boolean isMoved = false;
    private Time currentTime = new Time();
    public final String TAG = "MainActivity:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        previousFrame = new Mat(720, 1280, CvType.CV_8UC3);
        currentFrame = new Mat(720, 1280, CvType.CV_8UC3);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mOpenCvCameraView.enableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        currentFrame = inputFrame.rgba();
        try{
            currentFrame = detectMotion((Mat)currentFrame, (Mat)previousFrame);
            Log.d(TAG, String.valueOf(isMoved));
            if(isMoved){
                currentTime.setToNow();
                savePicture((Mat)currentFrame);
                isMoved = false;
            }
            Thread.currentThread().sleep(1000);
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }
        previousFrame = inputFrame.rgba();
        return (Mat)currentFrame;
    }

    public Mat detectMotion(Mat currentInputFrame, Mat previousInputFrame) {
        Mat outputFrame = new Mat();
        Mat resultFrame = new Mat();
        currentInputFrame.copyTo(outputFrame);
        currentInputFrame.copyTo(outputFrame);
        Mat vector = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.GaussianBlur(currentInputFrame, currentInputFrame, size, 0);
        Imgproc.GaussianBlur(previousInputFrame, previousInputFrame, size, 0);
        Core.subtract(previousInputFrame, currentInputFrame, resultFrame);
        Imgproc.cvtColor(resultFrame, resultFrame, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(resultFrame, resultFrame, sensivity, 255, Imgproc.THRESH_BINARY);
        findContours(resultFrame, contours, vector, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for(int i = 0; i< contours.size(); i++){
            isMoved = true;
            Mat contour = contours.get(i);
            double minContourArea = Imgproc.contourArea(contour, false);
            if(minContourArea > maxArea) {
                Rect r = Imgproc.boundingRect(contours.get(i));
                Imgproc.rectangle(outputFrame, r.br(), r.tl(), redColor, 1);
            }
            contour.release();
        }
        return outputFrame;
    }
    public void sendEmail(Mat frame){
        savePicture(frame);
    }
    public void  savePicture(Mat frame){
        File path = new File(Environment.getExternalStorageDirectory() + "/MotionDetector");
        String filename = currentTime.format2445()+".png";
        File file = new File(path, filename);
        filename = file.toString();
        Log.d(TAG, filename);
        Imgcodecs.imwrite(filename,frame);
    }
}
