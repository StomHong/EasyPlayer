//
// Created by StomHong on 2018/2/8.
//

#include <jni.h>
#include <android/log.h>
#include <libavformat/avformat.h>
#include <libavutil/error.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <libavutil/time.h>
#include <unistd.h>
#include <libavutil/pixfmt.h>
#include <libyuv.h>
#include <libyuv/convert_from.h>
#include <libavutil/imgutils.h>
#include <pthread.h>
#include <libswresample/swresample.h>

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(^_^)", format, ##__VA_ARGS__)



/**
 * 一般来说，媒体播放有这几个步骤：解协议-->解封装-->解码-->视音频同步
 *  目前只是需要：解封装-->解码-->播放视频
 *
 *  解码播放
 * @param env       JNI接口指针
 * @param instance  this指针
 * @param _url      视频地址
 */

JNIEXPORT  void JNICALL
Java_com_stomhong_easyplayer_MainActivity_setDataResource
        (JNIEnv *env, jobject instance, jobject surface) {

    char *url = "/storage/emulated/0/huitoutainan.mp4";

    //注册所有的解码器
    av_register_all();

    AVFormatContext *pFormatCtx = NULL;
    AVCodec *pCodecAudio = NULL;
    AVCodec *pCodecVideo = NULL;

    SwrContext *swrContext = NULL;
    uint8_t *out_buffer;
    int out_channer_nb;


    //1.获得实例对应的class类
    jclass jcls = (*env)->GetObjectClass(env,instance);

    //2.通过class类找到对应的method id
    //name 为java类中变量名，Ljava/lang/String; 为变量的类型String
    jmethodID jmid = (*env)->GetMethodID(env,jcls,"play","([B)V");


    //打开输入流并读取数据头 也可以使用avformat_alloc_context()
    int ret = avformat_open_input(&pFormatCtx, url, NULL, NULL);
    if (ret < 0) {
        char buf[1024] = {0};
        av_strerror(ret, buf, sizeof(buf));
        LOGE("open %s failed: %s", url, buf);
        return;
    }

    //获取视频总时长(second)
    int64_t totalSec = pFormatCtx->duration / AV_TIME_BASE;
    LOGE("totalSec %lld", totalSec);



    //获取视频流
    int videoStream = -1;
    int audioStream = -1;
    videoStream = av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, &pCodecVideo, 0);
    audioStream = av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1, &pCodecAudio, 0);

    if (videoStream < 0) {
        LOGE("find videoStream failed");
        return;
    }
    if (audioStream < 0) {
        LOGE("find audioStream failed");
        return;
    }

    //初始化解码器上下文
    AVCodecContext *pCodecCtxAudio = avcodec_alloc_context3(pCodecAudio);
    avcodec_parameters_to_context(pCodecCtxAudio, pFormatCtx->streams[audioStream]->codecpar);
    int err = avcodec_open2(pCodecCtxAudio, pCodecAudio, NULL);

    AVCodecContext *pCodecCtxVideo = avcodec_alloc_context3(pCodecVideo);
    avcodec_parameters_to_context(pCodecCtxVideo, pFormatCtx->streams[videoStream]->codecpar);
    avcodec_open2(pCodecCtxVideo, pCodecVideo, NULL);
//    if (err < 0) {
//        char buf[1024] = {0};
//        av_strerror(err, buf, sizeof(buf));
//        LOGE("open videoCodec failed: %s", buf);
//        return;
//    }



    // 获取native window
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);

    // 获取视频宽高
    int videoWidth = pCodecCtxVideo->width;
    int videoHeight = pCodecCtxVideo->height;

    // 设置native window的buffer大小,可自动拉伸
    ANativeWindow_setBuffersGeometry(nativeWindow, videoWidth, videoHeight, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer windowBuffer;

    //初始化AVPacket
    AVPacket *pPacket = av_packet_alloc();
    //初始化AVFrame
    AVFrame *pFrame = av_frame_alloc();
    AVFrame *pFrameRGBA = av_frame_alloc();


    //    mp3  里面所包含的编码格式   转换成  pcm   SwcContext
    swrContext = swr_alloc();

    out_buffer = (uint8_t *) av_malloc(44100 * 2);
    uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;
//    输出采样位数  16位
    enum AVSampleFormat out_formart = AV_SAMPLE_FMT_S16;
//输出的采样率必须与输入相同
    int out_sample_rate = pCodecCtxAudio->sample_rate;


    swr_alloc_set_opts(swrContext, out_ch_layout, out_formart, out_sample_rate,
                       pCodecCtxAudio->channel_layout, pCodecCtxAudio->sample_fmt, pCodecCtxAudio->sample_rate, 0,
                       NULL);

    swr_init(swrContext);
//    获取通道数  2
    out_channer_nb = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
    unsigned int index = 0;
    //循环读取数据帧
    while (av_read_frame(pFormatCtx, pPacket) == 0) {

        if(pPacket->stream_index == audioStream){
            LOGE("audioStream");
            avcodec_send_packet(pCodecCtxAudio, pPacket);
            if (avcodec_receive_frame(pCodecCtxAudio, pFrame) >= 0) {
                LOGE("读取音频:%d", index++);

                LOGE("音频声道数:%d 采样率：%d 数据格式: %d", pFrame->channels, pFrame->sample_rate,
                     pFrame->format);

                swr_convert(swrContext, &out_buffer, 44100 * 2, (const uint8_t **) pFrame->data,
                            pFrame->nb_samples);
//                缓冲区的大小
                int size = av_samples_get_buffer_size(NULL, out_channer_nb, pFrame->nb_samples,
                                                      AV_SAMPLE_FMT_S16, 1);
                //创建JbyteArray对象，并设置JArray对象的值
                jbyteArray jbArr = (*env)->NewByteArray(env, size);
                (*env)->SetByteArrayRegion(env, jbArr, 0, size, (jbyte *) out_buffer);
                //3.通过obj获得对应的method
                (*env)->CallVoidMethod(env,instance,jmid,jbArr);
//                usleep(100);
                //释放局部引用
                (*env)->DeleteLocalRef(env,jbArr);
            }
        }

        //如果是视频数据包
        if (pPacket->stream_index == videoStream) {
            avcodec_send_packet(pCodecCtxVideo, pPacket);

            int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, pCodecCtxVideo->width,
                                                    pCodecCtxVideo->height, 1);
            uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
            av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
                                 pCodecCtxVideo->width, pCodecCtxVideo->height, 1);

            // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
            struct SwsContext *sws_ctx = sws_getContext(pCodecCtxVideo->width,
                                                        pCodecCtxVideo->height,
                                                        pCodecCtxVideo->pix_fmt,
                                                        pCodecCtxVideo->width,
                                                        pCodecCtxVideo->height,
                                                        AV_PIX_FMT_RGBA,
                                                        SWS_BILINEAR,
                                                        NULL,
                                                        NULL,
                                                        NULL);

            if (avcodec_receive_frame(pCodecCtxVideo, pFrame) >= 0) {
                LOGE("读取视频帧:%d", index++);
                ANativeWindow_lock(nativeWindow, &windowBuffer, 0);

                // 格式转换
                sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
                          pFrame->linesize, 0, pCodecCtxVideo->height,
                          pFrameRGBA->data, pFrameRGBA->linesize);

                // 获取stride
                uint8_t *dst = windowBuffer.bits;
                int dstStride = windowBuffer.stride * 4;
                uint8_t *src = pFrameRGBA->data[0];
                int srcStride = pFrameRGBA->linesize[0];

                // 由于window的stride和帧的stride不同,因此需要逐行复制
                int h;
                for (h = 0; h < videoHeight; h++) {
                    memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
                }

                ANativeWindow_unlockAndPost(nativeWindow);

//                usleep(100);

            }
        }
    }
    //释放资源
    av_frame_free(&pFrameRGBA);
    av_frame_free(&pFrame);
    av_packet_unref(pPacket);

    avformat_close_input(&pFormatCtx);
    avformat_free_context(pFormatCtx);
}

