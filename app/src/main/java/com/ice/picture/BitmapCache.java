package com.ice.picture;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by asd on 1/18/2017.
 */

public class BitmapCache implements ImageLoader.ImageCache {

    LruCache<String,Bitmap> mCache;

    public BitmapCache() {
        int maxSize = (4 * 1024 * 1024);
        mCache = new android.support.v4.util.LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return mCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mCache.put(url, bitmap);
    }
}
