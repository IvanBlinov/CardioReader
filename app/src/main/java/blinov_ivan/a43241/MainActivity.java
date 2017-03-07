package blinov_ivan.a43241;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends ListActivity {


    public final static String Server_URL = "https://zinoviy.localtunnel.me/api/add";
    public final static String UUID = "00001101-0000-1000-8000-00805F9B34FB";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    //e91521df-92b9-47bf-96d5-c52ee838f6f6
    private class WriteTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... args) {
            try {
                clientThread.getCommunicator().write(args[0]);
            } catch (Exception e) {
                Log.d("MainActivity", e.getClass().getSimpleName() + " " + e.getLocalizedMessage());
            }
            return null;
        }
    }


    private final static int REQUEST_ENABLE_BT = 1;
    private final static String DB_NAME = "CardioTable";
    private final static String LOG_TAG = "CardioSQL";

    /*private DBHelper dbHelper;
    private SQLiteDatabase db;*/

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver discoverDevicesReceiver;
    private BroadcastReceiver discoveryFinishedReceiver;

    private final List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();

    private ArrayAdapter<BluetoothDevice> listAdapter;

    private TextView textData;
    private EditText textMessage;

    private ProgressDialog progressDialog;

    private ServerThread serverThread;

    private ClientThread clientThread;

    JSONObject jsonObject = new JSONObject();

    AsyncHttpClient Aclient = new AsyncHttpClient();
    RequestParams params = new RequestParams();

    int count = 0;
    String json = "{\"impulses\":[";
    //String json = "";
    ArrayList<Pair<Integer, Integer>> buffer;

    String createJson(ArrayList<Pair<Long, Integer>> array) throws Exception {
        JSONObject resultJSON = new JSONObject();
        JSONArray resultArray = new JSONArray();

        for (Pair<Long, Integer> pair : array) {
            JSONObject obj = new JSONObject();
            obj.put("time", pair.first);
            obj.put("value", pair.second);
            resultArray.put(obj);
        }
        resultJSON.put("impulses", resultArray);

        return resultJSON.toString();
    }

    private final CommunicatorService communicatorService = new CommunicatorService() {
        @Override
        public Communicator createCommunicatorThread(BluetoothSocket socket) {
            return new CommunicatorImpl(socket, new CommunicatorImpl.CommunicationListener() {
                @Override
                public void onMessage(final String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textData.setText(message);
                            long time = new Date().getTime();
                            // buffer.add(new Pair<Long, Integer>(time, Integer.parseInt(message)));
                            try {
                                if (count == 100) {

                                    StringEntity se = new StringEntity(json + "]}");

                                    Aclient.post(getBaseContext(), Server_URL, se, "application/json", new AsyncHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                            //Toast.makeText(getBaseContext(),"Success " + statusCode ,Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                            Toast.makeText(getBaseContext(), "Failure " + error.toString(), Toast.LENGTH_LONG).show();

                                        }
                                    });
                                    count = 0;
                                    json = "{\"impulses\":[";
                                } else {
                                    json += "{\"time\":" + time + ",\"value\":" + message.split(" ")[0] + "}";
                                    if (count != 99) {
                                        json += ",";
                                    }
                                    count++;
                                }
                            } catch (IOException e) {
                                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }
            });
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       /* dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();*/

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {

            textData = (TextView) findViewById(R.id.data_text);
            //textMessage = (EditText) findViewById(R.id.message_text);

            listAdapter = new ArrayAdapter<BluetoothDevice>(getBaseContext(), android.R.layout.simple_list_item_1, discoveredDevices) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    final BluetoothDevice device = getItem(position);
                    ((TextView) view.findViewById(android.R.id.text1)).setText(device.getName());
                    return view;
                }
            };
            setListAdapter(listAdapter);
        } else {
            // Bluetooth выключен. Предложим пользователю включить его.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void makeDiscoverable(View view) {
        Intent i = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(i);
    }

    public void discoverDevices(View view) {

        discoveredDevices.clear();
        listAdapter.notifyDataSetChanged();

        if (discoverDevicesReceiver == null) {
            discoverDevicesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        if (!discoveredDevices.contains(device)) {
                            discoveredDevices.add(device);
                            listAdapter.notifyDataSetChanged();
                        }
                        if (device.getName().equals("HC-06")) {
                            bluetoothAdapter.cancelDiscovery();
                        }
                    }
                }
            };
        }

        if (discoveryFinishedReceiver == null) {
            discoveryFinishedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    getListView().setEnabled(true);
                    if (progressDialog != null)
                        progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), "Поиск закончен. Выберите устройство для отправки ообщения.", Toast.LENGTH_LONG).show();
                    unregisterReceiver(discoveryFinishedReceiver);
                }
            };
        }

        registerReceiver(discoverDevicesReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(discoveryFinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        getListView().setEnabled(false);

        progressDialog = ProgressDialog.show(this, "Поиск устройств", "Подождите...");

        bluetoothAdapter.startDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        bluetoothAdapter.cancelDiscovery();

        if (discoverDevicesReceiver != null) {
            try {
                unregisterReceiver(discoverDevicesReceiver);
            } catch (Exception e) {
                Log.d("MainActivity", "Не удалось отключить ресивер " + discoverDevicesReceiver);
            }
        }

        if (clientThread != null) {
            clientThread.cancel();
        }
        if (serverThread != null) serverThread.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        serverThread = new ServerThread(communicatorService);
        serverThread.start();

        discoveredDevices.clear();
        listAdapter.notifyDataSetChanged();
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }*/

    public void onListItemClick(ListView parent, View v,
                                int position, long id) {
        if (clientThread != null) {
            clientThread.cancel();
        }

        BluetoothDevice deviceSelected = discoveredDevices.get(position);

        clientThread = new ClientThread(deviceSelected, communicatorService);
        clientThread.start();

        Toast.makeText(this, "Вы подключились к устройству \"" + discoveredDevices.get(position).getName() + "\"", Toast.LENGTH_SHORT).show();
    }

    public void sendMessage(View view) {
        if (clientThread != null) {
            new WriteTask().execute(textMessage.getText().toString());
            textMessage.setText("");
        } else {
            Toast.makeText(this, "Сначала выберите клиента", Toast.LENGTH_SHORT).show();
        }
    }

    /*class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");

            db.execSQL("create table " + DB_NAME + " ("
                    + "id integer primary key autoincrement," + "data text,"
                    + "time text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }*/

}


