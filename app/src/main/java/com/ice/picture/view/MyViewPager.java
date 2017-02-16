package com.ice.picture.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ice.picture.R;
import com.ice.picture.adapter.MyPagerAdapter;

/**
 * Created by asd on 1/21/2017.
 */

public class MyViewPager extends ViewPager {
    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        View view = ((MyPagerAdapter)getAdapter()).getCurrentView();
        if(((MyImageView) view.findViewById(R.id.imageView)).isScaled){
            return false;
        }else {
            return super.onInterceptTouchEvent(ev);
        }
    }

}
