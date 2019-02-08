#include "grabcutter_p.h"
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>


namespace  Vision {

int GrabcutData::_gcFirstRunResolution = 128;
int GrabcutData::_gcCorrectionResolution = 256;
std::vector<cv::Point2f> GrabcutData::_oldContours;
cv::Mat GrabcutData::_croppedImage;
cv::Mat GrabcutData::_resizedImage;
cv::Mat GrabcutData::_userdefinedBackground;
cv::Mat GrabcutData::_userdefinedForeground;

int Config::innerLineWidth = 11;
int Config::outerLineWidth = 1;
int Config::endPointMaskSize = 10;
bool Config::writeDebugImage = false;

typedef std::vector<std::vector<cv::Point>> CvContours;

using namespace std;
using namespace cv;



/**
 * @brief normalizeContours Konturen von der Bildauflösung unabhängig machen
 * @details Die Werte in c (im Wertebereich von 0/0 bis img.cols/img.rows) werden
 * auf einen Wertebereich von (-0.5*aspectRatio/-0.5 bis 0.5*aspectRatio/0.5 gebracht,
 * (mit aspectRatio=img.cols/img.rows). Das heisst sie werden normalisiert und um 0 zentriert
 * um von Auflösung und aspectratio unabhängig zu sein.
 * @param c Die Eingangskontur im Wertebereich img.cols/img.rows
 * @param c Die Ausgangskontur im Wertebereich -0.5*aspectRatio/-0.5 bis 0.5*aspectRatio/0.5
 */
void normalizeContours(const Mat& img, vector<Point2f> &c, vector<Point2f> & outC)
{
	float factor =  1.0f / (float)img.rows;

	outC.reserve((int)c.size());
	for(size_t i = 0; i < c.size(); i++) {
		float centerx = c[i].x - (float)img.cols/2.0;
		float x = centerx * factor;
		float y = c[i].y * factor - 0.5;
		outC.push_back(cv::Point2f(x,y));
	}
}

// siehe header file
void denormalizeContours(const cv::Mat& img,
						  const std::vector<cv::Point2f>& contours,
						 std::vector<std::vector<cv::Point>> *outPaths)
{
	float factor = (float)img.rows;

	outPaths->push_back(std::vector<cv::Point>());
	outPaths->front().reserve(contours.size()); //- "allocator<T>::allocate(size_t n) 'n' exceeds maximum supported size"
	for(int j = 0; j < contours.size(); j++) {
		float x = contours[j].x * factor + (float)img.cols/2.0;
		float y = (contours[j].y + 0.5) * factor;
		outPaths->front().push_back(cv::Point(round(x),round(y)));
	}
}

Point denormalizePoint(const cv::Mat& img,cv::Point2f point)
{
	float factor = (float)img.rows;
	float x = point.x * factor + (float)img.cols/2.0;
	float y = (point.y + 0.5) * factor;
	Point result = Point(round(x),round(y));
	return result;
}

void writeDebugImage(const std::string& title, const cv::Mat& img)
{
	if (Config::writeDebugImage) {
		cv::Mat tmp, greenImg, resizedBgr, result;

		cv::cvtColor(GrabcutData::_resizedImage, resizedBgr, cv::COLOR_Lab2BGR);
		cv::cvtColor(img, tmp, cv::COLOR_GRAY2BGR, 0);
		cv::multiply(tmp, cv::Scalar(0.0, 0.5, 0.0), greenImg);
		cv::Mat invGreen; cv::subtract(Scalar(255, 255, 255), greenImg, invGreen);

		cv::multiply(resizedBgr, invGreen, result, 1.0/255.0);
		cv::add(result, greenImg, result);
		//cv::imwrite(title, result);
	}
}

void writeDebugImageCol(const std::string& title, const cv::Mat& img)
{
	if (Config::writeDebugImage) {
		cv::Mat resizedBgr;
		cv::cvtColor(GrabcutData::_resizedImage, resizedBgr, cv::COLOR_Lab2BGR);
		//cv::imwrite(title, resizedBgr/2 + img/2);
	}
}



/**
 * @brief drawPaths Die alte und neue Kontur zeichnen und eine Grabcut taugliche Maske damit machen
 * @details
 * @param img
 * @param contDat
 * @param oldContDat
 */
void drawPaths(cv::Mat& img, const vector<Point2f>& contDat, const vector<Point2f>& oldContDat,
			   Point2f startPointNorm, Point2f endPointNorm)
{
	// oldPaths are the paths before the editing
	// we want to work only in the different area
	vector<vector<Point>> cvPaths;
	vector<vector<Point>> oldCvPaths;
	denormalizeContours(img, contDat, &cvPaths);
	denormalizeContours(img, oldContDat, &oldCvPaths);

	//  denormalize startPoint and EndPoint
	Point startPoint = denormalizePoint(img, startPointNorm);
	Point endPoint = denormalizePoint(img, endPointNorm);

	if (cvPaths.size() > 0 && oldCvPaths.size() > 0) {
		cv::Mat oldC = cv::Mat::zeros(img.size(), CV_8UC1);
		cv::Mat newC = cv::Mat::zeros(img.size(), CV_8UC1);
		cv::Mat diffC = cv::Mat::zeros(img.size(), CV_16SC1);

		// draw both contours in an image
		cv::fillPoly(oldC, oldCvPaths, 1);
		cv::fillPoly(newC, cvPaths, 1);

		// difference of old and new contour, added areas are 1,
		// subtracted areas are -1
		cv::subtract(newC, oldC, diffC, noArray(), CV_16SC1);

		writeDebugImage("../oldC.png", oldC*256);
		writeDebugImage("../newC.png", newC*256);
		cv::Mat diffCPos = (diffC+1.0) * 128;
		cv::Mat diffCPos8U;diffCPos.convertTo(diffCPos8U, CV_8U );
		cv::Mat diffCPosCol;
		applyColorMap(diffCPos8U, diffCPosCol, COLORMAP_JET);
		writeDebugImageCol("../diffC.png", diffCPosCol);

		// make mask for added and subtracted areas
		// sureBgMask ist wo im alten was war, was im neuen nicht ist
		cv::Mat sureBgMask; cv::compare(diffC, cv::Scalar(0), sureBgMask, cv::CMP_LT);
		// probFgMas ist wo im neuen was ist, was im alten nicht war
		cv::Mat probFgMask; cv::compare(diffC, cv::Scalar(0), probFgMask, cv::CMP_GT);

		writeDebugImage("../sureBgMask.png", sureBgMask);
		writeDebugImage("../probFgMask.png", probFgMask);

		const int innerSureFgWidth = Config::innerLineWidth;
		Mat element = getStructuringElement( cv::MORPH_ELLIPSE, Size( innerSureFgWidth, innerSureFgWidth ) );
		const int outerSureFgWidth = Config::outerLineWidth;
		Mat smallElement = getStructuringElement( cv::MORPH_ELLIPSE, Size( outerSureFgWidth, outerSureFgWidth ) );

		// Dreck weg
		/// @note Peter/Markus War dazu da um Probleme im ContourItem zu fixen
		/// hat eh nicht immer funktionert
		//cv::morphologyEx(sureBgMask, sureBgMask, cv::MORPH_OPEN, smallElement);
		//cv::morphologyEx(probFgMask, probFgMask, cv::MORPH_OPEN, smallElement);

		writeDebugImage("../sureBgMask.png", sureBgMask);
		writeDebugImage("../probFgMask.png", probFgMask);

		// probable Fg Mask gscheit ausdehnen, mit alter Kontur schneiden, um einen
		// sicheren Vordergrund zu kriegen

		cv::Mat biggerProbFg;
		cv::Mat alsoSureFg;
		cv::dilate(probFgMask, biggerProbFg, element);
		cv::multiply(oldC, biggerProbFg, alsoSureFg);
		//cv::erode(alsoSureFg, alsoSureFg, erodeelement);

		// alte Kontur bissl ausdehnen, mit probable Fg schneiden, gibt einen hoffentlich
		// sicheren Vordergrund auf der alten Kante, dass die nicht wieder gfunden wird
		cv::Mat biggerOldC, alsoSureFg2;
		cv::dilate(oldC, biggerOldC, smallElement);
		// dort, wo die aufgedehnte alte Kontur und die hinzugefügten Teile der neuen Kontur gleichzeitig existieren
		// (nur ein Streifen entlang der alten Kontur zwischen Start-und Endpunkt der Korrektur)
		// wird sicherer Vordergrund gesetzt
		cv::multiply(probFgMask, biggerOldC, alsoSureFg2);

		writeDebugImage("../alsoSureFgInner.png", alsoSureFg);
		writeDebugImage("../alsoSureFgOuter.png", alsoSureFg2);

		// die beiden sicheren Vordergründe vereinen
		cv::add(alsoSureFg, alsoSureFg2, alsoSureFg);

		//  delete sureFgMask around startPOint and endpoint
		int endPointMaskSize = Config::endPointMaskSize;
		cv::circle(alsoSureFg, startPoint, endPointMaskSize, Scalar(0), -1);
		cv::circle(alsoSureFg, endPoint, endPointMaskSize, Scalar(0), -1);

		writeDebugImage("../biggerProbFg.png", biggerProbFg);
		writeDebugImage("../alsoSureFg.png", alsoSureFg);

		// sicheren Background für immer speichern
		GrabcutData::_userdefinedBackground += sureBgMask;
		// was ma editiert haben, wieder löschenb
		GrabcutData::_userdefinedBackground -= probFgMask;

		// sicheren Vordergrund für immer speichern
		GrabcutData::_userdefinedForeground += alsoSureFg;
		// was ma editiert haben wieder löschen
		GrabcutData::_userdefinedForeground -= sureBgMask;

		writeDebugImage("../_userdefinedBackground.png", GrabcutData::_userdefinedBackground);
		writeDebugImage("../_userdefinedForeground.png", GrabcutData::_userdefinedForeground);

		// Maske erstellen:

		// draw everything with sure background
		cv::Rect r = cv::Rect(0, 0, img.cols, img.rows);
		cv::rectangle(img, r, cv::GC_BGD, cv::FILLED);
		// prob fg where old contour was
		img.setTo(cv::Scalar(GC_PR_FGD), oldC);
		// draw probable foreground in the positive  difference areas
		img.setTo(cv::Scalar(cv::GC_PR_FGD), probFgMask);
		// sure fg at edge between old contour and new when there was adding
		img.setTo(cv::Scalar(GC_FGD), GrabcutData::_userdefinedForeground);
		// draw user defined background in the end, it should never be overwritten.
		img.setTo(cv::Scalar(cv::GC_BGD), GrabcutData::_userdefinedBackground);

		cv::Mat tmp;
		applyColorMap(img*85, tmp, COLORMAP_JET);
		writeDebugImageCol("../Mask.png", tmp );

	}
}


void initFirstMask(cv::Mat& mask) {
	// initialially mark everything as sure bg
	cv::Rect r = cv::Rect(0, 0, mask.cols, mask.rows);
	cv::rectangle(mask, r, cv::GC_BGD, cv::FILLED);
	// draw a rectangle in the middle, probably foreground
	cv::Rect r2 = cv::Rect(3, 3, mask.cols-6, mask.rows-6);
	cv::rectangle(mask, r2, cv::GC_PR_FGD, cv::FILLED);
}

void initSecondMask(cv::Mat& mask, const vector<Point2f>& contDat) {
	// draw contourdat
	vector<vector<Point>> cvPaths;
	denormalizeContours(mask, contDat, &cvPaths);
	cv::Mat firstPassContours = cv::Mat::zeros(mask.size(), CV_8UC1);
	cv::Mat sureFg = cv::Mat::zeros(mask.size(), CV_8UC1);
	cv::Mat probFgAndSureBg = cv::Mat::zeros(mask.size(), CV_8UC1);

	// draw contours in an image
	cv::fillPoly(firstPassContours, cvPaths, 255);
	// dilate and erode
	Mat element = getStructuringElement( cv::MORPH_ELLIPSE, Size( 9, 9 ) );
	// smaller version of contourdat as sure fg
	cv::erode(firstPassContours, sureFg, element);

	// bigger version of contourdat as probable fg, the rest sure bg
	cv::dilate(firstPassContours, probFgAndSureBg, element);
	cv::threshold(probFgAndSureBg, probFgAndSureBg, 128, cv::GC_PR_FGD, cv::THRESH_BINARY);

	//combine, first probFgAndSureBg, then draw sure fg (which is a subset of prob Fg) over it
	mask = probFgAndSureBg;
	mask.setTo(cv::GC_FGD, sureFg);

	cv::Mat tmp;
	applyColorMap(mask*85, tmp, COLORMAP_JET);
	polylines(tmp, cvPaths, true, Scalar(0.0, 255.0, 255.0));
	writeDebugImageCol("../SecondPassMask.png", tmp );

}

void initCorrectionMask(cv::Mat& mask, const vector<Point2f>& contDat, const vector<Point2f>& oldContDat,
						Point2f startPoint, Point2f endPoint) {
	// initialially mark everything as sure bg
	cv::Rect r = cv::Rect(0, 0, mask.cols, mask.rows);
	cv::rectangle(mask, r, cv::GC_BGD, cv::FILLED);

	// draw the paths as probably fg
	drawPaths(mask, contDat, oldContDat, startPoint, endPoint);
}

cv::Mat makeRGBAImage(const Mat &m, const std::vector<std::vector<Point2i> > &contours)
{
	// maske am Anfang schwarz
	cv::Mat mask = cv::Mat::zeros(m.size(), CV_8UC1);
	// Konturen weiss reinmalen (gefüllt)
	cv::fillPoly(mask, contours, cv::Scalar(255));
	assert(mask.size() == m.size());
	cv::Mat newM = cv::Mat(m.size(), CV_8UC4);
	int from_to[] = { 0,0, 1,1, 2,2, 3,3  };
	cv::Mat inp[] = {m, mask};
	cv::mixChannels(inp, 2, &newM, 1, from_to, 4);
	return newM;
}

std::vector<cv::Point2f> scaleContours(std::vector<cv::Point>& contours, double scalex, double scaley)
{
	std::vector<cv::Point2f> floatC(contours.size());
	for(size_t i = 0; i < contours.size(); i++) {
		floatC[i].x = ((float)contours[i].x) * scalex;
		floatC[i].y = ((float)contours[i].y) * scaley;
	}

	return floatC;
}


const cv::Mat getCroppedMat(const cv::Mat& input) {
	cv::Rect r;
	r.height = input.rows/2;
	r.width = (input.rows/2)* 4.0 / 3.0;
	r.y = input.rows/4;
	r.x = (input.rows/4) * 4.0 / 3.0;

	return input(r);
}

cv::Mat getResizedMat(const cv::Mat& input, double factor) {
	cv::Mat tmp;
	cv::resize(input, tmp,cv::Size(), factor, factor, cv::INTER_AREA);
	return tmp;
}

void createBgFgMasks(const cv::Mat& markerImg, cv::Mat& pfgMask, cv::Mat& sfgMask, cv::Mat& fgMask) {
	cv::compare(markerImg, cv::Scalar(cv::GC_PR_FGD), pfgMask, cv::CMP_EQ);
	cv::compare(markerImg, cv::Scalar(cv::GC_FGD), sfgMask, cv::CMP_EQ);
	cv::bitwise_or(pfgMask, sfgMask, fgMask);

}

/// only call this if c is not empty
vector<Point>& getLargestContour(vector<vector<cv::Point>>& c) {
	assert(c.size() > 0);
	int largestidx = -1;
	int largestsize = 0;
	for(int i = 0; i < (int)c.size(); i++) {
		size_t currentSize = c[i].size();
		if (currentSize > largestsize) {
			largestsize = currentSize;
			largestidx = i;
		}
	}
	return c[largestidx];
}

int getContourFromMask(const cv::Mat& fgMask, vector<Point>& largestCont)
{
	vector<vector<Point>> contours;
	cv::findContours(fgMask, contours, RETR_LIST, CHAIN_APPROX_NONE);

	if (contours.size() == 0) {
		// found no contour, show error message and return
		return -1;
	}

	// we only need the largest contour
	largestCont = getLargestContour(contours);
	return 0;
}

int cutAndGetContour(const cv::Mat& resizedMat, const cv::Mat& markerImg, double factor, vector<Point2f>* iSpaceContour)
{
	// these two are only used internally in grabcut
	cv::Mat bgModel, fgModel;
	cv::grabCut(resizedMat, markerImg, cv::Rect(), bgModel, fgModel, 1, cv::GC_INIT_WITH_MASK);

	cv::Mat pfgMask, sfgMask, fgMask;
	createBgFgMasks(markerImg, pfgMask, sfgMask, fgMask);

	vector<Point> largestContour;
	int result = getContourFromMask(fgMask, largestContour);
	if (result < 0)
		return result;
	*iSpaceContour = scaleContours(largestContour, 1.0/factor, 1.0/factor);


	return 0;
}

int runGrabCutFirstTime(const cv::Mat& m,
						vector<Point2f>* outContours)
{

	GrabcutData::_croppedImage = m;//= getCroppedMat(m); //--commented to due error @Kumar
	// in LAB color umwandeln
	cv::cvtColor(GrabcutData::_croppedImage, GrabcutData::_croppedImage, cv::COLOR_BGR2Lab);

	// factor für resizen im ersten Pass ausrechnen
	double factor = (double)GrabcutData::_gcFirstRunResolution / (double)GrabcutData::_croppedImage.rows;
	cv::Mat resizedMat = getResizedMat(GrabcutData::_croppedImage, factor );

	cv::Mat markerImg(resizedMat.size(), CV_8UC1);
	initFirstMask(markerImg);

	vector<Point2f> iSpaceContour;
	int result = cutAndGetContour(resizedMat, markerImg, factor, &iSpaceContour);

	if (result < 0)
		return result;

	normalizeContours(GrabcutData::_croppedImage, iSpaceContour, *outContours );

	return result;
}

int runGrabCutSecondTime(const vector<Point2f>& firstTimeContours,vector<Point2f>* outContours)
{
	// factor für resizen in Korrekturpasses ausrechnen und userdefined Masks füllen
	double factor = (double)GrabcutData::_gcCorrectionResolution / (double)GrabcutData::_croppedImage.rows;
	Size size = Size(GrabcutData::_croppedImage.cols*factor, GrabcutData::_croppedImage.rows*factor);
	GrabcutData::_userdefinedBackground = cv::Mat::zeros(size, CV_8UC1);
	GrabcutData::_userdefinedForeground = cv::Mat::zeros(size, CV_8UC1);

	GrabcutData::_resizedImage = getResizedMat(GrabcutData::_croppedImage, factor );
	cv::Mat markerImg(GrabcutData::_resizedImage.size(), CV_8UC1);

	// dont make a full grabcut but only in area of contour in second pass
	initSecondMask(markerImg, firstTimeContours);

	vector<Point2f> iSpaceContour;
	int result = cutAndGetContour(GrabcutData::_resizedImage, markerImg, factor, &iSpaceContour);

	if (result < 0)
		return result;
	normalizeContours(GrabcutData::_croppedImage, iSpaceContour, *outContours );
	GrabcutData::_oldContours = *outContours;
	return result;
}

int runGrabCutForCorrection(const vector<Point2f>& correctionContours,
							vector<Point2f>* outContours, Point2f startPoint, Point2f endPoint)
{
	// könnte man was netteres machen als asserten, aber es sollte nicht vorkommen
	assert(GrabcutData::_oldContours.size() != 0);
	assert(correctionContours.size() != 0);

	double factor = (double)GrabcutData::_gcCorrectionResolution / (double)GrabcutData::_croppedImage.rows;

	cv::Mat markerImg(GrabcutData::_resizedImage.size(), CV_8UC1);

	initCorrectionMask(markerImg, correctionContours, GrabcutData::_oldContours, startPoint, endPoint);

	vector<Point2f> iSpaceContour;
	int result = cutAndGetContour(GrabcutData::_resizedImage, markerImg, factor, &iSpaceContour);

	if (result < 0)
		return result;

	normalizeContours(GrabcutData::_croppedImage, iSpaceContour, *outContours);
	GrabcutData::_oldContours = *outContours;

	return result;
}

} // Namespace
