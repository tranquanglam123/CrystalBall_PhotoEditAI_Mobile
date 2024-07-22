cmake_minimum_required(VERSION 3.4.1)

#include
include_directories(src/main/cpp/include src/main/cpp/ src/main/include/opencv src/main/include/opencv2 src/../../../Engine_src src/../../../Engine_src/vl)

#source directory
file(GLOB CRYSTALBALL_SRC src/main/cpp/*.h src/main/cpp/*.cpp src/../../../Engine_src/*.cpp src/../../../Engine_src/alphamat/*.cpp)
set(CRYSTALBALL_COMPILE_CODE ${CRYSTALBALL_SRC})

#vlkdtree
file(GLOB VLKDTREE_SRC src/../../../Engine_src/vl/*.h src/../../../Engine_src/vl/*.c src/../../../Engine_src/vl/*.cpp)
set(VLKDTREE_COMPILE_CODE ${VLKDTREE_SRC})

#opencv
set(OpenCV_DIR src/main/sdk/native/jni)

find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS}) # Not needed for CMake >= 2.8.11
#target_link_libraries(OPENCV ${OpenCV_LIBS})

#ncnn
add_library(libncnn STATIC IMPORTED )
set_target_properties(libncnn
  PROPERTIES IMPORTED_LOCATION
  ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

#opencv
#add_library(opencv_java3 SHARED IMPORTED)
#set_target_properties(opencv_java3
# PROPERTIES IMPORTED_LOCATION
 # ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)


add_library(CrystalBall SHARED ${CRYSTALBALL_COMPILE_CODE})
add_library(vlkdtree STATIC ${VLKDTREE_COMPILE_CODE})

find_library(  log-lib log )
target_link_libraries(CrystalBall
                        vlkdtree
                        libncnn
                         ${OpenCV_LIBS}
                       jnigraphics
                       z
                       ${log-lib})