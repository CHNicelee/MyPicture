package com.ice.picture.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by asd on 12/20/2016.
 */
public class Version extends BmobObject{
    public Version(){

    }
    public Integer version;

    public String detail;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String apkName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String url;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
