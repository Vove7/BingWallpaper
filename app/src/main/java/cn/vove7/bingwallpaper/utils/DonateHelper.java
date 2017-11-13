package cn.vove7.bingwallpaper.utils;

import android.content.Context;
import android.didikee.donate.AlipayDonate;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.activities.MainActivity;

/**
 * Created by Vove on 2017/11/12.
 */

public class DonateHelper {
   private Context context;

   public DonateHelper(Context context) {
      this.context = context;
   }

   private static final String payCode = "FKX07237LYKEFIVIY8MSE9";

   public void donateWithAlipay() {
      boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(context);
      if (hasInstalledAlipayClient) {
         if (AlipayDonate.startAlipayClient((MainActivity) context, payCode)) {
            Toast.makeText(context, R.string.thanks_support, Toast.LENGTH_SHORT).show();
         } else {
            Snackbar.make(((MainActivity) context).getRecyclerView(), R.string.take_ali_failed, Snackbar.LENGTH_SHORT)
                    .show();
            LogHelper.logD(null, "take ali failed");
         }
      } else {
         Snackbar.make(((MainActivity) context).getRecyclerView(), R.string.no_alipay, Snackbar.LENGTH_SHORT)
                 .show();
         LogHelper.logD(null, "not install alipay");
      }
   }

}
