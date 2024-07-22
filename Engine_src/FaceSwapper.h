#pragma once

#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
//#include <opencv2/video/video.hpp>
// #include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/photo.hpp>

#include <iostream>
#include "common.h"

using namespace std;
using namespace cv;

#define MAX_FACENUM			64
#define LANDMARK_COUNT		68

//void applyAffineTransform(cv::Mat &warpImage, cv::Mat &src, vector<Point2f> &srcTri, vector<Point2f> &dstTri);
cv::Mat faceswap_main(cv::Mat imgModel, cv::Mat imgUser, vector<Point2f> pointsModel, vector<Point2f> pointsUser);
cv::Mat faceswap_main_part(cv::Mat &pImageModel, cv::Mat &pImageUser, FaceLocation* pFaceInfoModel, FaceLocation* pFaceInfoUser, vector<Point2f> &pointsModel, vector<Point2f> &pointsUser);

