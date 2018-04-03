
cmake_minimum_required(VERSION 3.4.1)

#set(lib_src_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

#第三方库使用到的头文件
include_directories( ${CMAKE_SOURCE_DIR}/src/main/cpp/include/)

#生成lib名称(可以随便起)
add_library( EasyPlayer SHARED src/main/cpp/video.c src/main/cpp/audio.c src/main/cpp/decode.c)

#作为支持库
add_library( avcodec-57 SHARED IMPORTED )
set_target_properties(avcodec-57 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libavcodec-57.so)

add_library( avdevice-57 SHARED IMPORTED )
set_target_properties(avdevice-57 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libavdevice-57.so)

add_library( avfilter-6 SHARED IMPORTED )
set_target_properties(avfilter-6 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libavfilter-6.so)

add_library( avformat-57 SHARED IMPORTED )
set_target_properties(avformat-57 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libavformat-57.so)

add_library( avutil-55 SHARED IMPORTED )
set_target_properties(avutil-55 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libavutil-55.so)

add_library( swresample-2 SHARED IMPORTED )
set_target_properties(swresample-2 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libswresample-2.so)

add_library( swscale-4 SHARED IMPORTED )
set_target_properties(swscale-4 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libswscale-4.so)

add_library( yuv SHARED IMPORTED )
set_target_properties(yuv PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/armeabi/libyuv.so)

find_library(log-lib log )

target_link_libraries(
                         EasyPlayer
                         avdevice-57
                         avcodec-57
                         avfilter-6
                         avformat-57
                         avutil-55
                         swresample-2
                         swscale-4
                         yuv
                         android
                         EGL
                         GLESv2
                         OpenSLES
                       ${log-lib} )