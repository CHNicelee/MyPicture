package com.ice.picture.adapter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ice.picture.R;
import com.ice.picture.bean.Image;
import com.ice.picture.view.MyImageView;
import com.ice.picture.view.MyViewPager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static android.view.View.GONE;


/**
 * Created by asd on 1/21/2017.
 */

public class MyPagerAdapter extends PagerAdapter {

    View[] views;


    List<Image> imageList;
    LayoutInflater inflater;
    MyViewPager viewPager;
    Context context;
    public MyPagerAdapter(Context context, List<Image> imageList, MyViewPager viewPager){
        this.imageList = imageList;
        inflater = LayoutInflater.from(context);
        views = new View[imageList.size()];
        this.viewPager = viewPager;
        this.context = context;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.pager_item,null);
        MyImageView imageView = (MyImageView) view.findViewById(R.id.imageView);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        LoadTask loadTask = new LoadTask();
        loadTask.setImageView(imageView,progressBar);
        if(imageList.get(position).height>4000){
            loadTask.execute(imageList.get(position).thumbnail_url);

        }else {
            loadTask.execute(imageList.get(position).image_url);
        }
        container.addView(view);
        views[position] = view;
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views[position]);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    private View currentView;
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentView = (View) object;
    }

    public View getCurrentView(){
        return currentView;
    }

    //下载类
    class LoadTask extends AsyncTask<String,Integer,Bitmap> {

        private MyImageView imageView;
        private ProgressBar progressBar;
        private String dest;

        public void setImageView(MyImageView imageView,ProgressBar progressBar){
            this.imageView = imageView;
            this.progressBar = progressBar;
        }


        private boolean fileExist(String str) {
            String path = dest+"/"+ str.substring(str.length()-10);
            File file = new File(path);
            if(file.exists()){
                return true;
            }else {
                return false;
            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            /**
             * 如果本地有图片了  那就直接加载本地的
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (fileExist(params[0])) {
                        try {
                            dest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+"/美图";
                            InputStream inputStream = new FileInputStream(new File(dest + "/" + params[0].substring(params[0].length() - 10)));
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();
                            return bitmap;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            URL url = null;
            try {
                url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); //允许输入流，即允许下载
                conn.setUseCaches(false); //不使用缓冲
                conn.setRequestMethod("GET"); //使用get请求
                conn.setReadTimeout(4000);
                InputStream is = conn.getInputStream();   //获取输入流，此时才真正建立链接
                if(is ==null){
                    return null;
                }
                Bitmap bitmap= BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            progressBar.setVisibility(GONE);

        }
    }

}
