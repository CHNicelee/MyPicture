package com.ice.picture.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ice.picture.R;

@SuppressLint("ResourceAsColor")
public class ToastCommon {

    private static ToastCommon toastCommom;

    private Toast toast;

    private ToastCommon(){
    }

    public static ToastCommon createToastConfig(){
        if (toastCommom==null) {
            toastCommom = new ToastCommon();
        }
        return toastCommom;
    }

    /**
     * 显示Toast
     * @param context
     * @param root
     * @param tvString
     */

    public void ToastShow(Context context,ViewGroup root,String tvString){
        View layout = LayoutInflater.from(context).inflate(R.layout.toast_xml,root);
        TextView text = (TextView) layout.findViewById(R.id.textView);
        text.setText(tvString);
        text.setTextColor(Color.WHITE);
        if(toast==null)
            toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 600);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}