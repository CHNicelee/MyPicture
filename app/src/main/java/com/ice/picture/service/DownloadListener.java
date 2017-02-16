package com.ice.picture.service;

/**
 * Created by asd on 12/23/2016.
 */

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onCanceled();
    void onPause();
}
