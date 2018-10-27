package com.docwei.imageupload_lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.docwei.imageupload_lib.R;
import com.docwei.imageupload_lib.utils.DensityUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CropImageView extends AppCompatImageView {

    /******************************** 图片缩放位移控制的参数 ************************************/
    private static final float MAX_SCALE = 4.0f;  //最大缩放比，图片缩放后的大小与中间选中区域的比值
    private static final int NONE = 0;   // 初始化
    private static final int DRAG = 1;   // 拖拽
    private static final int ZOOM = 2;   // 缩放
    private static final int SAVE_SUCCESS = 1001;  // 缩放或旋转
    private static final int SAVE_ERROR = 1002;  // 缩放或旋转
    private static Handler mHandler = new InnerHandler();
    /**
     * 图片保存完成的监听
     */
    private static OnBitmapSaveCompleteListener mListener;
    /******************************** 中间的FocusView绘图相关的参数 *****************************/


    private int mFocusWidth = 0;         //焦点框的宽度
    private int mFocusHeight = 0;        //焦点框的高度
    private Path mFocusPath = new Path();
    private RectF mFocusRect = new RectF();
    private Paint mPaint;
    private int mImageWidth;
    private int mImageHeight;
    private Matrix matrix = new Matrix();      //图片变换的matrix
    private Matrix savedMatrix = new Matrix(); //开始变幻的时候，图片的matrix
    private PointF pA = new PointF();          //第一个手指按下点的坐标
    private PointF pB = new PointF();          //第二个手指按下点的坐标
    private PointF midPoint = new PointF();    //两个手指的中间点
    private PointF doubleClickPos = new PointF();  //双击图片的时候，双击点的坐标
    private PointF mFocusMidPoint = new PointF();  //中间View的中间点
    private int mode = NONE;            //初始的模式
    private long doubleClickTime = 0;   //第二次双击的时间
    private float oldDist = 1;          //双指第一次的距离
    private int sumRotateLevel = 0;     //旋转的角度，90的整数倍
    private float mMaxScale = MAX_SCALE;//程序根据不同图片的大小，动态得到的最大缩放比
    private boolean isInited = false;   //是否经过了 onSizeChanged 初始化
    private boolean mSaving = false;    //是否正在保存

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //只允许图片为当前的缩放模式
        setScaleType(ScaleType.MATRIX);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), 1));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initImage();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initImage();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        initImage();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        initImage();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        isInited = true;

        int dimen_25 = DensityUtil.dip2px(getContext(), 25);
        mFocusRect = new RectF();
        mFocusRect.left = dimen_25;
        mFocusRect.right = w - dimen_25;
        mFocusRect.top = h * 1.0f / 2 - w * 1.0f / 2 + dimen_25;
        mFocusRect.bottom = h * 1.0f / 2 + w * 1.0f / 2 - dimen_25;
        mFocusWidth = mFocusHeight = (int) (mFocusRect.right - mFocusRect.left);
        initImage();
    }

    /**
     * 初始化图片和焦点框
     */
    private void initImage() {
        Drawable d = getDrawable();
        if (!isInited || d == null) return;
        mode = NONE;
        matrix = getImageMatrix();
        mImageWidth = d.getIntrinsicWidth();
        mImageHeight = d.getIntrinsicHeight();
        //计算出焦点框的中点的坐标和上、下、左、右边的x或y的值
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float midPointX = viewWidth / 2;
        float midPointY = viewHeight / 2;
        mFocusMidPoint = new PointF(midPointX, midPointY);
        //适配焦点框的缩放比例（图片的最小边不小于焦点框的最小边）
        float fitFocusScale = getScale(mImageWidth, mImageHeight, mFocusWidth, mFocusHeight);
        mMaxScale = fitFocusScale * MAX_SCALE;
        //图像中点为中心进行缩放
        matrix.setScale(fitFocusScale, fitFocusScale);
        float[] mImageMatrixValues = new float[9];
        matrix.getValues(mImageMatrixValues); //获取缩放后的mImageMatrix的值
        float transX = mFocusMidPoint.x - (mImageMatrixValues[2] + mImageWidth * mImageMatrixValues[0] / 2);  //X轴方向的位移
        float transY = mFocusMidPoint.y - (mImageMatrixValues[5] + mImageHeight * mImageMatrixValues[4] / 2); //Y轴方向的位移
        matrix.postTranslate(transX, transY);
        setImageMatrix(matrix);
        invalidate();
    }

    /**
     * 计算边界缩放比例 isMinScale 是否最小比例，true 最小缩放比例， false 最大缩放比例
     */
    private float getScale(int bitmapWidth, int bitmapHeight, int minWidth, int minHeight) {
        float scale;
        float scaleX = (float) minWidth / bitmapWidth;
        float scaleY = (float) minHeight / bitmapHeight;
        scale = scaleX > scaleY ? scaleX : scaleY;
        return scale;
    }

    /**
     * 绘制焦点框
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mFocusPath.addRect(mFocusRect, Path.Direction.CCW);
        canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());
        canvas.clipPath(mFocusPath, Region.Op.DIFFERENCE);
        canvas.drawColor(getResources().getColor(R.color.transparent_black));
        canvas.restore();
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(mFocusRect, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSaving || null == getDrawable()) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:  //第一个点按下
                savedMatrix.set(matrix);   //以后每次需要变换的时候，以现在的状态为基础进行变换
                pA.set(event.getX(), event.getY());
                pB.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:  //第二个点按下
                if (event.getActionIndex() > 1) break;
                pA.set(event.getX(0), event.getY(0));
                pB.set(event.getX(1), event.getY(1));
                midPoint.set((pA.x + pB.x) / 2, (pA.y + pB.y) / 2);
                oldDist = spacing(pA, pB);
                savedMatrix.set(matrix);  //以后每次需要变换的时候，以现在的状态为基础进行变换
                if (oldDist > 10f) mode = ZOOM;//两点之间的距离大于10才有效
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - pA.x, event.getY() - pA.y);
                    fixTranslation();
                    setImageMatrix(matrix);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        // 这里之所以用 maxPostScale 矫正一下，主要是防止缩放到最大时，继续缩放图片会产生位移
                        float tScale = Math.min(newDist / oldDist, maxPostScale());
                        if (tScale != 0) {
                            matrix.postScale(tScale, tScale, midPoint.x, midPoint.y);
                            fixScale();
                            fixTranslation();
                            setImageMatrix(matrix);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (mode == DRAG) {
                    if (spacing(pA, pB) < 50) {
                        long now = System.currentTimeMillis();
                        if (now - doubleClickTime < 500 && spacing(pA, doubleClickPos) < 50) {
                            doubleClick(pA.x, pA.y);
                            now = 0;
                        }
                        doubleClickPos.set(pA);
                        doubleClickTime = now;
                    }
                }
                mode = NONE;
                break;
        }
        //解决部分机型无法拖动的问题
        ViewCompat.postInvalidateOnAnimation(this);
        return true;
    }

    /**
     * 修正图片的缩放比
     */
    private void fixScale() {
        float imageMatrixValues[] = new float[9];
        matrix.getValues(imageMatrixValues);
        float currentScale = Math.abs(imageMatrixValues[0]) + Math.abs(imageMatrixValues[1]);
        float minScale = getScale(mImageWidth, mImageHeight, mFocusWidth, mFocusHeight);
        mMaxScale = minScale * MAX_SCALE;

        //保证图片最小是占满中间的焦点空间
        if (currentScale < minScale) {
            float scale = minScale / currentScale;
            matrix.postScale(scale, scale);
        } else if (currentScale > mMaxScale) {
            float scale = mMaxScale / currentScale;
            matrix.postScale(scale, scale);
        }
    }

    /**
     * 修正图片的位移
     */
    private void fixTranslation() {
        RectF imageRect = new RectF(0, 0, mImageWidth, mImageHeight);
        matrix.mapRect(imageRect);  //获取当前图片（缩放以后的）相对于当前控件的位置区域，超过控件的上边缘或左边缘为负
        float deltaX = 0, deltaY = 0;
        if (imageRect.left > mFocusRect.left) {
            deltaX = -imageRect.left + mFocusRect.left;
        } else if (imageRect.right < mFocusRect.right) {
            deltaX = -imageRect.right + mFocusRect.right;
        }
        if (imageRect.top > mFocusRect.top) {
            deltaY = -imageRect.top + mFocusRect.top;
        } else if (imageRect.bottom < mFocusRect.bottom) {
            deltaY = -imageRect.bottom + mFocusRect.bottom;
        }
        matrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 获取当前图片允许的最大缩放比
     */
    private float maxPostScale() {
        float imageMatrixValues[] = new float[9];
        matrix.getValues(imageMatrixValues);
        float curScale = Math.abs(imageMatrixValues[0]) + Math.abs(imageMatrixValues[1]);
        return mMaxScale / curScale;
    }

    /**
     * 计算两点之间的距离
     */
    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 计算两点之间的距离
     */
    private float spacing(PointF pA, PointF pB) {
        return spacing(pA.x, pA.y, pB.x, pB.y);
    }

    /**
     * 双击触发的方法
     */
    private void doubleClick(float x, float y) {
        float p[] = new float[9];
        matrix.getValues(p);
        float curScale = Math.abs(p[0]) + Math.abs(p[1]);
        float minScale = getScale(mImageWidth, mImageHeight, mFocusWidth, mFocusHeight);
        if (curScale < mMaxScale) {
            //每次双击的时候，缩放加 minScale
            float toScale = Math.min(curScale + minScale, mMaxScale) / curScale;
            matrix.postScale(toScale, toScale, x, y);
        } else {
            float toScale = minScale / curScale;
            matrix.postScale(toScale, toScale, x, y);
            fixTranslation();
        }
        setImageMatrix(matrix);
    }

    /**
     * @param expectWidth  期望的宽度
     * @param exceptHeight 期望的高度
     * @return 裁剪后的Bitmap
     */
    public Bitmap getCropBitmap(int expectWidth, int exceptHeight) {
        if (expectWidth <= 0 || exceptHeight < 0) return null;
        Bitmap srcBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        srcBitmap = rotate(srcBitmap, sumRotateLevel * 90);  //最好用level，因为角度可能不是90的整数
        return makeCropBitmap(srcBitmap, mFocusRect, getImageMatrixRect(), expectWidth, exceptHeight);
    }

    /**
     * @param bitmap  要旋转的图片
     * @param degrees 选择的角度（单位 度）
     * @return 旋转后的Bitmap
     */
    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (bitmap != rotateBitmap) {
//                    bitmap.recycle();
                    return rotateBitmap;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * @return 获取当前图片显示的矩形区域
     */
    private RectF getImageMatrixRect() {
        RectF rectF = new RectF();
        rectF.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        matrix.mapRect(rectF);
        return rectF;
    }

    /**
     * @param bitmap          需要裁剪的图片
     * @param focusRect       中间需要裁剪的矩形区域
     * @param imageMatrixRect 当前图片在屏幕上的显示矩形区域
     * @param expectWidth     希望获得的图片宽度，如果图片宽度不足时，拉伸图片
     * @param exceptHeight    希望获得的图片高度，如果图片高度不足时，拉伸图片
     * @return 裁剪后的图片的Bitmap
     */
    private Bitmap makeCropBitmap(Bitmap bitmap, RectF focusRect, RectF imageMatrixRect, int expectWidth, int exceptHeight) {
        if (imageMatrixRect == null || bitmap == null) {
            return null;
        }
        float scale = imageMatrixRect.width() / bitmap.getWidth();
        int left = (int) ((focusRect.left - imageMatrixRect.left) / scale);
        int top = (int) ((focusRect.top - imageMatrixRect.top) / scale);
        int width = (int) (focusRect.width() / scale);
        int height = (int) (focusRect.height() / scale);

        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (left + width > bitmap.getWidth()) width = bitmap.getWidth() - left;
        if (top + height > bitmap.getHeight()) height = bitmap.getHeight() - top;

        try {
            bitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
            if (expectWidth != width || exceptHeight != height) {
                bitmap = Bitmap.createScaledBitmap(bitmap, expectWidth, exceptHeight, true);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * @param folder       希望保存的文件夹
     * @param expectWidth  希望保存的图片宽度
     * @param exceptHeight 希望保存的图片高度
     */
    public void saveBitmapToFile(File folder, int expectWidth, int exceptHeight) {
        if (mSaving) return;
        mSaving = true;
        final Bitmap croppedImage = getCropBitmap(expectWidth, exceptHeight);
        Bitmap.CompressFormat outputFormat = Bitmap.CompressFormat.JPEG;
        File saveFile = createFile(folder, "IMG_", ".jpg");
        final Bitmap.CompressFormat finalOutputFormat = outputFormat;
        final File finalSaveFile = saveFile;

        new Thread() {
            @Override
            public void run() {
                saveOutput(croppedImage, finalOutputFormat, finalSaveFile);
            }
        }.start();
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    private File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        try {
            File nomedia = new File(folder, ".nomedia");  //在当前文件夹底下创建一个 .nomedia 文件
            if (!nomedia.exists()) nomedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 将图片保存在本地
     */
    private void saveOutput(Bitmap croppedImage, Bitmap.CompressFormat outputFormat, File saveFile) {
        OutputStream outputStream = null;
        try {
            outputStream = getContext().getContentResolver().openOutputStream(Uri.fromFile(saveFile));
            if (outputStream != null) croppedImage.compress(outputFormat, 90, outputStream);
            Message.obtain(mHandler, SAVE_SUCCESS, saveFile).sendToTarget();
        } catch (IOException ex) {
            ex.printStackTrace();
            Message.obtain(mHandler, SAVE_ERROR, saveFile).sendToTarget();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mSaving = false;
        croppedImage.recycle();
    }

    public void setOnBitmapSaveCompleteListener(OnBitmapSaveCompleteListener listener) {
        mListener = listener;
    }


    public interface OnBitmapSaveCompleteListener {
        void onBitmapSaveSuccess(File file);

        void onBitmapSaveError(File file);
    }

    private static class InnerHandler extends Handler {
        public InnerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            File saveFile = (File) msg.obj;
            switch (msg.what) {
                case SAVE_SUCCESS:
                    if (mListener != null) mListener.onBitmapSaveSuccess(saveFile);
                    break;
                case SAVE_ERROR:
                    if (mListener != null) mListener.onBitmapSaveError(saveFile);
                    break;
            }
        }
    }

}