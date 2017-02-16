package com.ice.picture;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ice.picture.adapter.MyPagerAdapter;
import com.ice.picture.bean.Image;
import com.ice.picture.bean.ImageHot;
import com.ice.picture.bean.UserCollection;
import com.ice.picture.util.Util;
import com.ice.picture.view.MyImageView;
import com.ice.picture.view.MyViewPager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * Created by asd on 1/18/2017.
 */

public class DetailImageActivity extends BaseActivity {

    private Button btn_downLoad;
    private static final int OPEN = 1;
    private LinearLayout wrapper;
    Button btn_collection;
    private MyImageView imageView;
    private int position;
    private String dest;
    protected static List<Image> imageList = new ArrayList<>();
    private String childType;
    private MyViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      /* // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

       // 隐藏状态栏
    //    定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
     //   获得当前窗体对象
        Window window=this.getWindow();
   //     设置当前窗体为全屏显示
        window.setFlags(flag, flag);*/
        setContentView(R.layout.detail_image);

        position = getIntent().getIntExtra("position",0);
        childType = getIntent().getStringExtra("childType");
        imageList = (List<Image>) getIntent().getSerializableExtra("imageList");
        dest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+"/美图";
        initWidget();

    }

    public void setBitmap(Bitmap bitmap){

        imageView.setImageBitmap(bitmap);
    }

    //检查是否已经是收藏了
    boolean isContainUrl(String url){
        for (UserCollection userCollection : Util.collectionList) {
            if(userCollection.image_url.equals(url)){
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        btn_collection =f(R.id.btn_collect);
        imageView = f(R.id.image);
        wrapper = f(R.id.wrapper);
        btn_downLoad = f(R.id.btn_downLoad);

        viewPager = f(R.id.viewPager);
        viewPager.setAdapter(new MyPagerAdapter(this,imageList,viewPager));
        viewPager.setCurrentItem(position);

        if(isContainUrl(imageList.get(position).image_url)){
            btn_collection.setText("取消");
        }
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                DetailImageActivity.this.position = position;
                if(Util.collectionList==null || Util.collectionList.size()==0){
                    getUserCollection();
                    return;
                }
                if(isContainUrl(imageList.get(position).image_url)){
                    btn_collection.setText("取消");
                }else {
                    btn_collection.setText("收藏");
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }
    int time =5;
    public void getUserCollection() {

        final BmobQuery<UserCollection> userCollectionBmobQuery = new BmobQuery<>();
        if(TextUtils.isEmpty(Util.user.userId)){
            userCollectionBmobQuery.addWhereEqualTo("username",Util.user.username);
        }else {
            userCollectionBmobQuery.addWhereEqualTo("userId",Util.user.userId);
        }
        userCollectionBmobQuery.findObjects(new FindListener<UserCollection>() {
            @Override
            public void done(List<UserCollection> list, BmobException e) {
                if(e==null){
                    Util.collectionList = list;
                }else {
                    if(time > 0)
                        getUserCollection();
                    time --;

                }
            }
        });
    }

    public void saveBitmap(){

       File file = new File(dest);
        if(!file.exists()){
            if(!file.mkdir()){
                //创建目录不成功
                show("保存失败，目录创建不成功，请检查权限");
                btn_downLoad.setClickable(true);
                return;
            }
        }

        if(fileExist(imageList.get(position).image_url)){
            show("图片已存在，无需下载");
            btn_downLoad.setClickable(true);
            return;
        }

        new loadTask().execute(imageList.get(position).image_url);

    }

    private Bitmap mBitmap;

    /**
     * 点击下载按钮 下载图片
     * @param v
     */
    public void downLoad(View v){
        btn_downLoad.setClickable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                getPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionCallback() {
                    @Override
                    public void onSuccess(String[] permission) {

                    }

                    @Override
                    public void onDenied(String[] permission) {
                        if(permission.length==0){
                            //获得了权限
                            saveBitmap();
                        }else {
                            show("对不起，您拒绝了权限，无法下载图片到本地");
                        }
                    }
                });
            }else {
               //有权限
                saveBitmap();
            }
        }
    }

    /**
     * 保存图片
     * @param bitmap
     * @param path
     * @return
     */
    public boolean saveBitmap(Bitmap bitmap, String path){
        if(bitmap==null){
            return false;
        }
        File file=new File(path);

        FileOutputStream fileOutputStream=null;

        try {
            fileOutputStream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 收藏按钮 收藏图片
     * @param v
     */
    public void collect(final View v){

        v.setClickable(false);
        if(btn_collection.getText().equals("取消")){
            //取消收藏
            for (final UserCollection userCollection : Util.collectionList) {
                if(userCollection.image_url.equals(imageList.get(position).image_url)){
                    userCollection.delete(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            show("已经从您的收藏中移出");
                            Util.collectionList.remove(userCollection);
                            v.setClickable(true);
                            btn_collection.setText("收藏");
                        }
                    });
                    break;
                }
            }
        }else {
            final UserCollection uc = new UserCollection();
            uc.image_url = imageList.get(position).image_url;
            if(Util.userType == Util.QQ_USER){
                uc.userId = Util.user.userId;
            }else {
                uc.username = Util.user.username;
            }
            uc.thumbnail_url = imageList.get(position).thumbnail_url;
            uc.width = imageList.get(position).width;
            uc.height = imageList.get(position).height;

            uc.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if(e==null){
                        uc.setObjectId(s);
                        Util.collectionList.add(uc);
                        show("收藏成功 您可以返回在左侧收藏中查看");
                        btn_collection.setText("取消");
                        v.setClickable(true);
                    }else{
                        show("收藏失败，请检查网络");
                        btn_collection.setText("收藏");
                        v.setClickable(true);
                    }
                }
            });
        }
        


    }

    public void goBack(View v){
        finish();
    }

    /**
     * 喜欢按钮
     * @param v
     */
    public void like(final View v){
        v.setClickable(false);
        BmobQuery<ImageHot>  query = new BmobQuery<>();
        query.addWhereEqualTo("image_url",imageList.get(position).image_url);
        query.findObjects(new FindListener<ImageHot>() {
            @Override
            public void done(List<ImageHot> list, BmobException e) {
                if(e==null){
                    show("图片热度已经提升");
                    v.setClickable(true);
                    if(list.size()>0){
                        //更新热度图片
                        ImageHot imageHot = list.get(0);
                        imageHot.likeCount++;
                        imageHot.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {

                            }
                        });
                    }else {
                        //新建一个热度图片
                        ImageHot imageHot = new ImageHot();
                        imageHot.likeCount++;
                        imageHot.image_url = imageList.get(position).image_url;
                        imageHot.width = imageList.get(position).width;
                        imageHot.height = imageList.get(position).height;
                        imageHot.thumbnail_url = imageList.get(position).thumbnail_url;
                        imageHot.childType = childType;
                        imageHot.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {

                            }
                        });
                    }
                }else {
                    show("网络似乎有点问题");
                }
            }
        });
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

    class loadTask extends AsyncTask<String,Integer,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {

            /**
             * 如果本地有图片了  那就直接加载本地的
             */
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (fileExist(params[0])) {
                    try {
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

            URL url = null;
            try {
                url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); //允许输入流，即允许下载
                conn.setUseCaches(false); //不使用缓冲
                conn.setRequestMethod("GET"); //使用get请求
                conn.setReadTimeout(4000);
                InputStream is = conn.getInputStream();   //获取输入流，此时才真正建立链接
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
            mBitmap = bitmap;
            if(mBitmap!=null){
                String str = imageList.get(position).image_url;
                if(!saveBitmap(mBitmap,dest+"/"+ str.substring(str.length()-10))){
                    show("下载失败，请检查权限或者网络");
                    btn_downLoad.setClickable(true);
                }else {
                    show("下载成功，保存在："+dest);
                    btn_downLoad.setClickable(true);
                }
            }else {
                show("下载失败，请检查权限或者网络");
                btn_downLoad.setClickable(true);
            }

            }
    }

    public void foldMenu(){
        Log.d("ICE","foldMenu");
        if(wrapper.getVisibility() == VISIBLE){
            wrapper.setVisibility(GONE);
            for (int i = 0; i < wrapper.getChildCount(); i++) {
                setAnimation(wrapper.getChildAt(i),i,2);
            }
        }else {
            wrapper.setVisibility(VISIBLE);
            for (int i = 0; i < wrapper.getChildCount(); i++) {
                setAnimation(wrapper.getChildAt(i),i,OPEN);
            }

        }
    }

    private void setAnimation(View childAt, int i,int STATE) {
        ObjectAnimator an =null;
        if(STATE==OPEN){
            an= ObjectAnimator.ofFloat(childAt,"translationY",0);
        }else {
            an= ObjectAnimator.ofFloat(childAt,"translationY",120);
        }
        an.setDuration(300);
        an.setStartDelay(50*i);
        an.start();
    }



}
