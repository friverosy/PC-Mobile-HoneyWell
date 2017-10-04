package com.axxezo.MobileReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class find_people_in_manifest extends AppCompatActivity implements BarcodeReader.BarcodeListener,
        BarcodeReader.TriggerListener {

    private Vibrator mVibrator;
    private String barcodeStr;
    private EditText find_people;
    private TextView show_dni;
    private TextView show_name;
    private TextView show_origin;
    private TextView show_destination;
    private ImageView image_authorized;
    private Button button_find_in_manifest;
    MediaPlayer mp3Dennied;
    MediaPlayer mp3Permitted;
    MediaPlayer mp3Error;
    private DatabaseHelper db;
    // HonetWell Objects
    //private com.honeywell.aidc.BarcodeReader barcodeReader;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people_in_manifest);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        find_people = (EditText) findViewById(R.id.editText_find_people);
        show_dni = (TextView) findViewById(R.id.textView_show_DNI);
        show_name = (TextView) findViewById(R.id.textView_show_name);
        show_origin = (TextView) findViewById(R.id.textView_show_origin);
        show_destination = (TextView) findViewById(R.id.textView_show_destination);
        image_authorized = (ImageView) findViewById(R.id.imageView_is_in_manifest);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mp3Dennied = MediaPlayer.create(this, R.raw.bad);
        mp3Permitted = MediaPlayer.create(this, R.raw.good);
        mp3Error = MediaPlayer.create(this, R.raw.error);
        db = DatabaseHelper.getInstance(this);
        button_find_in_manifest = (Button) findViewById(R.id.button_manual_search);
        button_find_in_manifest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVibrator.vibrate(100);
                findInManifest(find_people.getText().toString().toUpperCase().trim());
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                mVibrator.vibrate(100);
                reset("");
                show_dni.setText("");
                find_people.setText("");
            }
        });
        barcodeReader = MainActivity.getBarcodeObject();
        configureBarcode();


    }

    private void configureBarcode() {
        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                // use the manager to create a BarcodeReader with a session
                // associated with the internal imager.
                barcodeReader = manager.createBarcodeReader();
                if (barcodeReader != null) {
                    try {
                        // apply settings
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_NOTIFICATION_GOOD_READ_ENABLED, false);
                        // Set Max Code 39 barcode length
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
                        // Turn on center decoding
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                        // Disable bad read response, handle in onFailureEvent
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
                        // register bar code event listener
                        barcodeReader.addBarcodeListener(find_people_in_manifest.this);
                        // register trigger state change listener
                        barcodeReader.addTriggerListener(find_people_in_manifest.this);
                        try {
                            barcodeReader.claim();
                        } catch (ScannerUnavailableException e) {
                            Log.e("error", e.getMessage());
                        }

                    } catch (UnsupportedPropertyException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void handleBarcode(BarcodeReadEvent event) {
        try {
            if (mp3Error.isPlaying()) mp3Error.stop();
            if (mp3Dennied.isPlaying()) mp3Dennied.stop();
            if (mp3Permitted.isPlaying()) mp3Permitted.stop();
            mVibrator.vibrate(100);
            reset("");
            Slack slack = new Slack(getApplicationContext());
            barcodeStr = event.getBarcodeData();
            String rawCode = barcodeStr;
            String barcodeType = event.getAimId();
            int flag = 0; // 0 for end without k, 1 with k
            People person = new People();

            if (barcodeType.equals("]Q1")) { // QR code
                if (barcodeStr.contains("client_code") && barcodeStr.contains("id_itinerary")) {
                    try { // Its a ticket
                        JSONObject json = new JSONObject(barcodeStr);
                        String doc = json.getString("client_code");

                        if (doc.contains("-")) {
                            doc = doc.substring(0, doc.indexOf("-"));
                        }
                        person.setDocument(doc);
                        barcodeStr = doc;
                        find_people.setText(barcodeStr);
                        findInManifest(barcodeStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (barcodeStr.startsWith("https://")) { // Its a new DNI Cards.
                    barcodeStr = barcodeStr.substring(
                            barcodeStr.indexOf("RUN=") + 4,
                            barcodeStr.indexOf("&type"));
                    // Remove DV.
                    barcodeStr = barcodeStr.substring(0, barcodeStr.indexOf("-"));
                    find_people.setText(barcodeStr);
                    findInManifest(barcodeStr);
                }
            }
            if (barcodeType.equals("]L2")) { // PDF417->old dni
                // Validate if the rut is > 10 millions
                String rutValidator = barcodeStr.substring(0, 8);
                rutValidator = rutValidator.replace(" ", "");
                rutValidator = rutValidator.endsWith("K") ? rutValidator.replace("K", "0") : rutValidator;
                char dv = barcodeStr.substring(8, 9).charAt(0);
                boolean isvalid = rutValidator(Integer.parseInt(rutValidator), dv);
                if (isvalid)
                    barcodeStr = rutValidator;
                else {
                    // Try validate rut size below 10.000.000
                    rutValidator = barcodeStr.substring(0, 7);
                    rutValidator = rutValidator.replace(" ", "");
                    rutValidator = rutValidator.endsWith("K") ? rutValidator.replace("K", "0") : rutValidator;
                    dv = barcodeStr.substring(7, 8).charAt(0);
                    isvalid = rutValidator(Integer.parseInt(rutValidator), dv);
                    if (isvalid)
                        barcodeStr = rutValidator;
                    else {
                        barcodeStr = "";
                        slack.sendMessage("ERROR", "Dni invalido " + barcodeStr + "\nfind_people_in_manifest Line: " + new Throwable().getStackTrace()[0].getLineNumber());
                    }
                }
                findInManifest(barcodeStr);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean rutValidator(int rut, char dv) {
        dv = dv == 'k' ? dv = 'K' : dv;
        int m = 0, s = 1;
        for (; rut != 0; rut /= 10) {
            s = (s + rut % 10 * (9 - m++ % 6)) % 11;
        }
        return dv == (char) (s != 0 ? s + 47 : 75);
    }

    public void reset(String content) {
        try {
            show_name.setText(content);
            show_origin.setText(content);
            show_destination.setText(content);
            //find_people.setText("");
            image_authorized.setImageResource(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (barcodeReader != null) {
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);
            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
            // close BarcodeReader to clean up resources.
            // once closed, the object can no longer be used.
            barcodeReader.close();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (barcodeReader != null)
            configureBarcode();

    }

    public void findInManifest(String document) {
        Cursor get_selected_dni = db.select("select ma.id_people,(select name from ports where id_mongo=ma.origin),(select name from ports where id_mongo=ma.destination), p.name  from manifest as ma left join people as p on ma.id_people=p.document where ma.id_people='" + document + "'");
        if (get_selected_dni != null && get_selected_dni.getCount() > 0) {
            mp3Permitted.start();
            show_dni.setText(document);
            show_origin.setText(get_selected_dni.getString(1));
            show_destination.setText(get_selected_dni.getString(2));
            show_name.setText(get_selected_dni.getString(3));
            image_authorized.setImageResource(R.drawable.icon_tick_true_manifest);
        } else {
            mp3Dennied.start();
            show_dni.setText(document);
            reset("< No Encontrado >");
            image_authorized.setImageResource(R.drawable.icon_tick_false_manifest);
        }
        if (get_selected_dni != null)
            get_selected_dni.close();
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleBarcode(event);
            }
        });

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(find_people_in_manifest.this, "No se ha leido ningun codigo", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            barcodeReader.aim(event.getState());
            barcodeReader.light(event.getState());
            barcodeReader.decode(event.getState());

        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
        }

    }
}
