package com.samhung.crystalball.photoeditor.Widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
//import android.widget.ImageView;
import android.support.v7.widget.AppCompatImageView;
public class SuperImageView extends AppCompatImageView {

    int originalBM_width = 0;
    int originalBM_height = 0;
    boolean m_bScaled = false;
    public SuperImageView(Context context) {
        super(context);
    }

    public SuperImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        originalBM_height = bm.getHeight();
        originalBM_width = bm.getWidth();
        if(originalBM_width > 3000 || originalBM_height > 3000)
            m_bScaled = true;
        else
            m_bScaled = false;
        Bitmap bmDraw = null;
        if(m_bScaled)
            bmDraw = Bitmap.createScaledBitmap(bm, bm.getWidth()/2, bm.getHeight()/2, true);
        else
            bmDraw = bm;
        super.setImageBitmap(bmDraw);
    }

    @Override
    public Matrix getImageMatrix() {
        Matrix matrix1 = new Matrix(super.getImageMatrix());
        if(m_bScaled) {
            float[] x = new float[9];
            matrix1.getValues(x);
            x[Matrix.MSCALE_X] /= 2;
            x[Matrix.MSCALE_Y] /= 2;
            x[Matrix.MSKEW_X] /= 2;
            x[Matrix.MSKEW_Y] /= 2;

            matrix1.setValues(x);
        }
        return matrix1;
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        Matrix matrix1 = new Matrix(matrix);
        if(m_bScaled) {
            float[] x = new float[9];
            matrix.getValues(x);
            x[Matrix.MSCALE_X] *=2;
            x[Matrix.MSCALE_Y] *=2;
            x[Matrix.MSKEW_X] *= 2;
            x[Matrix.MSKEW_Y] *= 2;

            matrix1.setValues(x);
        }
        super.setImageMatrix(matrix1);
    }
}
