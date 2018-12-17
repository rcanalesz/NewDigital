package cl.entel.plugins.digital;

import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


import android.util.Log;

import android.content.Context;

public class Connection extends Activity {

    private ReaderCollection readers;
	private Bundle savedInstanceState = null;

    private static final String TAG = "DIGITAL-CONNECTION";
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        Log.i(TAG, "ON CONNECTION!!!!!");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_list);


        // initiliaze dp sdk
        try {
			Context applContext = getApplicationContext();
			readers = Globals.getInstance().getReaders(applContext);
		} catch (UareUException e) {
			onBackPressed();
		}
        
        int nSize = readers.size();
        if (nSize > 1)
        {      
            Log.i(TAG, "TOO MANY READERS");
            //RETURN ERROR    	
        }
        else
        {   
			Intent i = new Intent();
			i.putExtra("serial_number", (nSize == 0 ? "" : readers.get(0).GetDescription().serial_number));
			i.putExtra("device_name", (nSize == 0 ? "" : readers.get(0).GetDescription().name));
			setResult(Activity.RESULT_OK, i);					
//			setResult(Activity.RESULT_OK, new Intent().putExtra("serial_number", (nSize == 0 ? "" : readers.get(0).GetDescription().serial_number)));
			finish();         	
        }
    }
    
    @Override
    public void onBackPressed() {	

		Intent i = new Intent();
		setResult(Activity.RESULT_OK, i);										
		finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {		
        onCreate(savedInstanceState);
        
        super.onConfigurationChanged(newConfig);
    }
}
