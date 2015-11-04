package com.example.ddong.xphoto;

import android.content.Context;

/**
 * Created by ddong on 03/11/15.
 */
public class XPUtils {
    static private boolean initialized = false;
    static public void Init(Context appContext) {
        if(!initialized) {
            SharePrefHelper.getInstance().setAppContext(appContext);
            HttpHelper.getInstance().setAppContext(appContext);
            AccountManager.getInstance().setAppContext(appContext);
            AccountManager.getInstance().login();
            initialized = true;
        }
    }
}
