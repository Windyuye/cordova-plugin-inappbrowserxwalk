package com.example.plugin.InAppBrowserXwalk;

import com.example.plugin.InAppBrowserXwalk.BrowserDialog;

import android.content.res.Resources;
import org.apache.cordova.*;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.internal.XWalkViewInternal;
import org.xwalk.core.XWalkCookieManager;

import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Typeface;
import android.widget.Toast;

import android.webkit.WebResourceResponse;

public class InAppBrowserXwalk extends CordovaPlugin {

    private BrowserDialog dialog;
    private XWalkView xWalkWebView;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if(action.equals("open")) {
            this.callbackContext = callbackContext;
            this.openBrowser(data);
        }

        if(action.equals("close")) {
            this.closeBrowser();
        }

        if(action.equals("show")) {
            this.showBrowser();
        }

        if(action.equals("hide")) {
            this.hideBrowser();
        }

        return true;
    }

    class MyResourceClient extends XWalkResourceClient {
           MyResourceClient(XWalkView view) {
               super(view);
           }

           @Override
           public void onLoadStarted (XWalkView view, String url) {
               try {
                   JSONObject obj = new JSONObject();
                   obj.put("type", "loadstart");
                   obj.put("url", url);
                   PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                   result.setKeepCallback(true);
                   callbackContext.sendPluginResult(result);
               } catch (JSONException ex) {}
           }

           @Override
           public void onLoadFinished (XWalkView view, String url) {
               try {
                   JSONObject obj = new JSONObject();
                   obj.put("type", "loadstop");
                   obj.put("url", url);
                   PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                   result.setKeepCallback(true);
                   callbackContext.sendPluginResult(result);
               } catch (JSONException ex) {}
           }
   }

    private void openBrowser(final JSONArray data) throws JSONException {
        final String url = data.getString(0);
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new BrowserDialog(cordova.getActivity(), android.R.style.Theme_NoTitleBar);
                xWalkWebView = new XWalkView(cordova.getActivity(), cordova.getActivity());
                XWalkCookieManager mCookieManager = new XWalkCookieManager();
                mCookieManager.setAcceptCookie(true);
                mCookieManager.setAcceptFileSchemeCookies(true);
                xWalkWebView.setResourceClient(new MyResourceClient(xWalkWebView));
                xWalkWebView.load(url, "");


                LinearLayout main = new LinearLayout(cordova.getActivity());
                main.setOrientation(LinearLayout.VERTICAL);

                main.addView(xWalkWebView);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                dialog.setCancelable(true);
                LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                dialog.addContentView(main, layoutParams);
                dialog.show();
            }
        });
    }

    public void hideBrowser() {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(dialog != null) {
                    dialog.hide();
                }
            }
        });
    }

    public void showBrowser() {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(dialog != null) {
                    dialog.show();
                }
            }
        });
    }

    public void closeBrowser() {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                xWalkWebView.onDestroy();
                dialog.dismiss();
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "exit");
                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } catch (JSONException ex) {}
            }
        });
    }
}
