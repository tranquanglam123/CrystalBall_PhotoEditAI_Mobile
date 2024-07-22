#pragma once
#include "detect.h"
#include "MagicEngine.h"
#include "common.h"
#include "filters.h"
#include <vector>

using namespace std;
using namespace cv;

namespace CrystalBall {
	extern Detect *gDetect;
	extern vector<FaceLocation> g_landmarkInfo;
	extern MagicEngine *gMagicEngine;
	extern vector<Detection> g_faceInfos;
	extern bool g_bLandmarkDetected;
	Mat make_cv_Mat(unsigned char* pImage, int nWidth, int nHeight, int nChannel);
	Mat convertARGB2BGR(Mat &mat_ARGB);

	int Landmark_Init(const char* model_path);
    void DetectFace(unsigned char* pImage, int nWidth, int nHeight, int nChannel, vector<Detection> &result);
	int GetLandmark_68(Mat &image, vector<Detection> &faceInfo, vector<FaceLocation> &landmarkInfoList);
	Mat Morphing_Image(Mat &image, const FaceLocation& landmark, int face_Param, int eye_Param);  //param range -100~100
    Mat JawThin_Image(Mat &image, const FaceLocation& landmark, int jaw_Param); //param range 0~100 default 30
    Mat LipThin_Image(Mat &image, const FaceLocation& landmark, int lip_Param); //param range 0~100 default 50
    Mat NoseSharp_Image(Mat &image, const FaceLocation& landmark, int nose_Param); //param range 0~100 default  50
    Mat ForeheadHigh_Image(Mat &image, const FaceLocation& landmark, int forehead_Param);//param range 0~100 default 50

	Mat _effect_face_beauty(Mat &image, int beauty_param);
	Mat _effect_image_clarity(Mat &image, int clarity_param);
	Mat _effect_remove_acne(Mat &image);

	Mat _theme_romantic(Mat &image1, Mat &image2, int* pPoints);
	Mat _swap_face(Mat imgModel, Mat imgUser, FaceLocation *pModelLandmark = NULL, FaceLocation *pUserLandmark=NULL);

	Mat _magic_1_trimapMask(Mat &image);
	Mat _magic_1_trimapMaskAndInfoflow(Mat &image);

	Mat _resize_trrimapMask(Mat& trimap, int wid, int hei, bool bMono = false, bool bPreproc=true);
	Mat _magic_2_mattingImage(Mat &image, Mat& mask);
	Mat _magic_2_mattingImageGuideFilterOnly(Mat &image, Mat &mask);
	Mat _magic_2_mattingImageForEffect(Mat &image, Mat &mask, Point &pos);


	vector<Mat> slices_image(Mat &image, int cols, int rows);
	Mat merge_slices(vector<Mat> &slices, int cols, int rows);
}