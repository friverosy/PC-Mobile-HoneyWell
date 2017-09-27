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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class find_people_in_manifest extends AppCompatActivity {

    private final static String SCAN_ACTION = "urovo.rcv.message";//action
    private Vibrator mVibrator;
    private SoundPool soundpool = null;
    private String barcodeStr;
    private boolean isScaning = false;
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


    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO Auto-generated method stub
            if (mp3Error.isPlaying()) mp3Error.stop();
            if (mp3Dennied.isPlaying()) mp3Dennied.stop();
            if (mp3Permitted.isPlaying()) mp3Permitted.stop();

            isScaning = false;
            //soundpool.play(soundid, 1, 1, 0, 0, 1);

            mVibrator.vibrate(100);
            reset("");
            Slack slack=new Slack(getApplicationContext());

            byte[] barcode = intent.getByteArrayExtra("barocode");
            int barocodelen = intent.getIntExtra("length", 0);
            byte barcodeType = intent.getByteExtra("barcodeType", (byte) 0);
            Log.i("codetype", String.valueOf(barcodeType));
            barcodeStr = new String(barcode, 0, barocodelen);
            String rawCode = barcodeStr;
            Log.d("---", rawCode);

            int flag = 0; // 0 for end without k, 1 with k
            People person = new People();

            if (barcodeType == 28) { // QR code
                if (barcodeStr.contains("client_code")) {
                    // Its a ticket
                    try {
                        Log.d("barcode", barcodeStr);
                        JSONObject json = new JSONObject(barcodeStr);
                        String doc = json.getString("client_code");
                        doc = doc.substring(0, doc.length() - 2);
                        person.setDocument(doc);
                        barcodeStr = doc;
                        //send the text to edittext
                        find_people.setText(barcodeStr);
                        findInManifest(barcodeStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if(rawCode.startsWith("https://")) {
                    // Its a DNI Card.
                    barcodeStr = barcodeStr.substring(
                            barcodeStr.indexOf("RUN=") + 4,
                            barcodeStr.indexOf("&type"));
                    // Remove DV.
                    barcodeStr = barcodeStr.substring(0, barcodeStr.indexOf("-"));
                    find_people.setText(barcodeStr);
                    findInManifest(barcodeStr);
                }

            } else if (barcodeType == 17) { // PDF417
                // 1.- validate if the rut is > 10 millions
                String rutValidator = barcodeStr.substring(0, 8);
                rutValidator = rutValidator.replace(" ", "");
                rutValidator = rutValidator.endsWith("K") ? rutValidator.replace("K", "0") : rutValidator;
                char dv = barcodeStr.substring(8, 9).charAt(0);
                boolean isValid = rutValidator(Integer.parseInt(rutValidator), dv);
                if (isValid)
                    barcodeStr = rutValidator;
                else { //try validate rut size below 10.000.000
                    rutValidator = barcodeStr.substring(0, 7);
                    rutValidator = rutValidator.replace(" ", "");
                    rutValidator = rutValidator.endsWith("K") ? rutValidator.replace("K", "0") : rutValidator;
                    dv = barcodeStr.substring(7, 8).charAt(0);
                    isValid = rutValidator(Integer.parseInt(rutValidator), dv);
                    if (isValid)
                        barcodeStr = rutValidator;
                    else {
                        slack.sendMessage("ERROR","Dni invalido "+barcodeStr+ "\nfind_people_in_manifest Line: " + new Throwable().getStackTrace()[0].getLineNumber());
                        barcodeStr = "";
                    }
                }

                // Get name from DNI.
                String[] array = rawCode.split("\\s+"); // Split by whitespace.
                try {
                    find_people.setText(array[1].substring(0, array[1].indexOf("CHL")));
                } catch (ArrayIndexOutOfBoundsException e) {
                    find_people.setText(array[2].substring(0, array[2].indexOf("CHL")));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    find_people.setText("");
                }
                findInManifest(barcodeStr);
            }

            Log.i("Cooked Barcode", barcodeStr);
        }
    };

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
            initScan();
            IntentFilter filter = new IntentFilter();
            filter.addAction(SCAN_ACTION);
            registerReceiver(mScanReceiver, filter);
            //show_dni.setText("");
            show_name.setText(content);
            show_origin.setText(content);
            show_destination.setText(content);
            //find_people.setText("");
            image_authorized.setImageResource(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initScan() {
        // TODO Auto-generated method stub
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initScan();
        // sendRecordstoAPI();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    public void findInManifest(String document) {
        Cursor get_selected_dni = db.select("select ma.id_people,(select name from ports where id_mongo=ma.origin),(select name from ports where id_mongo=ma.destination), p.name  from manifest as ma left join people as p on ma.id_people=p.document where ma.id_people='" + document + "'");
        if (get_selected_dni!=null&&get_selected_dni.getCount() > 0) {
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
        if(get_selected_dni!=null)
            get_selected_dni.close();
    }


}
