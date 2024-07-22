#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <cstring>
#include <filters.h>

#include "net.h"
#include "detect.h"
#include "recognize.h"
#include "main.h"

using namespace cv;
using namespace CrystalBall;
#define TAG "CRYSTALBALL"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)


bool detection_sdk_init_ok = false;

extern "C" {

ImageFilters* filters = ImageFilters::getInstance();

jobject createBitmap(JNIEnv *env, int wid, int hei, int channel_count=4) {
    jclass bitmapCls = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapFunction = env->GetStaticMethodID(bitmapCls, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName;
    if(channel_count == 4)
        configName= env->NewStringUTF("ARGB_8888");
    else
        configName = env->NewStringUTF("ALPHA_8");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOfBitmapConfigFunction = env->GetStaticMethodID(bitmapConfigClass, "valueOf", "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass, valueOfBitmapConfigFunction, configName);
    jobject newBitmap = env->CallStaticObjectMethod(bitmapCls, createBitmapFunction, wid, hei, bitmapConfig);
    return newBitmap;
}

JNIEXPORT jboolean JNICALL
Java_org_CrystalBall_Engine_EngineInit(JNIEnv *env, jobject instance,
                                       jstring faceDetectionModelPath_, jstring lmdbPath_, jobject sketchTexture) {
    if (detection_sdk_init_ok) {
        return true;
    }
    jboolean tRet = false;
    if (NULL == faceDetectionModelPath_) {
        return tRet;
    }

    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        return tRet;
    }

    const char *lmdbPath = env->GetStringUTFChars(lmdbPath_, 0);

    std::string tFaceModelDir = faceDetectionModelPath;
    std::string tLastChar = tFaceModelDir.substr(tFaceModelDir.length() - 1, 1);
    if ("\\" == tLastChar) {
        tFaceModelDir = tFaceModelDir.substr(0, tFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tFaceModelDir += "/";
    }
    LOGD("init, tFaceModelDir=%s", tFaceModelDir.c_str());

    std::string tLMDir = lmdbPath;
    tLastChar = tLMDir.substr(tLMDir.length() - 1,1);
    if ("\\" == tLastChar) {
        tLMDir = tLMDir.substr(0, tLMDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tLMDir += "/";
    }
    int ret = Landmark_Init(tLMDir.c_str());
    LOGD("init, LMDIR=%s", tLMDir.c_str());
    if(ret != 0) return false;
    gDetect = new Detect(tFaceModelDir);
    if(!gDetect->bInitSucced) return false;
    gDetect->SetThreadNum(2);

    gMagicEngine = new MagicEngine(tFaceModelDir);
    if(!gMagicEngine->bInitSucced) return false;
    env->ReleaseStringUTFChars(faceDetectionModelPath_, faceDetectionModelPath);
    detection_sdk_init_ok = true;

    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;
    if ((ret = AndroidBitmap_getInfo(env, sketchTexture, &infoIn)) < 0 ) {
        return NULL;
    }
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }
    if ((ret = AndroidBitmap_lockPixels(env, sketchTexture, &pixelsIn)) < 0) {
        return NULL;
    }
    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    Mat texture(image.size(), CV_8UC1);
    cv::cvtColor(image, texture, COLOR_RGBA2GRAY);
    filters->setSketchTexture(texture);

    tRet = true;
    return tRet;
}


JNIEXPORT jintArray JNICALL
Java_org_CrystalBall_Engine_FaceDetect(JNIEnv *env, jobject instance,
                                             jobject bitmapIn) {
    if (!detection_sdk_init_ok) {
        return NULL;
    }

    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    if (imageWidth < 20 || imageHeight < 20) {
        return NULL;
    }

    int32_t minFaceSize = 80;
    gDetect->SetMinFace(minFaceSize);

    DetectFace((unsigned char*)pixelsIn, imageWidth, imageHeight, 4, g_faceInfos);
    if(g_faceInfos.size() < 1) return NULL;
    int num_face = g_faceInfos.size();

    int out_size = 1 + num_face * 14;
    int *faceInfo = new int[out_size];
    faceInfo[0] = num_face;
    for (int i = 0; i < num_face; i++) {
        faceInfo[14 * i + 1] = g_faceInfos[i].xmin;// finalBbox[i].x1;//left
        faceInfo[14 * i + 2] = g_faceInfos[i].ymin; //finalBbox[i].y1;//top
        faceInfo[14 * i + 3] = g_faceInfos[i].xmax;//finalBbox[i].x2;//right
        faceInfo[14 * i + 4] = g_faceInfos[i].ymax;//finalBbox[i].y2;//bottom
//        for (int j = 0; j < 10; j++) {
//            faceInfo[14 * i + 5 + j] = static_cast<int>(finalBbox[i].ppoint[j]);
//        }
    }
    jintArray tFaceInfo = env->NewIntArray(out_size);
    env->SetIntArrayRegion(tFaceInfo, 0, out_size, faceInfo);
    delete[] faceInfo;
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return tFaceInfo;
}

JNIEXPORT jintArray  JNICALL
Java_org_CrystalBall_Engine_getLandmarkInfo(JNIEnv *env, jobject instance,
                                         jint index) {
    if(!g_bLandmarkDetected) return NULL;
    if(index >= g_landmarkInfo.size()) return NULL;
    jintArray tLandmarkInfo = env->NewIntArray(4 + 70 * 2);
    int landmark_array[4+70 * 2];

    landmark_array[0] = g_landmarkInfo[index].faceRt.x;
    landmark_array[1] = g_landmarkInfo[index].faceRt.y;
    landmark_array[2] = g_landmarkInfo[index].faceRt.width;
    landmark_array[3] = g_landmarkInfo[index].faceRt.height;
    for(int i=0; i<70; i++)
    {
        landmark_array[4+i*2] = g_landmarkInfo[index].landmarks[i].x;
        landmark_array[4+i*2+1] = g_landmarkInfo[index].landmarks[i].y;
    }
    env->SetIntArrayRegion(tLandmarkInfo, 0, 4+70*2, landmark_array);
    return tLandmarkInfo;
}

JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_FaceLandmark(JNIEnv *env, jobject instance,
                                       jobject bitmapIn) {
    g_bLandmarkDetected = false;
    if (!detection_sdk_init_ok) {
        return 0;
    }

    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return 0;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return 0;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return 0;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    if (imageWidth < 20 || imageHeight < 20) {
        return 0;
    }
    if(g_faceInfos.size() < 1)
        DetectFace((unsigned char*)pixelsIn, imageWidth, imageHeight, 4, g_faceInfos);
    if(g_faceInfos.size() < 1) return 0;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    //FaceLocation landmark;

    if(GetLandmark_68(image, g_faceInfos, g_landmarkInfo) < 1)
    {
        g_bLandmarkDetected = false;
        return 0;
    }
    g_bLandmarkDetected = true;
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return g_landmarkInfo.size();
}

JNIEXPORT jbyteArray JNICALL
Java_org_CrystalBall_Engine_FaceMorphing(JNIEnv *env, jobject instance,
                                        jobject bitmapIn, jint faceIndex, jint eyeParam,jint faceParam) {
    if(!g_bLandmarkDetected) return NULL;
    if(faceIndex >= g_landmarkInfo.size()) return NULL;
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    image = Morphing_Image(image, g_landmarkInfo[faceIndex], faceParam, eyeParam);
    int size = image.rows* image.cols*image.channels();
    jbyteArray  retImg = env->NewByteArray(size);
    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return retImg;
}

JNIEXPORT jbyteArray JNICALL
Java_org_CrystalBall_Engine_FaceJaw(JNIEnv *env, jobject instance,
                                        jobject  bitmapIn, jint faceIndex, jint jawParam) {
    if(!g_bLandmarkDetected) return NULL;
    if(faceIndex >= g_landmarkInfo.size()) return NULL;
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    image = JawThin_Image(image, g_landmarkInfo[0], jawParam);
    int size = image.rows* image.cols*image.channels();
    jbyteArray  retImg = env->NewByteArray(size);
    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return retImg;
}

JNIEXPORT jbyteArray JNICALL
Java_org_CrystalBall_Engine_FaceLip(JNIEnv *env, jobject instance,
                                    jobject bitmapIn, jint faceIndex, jint lipParam) {
    if(!g_bLandmarkDetected) return NULL;
    if(faceIndex >= g_landmarkInfo.size()) return NULL;
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    image = LipThin_Image(image, g_landmarkInfo[0], lipParam);
    int size = image.rows* image.cols*image.channels();
    jbyteArray  retImg = env->NewByteArray(size);
    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return retImg;
}

JNIEXPORT jbyteArray JNICALL
Java_org_CrystalBall_Engine_FaceNose(JNIEnv *env, jobject instance,
                                   jobject bitmapIn, jint faceIndex,  jint noseParam) {
    if(!g_bLandmarkDetected) return NULL;
    if(faceIndex >= g_landmarkInfo.size()) return NULL;
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    image = NoseSharp_Image(image, g_landmarkInfo[0], noseParam);
    int size = image.rows* image.cols*image.channels();
    jbyteArray  retImg = env->NewByteArray(size);
    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return retImg;
}

JNIEXPORT jbyteArray JNICALL
Java_org_CrystalBall_Engine_FaceForehead(JNIEnv *env, jobject instance,
                                     jobject bitmapIn, jint faceIndex, jint foreheadParam) {
    if(!g_bLandmarkDetected) return NULL;
    if(faceIndex >= g_landmarkInfo.size()) return NULL;
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    image = ForeheadHigh_Image(image, g_landmarkInfo[0], foreheadParam);
    int size = image.rows* image.cols*image.channels();
    jbyteArray  retImg = env->NewByteArray(size);
    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return retImg;
}

JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_SoftFocusBackgroundFilter(JNIEnv *env, jobject instance,
                                         jobject bitmapIn, jint filterIndex, jint filterParam) {
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);

    switch(filterIndex) {
        case 0:

        cv::blur(image, image, cv::Size(filterParam+5, filterParam+5));
            break;
        case 1:
//            filters->applyPixelArt(image, image, filterParam, 20);
            filters->applyIceSketch(image, image, 0, filterParam);
            break;
        case 2:
            filters->applyColorSketch(image, image, 0, filterParam);
            break;
        case 3:
            filters->applyPencilSketch(image, image, 0, filterParam);
            break;
    }
    memcpy(pixelsIn, image.data, image.cols * image.rows * image.channels());
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return 1;
}

JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_FaceBeauty(JNIEnv *env, jobject instance,
                                            jobject bitmapIn, jobject bitmapOut, jint param) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoOut;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {
        return -1;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return -1;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0)  {
        return -1;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    int outWidth = infoOut.width;
    int outHeight = infoOut.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    //image = convertARGB2BGR(image);
    if(param!=0) {
        Mat nRet = _effect_face_beauty(image, param);
        int size = nRet.rows * nRet.cols * nRet.channels();
        memcpy(pixelsOut, nRet.data, size);
    } else {
        memcpy(pixelsOut, pixelsIn, imageWidth* imageHeight * 4);
    }
    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return 0;
}
//
//JNIEXPORT jint JNICALL
//Java_org_CrystalBall_Engine_ImageClarity(JNIEnv *env, jobject instance,
//                                       jobject bitmapIn, jobject bitmapOut, jint param) {
//    //  if(!g_bLandmarkDetected) return NULL;
//    AndroidBitmapInfo   infoIn;
//    AndroidBitmapInfo   infoOut;
//
//    void*               pixelsIn;
//    void*               pixelsOut;
//    int ret;
//
//    // Get image info
//    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {
//        return -1;
//    }
//
//    // Check image
//    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
//        return -1;
//    }
//
//    // Lock all images
//    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0)  {
//        return -1;
//    }
//
//    int imageWidth = infoIn.width;
//    int imageHeight = infoIn.height;
//    int outWidth = infoOut.width;
//    int outHeight = infoOut.height;
//
//    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
//    //image = convertARGB2BGR(image);
//    Mat nRet = _effect_image_clarity(image, param);
//    int size = nRet.rows* nRet.cols*nRet.channels();
//    memcpy(pixelsOut, nRet.data, size);
//
//    AndroidBitmap_unlockPixels(env, bitmapIn);
//    AndroidBitmap_unlockPixels(env, bitmapOut);
//    return 0;
//}

JNIEXPORT jobject JNICALL
Java_org_CrystalBall_Engine_ImageClarity(JNIEnv *env, jobject instance,
                                         jobject bitmapIn, jint param) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888  ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 )  {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    //image = convertARGB2BGR(image);
    image = _effect_image_clarity(image, param);
    int size = image.rows* image.cols*image.channels();

    jobject  bitmapOut = createBitmap(env, image.cols, image.rows);
    if ((ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0 )  {
        return NULL;
    }
    memcpy(pixelsOut, image.data, size);

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return bitmapOut;
}

JNIEXPORT jobject JNICALL
Java_org_CrystalBall_Engine_ImageCartoon(JNIEnv *env, jobject instance,
                                         jobject bitmapIn, jint param) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888  ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 )  {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    //image = convertARGB2BGR(image);
    Mat imageOut = image.clone();
    filters->cartoon_filter_gray(image, imageOut, param);
    int size = image.rows* image.cols*image.channels();

    jobject  bitmapOut = createBitmap(env, image.cols, image.rows);
    if ((ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0 )  {
        return NULL;
    }
    memcpy(pixelsOut, imageOut.data, size);

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return bitmapOut;
}

JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_removeAcneOne(JNIEnv *env, jobject instance,
                                         jobject bitmapIn, jobject bitmapOut) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoOut;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {
        return -1;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return -1;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0)  {
        return -1;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    int outWidth = infoOut.width;
    int outHeight = infoOut.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    //image = convertARGB2BGR(image);
    Mat nRet = _effect_remove_acne(image);
    int size = nRet.rows* nRet.cols*nRet.channels();
    memcpy(pixelsOut, nRet.data, size);

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return 0;
}

JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_ThemeRomantic(JNIEnv *env, jobject instance,
                                       jobject bitmapIn1, jobject bitmapIn2, jobject bitmapOut, jintArray points) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn1;
    AndroidBitmapInfo   infoIn2;
    AndroidBitmapInfo   infoOut;

    void*               pixelsIn1;
    void*               pixelsIn2;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn1, &infoIn1)) < 0 ||(ret = AndroidBitmap_getInfo(env, bitmapIn2, &infoIn2)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {
        return -1;
    }

    // Check image
    if (infoIn1.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||infoIn2.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return -1;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn1, &pixelsIn1)) < 0 ||(ret = AndroidBitmap_lockPixels(env, bitmapIn2, &pixelsIn2)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0)  {
        return -1;
    }
    if(env->GetArrayLength(points) < 16) return -1;

    jint* pPoints = env->GetIntArrayElements(points, NULL);
    int imageWidth1 = infoIn1.width;
    int imageHeight1 = infoIn1.height;
    int imageWidth2 = infoIn2.width;
    int imageHeight2 = infoIn2.height;
    int outWidth = infoOut.width;
    int outHeight = infoOut.height;

    Mat image1 = make_cv_Mat((unsigned char*)pixelsIn1, imageWidth1, imageHeight1, 4);
    image1 = convertARGB2BGR(image1);
    Mat image2 = make_cv_Mat((unsigned char*)pixelsIn2, imageWidth2, imageHeight2, 4);
    image2 = convertARGB2BGR(image2);

    Mat nRet = _theme_romantic(image1, image2, (int*)pPoints);

    int size = nRet.rows* nRet.cols*nRet.channels();
    memcpy(pixelsOut, nRet.data, size);

    env->ReleaseIntArrayElements(points, pPoints, 0);
    AndroidBitmap_unlockPixels(env, bitmapIn1);
    AndroidBitmap_unlockPixels(env, bitmapIn2);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return 0;
}


JNIEXPORT jboolean JNICALL
Java_org_CrystalBall_Engine_FaceModelUnInit(JNIEnv *env, jobject instance) {
    if (!detection_sdk_init_ok) {
        return true;
    }
    jboolean tDetectionUnInit = false;
    //delete mDetect;


    detection_sdk_init_ok = false;
    tDetectionUnInit = true;
    return tDetectionUnInit;

}

///////////Vision Mix Session/////////////////////////////////
JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_magicTrimapMask(JNIEnv *env, jobject instance,
                                         jobject bitmapIn, jobject bitmapOut) {
  //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoOut;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {
        return -1;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return -1;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0)  {
        return -1;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    int outWidth = infoOut.width;
    int outHeight = infoOut.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    Mat nRet = _magic_1_trimapMask(image);
    int size = nRet.rows* nRet.cols*nRet.channels();
    memcpy(pixelsOut, nRet.data, size);

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return 0;
}

JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_magicTrimapMaskAndInfoflow(JNIEnv *env, jobject instance,
                                            jobject bitmapIn, jobject bitmapOut) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoOut;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {
        return -1;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return -1;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0)  {
        return -1;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    int outWidth = infoOut.width;
    int outHeight = infoOut.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    Mat nRet = _magic_1_trimapMaskAndInfoflow(image);

    if(nRet.cols == 0 || nRet.rows == 0) {
        AndroidBitmap_unlockPixels(env, bitmapIn);
        AndroidBitmap_unlockPixels(env, bitmapOut);
        return -1;
    }

    int size = nRet.rows* nRet.cols*nRet.channels();
    memcpy(pixelsOut, nRet.data, size);

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return 0;
}

JNIEXPORT jint JNICALL
Java_org_CrystalBall_Engine_magicResizeTrimap(JNIEnv *env, jobject instance,
                                            jobject bitmapIn, jobject bitmapOut, jboolean bMono, jboolean bPreproc) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoOut;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) < 0) {
        return -1;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoOut.format != ANDROID_BITMAP_FORMAT_A_8 ) {
        return -1;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) < 0)  {
        return -1;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    int outWidth = infoOut.width;
    int outHeight = infoOut.height;

    Mat trimap_224 = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    Mat image = _resize_trrimapMask(trimap_224, outWidth, outHeight, (bool)bMono, (bool)bPreproc);
    int size = image.rows* image.cols*image.channels();
    memcpy(pixelsOut, image.data, size);

//    jbyteArray  retImg = env->NewByteArray(size);
//    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapOut);
    return 0;
}

//Get One Channel 0-R, 1-G, 2-B, 3-A
JNIEXPORT jobject JNICALL
Java_org_CrystalBall_Engine_magicGetOneChannel(JNIEnv *env, jobject instance,
                                              jobject bitmapIn, jint nChannel) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;

    void*               pixelsIn;
    void*               pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888  ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 )  {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    Mat input = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    cv::Mat channels[4];
    cv::split(input, channels);
    int size = imageWidth* imageHeight;

    jobject  outBitmap = createBitmap(env, imageWidth, imageHeight, 1);
    if ((ret = AndroidBitmap_lockPixels(env, outBitmap, &pixelsOut)) < 0)
    {
        return NULL;
    }
    memcpy(pixelsOut, channels[nChannel].data, size);

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, outBitmap);
    return outBitmap;
}

JNIEXPORT jbyteArray JNICALL
Java_org_CrystalBall_Engine_getAlphaByteArray(JNIEnv *env,
                                         jobject thiz,
                                         jobject bitmapIn) {
    AndroidBitmapInfo   infoIn;
    void*               pixelsIn;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0) {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    cv::Mat splits[4];
    cv::split(image, splits);
    jbyteArray retArray = env->NewByteArray(imageWidth * imageHeight);
    env->SetByteArrayRegion(retArray, 0, imageWidth*imageHeight, (jbyte*)splits[3].data);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    return retArray;
}

JNIEXPORT jobject JNICALL
Java_org_CrystalBall_Engine_magicGetMattingImage(JNIEnv *env, jobject instance,
                                            jobject bitmapIn,  jobject bitmapMask) {
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoMask;

    void*               pixelsIn;
    void*               pixelsMask;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapMask, &infoMask)) < 0) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoMask.format != ANDROID_BITMAP_FORMAT_A_8 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapMask, &pixelsMask)) < 0)  {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);

//    jbyte* maskData = env->functions->GetByteArrayElements(env, maskData_, NULL);
    Mat maskImage = make_cv_Mat((unsigned char*)pixelsMask, imageWidth, imageHeight, 1);
    image = _magic_2_mattingImage(image, maskImage);

    //int size = image.rows* image.cols*image.channels();

//    jbyteArray  retImg = env->NewByteArray(size);
//    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);

//    int sizes[2];
//    sizes[0] = image.cols;
//    sizes[1] = image.rows;
//    env->SetIntArrayRegion(outSize, 0, 2, (jint*)sizes);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapMask);

    jobject newBitmap = createBitmap(env, image.cols, image.rows);

    void* bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, newBitmap, &bitmapPixels)) < 0)
    {
        return NULL;
    }

    memcpy(bitmapPixels, image.data, image.rows * image.cols * image.channels());
    AndroidBitmap_unlockPixels(env, newBitmap);

    return newBitmap;
}

JNIEXPORT jobject JNICALL
Java_org_CrystalBall_Engine_magicGetMattingImageGuidefilterOnly(JNIEnv *env, jobject instance,
                                                 jobject bitmapIn,  jobject bitmapMask) {
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoMask;

    void*               pixelsIn;
    void*               pixelsMask;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapMask, &infoMask)) < 0) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoMask.format != ANDROID_BITMAP_FORMAT_A_8 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapMask, &pixelsMask)) < 0)  {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;

    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);

//    jbyte* maskData = env->functions->GetByteArrayElements(env, maskData_, NULL);
    Mat maskImage = make_cv_Mat((unsigned char*)pixelsMask, imageWidth, imageHeight, 1);
    image = _magic_2_mattingImageGuideFilterOnly(image, maskImage);

    //int size = image.rows* image.cols*image.channels();

//    jbyteArray  retImg = env->NewByteArray(size);
//    env->SetByteArrayRegion(retImg, 0, size, (jbyte*)image.data);

//    int sizes[2];
//    sizes[0] = image.cols;
//    sizes[1] = image.rows;
//    env->SetIntArrayRegion(outSize, 0, 2, (jint*)sizes);
    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapMask);

    jobject newBitmap = createBitmap(env, image.cols, image.rows);

    void* bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, newBitmap, &bitmapPixels)) < 0)
    {
        LOGD("-----8888");
        return NULL;
    }
    LOGD("-----9999");
    memcpy(bitmapPixels, image.data, image.rows * image.cols * image.channels());
    AndroidBitmap_unlockPixels(env, newBitmap);

    return newBitmap;
}

JNIEXPORT jobject JNICALL
Java_org_CrystalBall_Engine_magicGetMattingImageForEffect(JNIEnv *env, jobject instance,
                                              jobject bitmapIn, jobject bitmapMask, jintArray position) {
    //  if(!g_bLandmarkDetected) return NULL;
    AndroidBitmapInfo   infoIn;
    AndroidBitmapInfo   infoMask;

    void*               pixelsIn;
    void*               pixelsMask;
    void *              pixelsOut;
    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 || (ret = AndroidBitmap_getInfo(env, bitmapMask, &infoMask)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||  infoMask.format != ANDROID_BITMAP_FORMAT_A_8   ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 || (ret = AndroidBitmap_lockPixels(env, bitmapMask, &pixelsMask)) < 0 )  {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    int maskWidth = infoMask.width;
    int maskHeight = infoMask.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    image = convertARGB2BGR(image);
    Mat maskImage = make_cv_Mat((unsigned char*)pixelsMask, maskWidth, maskHeight, 1);

    cv::Point pos;
    image = _magic_2_mattingImageForEffect(image, maskImage, pos);
    int poss[2];
    poss[0] = pos.x;
    poss[1] = pos.y;
    env->SetIntArrayRegion(position, 0, 2, (jint*)poss);

    jobject retBitmap = createBitmap(env, image.cols, image.rows);
    if ((ret = AndroidBitmap_lockPixels(env, retBitmap, &pixelsOut)) < 0 )  {
        return NULL;
    }
    memcpy(pixelsOut, image.data, image.cols * image.rows * image.channels());

    AndroidBitmap_unlockPixels(env, bitmapIn);
    AndroidBitmap_unlockPixels(env, bitmapMask);
    AndroidBitmap_unlockPixels(env, retBitmap);
    return retBitmap;
}
//////////////////////////////////////////////////////////////////////
JNIEXPORT jobject JNICALL
Java_org_CrystalBall_Engine_makeBitmapToGray(JNIEnv *env, jobject instance,
                                                          jobject bitmapIn) {
    AndroidBitmapInfo   infoIn;

    void*               pixelsIn;
    void*               pixelsOut;

    int ret;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapIn, &infoIn)) < 0 ) {
        return NULL;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
        return NULL;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapIn, &pixelsIn)) < 0 )  {
        return NULL;
    }

    int imageWidth = infoIn.width;
    int imageHeight = infoIn.height;
    Mat image = make_cv_Mat((unsigned char*)pixelsIn, imageWidth, imageHeight, 4);
    Mat grayImage;
    cv::cvtColor(image, grayImage, CV_RGBA2GRAY);
    cv::cvtColor(grayImage, image, CV_GRAY2RGBA);

//    jobject retBitmap = createBitmap(env, image.cols, image.rows);
//    if ((ret = AndroidBitmap_lockPixels(env, retBitmap, &pixelsOut)) < 0 )  {
//        return NULL;
//    }
//    memcpy(pixelsOut, grayImage.data, grayImage.cols * grayImage.rows * grayImage.channels());
    memcpy(pixelsIn, image.data, image.cols * image.rows * 4);
    AndroidBitmap_unlockPixels(env, bitmapIn);
//    AndroidBitmap_unlockPixels(env, retBitmap);
    return NULL;
}

//////////////////
}
