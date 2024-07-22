#pragma once

#include <vector>
#include <string>
#ifndef WIN32
#include<android/log.h>
#endif
#include "net.h"
#include "Matting.h"
#include "vl/globalmatting.h"
#include "vl/fastguidedfilter.h"

using namespace std;
using namespace cv;

namespace CrystalBall {
	class MagicEngine {
		public:
			bool bInitSucced = false;
			MagicEngine();
			MagicEngine(const string& model_path);
			~MagicEngine();

			void init(const string& model_path);
			void trimapMask(ncnn::Mat& inputImage, cv::Mat& outImage);

			Mat getMaskImage(Mat srcImage, Mat trimapImage);
			void preprocTrimapImage(Mat trimapImage, bool bMono = false);
			Mat preprocTrimapImage(Mat& trimapImage, int cxNor, int cyNor);

			cv::Mat mattingImageColorLineLaplace2(cv::Mat& srcImage, cv::Mat& maskImage);
			cv::Mat mattingImageColorLineLaplace(cv::Mat& srcImage, cv::Mat& trimapImage);
			cv::Mat mattingImageInformationFlowDouble(cv::Mat& srcImage, cv::Mat& trimapImage);
			cv::Mat mattingImageInformationFlowFloat(cv::Mat& srcImage, cv::Mat& trimapImage);
			cv::Mat mattingImageGuideFilter(cv::Mat& srcImage, cv::Mat& trimapImage);
		cv::Mat mattingImageInformationFlowFloatGuideFilter(cv::Mat& srcImage, cv::Mat& trimapImage);
		cv::Mat mattingImageInformationFlowFloatOnly(cv::Mat& srcImage, cv::Mat& trimapImage);
		cv::Mat mattingImageGuideFilterOnly(cv::Mat& srcImage, cv::Mat& trimapImage);
			cv::Mat mattingImageForEffectImage(cv::Mat& srcImage, cv::Mat& trimapImage, cv::Point &position);

		cv::Mat make128Border(cv::Mat &srcImage, bool bEmpty = false);

		//background : blur (seamlessClone)
			cv::Mat effectImageBackBlur(cv::Mat& srcImage, cv::Mat& trimapImage, bool bGrayBackgound);

			//background : gray & blur (infoflow matting)
			cv::Mat effectImageColorOnYou(cv::Mat& srcImage, cv::Mat& trimapImage);

			//romantic
			cv::Mat effectImageRomantic(int cx, int cy, cv::Mat& srcImage1, cv::Mat& srcImage2, Point2f  pt1[4], Point2f  pt2[4]);

			//face beauty
			cv::Mat effectImageFaceBeauty(cv::Mat& srcImage, int smoothParam);

		private:
			void Visualization(cv::Mat prediction_map, cv::Mat& output_image);
			void GetMonoImage(unsigned char *pGrayImage, int cx, int cy);
			void Dilation(cv::Mat src, cv::Mat& dilation_dst, int dilation_size = 3);
			void Erosion(cv::Mat src, cv::Mat& erosion_dst, int erosion_size = 3);
			void GetSegmentImage(unsigned char* pGrayImage, int cx, int cy, unsigned char* pSegmentForeground, unsigned char* pSegmentBackground);
			void getActualSizeofBitmap(unsigned char *pAlphaImage, int wid, int hei, int &x_pos, int &y_pos, int &pic_wid, int &pic_hei);
			void makePNG(unsigned char*pData, int wid, int hei);
			void applyAlpha(unsigned char* pData, unsigned char* pAlpha, unsigned char* pOut, int wid, int hei);

			string LUT_file;
			ncnn::Net m_netSegmentation;
	};
} //namespace CrystalBall