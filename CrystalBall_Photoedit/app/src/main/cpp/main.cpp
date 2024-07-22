#include "main.h"
#include "detect.h"
#include "morphing.h"
#include "FaceSwapper.h"
#include <omp.h>

namespace CrystalBall {
	Detect *gDetect = NULL;
    MagicEngine *gMagicEngine = NULL;
	vector<FaceLocation> g_landmarkInfo;
	bool g_bLandmarkDetected = false;
    vector<Detection> g_faceInfos;
    Mat make_cv_Mat(unsigned char* pImage, int nWidth, int nHeight, int nChannel)
	{
//		Mat image;
//		image.create(nHeight, nWidth, CV_8UC4);
//		memcpy(image.data, pImage, nWidth * nHeight * nChannel);
//		return image;

		if(nChannel == 4)
		{
			Mat image(nHeight, nWidth, CV_8UC4, pImage);
			return image;
		}
		else if(nChannel == 3)
		{
			Mat image(nHeight, nWidth, CV_8UC3, pImage);
			return image;
		}
        else
        {
            Mat image(nHeight, nWidth, CV_8UC1, pImage);
            return image;
        }
	}
	
	Mat convertARGB2BGR(Mat &mat_ARGB)
	{
		if(mat_ARGB.channels() != 4) 
		{
			return mat_ARGB;
		}
		
		Mat split[4], mat_BGR;
		cv::split(mat_ARGB, split);
		Mat tmp = split[0].clone();
		split[0] = split[2].clone();
		split[2] = tmp;
		cv::merge(split, 3, mat_BGR);
		return mat_BGR;
	}

    int Landmark_Init(const char* model_path)
    {
        return LM68_dlib_init(model_path);
    }

    void DetectFace(unsigned char* pImage, int nWidth, int nHeight, int nChannel, vector<Detection> &result)
    {
		result.clear();

		int32_t minFaceSize = 80;
		gDetect->SetMinFace(minFaceSize);

        ncnn::Mat ncnn_img;
        if (nChannel == 3) {
            ncnn_img = ncnn::Mat::from_pixels(pImage, ncnn::Mat::PIXEL_BGR2RGB,
                                              nWidth, nHeight);
        } else {
            ncnn_img = ncnn::Mat::from_pixels(pImage, ncnn::Mat::PIXEL_RGBA2RGB, nWidth,
                                              nHeight);
        }
	    std::vector<Bbox> finalBbox;
        gDetect->start(ncnn_img, finalBbox);

        int32_t num_face = static_cast<int32_t>(finalBbox.size());
        for (int i=0; i<num_face; i++)
        {
            if(finalBbox[i].x1 < 0 || finalBbox[i].x1 >= nWidth || finalBbox[i].x2<0|| finalBbox[i].x2>=nWidth||finalBbox[i].x1 >= finalBbox[i].x2)
                continue;
            if(finalBbox[i].y1 < 0 || finalBbox[i].y1 >= nHeight || finalBbox[i].y2<0|| finalBbox[i].y2>=nHeight||finalBbox[i].y1 >= finalBbox[i].y2)
                continue;


            Detection det;
            det.label_id = i;
            det.score = finalBbox[i].score;
            det.xmin = finalBbox[i].x1;
            det.xmax = finalBbox[i].x2;
            det.ymin = finalBbox[i].y1;
            det.ymax = finalBbox[i].y2;
            for (int j=0; j<5; j++)
            {
                Point2f pt;
                pt.x = finalBbox[i].ppoint[j];
                pt.y = finalBbox[i].ppoint[5+j];
                det.keyPoints.push_back(pt);
            }
            result.push_back(det);
        }
    }
	
	int GetLandmark_68(Mat &image, vector<Detection> &faceInfo, vector<FaceLocation> &landmarkInfoList)
	{
        landmarkInfoList.clear();
        for(int i=0; i<faceInfo.size(); i++)
        {
            FaceLocation landmarkInfo;
//            char str[256];
//            sprintf(str, "%d %f %f %f %f", i, faceInfo[i].xmin, faceInfo[i].xmax, faceInfo[i].ymin, faceInfo[i].ymax);
//            __android_log_write(ANDROID_LOG_DEBUG, "---detect_landmark", str);

            if(0!=GetLandmark(image, faceInfo[i], landmarkInfo))
                continue;
            landmarkInfoList.push_back(landmarkInfo);
        }
		return landmarkInfoList.size();
	}

    FaceLocation offsetLandmarkByFace(FaceLocation landmark) {
        FaceLocation faceRegion = landmark;
        faceRegion.faceRt.x = 0;
        faceRegion.faceRt.y = 0;
        for(int i=0; i<70; i++) {
            faceRegion.landmarks[i].x -= landmark.faceRt.x;
            faceRegion.landmarks[i].y -= landmark.faceRt.y;
        }
        return faceRegion;
    }

	Mat Morphing_Image(Mat &image, const FaceLocation& landmark, int face_Param, int eye_Param)
	{
		Mat outImage = image.clone();
        FaceLocation faceRegion = offsetLandmarkByFace(landmark);
//        MorphingOne(image, landmark, face_Param, eye_Param, outImage);
        MorphingOne(image, faceRegion, face_Param, eye_Param, outImage);
		return outImage;
	}
    Mat JawThin_Image(Mat &image, const FaceLocation& landmark, int jaw_Param)
    {
        Mat outImage = image.clone();
        FaceLocation faceRegion = offsetLandmarkByFace(landmark);
        JawThinFromFaceInfo(image, faceRegion, jaw_Param, outImage);
//        JawThinFromFaceInfo(image, landmark, jaw_Param, outImage);
        return outImage;
    }
    Mat LipThin_Image(Mat &image, const FaceLocation& landmark, int lip_Param)
    {
        Mat outImage = image.clone();
        FaceLocation faceRegion = offsetLandmarkByFace(landmark);
        LipThinFromFaceInfo(image, faceRegion, lip_Param, outImage);
        return outImage;
    }
    Mat NoseSharp_Image(Mat &image, const FaceLocation& landmark, int nose_Param)
    {
        Mat outImage = image.clone();
        FaceLocation faceRegion = offsetLandmarkByFace(landmark);
        NoseSharpenFromFaceInfo(image, faceRegion, nose_Param, outImage);
        return outImage;
    }
    Mat ForeheadHigh_Image(Mat &image, const FaceLocation& landmark, int forehead_Param)
    {
        Mat outImage = image.clone();
        FaceLocation faceRegion = offsetLandmarkByFace(landmark);
        ForeheadHighFromFaceInfo(image, faceRegion, forehead_Param, outImage);
        return outImage;
    }

    Mat getAlphaChannel(Mat &input) {
        Mat inputs[4];
        cv::split(input, inputs);
        Mat alpha;
        cv::merge(inputs,3, input);
        return inputs[3];
    }

    Mat _effect_face_beauty(Mat &image, int beauty_param)
    {
        Mat inputs[4];
        Mat alpha = getAlphaChannel(image);
//        __android_log_write(ANDROID_LOG_DEBUG, "---beauty", "beauty_start");
        if(image.rows > 640 && image.cols > 640) {
            vector<Mat> slices=slices_image(image, 4, 4);
    #pragma omp parallel for(default)
            for(int i=0; i<slices.size() ;i++)
                slices[i] = gMagicEngine->effectImageFaceBeauty(slices[i], beauty_param);

    //        Mat ret = gMagicEngine->effectImageFaceBeauty(image, beauty_param);
            image = merge_slices(slices, 4, 4);
    //        __android_log_write(ANDROID_LOG_DEBUG, "---beauty", "beauty_end");
        } else {
            image = gMagicEngine->effectImageFaceBeauty(image, beauty_param);
        }
        cv::split(image, inputs);
        inputs[3] = alpha;
        cv::merge(inputs, 4, image);
        return image;
    }

    void detailEnhance2(InputArray _src, OutputArray dst, float sigma_s, float sigma_r)
    {
// 	CV_INSTRUMENT_REGION();

        Mat I = _src.getMat();

        float factor = 3.0f;

        Mat lab;
        I.convertTo(lab, CV_32FC3, 1.0 / 255.0);

        vector <Mat> lab_channel;
        cvtColor(lab, lab, COLOR_BGR2Lab);
        split(lab, lab_channel);

        //////////////////////////////////////////////////////////////////////////
        Mat L;
        lab_channel[0].convertTo(lab_channel[0], CV_32FC1, 1.0 / 255.0);

        Domain_Filter_our obj;

        Mat res;
        resize(lab_channel[0], lab_channel[0], Size(lab_channel[0].cols/4, lab_channel[0].rows/4));
        obj.filter(lab_channel[0], res, sigma_s, sigma_r, 1);

        Mat detail = lab_channel[0] - res;
        multiply(detail, factor, detail);
        lab_channel[0] = res + detail;

        lab_channel[0].convertTo(lab_channel[0], CV_32FC1, 255);

        //////////////////////////////////////////////////////////////////////////

        resize(lab_channel[0], lab_channel[0], Size(I.cols, I.rows));

        merge(lab_channel, lab);

        cvtColor(lab, lab, COLOR_Lab2BGR);
        lab.convertTo(dst, CV_8UC3, 255);
    }

    void detailEnhance3(InputArray _src, OutputArray dst, float factor)
    {
        Mat lab = _src.getMat();

//        float factor = 2.0f;
        factor = 1+ factor * 4 / 100;
        lab.convertTo(lab, CV_32FC3, 1.0 / 255.0);

        vector <Mat> lab_channel;
        //Mat lab_channel[3];
        cvtColor(lab, lab, COLOR_BGR2Lab);
        split(lab, lab_channel);
        lab.release();
        lab_channel[0].convertTo(lab_channel[0], CV_32FC1, 1.0 / 255.0);
        Mat res;

        cv::bilateralFilter(lab_channel[0], res, 5, 75, 75);

        Mat detail = lab_channel[0] - res;
        multiply(detail, factor, detail);
        lab_channel[0] = res + detail;

        lab_channel[0].convertTo(lab_channel[0], CV_32FC1, 255);

        merge(lab_channel, lab);
        lab_channel.clear();

        cvtColor(lab, lab, COLOR_Lab2BGR);
        lab.convertTo(dst, CV_8UC3, 255);
    }

    vector<Mat> slices_image(Mat &image, int cols, int rows) {
        vector<Mat> ret;
        int margin = 16;
        int wid = image.cols / cols;
        int hei = image.rows / rows;

        for(int i=0; i<cols; i++) {
            for(int j=0; j<rows; j++){
                int slice_wid = wid;
                int slice_hei = hei;
                int rightmargin = margin;
                int bottommargin = margin;
                int leftmargin = margin;
                int topmargin = margin;

                if(i==0) leftmargin=0;
                if(j==0) topmargin=0;
                if(i==cols-1) {slice_wid = image.cols - wid * i; rightmargin = 0;}
                if(j== rows-1) {slice_hei = image.rows - hei * j; bottommargin = 0;}

                cv::Rect rect(wid * i-leftmargin, hei*j-topmargin, slice_wid+leftmargin+rightmargin, slice_hei+topmargin+bottommargin);
                Mat m = Mat(image, rect).clone();
                ret.push_back(m);
            }
        }
        return ret;
    }

    Mat merge_slices(vector<Mat> &slices, int cols, int rows) {
        Mat ret;
        int margin = 16;
        for(int i=0; i<cols; i++){
            Mat r;
            for(int j=0; j<rows; j++) {
                int leftmargin = margin;
                int rightmargin = margin;
                int topmargin = margin;
                int bottommargin = margin;

                if(i==0) leftmargin = 0;
                if(j==0) topmargin = 0;
                if(i==rows-1) rightmargin = 0;
                if(j==cols-1) bottommargin = 0;

                cv::Rect rect(leftmargin, topmargin, slices[i*rows+j].cols-leftmargin-rightmargin, slices[i*rows+j].rows - topmargin-bottommargin);
                Mat tmp = Mat(slices[i*rows+j], rect);
                if(j==0) {
                    r=tmp;
                    continue;
                }
                cv::vconcat(r, tmp, r);
            }
            if(i==0) {
                ret = r;
                continue;
            }
            cv::hconcat(ret, r, ret);
        }

        return ret;
    }

    Mat _effect_image_clarity(Mat &image, int clarity_param)
    {
        Mat alpha = getAlphaChannel(image);
        //
        __android_log_write(ANDROID_LOG_DEBUG, "---clarity", "clarity_start");

        vector<Mat> slices = slices_image(image, 4, 4);
#pragma omp parallel for(default)
        for(int i=0; i<slices.size(); i++)
            detailEnhance3(slices[i], slices[i], (float)clarity_param);
        //detailEnhance3(image, image, (float)clarity_param);
        image = merge_slices(slices, 4, 4);
        slices.clear();
        __android_log_write(ANDROID_LOG_DEBUG, "---clarity", "clarity_finsih");
//        Mat ret = img;
        Mat inputs[4];
        cv::split(image, inputs);
        inputs[3] = alpha;
        cv::merge(inputs, 4, image);
        return image;
    }

    Mat _effect_remove_acne(Mat &image)
    {
        Mat inputs[4];
        cv::split(image, inputs);
        Mat img, alpha;
        cv::merge(inputs,3, img);
        alpha = inputs[3].clone();
        //

        Mat ret, inpaintMask = Mat::zeros(img.size(), CV_8U);

        cv::circle(inpaintMask, cv::Point(img.cols/2, img.rows/2), img.cols/4, Scalar::all(255), CV_FILLED);
        cv::inpaint(img, inpaintMask, ret, 3, INPAINT_NS);

        cv::split(ret, inputs);
        inputs[3] = alpha;
        cv::merge(inputs, 4, img);
        return img;
    }

    Mat _theme_romantic(Mat &image1, Mat &image2, int* pPoints) {
        Point2f pt1[4];
        Point2f pt2[4];
        for(int i=0; i<4; i++)
        {
            pt1[i].x = pPoints[i*2+0];
            pt1[i].y = pPoints[i*2+1];
            pt2[i].x = pPoints[8+i*2+0];
            pt2[i].y = pPoints[8 + i*2 +1];
        }
        //Point2f pt1[4] = { { 46, 407 }, { 74, 957 }, { 548, 913 }, { 510, 400 } };
        //Point2f pt2[4] = { { 638, 498}, { 814, 998 }, { 1258, 838 }, { 1097, 364 } };
        Mat ret =  gMagicEngine->effectImageRomantic(1280, 1280, image1, image2, pt1, pt2);
        Mat splits[4];
        cv::split(ret, splits);
        Mat tmp = splits[0].clone();
        splits[0] = splits[2];
        splits[2] = tmp;
        splits[3] = Mat(1280, 1280, CV_8UC1, 255);
        cv::merge(splits, 4, ret);
        return ret;
    }

    Mat _swap_face(Mat imgModel, Mat imgUser, FaceLocation *pModelLandmark, FaceLocation *pUserLandmark) {

        FaceLocation *modelLandmark = new FaceLocation();
        FaceLocation *userLandmark = new FaceLocation();
        if(pModelLandmark == NULL) {
            vector<Detection> faceM;
            DetectFace(imgModel.data, imgModel.cols, imgModel.rows, imgModel.channels(), faceM);
            if(faceM.size() == 0) return imgModel;
            GetLandmark(imgModel, faceM[0], *modelLandmark);
        } else
            memcpy(modelLandmark, pModelLandmark, sizeof(FaceLocation));


        if(pUserLandmark == NULL) {
            vector<Detection> faceU;
            DetectFace(imgUser.data, imgUser.cols, imgUser.rows, imgUser.channels(), faceU);
            if(faceU.size() == 0) return imgUser;
            GetLandmark(imgModel, faceU[0], *userLandmark);
        } else
            memcpy(userLandmark, pUserLandmark, sizeof(FaceLocation));


        vector<Point2f> pointModel;
        vector<Point2f> pointUser;
        Mat ret = faceswap_main_part(imgModel, imgUser, modelLandmark, userLandmark, pointModel, pointUser);

        delete modelLandmark;
        delete userLandmark;

        return ret;
    }
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    //input BGR cv mat ----return GRAY cv mat (224x224)
    Mat _magic_1_trimapMask(Mat &image)
    {
        cv::Mat outImage;
        int wid = image.cols;
        int hei = image.rows;
        ncnn::Mat inputImage = ncnn::Mat::from_pixels_resize(image.data, ncnn::Mat::PIXEL_RGB, wid, hei, 224, 224);
        gMagicEngine->trimapMask(inputImage, outImage);

        cv::Mat ret[4];
        cv::Mat m1= Mat::zeros(outImage.size(), CV_8UC1);
        ret[0] = m1.clone();
        ret[1] = m1.clone();
        ret[2] = m1.clone();
        ret[3] = outImage.clone();
        cv::Mat retImage = m1.clone();

        cv::merge(ret, 4, retImage);

        return retImage;
    }

    Mat _resize_trrimapMask(Mat& trimap, int wid, int hei, bool bMono, bool bPreproc)
    {
        cv::Mat outImage;
        cv::Mat splits[4];
        cv::split(trimap, splits );
        resize(splits[3], outImage, cv::Size(wid, hei));
        if(bPreproc)
            gMagicEngine->preprocTrimapImage(outImage, bMono);

//        cv::Mat rgba[4];
//        cv::Mat tmp = Mat::zeros(outImage.size(), CV_8UC1);
//        rgba[0] = outImage.clone();
//        rgba[1] = tmp;
//        rgba[2] = tmp.clone();
//        rgba[3] = outImage.clone();
//        cv::merge(rgba, 4, outImage);
        return outImage;
    }

    bool preprocessForInforflow(Mat &image) {
        int nRows = image.rows;
        int nCols = image.cols;
        int pix_128s = 0;
        int pix_255s = 0;
#pragma omp parallel for(default)
        for (int i = 0; i < nRows; ++i) {
            for (int j = 0; j < nCols; ++j) {
                uchar &pix = image.at<uchar>(i, j);
                if(pix == 0)
                    continue;

                if(pix == 255) {
                    pix_255s ++;
                    break;
                }
                if(pix == 128) {
                    pix_128s ++;
                    break;
                }
            }
        }
#pragma omp parallel for(default)
        for (int i = 0; i <nRows; ++i) {
            for (int j = 0; j <nCols; ++j) {
                int x = nRows-1 -i;
                int y = nCols-1-j;
                uchar &pix = image.at<uchar>(x, y);
                if(pix == 0)
                    continue;

                if(pix == 255) {
                    pix_255s ++;
                    break;
                }
                if(pix == 128) {
                    pix_128s ++;
                    break;
                }
            }
        }

        /////////////
        if(pix_128s < 100  && pix_255s <100) {
            //image = gMagicEngine->make128Border(image, true);
            return false;
        }

        if(pix_128s <= pix_255s * 2) {
            image = gMagicEngine->make128Border(image);
        }
        return true;

    }

    Mat _magic_1_trimapMaskAndInfoflow(Mat &image)
    {
        cv::Mat outImage;
        int wid = image.cols;
        int hei = image.rows;
        ncnn::Mat inputImage = ncnn::Mat::from_pixels_resize(image.data, ncnn::Mat::PIXEL_RGB, wid, hei, 224, 224);
        gMagicEngine->trimapMask(inputImage, outImage);
        bool nRet = preprocessForInforflow(outImage);
        if(nRet)
            outImage = gMagicEngine->mattingImageInformationFlowFloatOnly(image, outImage);
        else {
            cv::Mat empty;
            return empty;
        }
        cv::Mat ret[4];
        cv::Mat m1= Mat::zeros(outImage.size(), CV_8UC1);
        ret[0] = m1.clone();
        ret[1] = m1.clone();
        ret[2] = m1.clone();
        ret[3] = outImage.clone();
        cv::Mat retImage = m1.clone();

        cv::merge(ret, 4, retImage);

        return retImage;
    }

    //input BGR cv mat, Gray cv mat ---- return ARGB cv mat
    Mat _magic_2_mattingImageGuideFilterOnly(Mat &image, Mat &mask)
    {
//        gMagicEngine->preprocTrimapImage(mask);

        int validPixelCounts = 0;
#pragma omp parallel for(default)
        for (int i = 0; i < mask.rows; ++i) {
            for (int j = 0; j < mask.cols; ++j) {
                uchar &pix = mask.at<uchar>(i, j);
                if(pix != 0)
                    validPixelCounts++;
            }
        }

        if(validPixelCounts < mask.rows * mask.cols / 200)
        {
            mask |=255;
        }
        Mat ret = gMagicEngine->mattingImageGuideFilterOnly(image, mask);
        return ret;
    }

    //input BGR cv mat, Gray cv mat ---- return ARGB cv mat
    Mat _magic_2_mattingImage(Mat &image, Mat &mask)
    {
        cv::resize(mask, mask, cv::Size(224,224));
        gMagicEngine->preprocTrimapImage(mask);
        Mat ret = gMagicEngine->mattingImageInformationFlowFloatGuideFilter(image, mask);
        return ret;
    }

    Mat _magic_2_mattingImageForEffect(Mat &image, Mat &mask, cv::Point &pos) {
        cv::resize(mask, mask, cv::Size(224,224));
        gMagicEngine->preprocTrimapImage(mask);
        Mat ret = gMagicEngine->mattingImageForEffectImage(image, mask, pos);
        return ret;
    }
}