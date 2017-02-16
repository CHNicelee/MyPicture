package com.ice.picture.service;

import android.os.AsyncTask;
import android.os.Environment;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by asd on 12/23/2016.
 */

public class DownloadTask extends AsyncTask<String,Integer,Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_PAUSE = 1;
    public static final int TYPE_FAILED = 2;
    public static final int TYPE_CANCELED = 3;

    private DownloadListener listener;

    private  boolean isCanceled = false;
    private boolean isPause = false;

    //记录上一次的progress
    private int lastProgress;

    public DownloadTask(DownloadListener listener){
        this.listener = listener;
    }




    @Override
    protected Integer doInBackground(String... params) {


        InputStream is = null;//输入流
        RandomAccessFile savedFile = null;//随机存储文件
        File file = null;
        try {
        long downloadLength =0;//记录已下载的文件长度
        String downloadUrl = params[0];
        String fileName = params[1];
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//得到下载文件所用的目录
        file = new File(directory+fileName);
        if(file.exists()){
            downloadLength = file.length();//文件已经存在 把文件的长度读出来
        }
            long contentLength = getContentLength(downloadUrl);
            if(contentLength == 0){
                return TYPE_FAILED;//待下载的长度为0  下载失败   交给onPostExecute处理
            }else if(contentLength==downloadLength) {
                return TYPE_SUCCESS;//已经存在的与待下载的长度相同  说明是同一个文件
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //断点下载  指定从哪个字节开始下载
                    .addHeader("RANGE","bytes="+downloadLength+"-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if(response != null){
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file,"rw");//第一个是文件  第二个 rw指的是打开文件并读写  如果不存在则新建一个
                savedFile.seek(downloadLength);//跳过已下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len=is.read(b))!=-1){
                    if(isCanceled){
                        return TYPE_CANCELED;
                    }else if(isPause){
                        return TYPE_PAUSE;
                    }else {
                        total+=len;
                        savedFile.write(b,0,len);
                        //计算已下载的百分比
                        int progress = (int)((total + downloadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            try {
                if(is!=null){
                    is.close();
                }
                if(savedFile!=null){
                    savedFile.close();
                }
                if (isCanceled&&file!=null){
                    file.delete();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress= values[0];
        if(progress>lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            case TYPE_PAUSE:
                listener.onPause();
                break;
        }
    }

    public void pauseDownload(){
        isPause = true;
    }

    public void cancelDownload(){
        isCanceled = true;
    }



    private long getContentLength(String downloadURL) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadURL).build();

        Response response = client.newCall(request).execute();
        if(request!=null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }
}