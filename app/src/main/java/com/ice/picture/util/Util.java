package com.ice.picture.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.ice.picture.bean.Image;
import com.ice.picture.bean.Item;
import com.ice.picture.bean.Type;
import com.ice.picture.bean.User;
import com.ice.picture.bean.UserCollection;
import com.ice.picture.bean.Version;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;

/**
 * Created by asd on 1/18/2017.
 */

public class Util {

    public static Bitmap bitmap = null;

    public static final int QQ_USER = 1;
    public static final int NORMAL_USER = 2;

    public static int userType;

    public static Version version = new Version();
    public static List<Type> typeList = new ArrayList<>();//类型列表
    public static List<Item> itemList; //子类型列表
    public static List<UserCollection> collectionList = new ArrayList<>();//收藏列表
    public static List<Activity> activityList = new ArrayList<>();
    public static void connect(Context context){
//        Bmob.initialize(context,"7ef28ae1842fcf531149f17834d84646");
        BmobConfig config =new BmobConfig.Builder(context)
        //设置appkey
        .setApplicationId("7ef28ae1842fcf531149f17834d84646")
        //请求超时时间（单位为秒）：默认15s
        .setConnectTimeout(5)
        //文件分片上传时每片的大小（单位字节），默认512*1024
        .setUploadBlockSize(1024*1024)
        //文件的过期时间(单位为秒)：默认1800s
        .setFileExpiration(2500)
        .build();
        Bmob.initialize(config);
    }



    public static boolean isContainItem(String tag2){
        for (Item item : itemList) {
            if(item.item.equals(tag2)){
                return true;
            }
        }
        return false;
    }

    public static List<Image> getImages(String json){
        List<Image> images = new ArrayList<>();
        images.clear();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray data= jsonObject.getJSONArray("data");
            for (int i = 0; i < data.length()-1; i++) {
                JSONObject result = data.getJSONObject(i);
                Image image = new Image();
                image.image_url = result.getString("image_url");
                image.thumbnail_url = result.getString("thumbnail_url");
                image.height = result.getInt("image_height");
                image.width = result.getInt("image_width");
                images.add(image);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return images;
    }

    public static User user = new User();

}
