package com.example.alumno.inspect_mimetype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

//discover tv devices
import Interfaces.LyricFinderListener;
import Models.LyricSaver;
import Models.Track;
import Utils.LyricHandler;
import io.resourcepool.ssdp.model.DiscoveryListener;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import io.resourcepool.ssdp.model.SsdpServiceAnnouncement;
import io.resourcepool.ssdp.client.SsdpClient;

public class MainActivity extends Activity implements View.OnClickListener, RetrieveFeedTask.AsyncResponse, ListView.OnItemClickListener, ListView.OnItemLongClickListener, NavigationView.OnNavigationItemSelectedListener{

    private EditText URL;
    private String url;
    private WebView web;
    private Button btncargar;
    private final ArrayList<String> list = new ArrayList<String>();
    private ArrayAdapter adapter = null;
    private NavigationView navigationView;
    //ssdp
    private SsdpClient client;
    private ArrayList<SsdpService> servicesFound = new ArrayList<SsdpService>();
    private String tvSelected;

    /*broadcasterReceiver*/
    public static final String TEXT_PLAIN = "text/plain";
    /*broadcasterReceiver codigo necesario para recibir codigo del servicio :D*/

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if ( action.equals("2") ) {//codigo que recibo desde el servicio
                Toast.makeText(getBaseContext(), "Hola amigos de youtube " /*+ intent.getExtras().get( "res" )*/, Toast.LENGTH_LONG).show();
                hideLoading();
                //withItems( null );
                Log.d("XXXX", intent.getStringExtra("res") );
                String res = intent.getStringExtra("res"), title = res.substring( 0, res.indexOf("\n") );
                //quito la url recien extraida, en este caso title
                res = res.substring( res.indexOf("\n") + 1 );
                String video = res.substring( 0, res.indexOf("\n") );
                //quito la url recien extraida, en este caso video
                res = res.substring( res.indexOf("\n") + 1 );
                String audio = res.substring( 0, res.indexOf("\n") );
                Log.d("XXXX", title );
                Log.d("XXXX", video );
                Log.d("XXXX", audio );
                sendData2TVSetSelected( null, 0, title, video, audio );
            }
            else {
                Toast.makeText(getBaseContext(), "SOY IO 1 " , Toast.LENGTH_LONG).show();
            }
        }
    };

    public void withItems(View view) {
        //
        client = SsdpClient.create();
        DiscoveryRequest networkStorageDevice = DiscoveryRequest.builder()
                .serviceType("urn:samsung.com:service:MultiScreenService:1")//header established in documentation
                .build();
        client.discoverServices(networkStorageDevice, new DiscoveryListener() {
            @Override
            public void onServiceDiscovered(SsdpService service) {
                Log.d("XXXXXX","Found service: " + service);
                java.net.URL obj = null;
                servicesFound.add( service );//.getRemoteIp().toString()
                /*try {
                    obj = new URL( "http://192.168.0.165:8001/app/Convergence_Tutorial_TV_/info" );
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                    int responseCode = con.getResponseCode();
                    Log.d( "GET", "GET Response Code :: " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // print result
                        System.out.println(response.toString());
                    } else {
                        Log.d( "GET","GET request not worked");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }

            @Override
            public void onServiceAnnouncement(SsdpServiceAnnouncement announcement) {
                Log.d("XXXXXX","Service announced something: " + announcement);
            }

            @Override
            public void onFailed(Exception ex) {
                Log.d("XXXXXX","FAILED: " + ex.toString());
            }
        });
        prueba();
    }

    public void prueba(){
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper mServiceLooper = thread.getLooper();
        Handlerxx hanslerxx = new Handlerxx( mServiceLooper );
        Message msg = hanslerxx.obtainMessage();
        msg.arg1 = 0;
        hanslerxx.sendMessage( msg );
    }

    private void registerBroadcastReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("1");
        intentFilter.addAction("2");
        registerReceiver( broadcastReceiver, intentFilter);
    }
    /*broadcasterReceiver codigo necesario para recibir codigo del servicio :D!!!!!*/

    /*broadcasterReceiver codigo necesario para enviar la info al servicio*/
    private void processSendIntent(final Intent intent) {
        final String action = intent.getAction();
        final String type = intent.getType();
        if ( Intent.ACTION_SEND.equals( action ) && type != null) {
            if ( TEXT_PLAIN.equals( type ) ) {
                Log.d( "YOUMP3", "ENVIADO AL SERVICIO" );
                //Toast.makeText(getBaseContext(), "New send xd!!", Toast.LENGTH_LONG).show();
                final String sharedText = intent.getStringExtra( Intent.EXTRA_TEXT );
                startService(new Intent( this, ffmpeg.class).putExtra("url", sharedText ) );
                showLoading();
                //withItems(null);
            }
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        processSendIntent(intent);
    }
    /*broadcasterReceiver codigo necesario para enviar la info al servicio*/

    /*broadcasterReceiver*/

    protected void showLoading() {
        changeLoadingVisibility(View.VISIBLE);
    }

    protected void hideLoading() {
        changeLoadingVisibility(View.GONE);
    }

    protected void changeLoadingVisibility(int visibility) {
        final View loading = findViewById( R.id.loading );
        if(loading != null) {
            loading.setVisibility(visibility);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //Layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btncargar = ( Button ) findViewById( R.id.btncargar );
        URL = ( EditText ) findViewById( R.id.txturl );
        /*lista*/
        ListView listview = (ListView) findViewById(R.id.list);//List
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);//Adaptader for list
        listview.setAdapter( adapter );//Set adaptader, lo coloco aqui por que la lista forma parte del diseño

        /*lista*/

        /*Listeners*/
        btncargar.setOnClickListener( this );
        listview.setOnItemClickListener( this );//List too
        listview.setOnItemLongClickListener( this );

        //Web--and settings
        web = ( WebView ) findViewById( R.id.web );
        //web.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);//Supuestamente ayudaba para reproducir el video
        /*web.setDownloadListener(new DownloadListener() {//Descargar
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Toast.makeText( getBaseContext(), url + " " + mimetype, Toast.LENGTH_SHORT ).show();
            }
        });*/
        //Detecto mime type pero por extension de archivo, por lo tanto mejor llamo a asynctask :D
        web.getSettings().setSupportMultipleWindows(true);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient( new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                if ( ! web.getUrl().equals( URL.getText() ) /*&& ! url.equals( web.getUrl() )*/ ) URL.setText( web.getUrl() );
                execute( new String[] { "content" , url } );
                System.out.println( url );
                return false;
            }
        } );
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg)
            {
                try{
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    //Context context = view.getContext();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                    view.getContext().startActivity(browserIntent);
                }
                catch( Exception e){
                    Toast.makeText( getBaseContext(), "error " + e, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        //Barra lateral
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem( R.id.m_item_web );

        /*url from youtube*/
        registerBroadcastReceiver();
        processSendIntent( getIntent() );
        /*yoump3*/
        //cargar pagina preestablecida
        if ( ! getIntent().getAction().equals( Intent.ACTION_SEND ) )
            web.loadUrl( PreferenceManager.getDefaultSharedPreferences(this).getString( Preferences.prefInicio, "http://animeflv.com") );

        //primera ejecucion :D
        primera_ejecucion();

        //showLoading();
        //withItems( null );
        //sendData2TVSetSelected(null, 0);
        //get lyrics
        LyricHandler.Find(new Track("God Is A Woman","Ariana Grande"), new LyricFinderListener() {

            @Override
            public void OnFound(LyricSaver lyricSaver) {
                Log.d("XXXX", "Found -> "+ lyricSaver);
                Log.d("XXXX", lyricSaver.getLyricTxt() );
            }

            @Override
            public void OnNotFound(Track track) {
                System.out.println("NotFound -> "+track);
            }
        });
    }

    public void primera_ejecucion(){
        try {
            //Toast.makeText(getBaseContext(), getBaseContext().getFilesDir().getPath(), Toast.LENGTH_LONG).show();
            if ( ! new java.io.File( getBaseContext().getFilesDir().getPath() + "/bin/python" ).exists() ) {//ffmpeg
                Toast.makeText(getBaseContext(), "Inicializando la aplicación, esto puede tardar unos minutos", Toast.LENGTH_LONG).show();
                //download files
                if ( U_D.isOnline( getBaseContext() ) ) startService( new Intent( this, ffmpeg.class ).putExtra("inicio", true ) );
                else Toast.makeText(getBaseContext(), "No hay conexión a internet.", Toast.LENGTH_LONG).show();
            }
            else{
                //Toast.makeText(getBaseContext(), "Verificando actualización", Toast.LENGTH_LONG).show();
                //new RetrieveFeedTask(this,this, new String[] { "actYOU" } ).execute( );
                Toast.makeText(getBaseContext(), "Ready!!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText( getBaseContext(), "Error primera ejecucion xd" + e.toString(), Toast.LENGTH_LONG ).show();
            e.printStackTrace();
        }
    }

    private void execute( String... params ){
        new RetrieveFeedTask(this, params ).execute();
    }

    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        try{
            this.navigateToUrl( this, list.get( position ) );
        }
        catch ( Exception e ){
            Toast.makeText( getBaseContext(), "Error" + e.toString(), Toast.LENGTH_SHORT ).show();
        }
    }

    public void navigateToUrl( Activity activity, String url ){
        Intent i = new Intent( Intent.ACTION_VIEW );
        i.setData(Uri.parse( url ) );
        activity.startActivity(i);
    }

    @Override
    public void onClick( View v ) {
        String URL_s = URL.getText().toString();
        if ( v.getId() == btncargar.getId() && ! URL_s.equals("") ) {
            web.loadUrl( URL_s );//"http://vjs.zencdn.net/v/oceans.mp4""http://" +
            //Ocultar teclDO
            InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(URL.getWindowToken(), 0);
        }
    }

    @Override
    public void processFinish(String type, String url) {/*no quise cambiar todos los parametros en todos lados xd*/
        if ( type != null ){
            if ( type.indexOf("video") != -1 || type.equals("application/x-mpegurl") || type.equals("application/vnd.apple.mpegurl") ) {
                Toast.makeText(getBaseContext(), type, Toast.LENGTH_SHORT).show();
                list.add(url);
                adapter.notifyDataSetChanged();
            }
            else if ( type.equals("result") ){
                String mensaje = "";
                if ( url.equals("[false]") ) mensaje = "Añadido con éxito";
                else if ( url.equals("[true]") ) mensaje = "URL repetida";
                else mensaje = url;
                Toast.makeText(getBaseContext(), mensaje , Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        View frame_body_main = findViewById( R.id.frame_body_main );
        View tab1 = findViewById( R.id.tab1 );
        View tab2 = findViewById( R.id.tab2 );
        if ( frame_body_main.getVisibility() == View.GONE ) frame_body_main.setVisibility( View.VISIBLE );
        else {
            if ( tab2.getVisibility() == View.VISIBLE ) {
                findViewById(R.id.tab2).setVisibility(View.GONE);
                findViewById(R.id.tab1).setVisibility(View.VISIBLE);
                navigationView.setCheckedItem(R.id.m_item_web);
            } else if ( tab1.getVisibility() == View.VISIBLE ) {
                findViewById(R.id.tab1).setVisibility(View.GONE);
                findViewById(R.id.tab2).setVisibility(View.VISIBLE);
                navigationView.setCheckedItem(R.id.m_item_videos);
            } else {
                if ( web.canGoBack() ) web.goBack();
                else super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        android.app.AlertDialog.Builder alertdialog = new android.app.AlertDialog.Builder( MainActivity.this );
        alertdialog.setTitle("Add to my web ;)");
        //final CharSequence[] items = {"Añadir", "Añadir y eliminar"};
        alertdialog.setItems( new CharSequence[]{"Añadir", "Añadir y eliminar"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                //1= url servidor, 2=campo, 3= url
                String url_servidor =  PreferenceManager.getDefaultSharedPreferences( getApplicationContext() ).getString( Preferences.prefServer, "https://diegowebpage.000webhostapp.com");
                if ( item == 0 ) execute( new String[] { "pass", url_servidor, "url", list.get(position) } );
                else execute( new String[] { "pass", url_servidor, "url_d", list.get(position) } );
            }
        });
        alertdialog.show();
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        View frame_body_main = findViewById( R.id.frame_body_main );
        View tab1 = findViewById( R.id.tab1 );
        View tab2 = findViewById( R.id.tab2 );
        int id = item.getItemId();
        if (id == R.id.m_item_videos) {
            tab1.setVisibility( View.GONE );
            tab2.setVisibility( View.VISIBLE );
            if ( frame_body_main.getVisibility() == View.GONE ) frame_body_main.setVisibility( View.VISIBLE );
        } else if (id == R.id.m_item_web) {
            tab1.setVisibility( View.VISIBLE );
            tab2.setVisibility( View.GONE );
            if ( frame_body_main.getVisibility() == View.GONE ) frame_body_main.setVisibility( View.VISIBLE );
        } else if (id == R.id.m_item_conf) {
            frame_body_main.setVisibility( View.GONE );
            getFragmentManager().beginTransaction().replace( R.id.contenedor, new Preferences() ).commit();
        }
        //Para ocultar el menu cuando presiono una opción
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendData2TVSetSelected(final String friendlyName, final int which ) {
        try {
            //Toast.makeText(getApplicationContext(), friendlyName + " selected", Toast.LENGTH_SHORT).show();
            //tvSelected = "http:/" + servicesFound.get(which).getRemoteIp().toString() + "/ws/app/VidSing/connect";
            //Toast.makeText(getApplicationContext(), tvSelected, Toast.LENGTH_LONG).show();
            //send to emulator
            HandlerThread thread = new HandlerThread("ServiceStartArguments",
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            Looper mServiceLooper = thread.getLooper();
            Handlerxx hanslerxx = new Handlerxx( mServiceLooper );
            Message msg = hanslerxx.obtainMessage();
            msg.arg1 = 1;
            hanslerxx.sendMessage( msg );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendData2TVSetSelected(final String friendlyName, final int which, String title, String video, String audio ) {
        try {
            //Toast.makeText(getApplicationContext(), friendlyName + " selected", Toast.LENGTH_SHORT).show();
            //tvSelected = "http:/" + servicesFound.get(which).getRemoteIp().toString() + "/ws/app/VidSing/connect";
            //Toast.makeText(getApplicationContext(), tvSelected, Toast.LENGTH_LONG).show();
            //send to emulator
            HandlerThread thread = new HandlerThread("ServiceStartArguments",
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            Looper mServiceLooper = thread.getLooper();
            Handlerxx hanslerxx = new Handlerxx( mServiceLooper, video, audio, title, title );
            Message msg = hanslerxx.obtainMessage();
            msg.arg1 = 1;
            hanslerxx.sendMessage( msg );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //error handler
    private final class Handlerxx extends Handler {
        private String video;
        private String audio;
        private String lyrics;
        private String title;

        public Handlerxx(Looper looper){
            super(looper);
        }

        public Handlerxx(Looper looper, String video){
            super(looper);
            this.video = video;
        }

        public Handlerxx(Looper looper, String video, String audio, String title, String lyrics ) {
            super(looper);
            this.video = video;
            this.audio = audio;
            this.title = title;
            this.lyrics = lyrics;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if ( msg.arg1 == 0 ) {
                    Thread.sleep(3000);//I do think that it takes a long time detect all devices in the network
                    client.stopDiscovery();//detengo busqueda

                    //extract friendly name
                    final String[] friendlyName = new String[servicesFound.size()];
                    for (int i = 0; i < servicesFound.size(); i++) {
                        URL obj = new URL(servicesFound.get(i).getLocation());
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                        con.setRequestMethod("GET");
                        con.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                        int responseCode = con.getResponseCode();
                        Log.d("XXXX", "GET Response Code :: " + responseCode);
                        if (responseCode == HttpURLConnection.HTTP_OK) { // success
                            BufferedReader in = new BufferedReader(new InputStreamReader(
                                    con.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();

                            // print result
                            String responseString = response.toString();
                            Log.d("XXXX", responseString);
                            //stored in the array
                            friendlyName[i] = responseString.substring(responseString.indexOf("<friendlyName>") + 14, responseString.indexOf("</friendlyName>"));
                        } else {
                            Log.d("GET", "GET request not worked");
                        }
                    }


                    /*final String[] items = new String[ servicesFound.size() ];
                    for( int i = 0; i < servicesFound.size(); i++ ){//for ( SsdpService service : servicesFound ) {
                        items[i] = servicesFound.get( i ).getRemoteIp().toString();
                    }*/
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("List of Samsung TV's")

                            .setItems(friendlyName, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    sendData2TVSetSelected( friendlyName[which], which);
                                }
                            });

                    builder.setPositiveButton("OK", null);
                    builder.setNegativeButton("CANCEL", null);
                    //builder.setNeutralButton("NEUTRAL", null);
                    //builder.setPositiveButtonIcon(getResources().getDrawable(android.R.drawable.ic_menu_call, getTheme()));
                    builder.setIcon(getResources().getDrawable(R.drawable.ic_menu_manage, getTheme()));

                    AlertDialog alertDialog = builder.create();

                    alertDialog.show();

                    Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    button.setBackgroundColor(Color.BLACK);
                    button.setPadding(0, 0, 20, 0);
                    button.setTextColor(Color.WHITE);
                }
                else{



                    URL obj = new URL(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Preferences.prefServer, "http://192.168.42.88:8080/ws/app/VidSing") + "/connect");
                    Log.d("XXXX", "URL" + PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Preferences.prefServer, "http://192.168.42.88:8080/ws/app/VidSing") + "/connect");
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("POST");
                    //con.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                    //headers needed
                    con.setRequestProperty("SLDeviceID", "12345");
                    con.setRequestProperty("VendorID", "VenderMe");
                    con.setRequestProperty("DeviceName", "ANDROID");
                    con.setRequestProperty("GroupID", "feiGroup");
                    con.setRequestProperty("ProductID", "SMARTDev");
                    int responseCode = con.getResponseCode();
                    Log.d("XXXX", "POST connect Response Code :: " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) { // success
                        Log.d("XXXX", "SUCCESS POST connect");
                        //send json with data :D
                        Thread.sleep(3000);
                        obj = new URL(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Preferences.prefServer, "http://192.168.42.88:8080/ws/app/VidSing") + "/queue");
                        Log.d("XXXX", "URL" + PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Preferences.prefServer, "http://192.168.42.88:8080/ws/app/VidSing") + "/queue");
                        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                        conn.setRequestMethod("POST");
                        //conn.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                        //headers needed
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("SLDeviceID", "12345");
                        //JSON data
                        JSONObject data = new JSONObject();
                        data.put("video", video );
                        data.put( "audio", audio );
                        data.put( "title", title );


                        data.put( "lyrics", lyrics );
                        //
                        Log.d("XXXXV", video);
                        Log.d("XXXXA", audio);
                        //send JSON
                        OutputStreamWriter wr = new OutputStreamWriter( conn.getOutputStream() );
                        wr.write( data.toString() );
                        wr.flush();

                        //read response from server :')
                        int responseCodes = conn.getResponseCode();
                        Log.d("XXXX", "POST quee Response Code :: " + responseCode);
                        if ( responseCodes == HttpURLConnection.HTTP_OK ) { // success
                            Log.d("XXXX", "SUCCESS POST quee");
                        } else {
                            Log.d("XXXX", "POST quee not worked");
                        }
                    } else {
                        Log.d("XXXX", "POST connect not worked");
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
        }
    }
}