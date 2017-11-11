package cn.vove7.bingwallpaper.interfaces;

public interface DownloadListener {
   void onProgress();

   void onSuccess();

   void onFailed();

   void onPause();

   void onCanceled();
}