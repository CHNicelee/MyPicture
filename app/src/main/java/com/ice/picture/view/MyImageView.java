package com.ice.picture.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ice.picture.R;
import com.ice.picture.util.Util;

/**
 * Created by asd on 1/21/2017.
 */

public class MyImageView extends ImageView implements GestureDetector.OnGestureListener, View.OnTouchListener, GestureDetector.OnDoubleTapListener{
    ImageView imageView = this;
    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

            screenWidth = windowManager.getDefaultDisplay().getWidth();
            screenHeight = windowManager.getDefaultDisplay().getHeight()-100;
        }else {

            screenWidth = windowManager.getDefaultDisplay().getWidth();
            screenHeight = windowManager.getDefaultDisplay().getHeight();
        }
    }




    Matrix currentMatrix = new Matrix();
    boolean firstTime = true;
    Matrix matrix = new Matrix();
    private GestureDetector gestureDetector;

    private float height,width;

    private Bitmap bitmap;
    private float scaleRate;



    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm==null){
            setImageResource(R.drawable.error);
            return;
        }
        super.setImageBitmap(bm);
        this.bitmap = bm;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        scaleRate = screenWidth/width;

        setScale(new Matrix());


        //       给图片设置触摸事件
        imageView.setOnTouchListener(this);
//        得到手势对象，回调6方法
        gestureDetector = new GestureDetector(getContext(),this);
//        给手势设置双击事件，回调3方法
        gestureDetector.setOnDoubleTapListener(this);

    }



    private float screenWidth;

    private float screenHeight;


    public void setScale(Matrix matrix){
        if(height>4000 && firstTime){
            return;
        }
        matrix.postScale(scaleRate,scaleRate);
        float gap = screenHeight-height*scaleRate;
        if(height*scaleRate<screenHeight){
            matrix.postTranslate(0,gap/2);
        }
        imageView.setImageMatrix(matrix);
    }



    /**********************手势对象回调的方法***************************/
    /*
    * 按下时的回调方法，这里必须把返回值改为true，其他不改可以
    * */
    @Override
    public boolean onDown(MotionEvent e) {
        currentMatrix.set(getImageMatrix());
        startX =  e.getX();
        startY = e.getY();

        Matrix matrix2 = getImageMatrix();
        float[] values = new float[9];
        matrix2.getValues(values);
        ImageState mapState = new ImageState();
        mapState.setLeft(values[2]);
        mapState.setTop(values[5]);
        mapState.setRight(mapState.getLeft()+width*scaleRate*2);
        mapState.setBottom(mapState.getTop() + height * scaleRate * 2);
        downLeft = (int) mapState.getLeft();
        downTop = (int)mapState.getTop();
        downBottom = (int) mapState.getBottom();
        downRight = (int) mapState.getRight();
        return true;
    }
    /*
    * 按下后短时间内无位移时回调方法
    * */
    @Override
    public void onShowPress(MotionEvent e) {


    }

    private int downLeft,downTop,downBottom,downRight;

    /*
    * 单击时回调
    * */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    private float startY,startX,dy,dx;
    private RelativeLayout imageWrapper;
    /*
    * 手指滑动时回调 e1是点击时候的 e2是现在的
    * */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        dx = e2.getX() - startX;
        dy = e2.getY() - startY;

        setTranslation(dx,dy);




        return false;
    }
    float largeHeight;
    float largeWidth;

    private void setTranslation(float dx, float dy) {
        //如果是放大的状态  那么可以随意移动
        if(isScaled) {



            matrix.set(currentMatrix);

            float tx = dx,ty = dy;

            if(downLeft + dx >=0){
                tx = -downLeft;
                isOnLeftBound = true;
            }

            if(downRight+dx<=screenWidth){
                tx = -(downRight-screenWidth);
            }

            if(downTop + dy >=0){
                ty = -downTop;
            }

            if(downBottom  + dy <screenHeight){
                ty = - (downBottom-screenHeight);
            }

            if(screenHeight>largeHeight){
                //高度不够 不能够上下滑动
                ty = 0;
            }

            matrix.postTranslate(tx,ty);
            imageView.setImageMatrix(matrix);
        }else {
            //如果图片高度太大  那么可以上下移动
            if(height * scaleRate>screenHeight) {
                matrix.set(currentMatrix);
            /*    if(mapState.getTop()<=-1){
                    matrix.postTranslate(0,0);
                }else if (mapState.getBottom() <= height){
                    matrix.postTranslate(0,0);
                }*/
                imageView.setImageMatrix(matrix);
            }

        }
    }

    /*
    * 长按时回调，和onSingleTapUp()是互斥的
    * */
    @Override
    public void onLongPress(MotionEvent e) {


    }
    /*
    * 手指滑动屏幕后快速离开，屏幕还处于惯性滑动的状态时回调
    * */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {

        //继续滑动的动画
        ValueAnimator animator = new ValueAnimator().ofFloat(80);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(400);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                dx += velocityX/(value+100);
                dy += velocityY/(value+100);

                setTranslation(dx,dy);
            }
        });

        animator.start();
        return false;
    }

    private boolean isOnLeftBound =false;

    /*
    * imageView.setOnTouchListener(this)的回调方法
    * */
    @Override
    public boolean onTouch(View v, MotionEvent event) {


        MotionEvent obtain = MotionEvent.obtain(event);
        obtain.setLocation(event.getRawX(),event.getRawY());

        return gestureDetector.onTouchEvent(obtain);
    }
/*
* 确认是单击事件时回调
* */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Util.activityList.get(Util.activityList.size()-1).finish();
        return false;
    }
    /*
    * 双击事件时回调
    * */
    @Override
    public boolean onDoubleTap(MotionEvent e) {

        largeWidth= width*scaleRate*2;
        largeHeight =  height*scaleRate*2;

        if(isScaled){
            setScale(new Matrix());
            isScaled = false;

        }else {
            matrix = new Matrix();
            matrix.setScale(scaleRate*2,scaleRate*2);


            //将放大后的图片平移到中间
            matrix.postTranslate(-(largeWidth- screenWidth )/2,(screenHeight - largeHeight) /2);

            imageView.setImageMatrix(matrix);

            isScaled = true;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("ICE","onTouchEvent");
        return super.onTouchEvent(event);
    }

    /*
    * 双击事件
    * */

    public boolean isScaled = false;
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {

        return false;
    }


    class ImageState {
        private float left;
        private float top;
        private float right;
        private float bottom;

        public float getLeft() {
            return left;
        }

        public void setLeft(float left) {
            this.left = left;
        }

        public float getTop() {
            return top;
        }

        public void setTop(float top) {
            this.top = top;
        }

        public float getRight() {
            return right;
        }

        public void setRight(float right) {
            this.right = right;
        }

        public float getBottom() {
            return bottom;
        }

        public void setBottom(float bottom) {
            this.bottom = bottom;
        }
    }

}

