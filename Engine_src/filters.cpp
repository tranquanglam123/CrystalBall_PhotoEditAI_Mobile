#include "filters.h"

#define INPUT_MAX 100

#define CARTOON_THICK_MIN 3
#define CARTOON_THICK_MAX 51
#define CARTOON_THRESH_MIN 1
#define CARTOON_THRESH_MAX 10

#define SKETCH_BLEND_MIN 0.5
#define SKETCH_BLEND_MAX 1.0 
#define SKETCH_TEXSCALE_MIN 1.0
#define SKETCH_TEXSCALE_MAX 5.0

#define SKETCH2_BLUR_MIN 3
#define SKETCH2_BLUR_MAX 51
#define SKETCH2_CONTRAST_MIN 1.0
#define SKETCH2_CONTRAST_MAX 10.0 

#define OILPAINT_RADIUS_MIN 1
#define OILPAINT_RADIUS_MAX 10
#define OILPAINT_LEVELS_MIN 5
#define OILPAINT_LEVELS_MAX 30

#define PIXEL_SIZE_MIN 2
#define PIXEL_SIZE_MAX 30
#define PIXEL_COLORS_MIN 2
#define PIXEL_COLORS_MAX 20

#define CONVERT_RANGE(param, rangeMin, rangeMax)    (param*(rangeMax-rangeMin)/INPUT_MAX + rangeMin)
#define COLOR_DODGE_BLEND(srcPixel, blendPixel)     ((blendPixel >= 255)? 255:(min(255, (srcPixel*255)/(255-blendPixel) )))
#define LINEAR_DODGE_BLEND(srcPixel, blendPixel)    (min(255, srcPixel + blendPixel))
#define SCREEN_BLEND(srcPixel, blendPixel)          (255 - min(255, (255-srcPixel)*(255-blendPixel)/255))
#define LIGHTEN_BLEND(srcPixel, blendPixel)         ((srcPixel > blendPixel) ? srcPixel: blendPixel)


/* Singleton instance initialization */
ImageFilters* ImageFilters::sInstance = NULL;


/* Private Constructor to allow only single private instance */
ImageFilters::ImageFilters() 
{
    mScaleFactor = 1.0;
}


/* Static method to get singleton instance */
ImageFilters* ImageFilters::getInstance()
{
    if(sInstance == NULL) 
        sInstance = new ImageFilters();    
    return sInstance;
} 


/* Set common scale-factor for all filters*/
void ImageFilters::setScaleFactor(double scaleFactor)
{
    mScaleFactor = scaleFactor;
}


/* Set sketch texture for sketch-filters */
void ImageFilters::setSketchTexture(Mat& texture)
{
    mSketchTexture = texture;
    mSketchTexture = ~(1.2*~mSketchTexture);
}


/* Reverse sketch texture */
void ImageFilters::setSketchFlip(bool sketchFlip) {
    if(mSketchFlip != sketchFlip)
    {
        mSketchFlip = sketchFlip;
        flip(mSketchTexture, mSketchTexture, 0);
    }
}


/* Color-Cartoon Filter Imaplementation */
void ImageFilters::applyColorCartoon(Mat& src, Mat& dst, int edgeThickness, int edgeThreshold) 
{	
	edgeThickness = CONVERT_RANGE(edgeThickness, CARTOON_THICK_MIN, CARTOON_THICK_MAX);
	edgeThreshold = CONVERT_RANGE(edgeThreshold, CARTOON_THRESH_MIN, CARTOON_THRESH_MAX);
	
    edgeThickness *= mScaleFactor;
    if(edgeThickness%2 == 0) edgeThickness++;

    if(edgeThickness < CARTOON_THICK_MIN)
        edgeThickness = CARTOON_THICK_MIN;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    GaussianBlur(mSrcScaled, mSrcScaled, Size(5,5), 0);
    cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);
    
    getQuantizeSteps(mSrcGray, mStepNum, mStepProbArr, mStepValArr);
    quantize(mSrcGray, mDstGray, mStepValArr, mColorCartoonLevels);
	cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);
    mDstScaled = 0.7*mSrcScaled + 0.7*mDstScaled;
    
    adaptiveThreshold(mSrcGray, mEdgesGray, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, edgeThickness, edgeThreshold);
    cvtColor(mEdgesGray, mEdges, CV_GRAY2RGBA);
    mDstScaled = mDstScaled - ~mEdges;

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Gray-Cartoon Filter Implementation */
void ImageFilters::applyGrayCartoon(Mat& src, Mat& dst, int edgeThickness, int edgeThreshold) 
{
    edgeThickness = CONVERT_RANGE(edgeThickness, CARTOON_THICK_MIN, CARTOON_THICK_MAX);
    edgeThreshold = CONVERT_RANGE(edgeThreshold, CARTOON_THRESH_MIN, CARTOON_THRESH_MAX);
    
    edgeThickness *= mScaleFactor;
    if(edgeThickness%2 == 0) edgeThickness++;

    if(edgeThickness < CARTOON_THICK_MIN)
        edgeThickness = CARTOON_THICK_MIN;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    GaussianBlur(mSrcScaled, mSrcScaled, Size(5,5), 0);
    cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);
    
    getQuantizeSteps(mSrcGray, mStepNum, mStepProbArr, mStepValArr);
    quantize(mSrcGray, mDstGray, mStepValArr, mGrayCartoonLevels);
    cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);
    
    adaptiveThreshold(mSrcGray, mEdgesGray, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, edgeThickness, edgeThreshold);
    cvtColor(mEdgesGray, mEdges, CV_GRAY2RGBA);
    mDstScaled = mDstScaled - ~mEdges;

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Color-Sketch Filter Implementation */
void ImageFilters::applyColorSketch(Mat& src, Mat& dst, int blend, int textureScale) 
{
    float blend1 = CONVERT_RANGE(blend, SKETCH_BLEND_MIN, SKETCH_BLEND_MAX);
    float textureScale1 = CONVERT_RANGE(textureScale, SKETCH_TEXSCALE_MIN, SKETCH_TEXSCALE_MAX);
    textureScale1 /= mScaleFactor;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    if(mDstScaled.size()!=mSrcScaled.size() || mDstScaled.type()!=mSrcScaled.type())
        mDstScaled.create(mSrcScaled.size(), mSrcScaled.type());
#pragma omp parallel for(default)
    for(int i=0; i< mSrcScaled.rows; i++) 
        for(int j=0; j< mSrcScaled.cols; j++) {

            Vec4b srcPixel, dstPixel;
            srcPixel = mSrcScaled.at<Vec4b>(i,j);

            int texRow = (int)(i*textureScale1) % mSketchTexture.rows;
            int texCol = (int)(j*textureScale1) % mSketchTexture.cols;
            uchar texPixel = mSketchTexture.at<uchar>(texRow, texCol)*blend1;

            dstPixel.val[0] = COLOR_DODGE_BLEND(srcPixel.val[0], texPixel);
            dstPixel.val[1] = COLOR_DODGE_BLEND(srcPixel.val[1], texPixel);
            dstPixel.val[2] = COLOR_DODGE_BLEND(srcPixel.val[2], texPixel);
            dstPixel.val[3] = srcPixel.val[3];

            mDstScaled.at<Vec4b>(i,j) = dstPixel;
        }

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}

void ImageFilters::applyIceSketch(Mat& src, Mat& dst, int blend, int textureScale)
{
    float blend1 = CONVERT_RANGE(blend, SKETCH_BLEND_MIN, SKETCH_BLEND_MAX);
    float textureScale1 = CONVERT_RANGE(textureScale, SKETCH_TEXSCALE_MIN, SKETCH_TEXSCALE_MAX);
    textureScale1 /= mScaleFactor;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    if(mDstScaled.size()!=mSrcScaled.size() || mDstScaled.type()!=mSrcScaled.type())
        mDstScaled.create(mSrcScaled.size(), mSrcScaled.type());

    cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);
#pragma omp parallel for(default)
    for(int i=0; i< mSrcScaled.rows; i++)
        for(int j=0; j< mSrcScaled.cols; j++) {

            Vec4b dstPixel;
//            srcPixel = mSrcScaled.at<Vec4b>(i,j);
            uchar srcPixel = mSrcGray.at<uchar>(i,j);
            int texRow = (int)(i*textureScale1) % mSketchTexture.rows;
            int texCol = (int)(j*textureScale1) % mSketchTexture.cols;
            uchar texPixel = mSketchTexture.at<uchar>(texRow, texCol)*blend1;

            uchar value =COLOR_DODGE_BLEND(srcPixel, texPixel);
            dstPixel.val[0] = value ;
            dstPixel.val[1] = (value * 1.2) >= 255 ? 255:value*1.2 ;
            dstPixel.val[2] = (value*1.7) >= 255 ? 255:value*1.7 ;
            dstPixel.val[3] = 255;

            mDstScaled.at<Vec4b>(i,j) = dstPixel;
        }

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}

/* Pencil-Sketch Filter Implementation */
void ImageFilters::applyPencilSketch(Mat& src, Mat& dst, int blend, int textureScale) 
{
    float blend1 = CONVERT_RANGE(blend, SKETCH_BLEND_MIN, SKETCH_BLEND_MAX);
    float textureScale1 = CONVERT_RANGE(textureScale, SKETCH_TEXSCALE_MIN, SKETCH_TEXSCALE_MAX);
    textureScale1 /= mScaleFactor;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);

    if(mDstGray.size()!=mSrcGray.size() || mDstGray.type()!=mSrcGray.type())
        mDstGray.create(mSrcGray.size(), mSrcGray.type());
#pragma omp parallel for(default)
    for(int i=0; i< mSrcGray.rows; i++) 
        for(int j=0; j< mSrcGray.cols; j++) {     

            uchar srcPixel, dstPixel, texPixel;
            srcPixel = mSrcGray.at<uchar>(i,j);
            
            int texRow = (int)(i*textureScale1) % mSketchTexture.rows;
            int texCol = (int)(j*textureScale1) % mSketchTexture.cols;
            texPixel = mSketchTexture.at<uchar>(texRow, texCol)*blend1;

            dstPixel = COLOR_DODGE_BLEND(srcPixel, texPixel);

            mDstGray.at<uchar>(i,j) = dstPixel;
        }
    cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


// /* Pencil-Sketch2 Filter Implementation */
// void ImageFilters::applyPencilSketch2(Mat& src, Mat& dst, int blurRadius, int contrast)
// {
// 	blurRadius = CONVERT_RANGE(blurRadius, SKETCH2_BLUR_MIN, SKETCH2_BLUR_MAX);
// 	float contrast1 = CONVERT_RANGE(contrast, SKETCH_CONTRAST_MIN, SKETCH_CONTRAST_MAX);

//     blurRadius *= mScaleFactor;
//     if(blurRadius%2 == 0) blurRadius++;

//     if(blurRadius < SKETCH2_BLUR_MIN)
//         blurRadius = SKETCH2_BLUR_MIN;
	
//     resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);
//     cvtColor(mSrcScaled, mSrcGray, CV_RGBA2GRAY);
//     GaussianBlur(mSrcGray, mSrcGray, Size(3,3), 0);
//     GaussianBlur(~mSrcGray, mDstGray, Size(blurRadius,blurRadius), 0);

//     for(int i=0; i< mSrcGray.rows; i++) 
//         for(int j=0; j< mSrcGray.cols; j++) {     

//             uchar srcPixel, dstPixel, blendPixel;
//             srcPixel = mSrcGray.at<uchar>(i,j);
//             blendPixel = mDstGray.at<uchar>(i,j);

//             dstPixel = COLOR_DODGE_BLEND(srcPixel, blendPixel);

//             mDstGray.at<uchar>(i,j) = dstPixel;
//         }

//     mDstGray = ~(contrast1*(~mDstGray));
// 	cvtColor(mDstGray, mDstScaled, CV_GRAY2RGBA);

//     resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
// }


/* Pixel-Art Filter Implementation */
void ImageFilters::applyPixelArt(Mat& src, Mat& dst, int pixelSize, int numColors) 
{
    pixelSize = CONVERT_RANGE(pixelSize, PIXEL_SIZE_MIN, PIXEL_SIZE_MAX);
    numColors = CONVERT_RANGE(numColors, PIXEL_COLORS_MIN, PIXEL_COLORS_MAX);

    pixelSize *= mScaleFactor;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    if(mDstScaled.size()!=mSrcScaled.size() || mDstScaled.type()!=mSrcScaled.type())
        mDstScaled.create(mSrcScaled.size(), mSrcScaled.type());

    for(int i=0; i<mSrcScaled.rows; i+=pixelSize)
        for(int j=0; j<mSrcScaled.cols; j+=pixelSize) 
        {
            int sumRed=0, sumGreen=0, sumBlue=0, sumPix=0;
            for(int pi=i; pi<i+pixelSize; pi++)
                if(pi < mSrcScaled.rows) 
                    for(int pj=j; pj<j+pixelSize; pj++)
                        if(pj < mSrcScaled.cols)
                        {
                            Vec4b srcPixel = mSrcScaled.at<Vec4b>(pi,pj);
                            sumRed += srcPixel.val[0];
                            sumGreen += srcPixel.val[1];
                            sumBlue += srcPixel.val[2];
                            sumPix++;
                        }

            Vec4b dstPixel;
            dstPixel.val[0] = sumRed/sumPix;
            dstPixel.val[1] = sumGreen/sumPix;
            dstPixel.val[2] = sumBlue/sumPix;
            dstPixel.val[3] = 255;

            for(int pi=i; pi<i+pixelSize; pi++)
                if(pi < mSrcScaled.rows) 
                    for(int pj=j; pj<j+pixelSize; pj++)
                    if(pj < mSrcScaled.cols)
                        mDstScaled.at<Vec4b>(pi,pj) = dstPixel;
        }

    uchar qsteps[numColors];
    for(int i=0; i<numColors; i++)
        qsteps[i] = 255*(i+1)/numColors;

    //uchar qsteps[] = {30, 60, 90, 120, 150, 180, 210, 240, 255};
    //uchar qvals[]  = { 0, 45, 75, 105, 135, 165, 195, 225, 255};
    quantize(mDstScaled, mDstScaled, qsteps, qsteps);

    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Oil-Paint Filter Implementation */
void ImageFilters::applyOilPaint(Mat& src, Mat& dst, int radius, int levels) 
{	
	radius = CONVERT_RANGE(radius, OILPAINT_RADIUS_MIN, OILPAINT_RADIUS_MAX);
	levels = CONVERT_RANGE(levels, OILPAINT_LEVELS_MIN, OILPAINT_LEVELS_MAX);
	
    int intensityHist[levels], totalRed[levels], totalGreen[levels], totalBlue[levels];
	
    radius *= mScaleFactor;
    if(radius == 0) radius=1;

    resize(src, mSrcScaled, Size(), mScaleFactor, mScaleFactor, CV_INTER_LINEAR);

    mSrcScaled = mSrcScaled*0.8+30;

    if(mDstScaled.size()!=mSrcScaled.size() || mDstScaled.type()!=mSrcScaled.type())
        mDstScaled.create(mSrcScaled.size(), mSrcScaled.type());

    for(int row=0; row<mSrcScaled.rows; ++row) 
    {
        for(int i=0; i<levels; i++)
            intensityHist[i] = totalRed[i] = totalGreen[i] = totalBlue[i] = 0;
    
		Vec4b *dstRowPtr = mDstScaled.ptr<Vec4b>(row);
        int winRowMin = ((row-radius) < 0) ? 0:(row-radius);
        int winRowMax = ((row+radius) >= mSrcScaled.rows) ? (mSrcScaled.rows-1):(row+radius);

        for(int winRow=winRowMin; winRow<=winRowMax; ++winRow) 
        {
            Vec4b *srcRowPtr = mSrcScaled.ptr<Vec4b>(winRow);
            for(int winCol=0; winCol<radius; ++winCol) 
            {
                Vec4b pix = srcRowPtr[winCol];
                int red = (int)pix.val[0];
                int green = (int)pix.val[1];
                int blue = (int)pix.val[2];

                int level = ((red+green+blue)*levels)/768;
                intensityHist[level]++;
                totalRed[level] += red;
                totalGreen[level] += green;
                totalBlue[level] += blue;
            }
        }
		
        for(int col=0; col<mSrcScaled.cols; ++col)
        {
            for(int winRow=winRowMin; winRow<=winRowMax; ++winRow) 
            {
                Vec4b *srcRowPtr = mSrcScaled.ptr<Vec4b>(winRow);

                if(col-radius >= 0) {
                    Vec4b pix = srcRowPtr[col-radius];
                    int red = (int)pix.val[0];
                    int green = (int)pix.val[1];
                    int blue = (int)pix.val[2];

                    int level = ((red+green+blue)*levels)/768;
                    intensityHist[level]--;
                    totalRed[level] -= red;
                    totalGreen[level] -= green;
                    totalBlue[level] -= blue;
                }

                if(col+radius < mSrcScaled.cols) {
                    Vec4b pix = srcRowPtr[col+radius];
                    int red = (int)pix.val[0];
                    int green = (int)pix.val[1];
                    int blue = (int)pix.val[2];

                    int level = ((red+green+blue)*levels)/768;
                    intensityHist[level]++;
                    totalRed[level] += red;
                    totalGreen[level] += green;
                    totalBlue[level] += blue;
                }
            }

            Vec4b dstPix;
            int maxLevel, maxIntensity = 0;
            for(int i=0; i<levels; i++)
                if(intensityHist[i]>maxIntensity) 
                {
                    maxIntensity = intensityHist[i];
                    maxLevel = i;
                }

            dstPix.val[0] = (uchar)(totalRed[maxLevel] / intensityHist[maxLevel]);
            dstPix.val[1] = (uchar)(totalGreen[maxLevel] / intensityHist[maxLevel]);
            dstPix.val[2] = (uchar)(totalBlue[maxLevel] / intensityHist[maxLevel]);
            dstPix.val[3] = 255;
            dstRowPtr[col] = dstPix;
        }
    }
    resize(mDstScaled, dst, src.size(), 0, 0, CV_INTER_LINEAR);
}


/* Get Quantization step levels based on given step probabilities */
void ImageFilters::getQuantizeSteps(Mat& src, int stepNum, float* stepProbArr, uchar* stepValArr) {
    Mat hist;
    int histSize = 256;

    float range[] = { 0, 256 } ;
    const float* histRange = { range };

    calcHist(&src, 1, 0, Mat(), hist, 1, &histSize, &histRange, true, false);

    float sumHist = 0.0f;
    for(int i=0; i<histSize; i++)
        sumHist += hist.at<float>(i,0);
    hist = hist/sumHist;

    float stepProb = 0.0f;
    int stepIndex = 0;
    for(int i=0; i<histSize; i++) {
        stepProb += hist.at<float>(i,0);
        if(stepProb >= stepProbArr[stepIndex]) {
            stepValArr[stepIndex++]=i;
            stepProb = 0.0f;
        }
    }
    for(int i=stepIndex; i<stepNum; i++)
        stepValArr[i] = 255;
}


/* Quantize the src image into dst, using given step boundaries and dst pixel values */
void ImageFilters::quantize(Mat& src, Mat& dst, uchar* stepValArr, uchar* dstValArr) 
{
    uchar buffer[256];
    int j=0;
    for(int i=0; i!=256; ++i) {
        if(i > stepValArr[j])
            j++;
        buffer[i] = dstValArr[j];
    } 
    Mat table(1, 256, CV_8U, buffer, sizeof(buffer));
    LUT(src, table, dst);
}

void removePepperNoise(Mat &mask)
{
    // For simplicity, ignore the top & bottom row border.
    for (int y = 2; y < mask.rows - 2; y++) {
        // Get access to each of the 5 rows near this pixel.
        uchar *pThis = mask.ptr(y);
        uchar *pUp1 = mask.ptr(y - 1);
        uchar *pUp2 = mask.ptr(y - 2);
        uchar *pDown1 = mask.ptr(y + 1);
        uchar *pDown2 = mask.ptr(y + 2);

        // For simplicity, ignore the left & right row border.
        pThis += 2;
        pUp1 += 2;
        pUp2 += 2;
        pDown1 += 2;
        pDown2 += 2;
        for (int x = 2; x < mask.cols - 2; x++) {
            uchar v = *pThis;   // Get the current pixel value (either 0 or 255).
            // If the current pixel is black, but all the pixels on the 2-pixel-radius-border are white
            // (ie: it is a small island of black pixels, surrounded by white), then delete that island.
            if (v == 0) {
                bool allAbove = *(pUp2 - 2) && *(pUp2 - 1) && *(pUp2) && *(pUp2 + 1) && *(pUp2 + 2);
                bool allLeft = *(pUp1 - 2) && *(pThis - 2) && *(pDown1 - 2);
                bool allBelow = *(pDown2 - 2) && *(pDown2 - 1) && *(pDown2) && *(pDown2 + 1) && *(pDown2 + 2);
                bool allRight = *(pUp1 + 2) && *(pThis + 2) && *(pDown1 + 2);
                bool surroundings = allAbove && allLeft && allBelow && allRight;
                if (surroundings == true) {
                    // Fill the whole 5x5 block as white. Since we know the 5x5 borders
                    // are already white, just need to fill the 3x3 inner region.
                    *(pUp1 - 1) = 255;
                    *(pUp1 + 0) = 255;
                    *(pUp1 + 1) = 255;
                    *(pThis - 1) = 255;
                    *(pThis + 0) = 255;
                    *(pThis + 1) = 255;
                    *(pDown1 - 1) = 255;
                    *(pDown1 + 0) = 255;
                    *(pDown1 + 1) = 255;
                }
                // Since we just covered the whole 5x5 block with white, we know the next 2 pixels
                // won't be black, so skip the next 2 pixels on the right.
                pThis += 2;
                pUp1 += 2;
                pUp2 += 2;
                pDown1 += 2;
                pDown2 += 2;
            }
            // Move to the next pixel.
            pThis++;
            pUp1++;
            pUp2++;
            pDown1++;
            pDown2++;
        }
    }
}

void ImageFilters::cartoon_filter_gray(Mat srcColor, Mat dst, int param)
{
    param = CONVERT_RANGE(param, 20, 200);

    Mat srcGray;

    cvtColor(srcColor, srcGray, CV_RGBA2GRAY);

    // Remove the pixel noise with a good Median filter, before we start detecting edges.
    medianBlur(srcGray, srcGray, 7);

    Size size = srcColor.size();
    Mat mask = Mat(size, CV_8U);
    Mat edges = Mat(size, CV_8U);

    // Generate a nice edge mask, similar to a pencil line drawing.
    Laplacian(srcGray, edges, CV_8U, 5);
    threshold(edges, mask, param, 255, THRESH_BINARY_INV);

    // Mobile cameras usually have lots of noise, so remove small dots of black noise from the black & white edge mask.
    removePepperNoise(mask);
    // For sketch mode, we just need the mask!
    // The output image has 3 channels, not a single channel.
    cvtColor(mask, dst, CV_GRAY2RGBA);
}

////////////////DomainFilter Region///////////////////////////////
double myinf = std::numeric_limits<double>::infinity();

void Domain_Filter_our::diffx(const Mat &img, Mat &temp)
{
    int channel = img.channels();

    for (int i = 0; i < img.size().height; i++)
        for (int j = 0; j < img.size().width - 1; j++)
        {
            for (int c = 0; c < channel; c++)
            {
                temp.at<float>(i, j*channel + c) =
                        img.at<float>(i, (j + 1)*channel + c) - img.at<float>(i, j*channel + c);
            }
        }
}

void Domain_Filter_our::diffy(const Mat &img, Mat &temp)
{
    int channel = img.channels();

    for (int i = 0; i < img.size().height - 1; i++)
        for (int j = 0; j < img.size().width; j++)
        {
            for (int c = 0; c < channel; c++)
            {
                temp.at<float>(i, j*channel + c) =
                        img.at<float>((i + 1), j*channel + c) - img.at<float>(i, j*channel + c);
            }
        }
}

void Domain_Filter_our::getGradientx(const Mat &img, Mat &gx)
{
    int w = img.cols;
    int h = img.rows;
    int channel = img.channels();

    for (int i = 0; i < h; i++)
        for (int j = 0; j < w; j++)
            for (int c = 0; c < channel; ++c)
            {
                gx.at<float>(i, j*channel + c) =
                        img.at<float>(i, (j + 1)*channel + c) - img.at<float>(i, j*channel + c);
            }
}

void Domain_Filter_our::getGradienty(const Mat &img, Mat &gy)
{
    int w = img.cols;
    int h = img.rows;
    int channel = img.channels();

    for (int i = 0; i < h; i++)
        for (int j = 0; j < w; j++)
            for (int c = 0; c < channel; ++c)
            {
                gy.at<float>(i, j*channel + c) =
                        img.at<float>(i + 1, j*channel + c) - img.at<float>(i, j*channel + c);

            }
}

void Domain_Filter_our::find_magnitude(Mat &img, Mat &mag)
{
    int h = img.rows;
    int w = img.cols;

    vector <Mat> planes;
    split(img, planes);

    Mat magXR = Mat(h, w, CV_32FC1);
    Mat magYR = Mat(h, w, CV_32FC1);

    Mat magXG = Mat(h, w, CV_32FC1);
    Mat magYG = Mat(h, w, CV_32FC1);

    Mat magXB = Mat(h, w, CV_32FC1);
    Mat magYB = Mat(h, w, CV_32FC1);

    Sobel(planes[0], magXR, CV_32FC1, 1, 0, 3);
    Sobel(planes[0], magYR, CV_32FC1, 0, 1, 3);

    Sobel(planes[1], magXG, CV_32FC1, 1, 0, 3);
    Sobel(planes[1], magYG, CV_32FC1, 0, 1, 3);

    Sobel(planes[2], magXB, CV_32FC1, 1, 0, 3);
    Sobel(planes[2], magYB, CV_32FC1, 0, 1, 3);

    Mat mag1 = Mat(h, w, CV_32FC1);
    Mat mag2 = Mat(h, w, CV_32FC1);
    Mat mag3 = Mat(h, w, CV_32FC1);

    magnitude(magXR, magYR, mag1);
    magnitude(magXG, magYG, mag2);
    magnitude(magXB, magYB, mag3);

    mag = mag1 + mag2 + mag3;
    mag = 1.0f - mag;
}

void Domain_Filter_our::compute_Rfilter(Mat &output, Mat &hz, float sigma_h)
{
    int h = output.rows;
    int w = output.cols;
    int channel = output.channels();

    float a = (float)exp((-1.0 * sqrt(2.0)) / sigma_h);

    Mat temp = Mat(h, w, CV_32FC3);

    output.copyTo(temp);
    Mat V = Mat(h, w, CV_32FC1);

    for (int i = 0; i < h; i++)
        for (int j = 0; j < w; j++)
            V.at<float>(i, j) = pow(a, hz.at<float>(i, j));

    for (int i = 0; i < h; i++)
    {
        for (int j = 1; j < w; j++)
        {
            for (int c = 0; c < channel; c++)
            {
                temp.at<float>(i, j*channel + c) = temp.at<float>(i, j*channel + c) +
                                                   (temp.at<float>(i, (j - 1)*channel + c) - temp.at<float>(i, j*channel + c)) * V.at<float>(i, j);
            }
        }
    }

    for (int i = 0; i < h; i++)
    {
        for (int j = w - 2; j >= 0; j--)
        {
            for (int c = 0; c < channel; c++)
            {
                temp.at<float>(i, j*channel + c) = temp.at<float>(i, j*channel + c) +
                                                   (temp.at<float>(i, (j + 1)*channel + c) - temp.at<float>(i, j*channel + c))*V.at<float>(i, j + 1);
            }
        }
    }

    temp.copyTo(output);
}

void Domain_Filter_our::compute_boxfilter(Mat &output, Mat &hz, Mat &psketch, float radius)
{
    int h = output.rows;
    int w = output.cols;
    Mat lower_pos = Mat(h, w, CV_32FC1);
    Mat upper_pos = Mat(h, w, CV_32FC1);

    lower_pos = hz - radius;
    upper_pos = hz + radius;

    lower_idx = Mat::zeros(h, w, CV_32FC1);
    upper_idx = Mat::zeros(h, w, CV_32FC1);

    Mat domain_row = Mat::zeros(1, w + 1, CV_32FC1);

    for (int i = 0; i < h; i++)
    {
        for (int j = 0; j < w; j++)
            domain_row.at<float>(0, j) = hz.at<float>(i, j);
        domain_row.at<float>(0, w) = (float)myinf;

        Mat lower_pos_row = Mat::zeros(1, w, CV_32FC1);
        Mat upper_pos_row = Mat::zeros(1, w, CV_32FC1);

        for (int j = 0; j < w; j++)
        {
            lower_pos_row.at<float>(0, j) = lower_pos.at<float>(i, j);
            upper_pos_row.at<float>(0, j) = upper_pos.at<float>(i, j);
        }

        Mat temp_lower_idx = Mat::zeros(1, w, CV_32FC1);
        Mat temp_upper_idx = Mat::zeros(1, w, CV_32FC1);

        for (int j = 0; j<w; j++)
        {
            if (domain_row.at<float>(0, j) > lower_pos_row.at<float>(0, 0))
            {
                temp_lower_idx.at<float>(0, 0) = (float)j;
                break;
            }
        }
        for (int j = 0; j<w; j++)
        {
            if (domain_row.at<float>(0, j) > upper_pos_row.at<float>(0, 0))
            {
                temp_upper_idx.at<float>(0, 0) = (float)j;
                break;
            }
        }

        int temp = 0;
        for (int j = 1; j < w; j++)
        {
            int count = 0;
            for (int k = (int)temp_lower_idx.at<float>(0, j - 1); k < w + 1; k++)
            {
                if (domain_row.at<float>(0, k) > lower_pos_row.at<float>(0, j))
                {
                    temp = count;
                    break;
                }
                count++;
            }

            temp_lower_idx.at<float>(0, j) = temp_lower_idx.at<float>(0, j - 1) + temp;

            count = 0;
            for (int k = (int)temp_upper_idx.at<float>(0, j - 1); k < w + 1; k++)
            {


                if (domain_row.at<float>(0, k) > upper_pos_row.at<float>(0, j))
                {
                    temp = count;
                    break;
                }
                count++;
            }

            temp_upper_idx.at<float>(0, j) = temp_upper_idx.at<float>(0, j - 1) + temp;
        }

        for (int j = 0; j < w; j++)
        {
            lower_idx.at<float>(i, j) = temp_lower_idx.at<float>(0, j) + 1;
            upper_idx.at<float>(i, j) = temp_upper_idx.at<float>(0, j) + 1;
        }

    }
    psketch = upper_idx - lower_idx;
}
void Domain_Filter_our::compute_NCfilter(Mat &output, Mat &hz, Mat &psketch, float radius)
{
    int h = output.rows;
    int w = output.cols;
    int channel = output.channels();

    compute_boxfilter(output, hz, psketch, radius);

    Mat box_filter = Mat::zeros(h, w + 1, CV_32FC3);

    for (int i = 0; i < h; i++)
    {
        box_filter.at<float>(i, 1 * channel + 0) = output.at<float>(i, 0 * channel + 0);
        box_filter.at<float>(i, 1 * channel + 1) = output.at<float>(i, 0 * channel + 1);
        box_filter.at<float>(i, 1 * channel + 2) = output.at<float>(i, 0 * channel + 2);
        for (int j = 2; j < w + 1; j++)
        {
            for (int c = 0; c < channel; c++)
                box_filter.at<float>(i, j*channel + c) = output.at<float>(i, (j - 1)*channel + c) + box_filter.at<float>(i, (j - 1)*channel + c);
        }
    }

    Mat indices = Mat::zeros(h, w, CV_32FC1);
    Mat final = Mat::zeros(h, w, CV_32FC3);

    for (int i = 0; i < h; i++)
        for (int j = 0; j < w; j++)
            indices.at<float>(i, j) = (float)i + 1;

    Mat a = Mat::zeros(h, w, CV_32FC1);
    Mat b = Mat::zeros(h, w, CV_32FC1);

    // Compute the box filter using a summed area table.
    for (int c = 0; c < channel; c++)
    {
        Mat flag = Mat::ones(h, w, CV_32FC1);
        multiply(flag, c + 1, flag);

        Mat temp1, temp2;
        multiply(flag - 1, h*(w + 1), temp1);
        multiply(lower_idx - 1, h, temp2);
        a = temp1 + temp2 + indices;

        multiply(flag - 1, h*(w + 1), temp1);
        multiply(upper_idx - 1, h, temp2);
        b = temp1 + temp2 + indices;

        int p, q, r, rem;
        int p1, q1, r1, rem1;

        // Calculating indices
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {

                r = (int)b.at<float>(i, j) / (h*(w + 1));
                rem = (int)b.at<float>(i, j) - r*h*(w + 1);
                q = rem / h;
                p = rem - q*h;
                if (q == 0)
                {
                    p = h;
                    q = w;
                    r = r - 1;
                }
                if (p == 0)
                {
                    p = h;
                    q = q - 1;
                }

                r1 = (int)a.at<float>(i, j) / (h*(w + 1));
                rem1 = (int)a.at<float>(i, j) - r1*h*(w + 1);
                q1 = rem1 / h;
                p1 = rem1 - q1*h;
                if (p1 == 0)
                {
                    p1 = h;
                    q1 = q1 - 1;
                }

                final.at<float>(i, j*channel + 2 - c) = (box_filter.at<float>(p - 1, q*channel + (2 - r)) - box_filter.at<float>(p1 - 1, q1*channel + (2 - r1)))
                                                        / (upper_idx.at<float>(i, j) - lower_idx.at<float>(i, j));
            }
        }
    }

    final.copyTo(output);
}
void Domain_Filter_our::init(const Mat &img, int flags, float sigma_s, float sigma_r)
{
    int h = img.size().height;
    int w = img.size().width;
    int channel = img.channels();

    ////////////////////////////////////     horizontal and vertical partial derivatives /////////////////////////////////

    Mat derivx = Mat::zeros(h, w - 1, CV_32FC3);
    Mat derivy = Mat::zeros(h - 1, w, CV_32FC3);

    diffx(img, derivx);
    diffy(img, derivy);

    Mat distx = Mat::zeros(h, w, CV_32FC1);
    Mat disty = Mat::zeros(h, w, CV_32FC1);

    //////////////////////// Compute the l1-norm distance of neighbor pixels ////////////////////////////////////////////////

    for (int i = 0; i < h; i++)
        for (int j = 0, k = 1; j < w - 1; j++, k++)
            for (int c = 0; c < channel; c++)
            {
                distx.at<float>(i, k) =
                        distx.at<float>(i, k) + abs(derivx.at<float>(i, j*channel + c));
            }

    for (int i = 0, k = 1; i < h - 1; i++, k++)
        for (int j = 0; j < w; j++)
            for (int c = 0; c < channel; c++)
            {
                disty.at<float>(k, j) =
                        disty.at<float>(k, j) + abs(derivy.at<float>(i, j*channel + c));
            }

    ////////////////////// Compute the derivatives of the horizontal and vertical domain transforms. /////////////////////////////

    horiz = Mat(h, w, CV_32FC1);
    vert = Mat(h, w, CV_32FC1);

    Mat final = Mat(h, w, CV_32FC3);

    Mat tempx, tempy;
    multiply(distx, sigma_s / sigma_r, tempx);
    multiply(disty, sigma_s / sigma_r, tempy);

    horiz = 1.0f + tempx;
    vert = 1.0f + tempy;

    O = Mat(h, w, CV_32FC3);
    img.copyTo(O);

    O_t = Mat(w, h, CV_32FC3);

    if (flags == 2)
    {

        ct_H = Mat(h, w, CV_32FC1);
        ct_V = Mat(h, w, CV_32FC1);

        for (int i = 0; i < h; i++)
        {
            ct_H.at<float>(i, 0) = horiz.at<float>(i, 0);
            for (int j = 1; j < w; j++)
            {
                ct_H.at<float>(i, j) = horiz.at<float>(i, j) + ct_H.at<float>(i, j - 1);
            }
        }

        for (int j = 0; j < w; j++)
        {
            ct_V.at<float>(0, j) = vert.at<float>(0, j);
            for (int i = 1; i < h; i++)
            {
                ct_V.at<float>(i, j) = vert.at<float>(i, j) + ct_V.at<float>(i - 1, j);
            }
        }
    }

}

void Domain_Filter_our::filter(const Mat &img, Mat &res, float sigma_s = 60, float sigma_r = 0.4, int flags = 1)
{
    int no_of_iter = 3;
    int h = img.size().height;
    int w = img.size().width;
    float sigma_h = sigma_s;

    init(img, flags, sigma_s, sigma_r);

    if (flags == 1)
    {
        Mat vert_t = vert.t();

        for (int i = 0; i < no_of_iter; i++)
        {
            sigma_h = (float)(sigma_s * sqrt(3.0) * pow(2.0, (no_of_iter - (i + 1))) / sqrt(pow(4.0, no_of_iter) - 1));

            compute_Rfilter(O, horiz, sigma_h);

            O_t = O.t();

            compute_Rfilter(O_t, vert_t, sigma_h);

            O = O_t.t();

        }
    }
    else if (flags == 2)
    {

        Mat vert_t = ct_V.t();
        Mat temp = Mat(h, w, CV_32FC1);
        Mat temp1 = Mat(w, h, CV_32FC1);

        float radius;

        for (int i = 0; i < no_of_iter; i++)
        {
            sigma_h = (float)(sigma_s * sqrt(3.0) * pow(2.0, (no_of_iter - (i + 1))) / sqrt(pow(4.0, no_of_iter) - 1));

            radius = (float)sqrt(3.0) * sigma_h;

            compute_NCfilter(O, ct_H, temp, radius);

            O_t = O.t();

            compute_NCfilter(O_t, vert_t, temp1, radius);

            O = O_t.t();
        }
    }

    res = O.clone();
}

void Domain_Filter_our::pencil_sketch(const Mat &img, Mat &sketch, Mat &color_res, float sigma_s, float sigma_r, float shade_factor)
{

    int no_of_iter = 3;
    init(img, 2, sigma_s, sigma_r);
    int h = img.size().height;
    int w = img.size().width;

    /////////////////////// convert to YCBCR model for color pencil drawing //////////////////////////////////////////////////////

    Mat color_sketch = Mat(h, w, CV_32FC3);

    cvtColor(img, color_sketch, COLOR_BGR2YCrCb);

    vector <Mat> YUV_channel;
    Mat vert_t = ct_V.t();

    float sigma_h = sigma_s;

    Mat penx = Mat(h, w, CV_32FC1);

    Mat pen_res = Mat::zeros(h, w, CV_32FC1);
    Mat peny = Mat(w, h, CV_32FC1);

    Mat peny_t;

    float radius;

    for (int i = 0; i < no_of_iter; i++)
    {
        sigma_h = (float)(sigma_s * sqrt(3.0) * pow(2.0, (no_of_iter - (i + 1))) / sqrt(pow(4.0, no_of_iter) - 1));

        radius = (float)sqrt(3.0) * sigma_h;

        compute_boxfilter(O, ct_H, penx, radius);

        O_t = O.t();

        compute_boxfilter(O_t, vert_t, peny, radius);

        O = O_t.t();

        peny_t = peny.t();

        for (int k = 0; k < h; k++)
            for (int j = 0; j < w; j++)
                pen_res.at<float>(k, j) = (shade_factor * (penx.at<float>(k, j) + peny_t.at<float>(k, j)));

        if (i == 0)
        {
            sketch = pen_res.clone();
            split(color_sketch, YUV_channel);
            pen_res.copyTo(YUV_channel[0]);
            merge(YUV_channel, color_sketch);
            cvtColor(color_sketch, color_res, COLOR_YCrCb2BGR);
        }

    }
}