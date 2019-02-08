#declare folder path
set(pathToProject D:\\ANDROID-KUMAR-2018\\Projects\\3D-Render)
set(pathToOpenCv D:\\OpenCV-android-sdk)

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${pathToOpenCv}/sdk/native/jni/include)
include_directories(${pathToOpenCv}/sdk/native/libs)

#library location
add_library( lib_opencv SHARED IMPORTED )
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${pathToProject}/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

#define libraries
add_library(native-lib SHARED src/main/cpp/native-lib.cpp)
add_library(grabcut SHARED src/main/cpp/jni-grabcut-lib.cpp src/main/cpp/grabcutter_p.cpp src/main/cpp/grabcutter_p.h)

find_library(log-lib log)

#target libraries
target_link_libraries(native-lib ${log-lib} lib_opencv)
target_link_libraries(grabcut ${log-lib} lib_opencv)

