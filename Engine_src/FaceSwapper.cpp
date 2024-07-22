
#include "FaceSwapper.h"

#include <iostream>
#include <stdint.h>
#include "morphing.h"

//// Apply affine transform calculated using srcTri and dstTri to src
//void applyAffineTransform(cv::Mat &warpImage, cv::Mat &src, vector<Point2f> &srcTri, vector<Point2f> &dstTri)
//{
//	// Given a pair of triangles, find the affine transform.
//	cv::Mat warpMat = cv::getAffineTransform(srcTri, dstTri);
//	// Apply the Affine Transform just found to the src image
//	cv::warpAffine(src, warpImage, warpMat, warpImage.size(), INTER_LINEAR, BORDER_REFLECT_101);
//}

// Warps and alpha blends triangular regions from img1 and img2 to img
void warpTriangle(cv::Mat &i_imgSrc, cv::Mat &i_imgDst, vector<Point2f> &ptSrc, vector<Point2f> &ptDst)
{
	cv::Rect rtSrc = boundingRect(ptSrc);
	cv::Rect rtDst = boundingRect(ptDst);

	// Offset points by left top corner of the respective rectangles
	vector<Point2f> t1Rect, t2Rect;
	vector<Point> t2RectInt;
	for (int i = 0; i < 3; i++) {
		t1Rect.push_back(Point2f(ptSrc[i].x - rtSrc.x, ptSrc[i].y - rtSrc.y));
		t2Rect.push_back(Point2f(ptDst[i].x - rtDst.x, ptDst[i].y - rtDst.y));
		t2RectInt.push_back(Point(ptDst[i].x- rtDst.x, ptDst[i].y - rtDst.y)); // for fillConvexPoly
	}

	// Get mask by filling triangle
	cv::Mat mask = cv::Mat::zeros(rtDst.height, rtDst.width, i_imgSrc.type());
	cv::fillConvexPoly(mask, t2RectInt, Scalar(1.0, 1.0, 1.0), 16, 0);

	// Apply warpImage to small rectangular patches
	cv::Mat imgSrc;
	i_imgSrc(rtSrc).copyTo(imgSrc);

	cv::Mat imgDst = cv::Mat::zeros(rtDst.height, rtDst.width, imgSrc.type());

	applyAffineTransform(imgDst, imgSrc, t1Rect, t2Rect);

	multiply(imgDst, mask, imgDst);
	multiply(i_imgDst(rtDst), Scalar(1.0, 1.0, 1.0) - mask, i_imgDst(rtDst));

	i_imgDst(rtDst) = i_imgDst(rtDst) + imgDst;
}

// Calculate Delaunay triangles for set of points
// Returns the vector of indices of 3 points for each triangle
static void calculateDelaunayTriangles(Rect rect, vector<Point2f> &points, vector< vector<int> > &delaunayTri)
{
	// Create an instance of Subdiv2D
	Subdiv2D subdiv(rect);

	// Insert points into subdiv
	for (vector<Point2f>::iterator it = points.begin(); it != points.end(); it++)
		subdiv.insert(*it);

	vector<Vec6f> triangleList;
	subdiv.getTriangleList(triangleList);
	vector<Point2f> pt(3);
	vector<int> ind(3);

	for (size_t i = 0; i < triangleList.size(); i++) {
		Vec6f t = triangleList[i];
		pt[0] = Point2f(t[0], t[1]);
		pt[1] = Point2f(t[2], t[3]);
		pt[2] = Point2f(t[4], t[5 ]);

		if (rect.contains(pt[0]) && rect.contains(pt[1]) && rect.contains(pt[2])){
			for (int j = 0; j < 3; j++)
				for (size_t k = 0; k < points.size(); k++)
					if (abs(pt[j].x - points[k].x) < 1.0 && abs(pt[j].y - points[k].y) < 1)
						ind[j] = k;

			delaunayTri.push_back(ind);
		}
	}

	cout << "calculateDelaunayTriangles" << endl;
}

#define TEST_DRAW		0
cv::Mat faceswap_main(cv::Mat imgModel, cv::Mat imgUser, vector<Point2f> pointsModel, vector<Point2f> pointsUser)
{
#if TEST_DRAW //for draw
	int resize_x = 3;
	int resize_y = 3;
#endif

	cv::Mat img1Warped = imgModel.clone();
	cv::Mat img1UserCn = imgUser.clone();

	//convert Mat to float data type
	img1Warped.convertTo(img1Warped, CV_32F);
	imgUser.convertTo(imgUser, CV_32F);

// 	cv::Mat img11 = imgUser, img22 = imgModel;
// 	img11.convertTo(img11, CV_8UC3);
// 	img22.convertTo(img22, CV_8UC3);

	// Find convex hull
	vector<Point2f> hullUser;
	vector<Point2f> hullModel;
	vector<int> hullIndex;

	cv::convexHull(pointsUser, hullIndex, false, false);

	for (size_t i = 0; i < hullIndex.size(); i++) {
		hullModel.push_back(pointsModel[hullIndex[i]]);
		hullUser.push_back(pointsUser[hullIndex[i]]);
	}

#if TEST_DRAW //for draw
	for (int i = 0; i < hullIndex.size(); i++) {
		//drawMarker(img1UserCn, cv::Point(pointsUser[hullIndex[i]].x, pointsUser[hullIndex[i]].y), Scalar(0, 0, 255), 1, 4);
		drawMarker(imgModel,   cv::Point(hullModel[i].x, hullModel[i].y), Scalar(0, 0, 255), 1, 4);
		drawMarker(img1UserCn, cv::Point(hullUser[i].x, hullUser[i].y), Scalar(0, 0, 255), 1, 4);
	}

	// 	Mat resize_imgUser;
	// 	cv::resize(img1UserCn, resize_imgUser, Size(), resize_x, resize_y);
	cv::imshow("imgModel", imgModel);
	cv::imshow("imgUser", img1UserCn);
#endif

	// Find delaunay triangulation for points on the convex hull
	vector< vector<int> > dt;
	cv::Rect rect(0, 0, img1Warped.cols, img1Warped.rows);
	calculateDelaunayTriangles(rect, hullModel, dt);
	
	// Apply affine transformation to Delaunay triangles
#if 0
	vector<Point2f> t1, t2;
	// Get points for img1, img2 corresponding to the triangles
	t1.push_back(points1[8]);
	t2.push_back(points2[8]);
	t1.push_back(points1[36]);
	t2.push_back(points2[36]);
	t1.push_back(points1[45]);
	t2.push_back(points2[45]);

	warpTriangle(img1, img1Warped, t1, t2);
#else
	for (size_t i = 0; i < dt.size(); i++) {
		vector<Point2f> t1, t2;
		// Get points for img1, img2 corresponding to the triangles
		for(size_t j = 0; j < 3; j++) {
			t1.push_back(hullUser[dt[i][j]]);
			t2.push_back(hullModel[dt[i][j]]);
		}
		warpTriangle(imgUser, img1Warped, t1, t2);
	}
#endif

	// Calculate mask
	vector<Point> hull8U;
	for (size_t i = 0; i < hullModel.size(); i++) {
		Point pt(hullModel[i].x, hullModel[i].y);
		hull8U.push_back(pt);
	}

	Mat mask = Mat::zeros(imgModel.rows, imgModel.cols, imgModel.depth());
	fillConvexPoly(mask, &hull8U[0], hull8U.size(), Scalar(255,255,255), 9);

	cv::Size feather_amount;
	feather_amount.width = feather_amount.height = (int)cv::norm(pointsUser[0] - pointsUser[16]) / 8;
// 	cv::erode(mask, mask, getStructuringElement(cv::MORPH_RECT, feather_amount), cv::Point(-1, -1), 1, cv::BORDER_CONSTANT, cv::Scalar(0));
	//cv::blur(mask, mask, feather_amount, cv::Point(-1, -1), cv::BORDER_CONSTANT);

	// Clone seamlessly.
	Rect r = boundingRect(hullModel);
	img1Warped.convertTo(img1Warped, CV_8UC3);
	Mat img1WarpedSub = img1Warped(r);
	Mat img2Sub       = imgModel(r);
	Mat maskSub       = mask(r);

#if TEST_DRAW
	Mat resize_img2Sub;
	cv::resize(img2Sub, resize_img2Sub, Size(), resize_x, resize_y);
	cv::imshow("img2Sub pre", resize_img2Sub);
#endif

	Point center(r.width/2, r.height/2);


	Mat output;
	cv::seamlessClone(img1WarpedSub, img2Sub, maskSub, center, output, NORMAL_CLONE);
	output.copyTo(imgModel(r));

#if TEST_DRAW
	Mat resize_img1WarpedSub;
	cv::resize(img1WarpedSub, resize_img1WarpedSub, Size(), resize_x, resize_y);
	cv::imshow("img1WarpedSub", resize_img1WarpedSub);
	//Mat resize_img2Sub;
	cv::resize(img2Sub, resize_img2Sub, Size(), resize_x, resize_y);
	cv::imshow("img2Sub", resize_img2Sub);
	Mat resize_maskSub;
	cv::resize(maskSub, resize_maskSub, Size(), resize_x, resize_y);
	cv::imshow("maskSub", resize_maskSub);
#endif

	return imgModel;
}

cv::Mat faceswap_main_part(cv::Mat &pImageModel, cv::Mat &pImageUser, FaceLocation* pFaceInfoModel, FaceLocation* pFaceInfoUser, vector<Point2f> &pointsModel, vector<Point2f> &pointsUser)
{
	Point2f pt;
// 	vector<Point2f> pointsModel;
// 	vector<Point2f> pointsUser;

	// Find convex hull
	vector<Point2f> hullUser;
	vector<Point2f> hullModel;
	vector<int> hullIndex;

	int i;
	int nGapTopModel = 1*((pFaceInfoModel->landmarks[38].y - pFaceInfoModel->landmarks[20].y) + (pFaceInfoModel->landmarks[43].y - pFaceInfoModel->landmarks[23].y)) / 4;
	int nGapTopUser  = 1*((pFaceInfoUser->landmarks[38].y  - pFaceInfoUser->landmarks[20].y)  + (pFaceInfoUser->landmarks[43].y  - pFaceInfoUser->landmarks[23].y))  / 4;

	for (i = 17; i <= 26; i++) {
		pt = Point2f((float)pFaceInfoModel->landmarks[i].x, (float)max(0, pFaceInfoModel->landmarks[i].y - nGapTopModel));
		pointsModel.push_back(pt);
		pt = Point2f((float)pFaceInfoUser->landmarks[i].x,  (float)max(0, pFaceInfoUser->landmarks[i].y  - nGapTopUser));
		pointsUser.push_back(pt);
	}

	//////////////////////////////////////////////////////////////////////////
	pt = Point2f((float)((pFaceInfoModel->landmarks[26].x + pFaceInfoModel->landmarks[16].x) / 2), (float)((pFaceInfoModel->landmarks[26].y + pFaceInfoModel->landmarks[16].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[26].x  + pFaceInfoUser->landmarks[16].x) / 2),  (float)((pFaceInfoUser->landmarks[26].y  + pFaceInfoUser->landmarks[16].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[45].x + pFaceInfoModel->landmarks[14].x) / 2), (float)((pFaceInfoModel->landmarks[45].y + pFaceInfoModel->landmarks[14].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[45].x  + pFaceInfoUser->landmarks[14].x) / 2),  (float)((pFaceInfoUser->landmarks[45].y  + pFaceInfoUser->landmarks[14].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[46].x + pFaceInfoModel->landmarks[13].x) / 2), (float)((pFaceInfoModel->landmarks[46].y + pFaceInfoModel->landmarks[13].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[46].x  + pFaceInfoUser->landmarks[13].x) / 2),  (float)((pFaceInfoUser->landmarks[46].y  + pFaceInfoUser->landmarks[13].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[35].x + pFaceInfoModel->landmarks[13].x) / 2), (float)((pFaceInfoModel->landmarks[35].y + pFaceInfoModel->landmarks[13].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[35].x  + pFaceInfoUser->landmarks[13].x) / 2), (float)((pFaceInfoUser->landmarks[35].y  + pFaceInfoUser->landmarks[13].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[12].x + pFaceInfoModel->landmarks[54].x) / 2), (float)((pFaceInfoModel->landmarks[12].y + pFaceInfoModel->landmarks[54].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[12].x  + pFaceInfoUser->landmarks[54].x) / 2),  (float)((pFaceInfoUser->landmarks[12].y  + pFaceInfoUser->landmarks[54].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[11].x + pFaceInfoModel->landmarks[54].x) / 2), (float)((pFaceInfoModel->landmarks[11].y + pFaceInfoModel->landmarks[54].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[11].x  + pFaceInfoUser->landmarks[54].x) / 2),  (float)((pFaceInfoUser->landmarks[11].y  + pFaceInfoUser->landmarks[54].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[10].x + pFaceInfoModel->landmarks[55].x) / 2), (float)((pFaceInfoModel->landmarks[10].y + pFaceInfoModel->landmarks[55].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[10].x  + pFaceInfoUser->landmarks[55].x) / 2),  (float)((pFaceInfoUser->landmarks[10].y  + pFaceInfoUser->landmarks[55].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[9].x + pFaceInfoModel->landmarks[56].x) / 2), (float)((pFaceInfoModel->landmarks[9].y + pFaceInfoModel->landmarks[56].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[9].x  + pFaceInfoUser->landmarks[56].x) / 2),  (float)((pFaceInfoUser->landmarks[9].y  + pFaceInfoUser->landmarks[56].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[8].x + pFaceInfoModel->landmarks[57].x) / 2), (float)((pFaceInfoModel->landmarks[8].y + pFaceInfoModel->landmarks[57].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[8].x  + pFaceInfoUser->landmarks[57].x) / 2),  (float)((pFaceInfoUser->landmarks[8].y  + pFaceInfoUser->landmarks[57].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[7].x + pFaceInfoModel->landmarks[58].x) / 2), (float)((pFaceInfoModel->landmarks[7].y + pFaceInfoModel->landmarks[58].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[7].x  + pFaceInfoUser->landmarks[58].x) / 2),  (float)((pFaceInfoUser->landmarks[7].y  + pFaceInfoUser->landmarks[58].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[6].x + pFaceInfoModel->landmarks[59].x) / 2), (float)((pFaceInfoModel->landmarks[6].y + pFaceInfoModel->landmarks[59].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[6].x  + pFaceInfoUser->landmarks[59].x) / 2),  (float)((pFaceInfoUser->landmarks[6].y  + pFaceInfoUser->landmarks[59].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[5].x + pFaceInfoModel->landmarks[48].x) / 2), (float)((pFaceInfoModel->landmarks[5].y + pFaceInfoModel->landmarks[48].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[5].x  + pFaceInfoUser->landmarks[48].x) / 2),  (float)((pFaceInfoUser->landmarks[5].y  + pFaceInfoUser->landmarks[48].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[4].x + pFaceInfoModel->landmarks[48].x) / 2), (float)((pFaceInfoModel->landmarks[4].y + pFaceInfoModel->landmarks[48].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[4].x  + pFaceInfoUser->landmarks[48].x) / 2),  (float)((pFaceInfoUser->landmarks[4].y  + pFaceInfoUser->landmarks[48].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[3].x + pFaceInfoModel->landmarks[31].x) / 2), (float)((pFaceInfoModel->landmarks[3].y + pFaceInfoModel->landmarks[31].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[3].x  + pFaceInfoUser->landmarks[31].x) / 2),  (float)((pFaceInfoUser->landmarks[3].y  + pFaceInfoUser->landmarks[31].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[3].x + pFaceInfoModel->landmarks[41].x) / 2), (float)((pFaceInfoModel->landmarks[3].y + pFaceInfoModel->landmarks[41].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[3].x  + pFaceInfoUser->landmarks[41].x) / 2),  (float)((pFaceInfoUser->landmarks[3].y  + pFaceInfoUser->landmarks[41].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[2].x + pFaceInfoModel->landmarks[36].x) / 2), (float)((pFaceInfoModel->landmarks[2].y + pFaceInfoModel->landmarks[36].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[2].x  + pFaceInfoUser->landmarks[36].x) / 2),  (float)((pFaceInfoUser->landmarks[2].y  + pFaceInfoUser->landmarks[36].y) / 2));
	pointsUser.push_back(pt);

	pt = Point2f((float)((pFaceInfoModel->landmarks[0].x + pFaceInfoModel->landmarks[17].x) / 2), (float)((pFaceInfoModel->landmarks[0].y + pFaceInfoModel->landmarks[17].y) / 2));
	pointsModel.push_back(pt);
	pt = Point2f((float)((pFaceInfoUser->landmarks[0].x  + pFaceInfoUser->landmarks[17].x) / 2),  (float)((pFaceInfoUser->landmarks[0].y  + pFaceInfoUser->landmarks[17].y) / 2));
	pointsUser.push_back(pt);

	for (size_t i = 0; i < pointsModel.size(); i++) {
		hullModel.push_back(pointsModel[i]);
		hullUser.push_back(pointsUser[i]);
	}





#if TEST_DRAW //for draw
	int resize_x = 3;
	int resize_y = 3;
#endif

	cv::Mat img1Warped = pImageModel.clone();
	cv::Mat img1UserCn = pImageUser.clone();

	//convert Mat to float data type
	img1Warped.convertTo(img1Warped, CV_32F);
	pImageUser.convertTo(pImageUser, CV_32F);

	// 	cv::Mat img11 = imgUser, img22 = imgModel;
	// 	img11.convertTo(img11, CV_8UC3);
	// 	img22.convertTo(img22, CV_8UC3);

	// Find convex hull
	vector<Point2f> convexHullUser;
	vector<Point2f> convexHullModel;
	vector<int> convexHullIndex;

	cv::convexHull(hullUser, convexHullIndex, false, false);

	for (size_t i = 0; i < convexHullIndex.size(); i++) {
		convexHullModel.push_back(hullModel[convexHullIndex[i]]);
		convexHullUser.push_back(hullUser[convexHullIndex[i]]);
	}

#if TEST_DRAW //for draw
	for (int i = 0; i < hullModel.size(); i++) {
		//drawMarker(img1UserCn, cv::Point(pointsUser[hullIndex[i]].x, pointsUser[hullIndex[i]].y), Scalar(0, 0, 255), 1, 4);
		drawMarker(pImageModel, cv::Point(hullModel[i].x, hullModel[i].y), Scalar(0, 0, 255), 1, 4);
		drawMarker(img1UserCn, cv::Point(hullUser[i].x, hullUser[i].y), Scalar(0, 0, 255), 1, 4);
	}

	// 	Mat resize_imgUser;
	// 	cv::resize(img1UserCn, resize_imgUser, Size(), resize_x, resize_y);
	cv::imwrite("D:/imgModel.png", pImageModel);
	cv::imshow("imgModel", pImageModel);
	cv::imwrite("D:/imgUser.png", img1UserCn);
	cv::imshow("imgUser", img1UserCn);
#endif

	// Find delaunay triangulation for points on the convex hull
	vector< vector<int> > dt;
	cv::Rect rect(0, 0, img1Warped.cols, img1Warped.rows);
	calculateDelaunayTriangles(rect, convexHullModel, dt);

	// Apply affine transformation to Delaunay triangles
	for (size_t i = 0; i < dt.size(); i++) {
		vector<Point2f> t1, t2;
		// Get points for img1, img2 corresponding to the triangles
		for(size_t j = 0; j < 3; j++) {
			t1.push_back(convexHullUser[dt[i][j]]);
			t2.push_back(convexHullModel[dt[i][j]]);
		}
		warpTriangle(pImageUser, img1Warped, t1, t2);
	}

	img1Warped.convertTo(img1Warped, CV_8UC3);

#if TEST_DRAW //for draw
	cv::imshow("img1Warped", img1Warped);
#endif
	// Calculate mask
	vector<Point> hull8UModel;
	vector<Point> hull8UUser;
	for (size_t i = 0; i < hullModel.size(); i++) {
		Point ptM(hullModel[i].x, hullModel[i].y);
		hull8UModel.push_back(ptM);
		Point ptU(hullUser[i].x, hullUser[i].y);
		hull8UUser.push_back(ptU);
	}

	Mat maskModel = Mat::zeros(pImageModel.rows, pImageModel.cols, pImageModel.depth());
	fillConvexPoly(maskModel, &hull8UModel[0], hull8UModel.size(), Scalar(255,255,255), 8);

	hull8UModel.clear();
	for (size_t i = 0; i < 5; i++) {
		Point ptM(hullModel[i].x, hullModel[i].y);
		hull8UModel.push_back(ptM);
	}
	Point ptM(hullModel[hullModel.size()-1].x, hullModel[hullModel.size()-1].y);
	hull8UModel.push_back(ptM);
	fillConvexPoly(maskModel, &hull8UModel[0], hull8UModel.size(), Scalar(255,255,255), 8);

	hull8UModel.clear();
	for (size_t i = 5; i < 11; i++) {
		Point ptM(hullModel[i].x, hullModel[i].y);
		hull8UModel.push_back(ptM);
	}
	fillConvexPoly(maskModel, &hull8UModel[0], hull8UModel.size(), Scalar(255,255,255), 8);

	cv::Size feather_amount;
	feather_amount.width = feather_amount.height = (int)cv::norm(pointsModel[0] - pointsModel[16]) / 8;
	// 	cv::erode(maskModel, maskModel, getStructuringElement(cv::MORPH_RECT, feather_amount), cv::Point(-1, -1), 1, cv::BORDER_CONSTANT, cv::Scalar(0));
	//cv::blur(mask, mask, feather_amount, cv::Point(-1, -1), cv::BORDER_CONSTANT);

	Mat maskUser = Mat::zeros(pImageUser.rows, pImageUser.cols, pImageUser.depth());
	fillConvexPoly(maskUser, &hull8UUser[0], hull8UUser.size(), Scalar(255,255,255), 8);

	hull8UUser.clear();
	for (size_t i = 0; i < 5; i++) {
		Point ptU(hullUser[i].x, hullUser[i].y);
		hull8UUser.push_back(ptU);
	}
	Point ptU(hullUser[hullModel.size()-1].x, hullUser[hullModel.size()-1].y);
	hull8UUser.push_back(ptU);
	fillConvexPoly(maskUser, &hull8UUser[0], hull8UUser.size(), Scalar(255,255,255), 8);

	hull8UUser.clear();
	for (size_t i = 5; i < 11; i++) {
		Point ptU(hullUser[i].x, hullUser[i].y);
		hull8UUser.push_back(ptU);
	}
	fillConvexPoly(maskUser, &hull8UUser[0], hull8UUser.size(), Scalar(255,255,255), 8);

#if TEST_DRAW //for draw
	cv::imshow("maskModel", maskModel);
	cv::imshow("maskUser", maskUser);
#endif



//	cv::Size feather_amount;
	feather_amount.width = feather_amount.height = (int)cv::norm(pointsUser[0] - pointsUser[16]) / 8;
	// 	cv::erode(mask, mask, getStructuringElement(cv::MORPH_RECT, feather_amount), cv::Point(-1, -1), 1, cv::BORDER_CONSTANT, cv::Scalar(0));
	//cv::blur(mask, mask, feather_amount, cv::Point(-1, -1), cv::BORDER_CONSTANT);

	// Clone seamlessly.
	Rect r = boundingRect(hullModel);
	Mat img1WarpedSub = img1Warped(r);
	Mat img2Sub       = pImageModel(r);
	Mat maskSub       = maskModel(r);

#if TEST_DRAW
	Mat resize_img2Sub;
	cv::resize(img2Sub, resize_img2Sub, Size(), resize_x, resize_y);
	cv::imshow("img2Sub pre", resize_img2Sub);
#endif

	Point center(r.width/2, r.height/2);


	Mat output;
	cv::seamlessClone(img1WarpedSub, img2Sub, maskSub, center, output, NORMAL_CLONE);
	output.copyTo(pImageModel(r));

#if TEST_DRAW
	Mat resize_img1WarpedSub;
	cv::resize(img1WarpedSub, resize_img1WarpedSub, Size(), resize_x, resize_y);
	cv::imshow("img1WarpedSub", resize_img1WarpedSub);
	//Mat resize_img2Sub;
	cv::resize(img2Sub, resize_img2Sub, Size(), resize_x, resize_y);
	cv::imshow("img2Sub", resize_img2Sub);
	Mat resize_maskSub;
	cv::resize(maskSub, resize_maskSub, Size(), resize_x, resize_y);
	cv::imshow("maskSub", resize_maskSub);
#endif

	return pImageModel;
}
