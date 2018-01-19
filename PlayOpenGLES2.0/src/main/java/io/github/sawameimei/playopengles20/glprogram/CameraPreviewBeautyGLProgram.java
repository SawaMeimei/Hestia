package io.github.sawameimei.playopengles20.glprogram;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.FloatRange;
import android.util.Size;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

import io.github.sawameimei.opengleslib.glprogram.TextureGLProgram;
import io.github.sawameimei.playopengles20.R;
import io.github.sawameimei.opengleslib.common.GLUtil;
import io.github.sawameimei.opengleslib.common.GLVertex;
import io.github.sawameimei.opengleslib.common.RawResourceReader;
import io.github.sawameimei.opengleslib.common.ShaderHelper;
import io.github.sawameimei.opengleslib.common.TextureHelper;

/**
 * Created by huangmeng on 2017/12/19.
 */

public class CameraPreviewBeautyGLProgram implements TextureGLProgram {

    private final WeakReference<Context> mContext;

    private final FullRectangleCoords mFullRectangleCoords;
    private final FullRectangleTextureCoords mFullRectangleTextureCoords;

    private int mVertexShaderHandle;
    private int mFragmentShaderHandle;
    private int mProgramHandle;

    private int muMVPMatrixLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    private int muTexMatrixLoc;
    private int mvStepOffsetLoc;
    private int mfLevelLoc;

    private int[] mTextureId = new int[1];

    private float[] mTextureM = GLUtil.getIdentityM();
    private float[] muPositionM = GLUtil.getIdentityM();

    private float[] mStepOffset = new float[2];

    private float mBeautyLevel = 1.0F;

    {
        //Matrix.scaleM(muPositionM, 0, -1, 1, 1);
        Matrix.rotateM(muPositionM, 0, 270F, 0, 0, 1);
    }

    public CameraPreviewBeautyGLProgram(Context context, float[] textureM, int width, int height) {
        this.mContext = new WeakReference<>(context);
        this.mTextureM = textureM;
        mStepOffset[0] = 2.0F / width;
        mStepOffset[1] = 2.0F / height;
        mFullRectangleTextureCoords = new FullRectangleTextureCoords();
        mFullRectangleCoords = new FullRectangleCoords();
        mTextureId[0] = TextureHelper.loadOESTexture();
    }

    public CameraPreviewBeautyGLProgram(Context context, float[] textureM, int textureId, int width, int height) {
        this.mContext = new WeakReference<>(context);
        this.mTextureM = textureM;
        mStepOffset[0] = 2.0F / width;
        mStepOffset[1] = 2.0F / height;
        mFullRectangleTextureCoords = new FullRectangleTextureCoords();
        mFullRectangleCoords = new FullRectangleCoords();
        mTextureId[0] = textureId;
    }

    public void setBeautyLevel(@FloatRange(from = 0, to = 1) float level) {
        mBeautyLevel = 1.0F - (1.0F - 0.33F) * level;
    }

    public void setPreviewSize(int width, int height) {
        mStepOffset[0] = 2.0F / width;
        mStepOffset[1] = 2.0F / height;
    }

    @Override
    public void compile() {
        mVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, RawResourceReader.readTextFileFromRawResource(mContext.get(), R.raw.camera_preview_beauty_vertex_sharder));
        mFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, RawResourceReader.readTextFileFromRawResource(mContext.get(), R.raw.camera_preview_beauty_fragment_sharder));
        mProgramHandle = ShaderHelper.createAndLinkProgram(mVertexShaderHandle, mFragmentShaderHandle, new String[]{"aPosition", "aTextureCoord"});

        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");

        mvStepOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "vStepOffset");
        mfLevelLoc = GLES20.glGetUniformLocation(mProgramHandle, "fLevel");
    }

    @Override
    public void draw() {
        GLES20.glUseProgram(mProgramHandle);
        GLUtil.checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId[0]);
        GLUtil.checkGlError("glBindTexture:mTextureHandle");

        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, muPositionM, 0);
        GLUtil.checkGlError("glUniformMatrix4fv:muMVPMatrixLoc");

        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mTextureM, 0);
        GLUtil.checkGlError("glUniformMatrix4fv:muTexMatrixLoc");

        GLES20.glUniform2fv(mvStepOffsetLoc, 1, FloatBuffer.wrap(mStepOffset));
        GLUtil.checkGlError("glUniform2fv:mvStepOffsetLoc");

        GLES20.glUniform1f(mfLevelLoc, mBeautyLevel);
        GLUtil.checkGlError("glUniform1f:mfLevelLoc");

        GLES20.glVertexAttribPointer(maPositionLoc, mFullRectangleCoords.getSize(), GLES20.GL_FLOAT, false, mFullRectangleCoords.getStride(), mFullRectangleCoords.toByteBuffer().position(0));
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLUtil.checkGlError("glEnableVertexAttribArray:maPositionLoc");

        GLES20.glVertexAttribPointer(maTextureCoordLoc, mFullRectangleTextureCoords.getSize(), GLES20.GL_FLOAT, false, mFullRectangleTextureCoords.getStride(), mFullRectangleTextureCoords.toByteBuffer().position(0));
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLUtil.checkGlError("glEnableVertexAttribArray:maTextureCoordLoc");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mFullRectangleCoords.getCount());
        GLUtil.checkGlError("glDrawArrays");

        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        GLUtil.checkGlError("disable");
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
    }

    @Override
    public int[] texture() {
        return mTextureId;
    }

    private static class FullRectangleTextureCoords extends GLVertex.FloatGLVertex {

        public FullRectangleTextureCoords() {
            super(new float[]{
                    0.0f, 0.0f,     // 0 bottom left
                    1.0f, 0.0f,     // 1 bottom right
                    0.0f, 1.0f,     // 2 top left
                    1.0f, 1.0f      // 3 top right
            });
        }

        @Override
        public int getSize() {
            return 2;
        }
    }

    private static class FullRectangleCoords extends GLVertex.FloatGLVertex {

        public FullRectangleCoords() {
            super(new float[]{
                    -1.0f, -1.0f,   // 0 bottom left
                    1.0f, -1.0f,   // 1 bottom right
                    -1.0f, 1.0f,   // 2 top left
                    1.0f, 1.0f,   // 3 top right
            });
        }

        @Override
        public int getSize() {
            return 2;
        }
    }
}
