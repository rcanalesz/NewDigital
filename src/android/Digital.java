package cl.entel.plugins.digital;

// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;


import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.UareUException;

import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent; 
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import android.util.Base64;

public class Digital extends CordovaPlugin {

    private static final String TAG = "DIGITAL";

    private static final int FIRST_CHECK = 1;
    private static final int SECOND_SCAN = 2;



    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    
    private byte[] byteArray;    
    private Bitmap bmp;

    private CallbackContext callbackContext = null;

    private String m_sn = "";
	private String m_deviceName = "";

    private boolean isCamera;
	Reader m_reader;
    
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
            this.cordova.getActivity().startActivityForResult(i, FIRST_CHECK);

            return true;
        }
        
        else if("capture".equals(action)){
            callbackContext = newCallbackContext;
            cordova.setActivityResultCallback (this);

            Log.i(TAG, "capture");


            Intent i = new Intent(context, Capture.class); 
            i.putExtra("SerialNumber",m_sn);
            this.cordova.getActivity().startActivityForResult(i, SECOND_SCAN);


            return true;
        }




        callbackContext.error("No existe metodo: " + action);
        Log.i(TAG, "error");
        return false;
    }





    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "ACTIVITY RESUUUULT");

		if (data == null)
		{
			displayReaderNotFound();
			return;
		}


        switch (requestCode) {
		case FIRST_CHECK:

            if(resultCode != Activity.RESULT_OK){
                displayReaderNotFound();
            }

            Globals.ClearLastBitmap();

            m_sn = (String) data.getExtras().get("serial_number");
            m_deviceName = (String) data.getExtras().get("device_name");
            if((m_deviceName != null && !m_deviceName.isEmpty()) && (m_sn != null && !m_sn.isEmpty()) )
            {
                try {
                    Context applContext = cordova.getActivity().getApplicationContext();
                    m_reader = Globals.getInstance().getReader(m_sn, applContext);

                    if(m_reader.GetDescription().technology == Reader.Technology.HW_TECHNOLOGY_CAPACITIVE)
                    {
                        PendingIntent mPermissionIntent;                 
                        mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                        applContext.registerReceiver(mUsbReceiver, filter);

                        if(DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName))
                        {
                            CheckDevice();
                        }
                    }
                    else
                    {
                        CheckDevice();
                    }
                } catch (UareUException e1) {
                    displayReaderNotFound();
                }
                catch (DPFPDDUsbException e) {
                    displayReaderNotFound();
                }
            } else { 
                displayReaderNotFound();
            }			
			break;

        case SECOND_SCAN:
        
            Log.i(TAG, "ON RESULT OF SCAN");

            if(resultCode == Activity.RESULT_OK){
                Log.i(TAG, "RESULT OF SCAN STATUS - OK");
                
                String imageBase64 = data.getStringExtra("imageBase64");
                String wsqBase64 = data.getStringExtra("wsqBase64");

                Log.i(TAG,"imageBase64: "+imageBase64);
                Log.i(TAG,"wsqBase64: "+wsqBase64);



                try {
                    JSONObject json = new JSONObject(); 
                    json.put("imageBase64", imageBase64);
                    json.put("wsqBase64", wsqBase64);

                    String message = json.toString();

                    Log.i(TAG,"message: "+message);                

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, message );            
                    callbackContext.sendPluginResult(pluginResult);
                }
                catch (Exception e) {
                    e.printStackTrace();

                    Log.i(TAG, "RESULT OF SCAN STATUS - FAILED");
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Error on image JSON");
                    callbackContext.sendPluginResult(pluginResult);
                }
                
                             
            }else{
                Log.i(TAG, "RESULT OF SCAN STATUS - FAILED");
                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Error on Reader");
                callbackContext.sendPluginResult(pluginResult);
                
            }

			break;
		}









    }






    //CHECK CONNECTION FAILED
    private void displayReaderNotFound()
	{
		// empty serial number means reader not found		
		
		Globals.getInstance().enableCamera();

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				cordova.getActivity().getApplicationContext());

		alertDialogBuilder.setTitle("Reader Not Found");

		alertDialogBuilder
				.setMessage("Plug in a reader and try again.")
				.setCancelable(false)
				.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();		

        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Reader not found");
        callbackContext.sendPluginResult(pluginResult);
	}
    //CHECK CONNECTION SUCCESS
    protected void CheckDevice() {
		try {
			m_reader.Open(Priority.EXCLUSIVE);
            
            if(m_reader.GetCapabilities().can_capture){ Log.i(TAG, "Can capture "); }
            else{ Log.i(TAG, "Cannot capture ");}

            if(m_reader.GetCapabilities().can_stream){ Log.i(TAG, "Can stream "); }
            else{ Log.i(TAG, "Cannot stream "); }

			m_reader.Close();
			Globals.getInstance().enableCamera();

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "Reader connected");
            callbackContext.sendPluginResult(pluginResult);

		} catch (UareUException e1) {
			displayReaderNotFound();
		}

	}
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	if (ACTION_USB_PERMISSION.equals(action)) {
	    		synchronized (this) {
	    			UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	    			if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	    				if(device != null){
	    					//call method to set up device communication                   
						CheckDevice();
	    				}                
	    			}                 
	    			else {
					//setButtonsEnabled(false);
	    			}
	    		}
	    	}
	    }
	};	









}