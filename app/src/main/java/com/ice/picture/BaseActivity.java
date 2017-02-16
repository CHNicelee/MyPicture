package com.ice.picture;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.ice.picture.util.NetWorkUtil;
import com.ice.picture.util.Util;
import com.ice.picture.view.ToastCommon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asd on 1/18/2017.
 */

public class BaseActivity extends AppCompatActivity{



    protected NetWorkUtil netWorkUtil;

    protected ToastCommon toastCommon = ToastCommon.createToastConfig();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.activityList.remove(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Util.activityList.add(this);

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           if(this instanceof DetailImageActivity){
               getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                       | View.SYSTEM_UI_FLAG_IMMERSIVE
                       | View.SYSTEM_UI_FLAG_FULLSCREEN);
           }
           else {
               getWindow().setNavigationBarColor(getResources().getColor(R.color.navigationBar));
           }
        }
        netWorkUtil = new NetWorkUtil(this);
    }

    protected void show(String text){
        toastCommon.ToastShow(this, (ViewGroup) f(R.id.toast_root),text);
    }

    protected <T extends View>T f(int id){
        return (T) findViewById(id);
    }

    public  int px2dip(float pxValue) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    private List<String> permissionList;
    private PermissionCallback listener;
    public void getPermission(String[] permissions,PermissionCallback listener){
        this.listener = listener;
        permissionList = new ArrayList<>();
        if (permissions.length>0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //如果api大于23
                for (int i = 0; i < permissions.length; i++) {
                    if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                        permissionList.add(permissions[i]);
                    }
                }
                requestPermissions( permissionList.toArray(new String[permissionList.size()]),1);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode!=1) return;
        List<String> deniedList = new ArrayList<>();
        if(grantResults.length>0)
            for (int i = 0; i < grantResults.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    deniedList.add(permissions[i]);
                }
            }

            listener.onDenied(deniedList.toArray(new String[deniedList.size()]));
    }

    protected void putString(String key,String value){
        SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
        sharedPreferences.edit().putString(key,value).commit();
    }

    protected String getString(String key){
        SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }



}
interface PermissionCallback{
    void onSuccess(String[] permission);
    void onDenied(String[] permission);
}
