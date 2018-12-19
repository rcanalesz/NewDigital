package cl.entel.plugins.digital;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader.Priority;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;

public class Capture extends Activity {

    private Button m_back;
    private String m_sn = "";
    private String m_deviceName = "";

    private Reader m_reader = null;
    private Bitmap m_bitmap = null;
    private ImageView m_imgView;
    private TextView m_title;
    private boolean m_reset = false;
    private CountDownTimer m_timer = null;
    private TextView m_text_conclusion;
    private Reader.CaptureResult cap_result = null;

    private static final String LOG_TAG = "CAPTURE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("capture", "layout", package_name));
        //setContentView(R.layout.activity_check_fingerprint);






        int image_view_id = getApplication().getResources().getIdentifier("bitmap_image", "id", package_name);
        m_imgView = (ImageView) findViewById(image_view_id);
        //m_imgView = (ImageView) findViewById(R.id.bitmap_image);






        Log.i(LOG_TAG, "IMG VIEW ID : -- "  + Integer.toString(image_view_id) );


        if(m_imgView == null) { Log.i(LOG_TAG,"IMGVIEW null"); }
        else { Log.i(LOG_TAG,"IMGVIEW not null"); }

        









        m_bitmap = Globals.GetLastBitmap();
        //if (m_bitmap == null) m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        
        if (m_bitmap != null) m_imgView.setImageBitmap(m_bitmap);

        m_sn = getIntent().getExtras().getString("SerialNumber");



        m_text_conclusion = (TextView) findViewById(getApplication().getResources().getIdentifier("tvTitle", "id", package_name));
        //m_text_conclusion = (TextView) findViewById(R.id.tvTitle);



        if(m_sn != null){
            capture();
        }else{
            finish();
        }
    }
        //initializeActivity();

    private void capture(){
        // initiliaze dp sdk
        try {
            Context applContext = getApplicationContext();
            m_reader = Globals.getInstance().getReader(m_sn, applContext);
            m_reader.Open(Priority.EXCLUSIVE);
        } catch (Exception e)
        {
            Log.i(LOG_TAG,"error: "+e);
            Log.i(LOG_TAG, "error during init of reader");
            m_sn = "";
            m_deviceName = "";
            finish();
            return;
        }

        // updates UI continuously
        m_timer = new CountDownTimer(250, 250) {
        public void onTick(long millisUntilFinished) { }
        public void onFinish() {
        m_imgView.setImageBitmap(m_bitmap);
        m_imgView.invalidate();

        if (cap_result != null){

            if (cap_result.quality != null){

                switch(cap_result.quality){
                    case FAKE_FINGER:
                        m_text_conclusion.setText("Fake finger");
                        m_bitmap = null;
                        break;
                    case NO_FINGER:
                        m_text_conclusion.setText("No finger");
                        m_bitmap = null;
                        break;
                    case CANCELED:
                        m_text_conclusion.setText("Capture cancelled");
                        break;
                    case TIMED_OUT:
                         m_text_conclusion.setText("Capture timed out");
                         break;
                    case FINGER_TOO_LEFT:
                         m_text_conclusion.setText("Finger too left");
                         break;
                    case FINGER_TOO_RIGHT:
                         m_text_conclusion.setText("Finger too right");
                         break;
                    case FINGER_TOO_HIGH:
                         m_text_conclusion.setText("Finger too high");
                         break;
                    case FINGER_TOO_LOW:
                         m_text_conclusion.setText("Finger too low");
                         break;
                    case FINGER_OFF_CENTER:
                         m_text_conclusion.setText("Finger off center");
                         break;
                    case SCAN_SKEWED:
                         m_text_conclusion.setText("Scan skewed");
                         break;
                    case SCAN_TOO_SHORT:
                         m_text_conclusion.setText("Scan too short");
                         break;
                    case SCAN_TOO_LONG:
                         m_text_conclusion.setText("Scan too long");
                         break;
                    case SCAN_TOO_SLOW:
                         m_text_conclusion.setText("Scan too slow");
                         break;
                    case SCAN_TOO_FAST:
                         m_text_conclusion.setText("Scan too fast");
                         break;
                    case SCAN_WRONG_DIRECTION:
                         m_text_conclusion.setText("Wrong direction");
                         break;
                    case READER_DIRTY:
                         m_text_conclusion.setText("Reader dirty");
                         break;
                    case GOOD:
                         m_text_conclusion.setText("");
                         m_reset = true;
                         compressImage();
                         break;
                    default:
                         if (cap_result.image == null){
                             m_text_conclusion.setText("An error occurred");
                         }
                }
            }
        }

        if (!m_reset)
            m_timer.start();
        }
        }.start();

        // loop capture on a separate thread to avoid freezing the UI
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
               try {
                    m_reset = false;
                    while (!m_reset)
                    {
                        cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500, -1);
                        // an error occurred
                        if (cap_result == null || cap_result.image == null) continue;
                           // save bitmap image locally
                           m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());
                        }
                    } catch (Exception e)
                    {
                        Log.w("UareUSampleJava", "error during capture: " + e.toString());
                        m_sn = "";
                        m_deviceName = "";
                        onBackPressed();
                    }
                }
            }).start();

    }

    private void compressImage() {
        Bitmap bm = ((BitmapDrawable) m_imgView.getDrawable()).getBitmap();
        if(bm == null){
            Log.i(LOG_TAG,"bm null");
        }
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] byteArray = bStream.toByteArray();
        Intent i = new Intent();
        Log.i(LOG_TAG,"byteArray: "+byteArray);
        i.putExtra("bitmap", byteArray);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

 }