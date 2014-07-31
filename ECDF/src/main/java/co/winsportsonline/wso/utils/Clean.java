package co.winsportsonline.wso.utils;

import android.app.Application;

import com.androidquery.callback.BitmapAjaxCallback;

/**
 * Created by Esteban- on 17-04-14.
 */
public class Clean extends Application {

    @Override
    public void onLowMemory(){
        BitmapAjaxCallback.clearCache();
    }
}
