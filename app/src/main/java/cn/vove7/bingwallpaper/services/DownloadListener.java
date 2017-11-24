package cn.vove7.bingwallpaper.services;

public interface DownloadListener {
   void onProgress();

   void onSuccess();

   void onFailed();

   void onPause();

   void onCanceled();

   void dealContent_0();
}