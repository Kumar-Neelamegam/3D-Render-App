#include <jni.h>
#include <string>
#include <opencv2/core.hpp> // Open cv header
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include <opencv2/features2d.hpp>
//####################################################################################################
extern "C" JNIEXPORT jstring JNICALL
Java_app_asoss_demo1_core_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}


//####################################################################################################
/**
 * Sample method to check opencv
 */

extern "C" JNIEXPORT jstring JNICALL
Java_app_asoss_demo1_core_MainActivity_validate(JNIEnv *env, jobject, jlong addrGray, jlong addrRgba) {
    cv::Rect();
    cv::Mat();
    std::string hello1 = "\nMethod called from cpp class: Validate";
    return env->NewStringUTF(hello1.c_str());
}



//####################################################################################################


using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL
Java_app_asoss_demo1_core_MainActivity_FindFeatures(JNIEnv *, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL
Java_app_asoss_demo1_core_MainActivity_FindFeatures(JNIEnv *, jobject, jlong addrGray, jlong addrRgba) {

    cv::Mat &mGr = *(Mat *) addrGray;
    cv::Mat &mRgb = *(Mat *) addrRgba;
    vector<KeyPoint> v;

    Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);
    detector->detect(mGr, v);
    for (unsigned int i = 0; i < v.size(); i++) {
        const KeyPoint &kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255));
    }


}
}

//####################################################################################################
