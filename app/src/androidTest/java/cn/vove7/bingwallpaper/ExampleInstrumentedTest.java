package cn.vove7.bingwallpaper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import cn.vove7.bingwallpaper.utils.DBHelper;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
   @Test
   public void useAppContext() throws Exception {
      // Context of the app under test.
      Context appContext = InstrumentationRegistry.getTargetContext();
      String url = "1";
      DBHelper.setDownloadOk(url);
      boolean isOk = DBHelper.haveDownloaded(url);
      System.out.println(" isOK " + isOk);

      isOk=DBHelper.haveDownloaded("2");

      System.out.println(" isOK " + isOk);

   }
}
