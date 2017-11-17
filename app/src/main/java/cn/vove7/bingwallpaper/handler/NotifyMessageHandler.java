package cn.vove7.bingwallpaper.handler;


import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import cn.vove7.bingwallpaper.utils.MyApplication;

/**
 * Created by Vove on 2017/11/17.
 */

public class NotifyMessageHandler extends Handler {
   public static final int MSG_TOAST_ARG1 = 0;

   @Override
   public void handleMessage(Message msg) {
      switch (msg.what) {
         case MSG_TOAST_ARG1: {
            Toast.makeText(MyApplication.getApplication()
                    .getViewImageActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
         }
      }
   }
}
