package com.ice.picture.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by asd on 1/18/2017.
 */

public class Image extends BmobObject implements Serializable {
    private final long serialVersionUID  = 1111L;
    public String image_url;
    public String thumbnail_url;
    public int height;
    public int width;
}
