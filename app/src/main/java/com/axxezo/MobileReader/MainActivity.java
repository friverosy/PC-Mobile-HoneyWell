package com.axxezo.MobileReader;

/*
                      _..--''`-..._
                    -'             `-.
                 .-'                  `-.
                /   .-'''''''''--.       \
                |  /__........_   `-.    |
                 \ / -. .--._  ``-._ |   /
                  / /\ V //  `--._ -`-  /
                 / / || | |       \\ \`/
                  |  || |\|        \\ \
                 /| // /-._   _... ||  \
                //\ \ / <o>   <o>  ||\\|   TO PROTECT
                /  \//     /      // |\\    AND SERVE
                \| \\_\    ._/   // /|//
                 |\ //|\  .__.   / / |//
                  \|/// \ `=='  / / ///
                   \//  |\    .' / // -.
                       /-.`--' -'.' |   `------.
                _..---' \ `-..-'   /            \            .
             .-'         \  /@ \  /              \         . \\
            |     .$.     \/.--.\/_               \       /|#||
            /     $$$  _-'         `-.       /     .    .'  #||
           .   \  `$' /               \      |     |   :   _#/
           |    |    /           /    /      \     |   |  / #
           |    /    `.     \   /    /        \    |   /  : 6
           |   /       `-.      .   /          |   |  /   ' 9
           |  |           `-/.___.-'           |.==\.'   /.-6-.
           |  |            /      \            |   /    /((   ))
           |`= \        ..-    @   \          /   /    :  `-.-'
          |     `..___--            `--..__.-'  .'     :
          '      |\            @            /| /      /
         /       :|___________...._________/ \       /
        .        /|                       /   \     /
        :       / /                      /     `._.'
        |     .'./ \                    /
        |     :/    `.                 |
        |    /'       `-.              |
        :   /            `-.  c     _.'\
       /   /                `-..--''    \
      /   /       .                     |
     /   /`-.       .       @           |
    .-- \    `-._    .             .    |
  _/    \\_______\______________________/____
((_\\\\\|u((_________________________________)
    `---|             \       __..---''
        \              \ _.--'        |
                        /             /
         \             /
                      |              /
          \           |
           \          |             /
            \         |    :F_P:   /
             \        |           /*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import info.hoang8f.android.segmented.SegmentedGroup;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.axxezo.MobileReader.R.id.status;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,BarcodeReader.BarcodeListener,BarcodeReader.TriggerListener {


    private static String token_navieraAustral = "860a2e8f6b125e4c7b9bc83709a0ac1ddac9d40f";
    private static String token_transportesAustral = "49f89ee1b7c45dcca61a598efecf0b891c2b7ac5";
    private TextView TextViewFullname;
    private EditText TextViewRut;
    private TextView TextViewStatus;
    private TextView TextViewManifestUpdate;
    private ImageView imageview;
    private Vibrator mVibrator;
    private String barcodeStr;
    MediaPlayer mp3Dennied;
    MediaPlayer mp3Permitted;
    MediaPlayer mp3Error;
    private static String AxxezoAPI;
    private boolean is_input = true;
    private SegmentedGroup mySwitch;
    private Spinner comboLanded;
    private String selectedSpinnerLanded;
    private int selectedIntSpinnerLanded;
    private String updateTimePeople;
    private String TextViewTimePeople;
    public ArrayAdapter<String> adapter;
    private String route;
    private RadioButton in;
    private RadioButton out;

    // HonetWell Objects
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    /********
     * Timers Asyntask
     ****/
    private int timer_asyncUpdateManifest;
    private int timer_sendRecordsAPI;
    private int timer_asyncUpdatePeopleState;
    private int timer_asyncDeletePeopleManifest;

    /*****
     * Asyntask declarations.....
     */
    private RegisterTask Asynctask_sendRecord; //asyntask that send data to api axxezo
    private asyncTask_updatePeopleManifest AsyncTask_updatePeopleManifest; //Asyntask to update in realtime new people inserts in manifest
    private AsyncUpdateStateManifest AsynTask_UpdateStateManifest; // Asyntask to update states of people insert in manifest table;
    // private AsyncUpdateStateManifest AsynTask_UpdateDeletePerson; // Asyntask to update states of people insert in manifest table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        TextViewFullname = (TextView) findViewById(R.id.fullname);
        TextViewRut = (EditText) findViewById(R.id.rut);
        TextViewStatus = (TextView) findViewById(status);
        TextViewManifestUpdate = (TextView) findViewById(R.id.textView_lastManifestUpdate);
        comboLanded = (Spinner) findViewById(R.id.spinner_setLanded);
        imageview = (ImageView) findViewById(R.id.imageView);
        mp3Dennied = MediaPlayer.create(MainActivity.this, R.raw.bad);
        mp3Permitted = MediaPlayer.create(MainActivity.this, R.raw.good);
        mp3Error = MediaPlayer.create(MainActivity.this, R.raw.error);
        mySwitch = (SegmentedGroup) findViewById(R.id.mySwitch);
        in = (RadioButton) findViewById(R.id.in);
        out = (RadioButton) findViewById(R.id.out);
        selectedIntSpinnerLanded = -1;
        selectedSpinnerLanded = "";
        TextViewTimePeople = "";

        // Asign timers to Asyntask
        timer_sendRecordsAPI = 30000;                           //30 sec=30.000
        timer_asyncUpdateManifest = 120000;                     //2 min =120.000
        timer_asyncUpdatePeopleState = 15000;                   //15 sec=15.000
        timer_asyncDeletePeopleManifest = 420000;               //7 min =420000

        // Asign url api axxezo
        AxxezoAPI = "http://axxezo-test.brazilsouth.cloudapp.azure.com:5002/api";
        //AxxezoAPI = "http://axxezocloud.brazilsouth.cloudapp.azure.com:5002/api";
        DatabaseHelper db=DatabaseHelper.getInstance(this);
        route=db.selectFirst("select route_name from config")!=null?db.selectFirst("select route_name from config"):"";

        // create the AidcManager providing a Context and an
        // CreatedCallback implementation.
        configureReader();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mVibrator.vibrate(100);
                    if (TextViewRut.getText().toString().isEmpty()) {
                        TextViewRut.setHint("Ingrese Rut");
                        TextViewRut.setHintTextColor(Color.RED);
                        TextViewRut.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(TextViewRut, InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        PeopleValidator(TextViewRut.getText().toString().trim(), "", "","]L2");
                    }
                }
            });
        }

        // Second fab, to show information about travel
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        if (fab2 != null)
            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                    String text = "Ruta: " + db.selectFirst("select route_name from config") +
                            "\nItinerario: " + db.selectFirst("select route_id from config") +
                            "\nPasajeros Puerto: " + selectedSpinnerLanded +
                            "\nPasajeros Embarcados:" + db.selectFirst("select count(id) from manifest where is_inside=1 and origin=(select id_mongo from ports where name='" + selectedSpinnerLanded + "')") +
                            "\nPasajeros Pendientes:" + db.selectFirst("select count(id) from manifest where is_inside=0 and origin=(select id_mongo from ports where name='" + selectedSpinnerLanded + "')") +
                            "\nUltima Actualizacion Manifiesto: " + TextViewTimePeople;
                    Snackbar snack = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                            .setAction("Action", null);
                    View view1 = snack.getView();
                    TextView information = (TextView) view1.findViewById(android.support.design.R.id.snackbar_text);
                    information.setTextColor(Color.WHITE);
                    information.setMaxLines(7);
                    snack.show();
                }
            });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        in.toggle();//set enable by default embark in segmented button
        mySwitch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.in:
                        is_input = true;
                        break;
                    case R.id.out:
                        is_input = false;
                        break;
                }
            }
        });

        fillSpinner();

        // Call in onCreate asyntask
        sendRecordstoAPI();
        asyncUpdateManifestinTime();
        asyncUpdateManifestState(); // Pending change values from string to integer
        getWindow().getDecorView().findViewById(R.id.content_main).invalidate();
    }
    public void configureReader(){
        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                // use the manager to create a BarcodeReader with a session
                // associated with the internal imager.
                barcodeReader = manager.createBarcodeReader();

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
                    barcodeReader.addBarcodeListener(MainActivity.this);
                    // register trigger state change listener
                    barcodeReader.addTriggerListener(MainActivity.this);
                    try {
                        barcodeReader.claim();
                    } catch (ScannerUnavailableException e) {
                        Log.e("error",e.getMessage());
                    }

                } catch (UnsupportedPropertyException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public void fillSpinner() {
        // Enable WAL mode in DB
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        // Fill information in combobox
        Cursor getOriginandDestination = db.select("select name from ports order by name desc");
        ArrayList<String> listOriginDestination = new ArrayList<String>();
        if (getOriginandDestination != null && getOriginandDestination.getCount() > 0) {
            while (!getOriginandDestination.isAfterLast()) {
                if (getOriginandDestination.getString(0) != null)
                    listOriginDestination.add(getOriginandDestination.getString(0));
                getOriginandDestination.moveToNext();
            }
        }
        if (getOriginandDestination != null)
            getOriginandDestination.close();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listOriginDestination);
        comboLanded.setAdapter(adapter);
        comboLanded.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSpinnerLanded = comboLanded.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_MainWindow) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_records) {
            Intent intent = new Intent(this, lastRecordsList.class);
            startActivity(intent);
        }
        if (id == R.id.action_find) {
            Intent intent = new Intent(this, find_people_in_manifest.class);
            startActivity(intent);
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Configuration.class);
            startActivityForResult(intent, 0);
        }
        if (id == R.id.action_manual_registration) {
            Intent intent = new Intent(this, manual_registration.class);
            startActivity(intent);
        }
        if (id == R.id.action_exit) {
            exitApp();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mainWindow) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Configuration.class);
            startActivityForResult(intent, 0);
        }
        if (id == R.id.nav_records) {
            Intent intent = new Intent(this, lastRecordsList.class);
            startActivity(intent);
        }
        if (id == R.id.nav_find) {
            Intent intent = new Intent(this, find_people_in_manifest.class);
            startActivity(intent);
        }
        if (id == R.id.nav_manual_registration) {
            Intent intent = new Intent(this, manual_registration.class);
            startActivity(intent);
        }
        if (id == R.id.nav_exit) {
            exitApp();
        }

        // then close the drawer Layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
    /**
     * Return current local datetime in PDA, in format that specifies in string format
     *
     * @param format= how you want to receive the datetime,Ex:"dd-MM-yyyy HH:mm:SS"
     * @return return String with the current datetime
     */
    public String getCurrentDateTime(String format) {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat(format);
        return date.format(currentLocalTime);
    }

    /**
     * @param format string
     *               as yyyy-MM-dd'T'HH:mm:ss
     * @return current local time
     */
    public String getDeltasCurrentDateTime(String format) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        Date currentLocalTime = cal.getTime();
        SimpleDateFormat date = new SimpleDateFormat(format);
        return date.format(currentLocalTime);
    }

    /**
     * Method that validate old and new chilean national identity card
     *
     * @param rut=number without check digit
     * @param dv=        only check digit
     * @return true if the dni number is correct or false if dni number doesn´t match with check digit
     */
    public boolean ValidarRut(int rut, char dv) {
        dv = dv == 'k' ? dv = 'K' : dv;
        int m = 0, s = 1;
        for (; rut != 0; rut /= 10) {
            s = (s + rut % 10 * (9 - m++ % 6)) % 11;
        }
        return dv == (char) (s != 0 ? s + 47 : 75);
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("onDestroy","onDestroy");
        /*if (barcodeReader != null)
        {
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);
            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
            // close BarcodeReader to clean up resources.
            // once closed, the object can no longer be used.
            barcodeReader.close();
        }*/
        if (manager != null)
        {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.e("onPause","onPause");
        if (barcodeReader != null) {
            barcodeReader.close();
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("spinnerSelection", comboLanded.getSelectedItemPosition());
        editor.apply();
        //  s.ServerStop();//Remove if it needs to work with the screen off. Good practice: Server must stop.
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // Load spinner selected in sharedPreference method
        Log.e("onResume","onResume");
        if (barcodeReader != null) {
                configureReader();
        }
        SharedPreferences sharedPref = getSharedPreferences("userPreference", MODE_PRIVATE);
        int spinnerValue = sharedPref.getInt("spinnerSelection", -1);
        if (spinnerValue != -1) {// set the value of the spinner
            comboLanded.setSelection(spinnerValue);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();


    }

    /**
     * Each timer_asyncUpdateManifest time
     * create a new instance of updatePeopleManifest Asynctask
     * This method use executor for handle it
     */
    private void asyncUpdateManifestinTime() {
        final Handler handler = new Handler();
        final Slack slack = new Slack(getApplicationContext());
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            AsyncTask_updatePeopleManifest = new asyncTask_updatePeopleManifest();
                            AsyncTask_updatePeopleManifest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (Exception e) {
                            slack.sendMessage("Cannot update Manifest", e.getMessage() + "\nMainActivity  Line: " + new Throwable().getStackTrace()[0].getLineNumber());
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, timer_asyncUpdateManifest);  // 5 min=300000 // 6 min =360000
    }
    /**
     * /**
     * Each timer_asyncUpdatePeopleState time
     * create a new instance of AsyncUpdateStateManifest Asynctask
     * This method use executor for handle it
     */
    private void asyncUpdateManifestState() {
        final Handler handler = new Handler();
        final Slack slack = new Slack(getApplicationContext());
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            AsynTask_UpdateStateManifest = new AsyncUpdateStateManifest();
                            AsynTask_UpdateStateManifest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (Exception e) {
                            slack.sendMessage("ERROR", e.getMessage() + "\nMainActivity Line: " + new Throwable().getStackTrace()[0].getLineNumber());
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, timer_asyncUpdatePeopleState);  // 3min =180000 //4 min = 240000;
    }

    /**
     * Call method getUpdatePeopleManifest per each port save in DB, in each port, get new person and insert that
     *
     * @return integer with difference between old manifest and new manifest with people download
     */
    private int Asyntask_insertNewPeopleManifest() {
        // Update manifest
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Slack slack=new Slack(getApplicationContext());
        int count_before = Integer.parseInt(db.selectFirst("select count(id) from manifest"));
        int total_temp = 0;
        try {
            String id_route = db.selectFirst("select route_id from config");
            if (id_route != null && !id_route.isEmpty() && !id_route.equals("null"))
                db.insertJSON(new getAPIInformation(AxxezoAPI, Integer.parseInt(id_route), 0).execute().get(), "manifest");
            //else
            //    slack.sendMessage("ERROR", "route_id is null or empty, cannot execute asynctask" + "\nClase: MainActivity Line: " + new Throwable().getStackTrace()[0].getLineNumber());
            int count_after = Integer.parseInt(db.selectFirst("select count(id) from manifest"));
            if (count_before != count_after) {
                total_temp = count_after - count_before;
            }
            if (total_temp > 0) {
                db.insert("update config set date_last_update='" + getDeltasCurrentDateTime("yyyy-MM-dd'T'HH:mm:ss") + "' where id=1");
                updateTimePeople = db.selectFirst("select date_last_update from config");
                if (!updateTimePeople.isEmpty() && updateTimePeople != null && !updateTimePeople.equals("null")) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date newDate = null;
                    try {
                        newDate = format.parse(updateTimePeople);
                        format = new SimpleDateFormat("HH:mm");
                        String date = format.format(newDate);
                        if (!date.isEmpty() || !date.equals("null") || date != null)
                            TextViewTimePeople = date;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (android.database.SQLException | JSONException | ExecutionException | InterruptedException e) {
            slack.sendMessage("cannot insert people in manifest", e.getMessage() + "\nMainActivity Line: " + new Throwable().getStackTrace()[0].getLineNumber());
        }
        // Log.e("asynctask", "insertnewpeoplemanifest");
        return total_temp;
    }

    /**
     * Init scan object
     * Clean textviews
     */
    public void reset() {
        try {
            TextViewFullname.setText("Nombre");
            TextViewRut.setText("Nº Documento");
            TextViewStatus.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Each timer_sendRecordsAPI time ask if must post offline records to API
     */
    public void sendRecordstoAPI() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        final DatabaseHelper db = DatabaseHelper.getInstance(this);
        final Slack slack= new Slack(getApplicationContext());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (Asynctask_sendRecord == null) {
                                OfflineRecordsSynchronizer();
                            }
                            if (db.record_desync_count() > 0 && Asynctask_sendRecord.getStatus() != AsyncTask.Status.RUNNING)
                                OfflineRecordsSynchronizer();
                        } catch (android.database.SQLException e) {
                            slack.sendMessage("cannot send records to api", e.getMessage() + "\nMainActivity Line: " + new Throwable().getStackTrace()[0].getLineNumber());
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, timer_sendRecordsAPI);  // 360000= 6 minutes, 7 minutes=420000
    }

    /**
     * PeopleValidator, contains all validations of qr and pdf417 code, receive
     *
     * @param rut=          dni number extract in method broadcastReceivere
     * @param id_itinerary= travel id, use to validate person in table manifest
     * @param port=         embark person port
     * @param barcodetype=  indicate the type of code read
     */
    public void PeopleValidator(String rut, String id_itinerary, String port, String  barcodetype) {
        boolean valid = false;
        Cursor person = null;
        Record record = new Record(); // Object to be sended to API Axxezo.
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Cursor validation = null;
        rut = rut.trim().toUpperCase();
        if (comboLanded.getChildCount() == 0) { //1.-validations
            TextViewStatus.setText("PORFAVOR CONFIGURE EL MANIFIESTO PRIMERO");
            new LoadSound(4).execute();
            record.setReason(5);
        } else {
            TextViewRut.setText(rut);
            if (barcodetype.equals("]Q1") && !id_itinerary.isEmpty()) { //pure QR code
                if (id_itinerary.trim().equals(db.selectFirst("select route_id from config where route_id='" + id_itinerary + "'").trim())) {
                    validation = db.select("select p.name,m.origin from ports as p left join manifest as m on p.id_mongo=m.origin where m.id_people='" + rut + "'");
                    if (validation.getCount() != 0) {
                        if (is_input) {
                            if ((validation.getString(0)).trim().equals(selectedSpinnerLanded.trim()))
                                valid = true;
                            else {
                                TextViewStatus.setText("PUERTO EMBARQUE ES " + validation.getString(0));
                                record.setReason(1);
                            }
                        } else {
                            validation = db.select("select p.name from ports as p left join manifest as m on p.id_mongo=m.destination where m.id_people='" + rut + "'");
                            if (validation.getString(0).trim().equals(selectedSpinnerLanded.trim()))
                                valid = true;
                            else {
                                TextViewStatus.setText("PUERTO DESEMBARQUE ES " + validation.getString(0));
                                record.setReason(2);
                            }
                        }
                    } else {
                        TextViewStatus.setText("PERSONA NO SE ENCUENTRA EN MANIFIESTO");
                        record.setReason(3);
                    }
                } else {
                    TextViewStatus.setText("VIAJE NO CORRESPONDE");
                    record.setReason(4);
                }
            } else if (barcodetype.equals("]Q1")&& id_itinerary == "" || barcodetype.equals("]L2")) { //old dni and new dni validations
                validation = db.select("select p.name,m.id_people from ports as p left join manifest as m on p.id_mongo=m.origin where m.id_people='" + rut + "'");
                if (validation.getCount() != 0) {
                    if (is_input) {
                        if (!selectedSpinnerLanded.equals(validation.getString(0))) {
                            TextViewStatus.setText("PUERTO EMBARQUE ES " + validation.getString(0));
                            record.setReason(1);
                        } else
                            valid = true;
                    } else {
                        validation = db.select("select p.name from ports as p left join manifest as m on p.id_mongo=m.destination where m.id_people='" + rut + "'");
                        if (!selectedSpinnerLanded.equals(validation.getString(0))) {
                            TextViewStatus.setText("PUERTO DESEMBARQUE ES " + validation.getString(0));
                            record.setReason(2);
                        } else
                            valid = true;
                    }
                } else {
                    TextViewStatus.setText("PERSONA NO SE ENCUENTRA EN MANIFIESTO");
                    record.setReason(3);
                }
            }
            if (validation != null)
                validation.close();
        }

        // Fill person information in cursor person, order in cursor rut,name,origin,destination,boletus
        if (valid) {
            person = db.validatePerson(rut);
            record.setPerson_document(rut);
            if (person.getCount() > 0 && person.getString(1) != null) {
                TextViewFullname.setText(person.getString(1));
                record.setPerson_name(person.getString(1));
            } else {
                record.setPerson_name("");
                TextViewFullname.setText("");
            }
            record.setSync(0);
            record.setDatetime(getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
            record.setPort_registry(db.selectFirst("select id_mongo from ports where name = '" + comboLanded.getSelectedItem().toString() + "'"));
            TextViewRut.setText(rut);

            new LoadSound(2).execute();
            imageview.setImageResource(R.drawable.img_true);
            TextViewStatus.setText("");

            // Fill record
            record.setPermitted(1);

            if (is_input) {
                record.setInput(1);
                db.updatePeopleManifest(rut, person.getString(2), person.getString(3), 1);
            } else {
                record.setInput(2);
                db.updatePeopleManifest(rut, person.getString(2), person.getString(3), 2);
            }

            record.setOrigin(person.getString(2));
            record.setDestination(person.getString(3));
            record.setMongo_id_person(person.getString(5));
            record.setMongo_id_manifest(db.selectFirst("select manifest_id from config"));
            record.setMongo_id_register(person.getString(6));
        } else {
            record.setPerson_document(rut);
            TextViewFullname.setText("");
            new LoadSound(3).execute();
            imageview.setImageResource(R.drawable.img_false);

            if (is_input) record.setInput(1);
            else record.setInput(2);
            record.setPermitted(-1);
            record.setDatetime(getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
            record.setPort_registry(db.selectFirst("select id_mongo from ports where name = '" + comboLanded.getSelectedItem().toString() + "'"));
        }
        db.add_record(record);
        if (person != null)
            person.close();
    }

    public void makeToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Get all register in table records where sync=0 (not synchronized) and put this in a List<Records>
     * once done, send list to asyncronous sendRecords
     */
    public void OfflineRecordsSynchronizer() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        List<Record> records = db.get_desynchronized_records();
        Asynctask_sendRecord = new RegisterTask(records, AxxezoAPI);
        Asynctask_sendRecord.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * PUT method that send to api the data contains in local database
     *
     * @param record= contains all information of the register, like dni, name, origin, destination,etc
     * @param url=    addres of endpoint to send data
     * @param client= receive a client okhttp to send registers, the reason of that, is avoid to create per each record a object okhttp, and only usage one instance of this
     * @return
     */
    public String PUT(Record record, String url, OkHttpClient client) {
        String result = "";
        String json = "";
        JSONObject jsonObject = new JSONObject();
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        try {
            url = url + "/registers/" + record.getMongo_id_register();
            jsonObject.accumulate("person", record.getMongo_id_person());
            jsonObject.accumulate("seaport", record.getPort_registry());
            //jsonObject.accumulate("manifest", record.getMongo_id_manifest()); //falta
            jsonObject.accumulate("state", record.getInput() + "");
            jsonObject.accumulate("date", record.getDatetime()); //falta formatear 2017-01-01 00:00:00

            json = jsonObject.toString();

            RequestBody body = RequestBody.create(JSON, json);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", "application/json")
                    .put(body)
                    .build();

            //PUT using okhttp
            Response response = client.newCall(request).execute();

            String tmp = response.body().string(); //Response{protocol=http/1.1, code=401, message=Unauthorized, url=http://axxezo-test.brazilsouth.cloudapp.azure.com:9001/api/registers}
            // Convert inputstream to string
            if (tmp != null) {
                if (response.isSuccessful()) {
                    // If has sync=0 its becouse its an offline record to be will synchronized.
                    if (record.getSync() == 0) {
                        db.update_record(record.getId());
                    }
                }
            } else {
                result = String.valueOf(response.code());
            }
            // Result its the json to sent
            if (result.startsWith("http://"))
                result = "204"; //no content
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        // 11. return result
        return result;
    }

    /**
     * Post data to API
     *
     * @param record object contains data
     * @param url string contain base url of the endpoint
     * @param client http
     * @return reponse of post
     *
     * If post is siccessfull and its a manuall sell (new person)
     * obtain from response person object id
     */
    public String POST(Record record, String url, OkHttpClient client) {
        String result = "";
        String json = "";
        boolean isManualSell = false;
        JSONObject jsonObject = new JSONObject();
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        try {
            jsonObject.accumulate("documentId", record.getPerson_document());
            jsonObject.accumulate("itinerary", db.selectFirst("select id_mongo from routes where id=(select route_id from config order by id desc limit 1)"));
            jsonObject.accumulate("date", record.getDatetime());
            if (record.getTicket() != 0) {
                url += "/registers/manualSell/";//manual registers
                jsonObject.accumulate("name", record.getPerson_name());
                jsonObject.accumulate("origin", record.getOrigin());
                jsonObject.accumulate("destination", record.getDestination());
                jsonObject.accumulate("ticketId", record.getTicket());
                isManualSell = true;
            } else if (record.getPermitted() == -1) {//denied registers
                url += "/registers/deniedRegister";
                jsonObject.accumulate("deniedReason", record.getReason());
                jsonObject.accumulate("origin", record.getPort_registry());
            }

            json = jsonObject.toString();
            // Log.e(url, json);
            RequestBody body = RequestBody.create(JSON, json);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", "application/json")
                    .post(body)
                    .build();

            // POST using okhttp
            Response response = client.newCall(request).execute();

            String tmp = response.body().string(); //Response{protocol=http/1.1, code=401, message=Unauthorized, url=http://axxezo-test.brazilsouth.cloudapp.azure.com:9001/api/registers}
            // Convert inputstream to string

            if (tmp != null) {
                if (response.isSuccessful()) {
                    // If has sync=0 its becouse its an offline record to be will synchronized.
                    if (record.getSync() == 0) {
                        db.update_record(record.getId());
                    }
                    if (isManualSell) {
                        JSONObject jsonManualSell = new JSONObject(tmp);
                        String ObjectId = jsonManualSell.getString("registerId").trim();
                        String personId = jsonManualSell.getString("personId").trim();
                        if (!ObjectId.isEmpty() && !personId.isEmpty())
                            db.insert("update people set id_register='" + ObjectId + "',id_mongo='" + ObjectId + "' where document='" + record.getPerson_document() + "'");
                    }
                }
            } else {
                result = String.valueOf(response.code());
            }
            // Result its the json to sent
            if (result.startsWith("http://"))
                result = "204"; //no content
        } catch (JSONException | IOException e) {
            e.printStackTrace();

        }
        return result;
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
                Toast.makeText(MainActivity.this, "Error al intentar leer el codigo", Toast.LENGTH_SHORT).show();
            }
        });
    }
/* event of button and led usage*/
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
    public class RegisterTask extends AsyncTask<Void, Void, String> {
        private String url;
        private List<Record> newRecord;

        RegisterTask(List<Record> newRecord, String url) {
            this.newRecord = newRecord;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            String postReturn = "";
            final OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .writeTimeout(0, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .build();
            for (int i = 0; i < newRecord.size(); i++) {
                Record record = newRecord.get(i);
                if (record.getPermitted() == 1)
                    PUT(record, url, client);
                else if (record.getPermitted() == -1 || record.getTicket() != 0)//for denied registers
                    POST(record, url, client);
            }
            return postReturn;
        }
    }
    private void handleBarcode(BarcodeReadEvent event){
        try {
            new LoadSound(4).execute();
            mVibrator.vibrate(100);
            reset();
            barcodeStr = event.getBarcodeData();
            String rawCode = barcodeStr;
            String barcodeType=event.getAimId();
            Log.e("AIM ",barcodeType);
            Log.e("CodeId ",event.getCodeId());
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
                        PeopleValidator(doc, json.getString("id_itinerary"), json.getString("port"), barcodeType);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if (rawCode.equals("close trip now")) {
                    if (comboLanded.getCount() > 0) {
                        Intent closeTrip = new Intent(getApplicationContext(), CloseTrip.class);
                        startActivity(closeTrip);
                    } else {
                        makeToast("No hay viaje configurado para cerrar");
                    }
                } else if (barcodeStr.startsWith("https://")) { // Its a new DNI Cards.
                    barcodeStr = barcodeStr.substring(
                            barcodeStr.indexOf("RUN=") + 4,
                            barcodeStr.indexOf("&type"));
                    // Remove DV.
                    barcodeStr = barcodeStr.substring(0, barcodeStr.indexOf("-"));
                    PeopleValidator(barcodeStr, "", "", barcodeType);
                } else if (!barcodeStr.contains("id_itinerary")) {
                    new LoadSound(1).execute();
                    TextViewFullname.setText("");
                    TextViewStatus.setText("QR INVALIDO");
                    imageview.setImageResource(R.drawable.img_false);
                    //slack.sendMessage("ERROR", "Codigo QR invalido (" + rawCode + "),\nLine: " + new Throwable().getStackTrace()[0].getLineNumber());
                }

            }
            if (barcodeType.equals("]L2")) { // PDF417->old dni
                // Validate if the rut is > 10 millions
                String rutValidator = barcodeStr.substring(0, 8);
                rutValidator = rutValidator.replace(" ", "");
                rutValidator = rutValidator.endsWith("K") ? rutValidator.replace("K", "0") : rutValidator;
                char dv = barcodeStr.substring(8, 9).charAt(0);
                boolean isvalid = ValidarRut(Integer.parseInt(rutValidator), dv);
                if (isvalid)
                    barcodeStr = rutValidator;
                else {
                    // Try validate rut size below 10.000.000
                    rutValidator = barcodeStr.substring(0, 7);
                    rutValidator = rutValidator.replace(" ", "");
                    rutValidator = rutValidator.endsWith("K") ? rutValidator.replace("K", "0") : rutValidator;
                    dv = barcodeStr.substring(7, 8).charAt(0);
                    isvalid = ValidarRut(Integer.parseInt(rutValidator), dv);
                    if (isvalid)
                        barcodeStr = rutValidator;
                    else {
                        barcodeStr = "";
                        TextViewStatus.setText("RUT INVALIDO");
                    }
                }
                // Get name from DNI.
                String[] array = rawCode.split("\\s+"); // Split by whitespace.
                try {
                    TextViewFullname.setText(array[1].substring(0, array[1].indexOf("CHL")));
                } catch (ArrayIndexOutOfBoundsException e) {
                    TextViewFullname.setText(array[2].substring(0, array[2].indexOf("CHL")));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    TextViewFullname.setText("");
                }
                PeopleValidator(barcodeStr, "", "", barcodeType);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class asyncTask_updatePeopleManifest extends AsyncTask<Void, Void, Integer> {
        private int update_manifest_count;

        @Override
        protected Integer doInBackground(Void... params) {
            return update_manifest_count = Asyntask_insertNewPeopleManifest();

        }

        @Override
        protected void onPostExecute(Integer integer) {
           /* if (integer > 0) {
                TextViewManifestUpdate.setTextColor(Color.WHITE);
                TextViewManifestUpdate.setText("Manifiesto actualizado: " + TextViewTimePeople + " hrs.");
            }*/
        }
    }

    public class AsyncUpdateStateManifest extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //set in 1000 miliseconds to timeout connection
            final OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .writeTimeout(0, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .build();
            getUpdateStates(client);
            return null;
        }
    }


    /**
     * Add new People in manifest, according to datetime in PDA
     * the difference between this and the getmanifest from configuration is that only update the manifest, as you can see
     * the endpoint is different
     *
     * @param Url
     * @param ID_route
     * @return content JSON to insert
     * @throws IOException
     */
    public String Asyntask_insertNewPeopleManifest(String Url, int ID_route) throws IOException {
        //http://axxezo-test.brazilsouth.cloudapp.azure.com:9001/api/manifests?itinerary=1824&date=2017-04-14T10:44:00
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        updateTimePeople = db.selectFirst("select date_last_update from config");
        URL url = new URL(Url + "/manifests?itinerary=" + ID_route + "&date=" + updateTimePeople);
        String content = "";
        HttpURLConnection conn = null;
        try {
            // Log.e("URL async_new People", url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("TOKEN", token_navieraAustral);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.connect();

            int connStatus = conn.getResponseCode();
            InputStream getData = conn.getInputStream();
            if (connStatus != 200) {
                content = String.valueOf(getData);
            } else {
                content = convertInputStreamToString(getData);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (conn != null) {
            conn.disconnect();
        }
        if (content.length() <= 2) { //[]
            content = "204"; // No content
        }
        //finally updating
        return content;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    private void exitApp() {
        this.finishAffinity();
    }

    /**
     * Return value count of DB, corresponding to embark,landed and pending
     *
     * @param position= 1(manifestcount),2(pendingCount),3(embarkedCount),4(landedCount)
     * @return count=return count of selected position
     */
    public int getCountEDP(int position) {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        int manifestCount = -1;
        int PendingCount = -1;
        int EmbarkedCount = -1;
        int LandedCount = -1;
        Cursor getCountsofDB = db.select("select (select count(*) from manifest)," +
                "(select count(*) from manifest where is_inside=0),(select count(*) from manifest where is_inside=1)," +
                "(select count(*) from manifest where is_inside=2)");
        int count = 0;
        if (getCountsofDB != null && getCountsofDB.getCount() > 0) {
            manifestCount = getCountsofDB.getInt(0);
            PendingCount = getCountsofDB.getInt(1);
            EmbarkedCount = getCountsofDB.getInt(2);
            LandedCount = getCountsofDB.getInt(3);
        }
        switch (position) {
            case 1:
                count = manifestCount;
                break;
            case 2:
                count = PendingCount;
                break;
            case 3:
                count = EmbarkedCount;
                break;
            case 4:
                count = LandedCount;
                break;
        }
        if (getCountsofDB != null)
            getCountsofDB.close();
        return count;
    }

    public class getAPIInformation extends AsyncTask<String, Void, String> {
        private String URL;
        private String getInformation;
        private int flag;
        private int route;
        private int port;

        getAPIInformation(String URL, int route, int flag) {//manifest
            this.URL = URL;
            this.route = route;
            getInformation = "";
            this.flag = flag;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                switch (flag) {
                    case 0:
                        getInformation = Asyntask_insertNewPeopleManifest(URL, route);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return getInformation;
        }

        @Override
        public String toString() {
            return getInformation + "";
        }
    }

    /**
     * Get list from api, then, take each documentId and find this in manifest table
     * if found it, compare state that is entering with state in db
     */
    public void getUpdateStates(OkHttpClient client) {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Slack slack=new Slack(this);
        Cursor itinerary = db.select("select route_id from config");
        if (itinerary.getCount() > 0) {
            String url = AxxezoAPI + "/registers/status?itinerary=" + itinerary.getInt(0);
            String result = "";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = null;
            JSONArray jsonArray = null;

            if (itinerary != null)
                itinerary.close();
            // Obtaining json array with states from endpoint
            try {
                response = client.newCall(request).execute();
                if (response != null) {
                    try {
                        result = response.body().string();
                        jsonArray = new JSONArray(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    result = response.code() + "";
                }
                if (response != null)
                    response.close();
            } catch (IOException e) {
                AsynTask_UpdateStateManifest.cancel(true);
            }

            // Process JsonArray, ask each jsonobject if exist in manifest table and compare states
            if (jsonArray != null && jsonArray.length() > 0) {
                JSONObject person_information;
                String getInside;
                String dni_json;
                String origin;
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        person_information = jsonArray.getJSONObject(i);
                        dni_json = person_information.getString("documentId");
                        origin = person_information.getString("origin");
                        if (dni_json.contains("-"))
                            dni_json = dni_json.substring(0, dni_json.indexOf("-"));
                        getInside = db.selectFirst("select is_inside from manifest where id_people='" + dni_json + "'");

                        if (!getInside.isEmpty() && (!getInside.equals(person_information.getString("state"))) && !origin.isEmpty()) {
                            db.insert("update manifest set is_inside='" + person_information.getString("state") +
                                    "' where id_people='" + dni_json.trim().toUpperCase() + "' and origin='" + origin + "'");
                        }
                    } catch (JSONException e) {
                        slack.sendMessage("Json format",e.getMessage() + "\nMainActivity Line: " + new Throwable().getStackTrace()[0].getLineNumber());
                    }
                }
            }
        }

    }

    /**
     * Asyntask to play sounds in background
     * 1 Error
     * 2 Permitted
     * 3 Denied
     * 4 stop all
     */
    private class LoadSound extends AsyncTask<Void, Void, Void> {
        private int typeSound = -1;

        public LoadSound(int typeSound) {
            this.typeSound = typeSound;
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (typeSound) {
                case 1:
                    if (mp3Error.isPlaying()) mp3Error.pause();
                    mp3Error.seekTo(0);
                    mp3Error.start();
                    break;
                case 2:
                    if (mp3Permitted.isPlaying()) mp3Permitted.pause();
                    mp3Permitted.seekTo(0);
                    mp3Permitted.start();
                    break;
                case 3:
                    if (mp3Dennied.isPlaying()) mp3Dennied.pause();
                    mp3Dennied.seekTo(0);
                    mp3Dennied.start();
                    break;
                case 4:
                    if (mp3Error.isPlaying()) mp3Error.pause();
                    mp3Error.seekTo(0);
                    if (mp3Dennied.isPlaying()) mp3Dennied.pause();
                    mp3Dennied.seekTo(0);
                    if (mp3Permitted.isPlaying()) mp3Permitted.pause();
                    mp3Permitted.seekTo(0);
                    break;
            }
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /* protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (comboLanded != null && comboLanded.getCount() > 0)

            outState.putInt("combolanded", selectedIntSpinnerLanded);
        Log.e("state spinner", "" + selectedIntSpinnerLanded);
    }*/

    private void killAyntask(boolean state) {
        Asynctask_sendRecord.cancel(state); // Asyntask that send data to api axxezo
        AsyncTask_updatePeopleManifest.cancel(state);// Asyntask to update in realtime new people inserts in manifest
        AsynTask_UpdateStateManifest.cancel(state);// Asyntask to update states of people insert in manifest table;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            fillSpinner();
        }
    }
    static BarcodeReader getBarcodeObject() {
        return barcodeReader;
    }


}
