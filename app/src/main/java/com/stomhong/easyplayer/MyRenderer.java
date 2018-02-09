package com.stomhong.easyplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by StomHong on 2018/1/2.
 * 注：OpenGL 只能渲染点、线和三角形
 * android手机的坐标和OpenGL的坐标是不一致的
 */
public class MyRenderer implements GLSurfaceView.Renderer {

    private static final String tag = MyRenderer.class.getSimpleName();

    private byte[] mData = new byte[]{};
    private int mWidth,mHeight;
    private int index = 0;
    private GLSurfaceView mGlSurfaceView;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    " gl_Position = uMVPMatrix * vPosition;" +
                    " v_texCoord = a_texCoord;" +
                    "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    " gl_FragColor = texture2D(s_texture, v_texCoord);" +
                    "}";

    /**
     * 逆时针顺序
     */
    private static final float[] VERTEX = {   // in counterclockwise order:
            1, 1, 0,   // top right
            -1, 1, 0,  // top left
            -1, -1, 0, // bottom left
            1, -1, 0,  // bottom right
    };

    private static final short[] VERTEX_INDEX = {0, 1, 2, 0, 2, 3};

    private static final float[] TEX_VERTEX = {   // in clockwise order:
            1, 0,  // bottom right
            0, 0,  // bottom left
            0, 1,  // top left
            1, 1,  // top right
    };

    private final FloatBuffer mVertexBuffer;
    private final ShortBuffer mVertexIndexBuffer;
    private final FloatBuffer mTexVertexBuffer;
    private float[] mMVPMatrix = new float[16];

    private int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;
    private int mTexName;

    private Context mContext;
    private int mTexCoordHandle;
    private int mTexSamplerHandle;


    public MyRenderer(Context context, GLSurfaceView glSurfaceView) {
        this.mGlSurfaceView = glSurfaceView;
        this.mContext = context;

        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);

        mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX);
        mTexVertexBuffer.position(0);
    }

    static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // 清空场景为黑色。
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

//            创建 GLSL 程序：glCreateProgram
//            加载 shader 代码：glShaderSource 和 glCompileShader
//            attatch shader 代码：glAttachShader
//            链接 GLSL 程序：glLinkProgram
//            使用 GLSL 程序：glUseProgram
//            获取 shader 代码中的变量索引：glGetAttribLocation
//            启用 vertex：glEnableVertexAttribArray
//            绑定 vertex 坐标值：glVertexAttribPointer

        //创建 GLSL 程序
        mProgram = GLES20.glCreateProgram();
        //加载 shader 代码
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        //attach shader 代码
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        //链接 GLSL 程序
        GLES20.glLinkProgram(mProgram);
        //使用 GLSL 程序
        GLES20.glUseProgram(mProgram);
        //获取 shader 代码中的变量索引
//            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
//            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
//            //启用 vertex
//            GLES20.glEnableVertexAttribArray(mPositionHandle);
//            //绑定 vertex 坐标值
//            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                    12, mVertexBuffer);


        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                12, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                mTexVertexBuffer);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        //只有当
        //重新渲染
        GLES20.glViewport(0, 0, width, height);
        //图形变换
        Matrix.perspectiveM(mMVPMatrix, 0, 45, (float) width / height, 0.1f, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -5f);

        int[] texNames = new int[1];
        GLES20.glGenTextures(1, texNames, 0);
        mTexName = texNames[0];
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R);
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
//                GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
//                GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
//                GLES20.GL_REPEAT);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
//                GLES20.GL_REPEAT);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//        bitmap.recycle();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        int[] texNames = new int[1];
        GLES20.glGenTextures(1, texNames, 0);
        mTexName = texNames[0];
        if (mData.length == 0)
            return;

        Bitmap bitmap = BitmapFactory.decodeByteArray(mData, 0, mData.length);

        if (bitmap != null) {

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_REPEAT);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }

        // 清空相关缓存。
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform1i(mTexSamplerHandle, 0);

        // 用 glDrawElements 来绘制，mVertexIndexBuffer 指定了顶点绘制顺序
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
//        sendImage(100,100);
    }

    public void update(byte[] dataY, int width ,int height) {
        this.mData = dataY;
        this.mWidth = width;
        this.mHeight = height;
        this.mGlSurfaceView.requestRender();
    }

    public void destroy() {
        GLES20.glDeleteTextures(1, new int[]{mTexName}, 0);
    }

//    static void sendImage(int width, int height) {
//        ByteBuffer rgbaBuf = ByteBuffer.allocateDirect(width * height * 4);
//        rgbaBuf.position(0);
//        long start = System.nanoTime();
//        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
//                rgbaBuf);
//        long end = System.nanoTime();
//        Log.d("TryOpenGL", "glReadPixels: " + (end - start));
//        saveRgb2Bitmap(rgbaBuf, Environment.getExternalStorageDirectory().getAbsolutePath()
//                + "/gl_dump_" + width + "_" + height + ".png", width, height);
//    }
//
//    static void saveRgb2Bitmap(Buffer buf, String filename, int width, int height) {
//        Log.d("TryOpenGL", "Creating " + filename);
//        BufferedOutputStream bos = null;
//        try {
//            bos = new BufferedOutputStream(new FileOutputStream(filename));
//            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            bmp.copyPixelsFromBuffer(buf);
//            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
//            bmp.recycle();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (bos != null) {
//                try {
//                    bos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}