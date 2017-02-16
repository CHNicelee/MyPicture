package com.ice.picture.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by asd on 1/19/2017.
 */

public class Item extends BmobObject {
    public String item;
    public String type;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
}
