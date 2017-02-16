package com.ice.picture.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.ice.picture.R;
import com.ice.picture.util.Util;

import java.io.File;

/**
 * Created by asd on 12/23/2016.
 */

public class DownloadService extends Service {

    private DownloadTask downloadTask;
    private String downloadUrl;
    public static final int DOWNLOAD_SUCCESS = -2;


    private DownloadListener listener = new DownloadListener() {

        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("正在下载...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            //下载成功时通知前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Success",DOWNLOAD_SUCCESS));
            Toast.makeText(DownloadService.this, "下载成功，点击通知栏图标进行安装", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this, "下载失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"已取消下载", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "已暂停下载", Toast.LENGTH_SHORT).show();
        }
    };

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Intent getApkIntent(String name){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+name ));
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        return  intent;
    }

    private Notification getNotification(String title, int progress){
//        Intent intent = new Intent(this, EventTest.class);
        Intent intent = null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if(progress==DOWNLOAD_SUCCESS){
            intent = getApkIntent(Util.version.getApkName());
            PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
            builder.setContentIntent(pi);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        builder.setAutoCancel(true);
        if(progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }


    private DownloadBinder mBinder = new DownloadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DownloadBinder extends Binder {
        public void startDownload(String url){
            if(downloadTask== null){
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(url,Util.version.getApkName());
                startForeground(1,getNotification("正在下载...",0));
                Toast.makeText(DownloadService.this, "正在下载...", Toast.LENGTH_SHORT).show();

            }
        }

        public void pauseDownload(){
            if(downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload(){
            if(downloadTask!=null){
                downloadTask.cancelDownload();
            }else {
                if(downloadUrl != null){
                    //取消文件的时候需要将文件删除
                    String fileName = Util.version.getApkName();
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory+fileName);
                    if(file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "已取消下载", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
