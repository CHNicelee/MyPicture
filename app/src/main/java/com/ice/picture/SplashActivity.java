package com.ice.picture;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.ice.picture.bean.User;
import com.ice.picture.bean.Version;
import com.ice.picture.service.DownloadService;
import com.ice.picture.util.Util;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.ice.picture.util.Util.user;

/**
 * Created by asd on 1/19/2017.
 */

public class SplashActivity extends BaseActivity {

    ImageView imageView;
    DownloadService.DownloadBinder mBinder;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        Util.connect(this);
        imageView = f(R.id.imageView);
        Intent downloadIntent = new Intent(SplashActivity.this,DownloadService.class);
        startService(downloadIntent);//启动服务
        bindService(downloadIntent,connection,BIND_AUTO_CREATE);//绑定服务


        setAnimation();
    }

    Version version;

    private void setAnimation() {
        Animation animation = new ScaleAnimation(1.0f,1f,1.0f,1f);
        animation.setDuration(1500);
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(animation);
        set.setDuration(1500);
        imageView.setAnimation(set);
        autoLogin();
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {


                BmobQuery<Version> query1 = new BmobQuery();
                query1.findObjects(new FindListener<Version>() {
                    @Override
                    public void done(List<Version> list, BmobException e) {
                        if(e!=null){
                            return;
                        }
                        version = list.get(0);
                        Util.version = list.get(0);
                    }
                });


            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {

                    Log.d("ICE","version:"+version);
                    //第一个参数是包名  在build.gradle里面有
                    PackageInfo info = getPackageManager().getPackageInfo("com.ice.picture",
                            PackageManager.GET_CONFIGURATIONS);
                    //获得版本号  是一个int类型的数据
                    final int versionCode =info.versionCode;
                    //获取版本名  是String类型的数据
                    String versionName = info.versionName;
                    if(version==null){
                        BmobQuery<Version> query1 = new BmobQuery();
                        query1.findObjects(new FindListener<Version>() {
                            @Override
                            public void done(List<Version> list, BmobException e) {
                                if(e!=null){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                                    builder.setTitle("网络连接有误");
                                    builder.setMessage("对不起，目前无法连接网络，请检查网络后再打开应用");

                                    builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                                    builder.setCancelable(false);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                                else {
                                    login();

                                }
                            }
                        });


                        return;
                    }
                    if(versionCode<version.getVersion()){
                        showDialog();
                    }else {
                        login();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }




            }

            public void login(){
                if(auto){
                    Intent intent = new Intent(SplashActivity.this, TypeActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            public void showDialog(){
                final SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
                if(!sp.getBoolean("update",true)){
                    login();

                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setTitle("版本过低");
                builder.setMessage("对不起，您的版本过低，请下载最新版本");
                builder.setCancelable(false);

                builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mBinder.startDownload(Util.version.getUrl());
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        set.start();

    }

    private boolean auto;
    private void autoLogin() {
        final String autoLogin = getString("autoLogin");
        if(autoLogin.equals("true")){
            auto = true;
            String username = getString("username");
            if(!username.equals("")) {
                Util.user.username = username;
                Util.user.password = getString("password");
                Util.user.name = getString("name");

                BmobQuery<User> query = new BmobQuery<>();
                query.addWhereEqualTo("username", username);
                query.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        if (e == null) {
                            user = list.get(0);

                        } else {
                            auto = false;
                            show("网络似乎遇到了故障");
                        }
                    /*else{
                        show("网络似乎有点故障");
                    }*/
                    }
                });
                return;
            }

            if(!getString("userId").equals("")){
                Util.user.name = getString("name");
                Util.user.userId = getString("userId");
                BmobQuery<User> query = new BmobQuery<>();
                query.addWhereEqualTo("userId", getString("userId"));
                query.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        if(e ==null){
                            if(list.size()>0){
                                Util.user = list.get(0);
                            }
                        }
                    }
                });
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
