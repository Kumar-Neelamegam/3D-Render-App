#ifndef GRABCUTTER_P_H
#define GRABCUTTER_P_H


#include <opencv2/core/core.hpp>

namespace Vision {

struct Config {
	static int innerLineWidth;
	static int outerLineWidth;
	static int endPointMaskSize;
	static bool writeDebugImage;
};

struct GrabcutData {
	static int _gcFirstRunResolution;
	static int _gcCorrectionResolution;
	static std::vector<cv::Point2f> _oldContours;
	static cv::Mat _croppedImage;
	static cv::Mat _resizedImage; // resized for correction pass
	/**  Background, der immer bleiben soll, weils der User so gsagt hat */
	static cv::Mat _userdefinedBackground;
	/**  Vordergrund, der immer bleiben soll, weils der User so gsagt hat */
	static cv::Mat _userdefinedForeground;
};

int runGrabCutFirstTime(const cv::Mat& m,
						std::vector<cv::Point2f>* outContours);

int runGrabCutSecondTime(const std::vector<cv::Point2f>& firstTimeContours,
						 std::vector<cv::Point2f>* outContours);

int runGrabCutForCorrection(const std::vector<cv::Point2f>& correctionContours,
							std::vector<cv::Point2f>* outContours, cv::Point2f startPoint, cv::Point2f endPoint);

/**
* @brief macht ein RGBA Bild aus einem Farbbild und einem Konturvektor als Maske
* @details Der Konturvektor wird als 255 in den Alpha gerendert
*/
cv::Mat makeRGBAImage(const cv::Mat& m, const std::vector<std::vector<cv::Point2i>>& contours);

/**
 * @brief Konturen von normalisiertem Format in Bildkoordinaten bringen
*  @details Die Konturen werden in "normalisiertem" Format (zentriert um Bildmittelpunkt,
*			y zwischen -0.5 und 0.5, x zwischen -0.5 * aspectratio und 0.5 * aspectratio)
*			gespeichert, um mit verschiedenen aspect ratios und Auflösungen umgehen zu können.
*			Diese Funktion konvertiert von normalisierten Koordinaten auf die Grösse des Bildes
*			img.
*			Die y-Koordinaten werden direkt berechnet ((y+0.5) * img.height).
*			Die x-Koordinaten werden proportional zu den y-Koordinaten berechnen (x*img.width + img.width/2)
*	@warning Wenn das Ursprungsbild z.B. 16:9 (aspect 1.77) war und Mat jetzt z.B. 4:3 (aspect 1.33) ist,
*			können Teile der Kontur abgeschnitten sein.
*/
void denormalizeContours(const cv::Mat& img,
						  const std::vector<cv::Point2f>& contours,
						 std::vector<std::vector<cv::Point>> *outPaths);

} // Namespace

#endif // GRABCUTTER_P_H
