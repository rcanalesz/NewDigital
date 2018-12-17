package cl.entel.plugins.digital;

// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import android.graphics.Bitmap;
import android.util.Log;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import android.util.Base64;

public class Digital extends CordovaPlugin {

    private static final String TAG = "DIGITAL";
    
    private byte[] byteArray;
    private CallbackContext callbackContext = null;
    
    //FIJO
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }



    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext newCallbackContext) {
        Log.i(TAG, "execute");

        Context context = cordova.getActivity().getApplicationContext();


        if ("connect".equals(action)) {
            callbackContext = newCallbackContext;
            cordova.setActivityResultCallback (this);

            Log.i(TAG, "connect");


            Intent i = new Intent(context, Connection.class);
            startActivityForResult(i, 1);

            return true;
        }




        callbackContext.error("No existe metodo: " + action);
        Log.i(TAG, "error");
        return false;
    }





    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "ACTIVITY RESUUUULT");

		if (data == null)
		{
			displayReaderNotFound();
			return;
		}
    }



}