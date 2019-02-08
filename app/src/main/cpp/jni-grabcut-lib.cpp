#include <jni.h>
#include <string>
#include <android/log.h>
#include <iostream>
#include <opencv2/core/mat.hpp>
#include "grabcutter_p.h"

struct JNI_POSREC {
    jclass cls;
    jmethodID constructortorID;
    jfieldID xID;
    jfieldID yID;
};

//####################################################################################################
extern "C" JNIEXPORT jint JNICALL Java_com_asoss_a3drender_GrubCutter_mathAdd(
        JNIEnv *pEnv,
        jobject pThis,
        jint a,
        jint b) {
    return a + b;
}

//####################################################################################################
/**
 * RUN GRAB CUTTER FIRST TIME
 */
extern "C" JNIEXPORT jobjectArray JNICALL Java_com_asoss_a3drender_app_CoreModules_GrabCutter_runGrabCutFirstTime(JNIEnv *env, jobject, jlong input_image) {

    cv::Mat *pInputImage = (cv::Mat *) input_image;
    cv::Mat tmp;
    pInputImage->copyTo(tmp);

    std::vector<cv::Point2f> outContours;
    Vision::runGrabCutFirstTime(tmp, &outContours);//

    JNI_POSREC jniPosRec;

    jniPosRec.cls = env->FindClass("com/asoss/a3drender/app/GlobalObjects/Point");
    jniPosRec.constructortorID = env->GetMethodID(jniPosRec.cls, "<init>", "()V");
    jniPosRec.xID = env->GetFieldID(jniPosRec.cls, "x", "F");
    jniPosRec.yID = env->GetFieldID(jniPosRec.cls, "y", "F");

    jobjectArray jPosRecArray = env->NewObjectArray(outContours.size(), jniPosRec.cls, NULL);

    for (size_t i = 0; i < outContours.size(); i++) {
        jobject jPosRec = env->NewObject(jniPosRec.cls, jniPosRec.constructortorID);

        env->SetFloatField( jPosRec, jniPosRec.xID,  outContours[i].x);
        env->SetFloatField( jPosRec, jniPosRec.yID, outContours[i].y);

        env->SetObjectArrayElement(jPosRecArray, i, jPosRec);

        env->DeleteLocalRef(jPosRec);
    }

    return jPosRecArray;
}


//####################################################################################################
/*
 * RUN GRAB CUTTER SECOND TIME
 */
extern "C" JNIEXPORT jobjectArray JNICALL Java_com_asoss_a3drender_app_CoreModules_GrabCutter_runGrabCutSecondTime(JNIEnv *env, jobject, jobjectArray firstTimeContours) {
    JNI_POSREC jniPosRec;

    jniPosRec.cls = env->FindClass("com/asoss/a3drender/app/GlobalObjects/Point");
    jniPosRec.constructortorID = env->GetMethodID(jniPosRec.cls, "<init>", "()V");
    jniPosRec.xID = env->GetFieldID(jniPosRec.cls, "x", "F");
    jniPosRec.yID = env->GetFieldID(jniPosRec.cls, "y", "F");

    jint contoursSize = env->GetArrayLength(firstTimeContours);

    std::vector<cv::Point2f> firstTimeContourVector;

    for (int i=0; i<contoursSize; i++)
    {
        jobject pos = env->GetObjectArrayElement(firstTimeContours, i);

        float x = env->GetFloatField( pos, jniPosRec.xID);
        float y = env->GetFloatField( pos, jniPosRec.yID);

        firstTimeContourVector.push_back(cv::Point2f(x,y));

    }

    std::vector<cv::Point2f> outContours;
    Vision::runGrabCutSecondTime(firstTimeContourVector, &outContours);

    jobjectArray jPosRecArray = env->NewObjectArray(outContours.size(), jniPosRec.cls, NULL);

    for (size_t i = 0; i < outContours.size(); i++) {
        jobject jPosRec = env->NewObject(jniPosRec.cls, jniPosRec.constructortorID);

        env->SetFloatField( jPosRec, jniPosRec.xID,  outContours[i].x);
        env->SetFloatField( jPosRec, jniPosRec.yID, outContours[i].y);

        env->SetObjectArrayElement(jPosRecArray, i, jPosRec);

        env->DeleteLocalRef(jPosRec);
    }

    return jPosRecArray;
}

//####################################################################################################
/*
 * RUN GRAB CUTTER FOR CORRECTION
 */
extern "C" JNIEXPORT jobjectArray JNICALL Java_com_asoss_a3drender_app_CoreModules_GrabCutter_runGrabCutForCorrection(JNIEnv *env, jobject, jobjectArray correctionContours,
jobject startPoint, jobject endPoint){

    JNI_POSREC jniPosRec;

    jniPosRec.cls = env->FindClass("com/asoss/a3drender/app/GlobalObjects/Point");
    jniPosRec.constructortorID = env->GetMethodID(jniPosRec.cls, "<init>", "()V");
    jniPosRec.xID = env->GetFieldID(jniPosRec.cls, "x", "F");
    jniPosRec.yID = env->GetFieldID(jniPosRec.cls, "y", "F");

    jint contoursSize = env->GetArrayLength(correctionContours);

    std::vector<cv::Point2f> correctionContoursVector;
    cv::Point2f cvStartPoint;
    cv::Point2f cvEndPoint;

    cvStartPoint.x = env->GetFloatField( startPoint, jniPosRec.xID);
    cvStartPoint.y = env->GetFloatField( startPoint, jniPosRec.yID);

    cvEndPoint.x = env->GetFloatField( endPoint, jniPosRec.xID);
    cvEndPoint.y = env->GetFloatField( endPoint, jniPosRec.yID);

    for (int i=0; i<contoursSize; i++)
    {
        jobject pos = env->GetObjectArrayElement(correctionContours, i);

        float x = env->GetFloatField( pos, jniPosRec.xID);
        float y = env->GetFloatField( pos, jniPosRec.yID);

        correctionContoursVector.push_back(cv::Point2f(x,y));

    }

    std::vector<cv::Point2f> outContours;
    Vision::runGrabCutForCorrection(correctionContoursVector, &outContours, cvStartPoint, cvEndPoint);

    jobjectArray jPosRecArray = env->NewObjectArray(outContours.size(), jniPosRec.cls, NULL);

    for (size_t i = 0; i < outContours.size(); i++) {
        jobject jPosRec = env->NewObject(jniPosRec.cls, jniPosRec.constructortorID);

        env->SetFloatField( jPosRec, jniPosRec.xID,  outContours[i].x);
        env->SetFloatField( jPosRec, jniPosRec.yID, outContours[i].y);

        env->SetObjectArrayElement(jPosRecArray, i, jPosRec);

        env->DeleteLocalRef(jPosRec);
    }

    return jPosRecArray;
}

//####################################################################################################


extern "C" JNIEXPORT jobjectArray JNICALL Java_com_asoss_a3drender_app_CoreModules_GrabCutter_getPoints(JNIEnv * env, jclass) {

    JNI_POSREC jniPosRec;

    jniPosRec.cls = env->FindClass("com/asoss/a3drender/app/GlobalObjects/Point");
    jniPosRec.constructortorID = env->GetMethodID(jniPosRec.cls, "<init>", "()V");
    jniPosRec.xID = env->GetFieldID(jniPosRec.cls, "x", "I");
    jniPosRec.yID = env->GetFieldID(jniPosRec.cls, "y", "I");

    jobjectArray jPosRecArray = env->NewObjectArray(6, jniPosRec.cls, NULL);

    for (size_t i = 0; i < 6; i++) {
        jobject jPosRec = env->NewObject(jniPosRec.cls, jniPosRec.constructortorID);

        env->SetIntField( jPosRec, jniPosRec.xID, i);
        env->SetIntField( jPosRec, jniPosRec.yID, i+12);

        env->SetObjectArrayElement(jPosRecArray, i, jPosRec);
    }

    return jPosRecArray;
}

//####################################################################################################


















