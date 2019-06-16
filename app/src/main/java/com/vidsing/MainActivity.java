package com.vidsing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

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

public class MainActivity extends Activity implements ListView.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener, View.OnKeyListener {

    private String currentWebSite;
    private EditText txtURL;
    private WebView webView;
    private Button btnCargar;
    private final ArrayList<String> WebViewVideosFound = new ArrayList<String>();
    private ArrayAdapter arrayAdapterWebViewVideosFound = null;
    private ArrayAdapter arrayAdapterTVsFound;
    private NavigationView navigationView;
    //normal video{video, urlToCheck, type :)--- and before that the urls has to be checked out if ther're videos or not :)
    //--youtube{video, audio, title and lyrics}
    public String type, video, audio, title, lyrics;
    //ssdp
    private SsdpClient client;
    private ArrayList<SsdpService> servicesFound = new ArrayList<>();
    private List<String> friendlyNames = new ArrayList<>();
    private String TVSelected;
    public static final String TEXT_PLAIN = "text/plain";//share link from youtube or any other app xd

    /*broadcasterReceiver get data del servicio :D!!!!!*/
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals("2")) {//codigo que recibo desde el servicio
                //obtengo respuesta del servidor
                String res = intent.getStringExtra("res");
                //send to MyHandler and then to smart tv :D
                processInformationFromService(res);
            }
        }
    };
    /*broadcasterReceiver get data del servicio :D!!!!!*/

    public void processInformationFromService(String res) {/*This method is used due to the fact that the global variables must be set*/
        //extraigo el contenido en variables
        title = res.substring(0, res.indexOf("\n"));
        //quito la url recien extraida, en este caso title
        res = res.substring(res.indexOf("\n") + 1);
        video = res.substring(0, res.indexOf("\n"));
        //quito la url recien extraida, en este caso video
        res = res.substring(res.indexOf("\n") + 1);
 //        audio = res.substring(0, res.indexOf("\n"));----no needed anymore
        //remuevo parentesis (ABC) or [ABC] stuff-- ejemplo Bruno Mars - Finesse (Remix) [Official (CardiB] video]
        title = title.replaceAll("([\\[])+([\\s\\S])*([\\]])", "");
        title = title.replaceAll("([\\(])+([\\s\\S])*([\\)])", "");

        lyrics = "";//in case lyrics aren't found :)
        hideLoading();
        //then select smart tv
        selectSmartTV( 0 );
    }

    //share link from youtube or any other app xd

    private void registerBroadcastReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("1");
        intentFilter.addAction("2");
        registerReceiver( broadcastReceiver, intentFilter);
    }


    private void processSendIntent(final Intent intent) {
        final String action = intent.getAction();
        final String type = intent.getType();
        if ( Intent.ACTION_SEND.equals( action ) && type != null) {
            if ( TEXT_PLAIN.equals( type ) ) {
                Log.d( "YOUMP3", "ENVIADO AL SERVICIO" );
                //Toast.makeText(getBaseContext(), "New send xd!!", Toast.LENGTH_LONG).show();
                final String sharedText = intent.getStringExtra( Intent.EXTRA_TEXT );
                startService(new Intent( this, VidSingService.class).putExtra("url", sharedText ) );
                showLoading();
            }
        }
    }

    //share link from youtube or any other app xd

    protected void showLoading() {
        changeLoadingVisibility(View.VISIBLE);
    }

    protected void hideLoading() {
        changeLoadingVisibility(View.GONE);
    }

    protected void changeLoadingVisibility(int visibility) {
        View loading = findViewById( R.id.loading );
        if(loading != null) {
            loading.setVisibility(visibility);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCargar = findViewById( R.id.btncargar );
        txtURL = findViewById( R.id.txturl );
        //load the text needed in the "loading" progressBar
        ( (TextView) findViewById( R.id.loading ).findViewById( R.id.txtProgressBar ) ).setText("Cargando...");
        //lista
        ListView listview = findViewById(R.id.list);//List
        arrayAdapterWebViewVideosFound = new ArrayAdapter( this, android.R.layout.simple_list_item_1, WebViewVideosFound );//Adaptader for video WebViewVideosFound
        listview.setAdapter(arrayAdapterWebViewVideosFound);
        //listeners
        btnCargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCargarClick();
            }
        });
        txtURL.setOnKeyListener( this );
        listview.setOnItemClickListener( this );
//        listview.setOnItemLongClickListener( this );

        //Web--and settings
        webView = ( WebView ) findViewById( R.id.web );
        webView.getSettings().setSupportMultipleWindows( true );
        webView.getSettings().setJavaScriptEnabled( true );//needed to load videos with kind of advertisements
        //if the user attempts open a link
        webView.setWebChromeClient(new WebChromeClient() {
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
                    Toast.makeText( getBaseContext(), getString( R.string.errorTitle ) + ": " + e, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });


        //cargar pagina preestablecida. el if es para que no se cargue when the activity has received an intent :)
        if ( ! getIntent().getAction().equals( Intent.ACTION_SEND ) ){
            webView.loadUrl( PreferenceManager.getDefaultSharedPreferences(this ).getString( Preferences.prefInicio, "http://techslides.com/demos/sample-videos/small.mp4" ) );
            currentWebSite = webView.getUrl();
            txtURL.setText( currentWebSite );
        }

        //so as to analyze url from videos :)
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest urlToCheck ) {
                //set the new url if the website has changed by the user... because this method is executed like a thread, i decided not created one and just set this code here :)
                //if ( ! webView.getUrl().equals( currentWebSite ) ) txtURL.setText( webView.getUrl() );
                createHandler( 2, urlToCheck.getUrl().toString() );
                return null;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                txtURL.setText( url );
            }
        } );

        //Barra lateral
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener( this );
        navigationView.setCheckedItem( R.id.m_item_web );

        //primera ejecucion :D
        primeraEjecucion();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        /*set information to the service :D*/
        if ( getIntent().getStringExtra( Intent.EXTRA_TEXT ) != null ) {
            registerBroadcastReceiver();
            processSendIntent( getIntent() );
            getIntent().removeExtra( Intent.EXTRA_TEXT );
        }
        /*set information to the service :D*/
    }

    public void primeraEjecucion(){
        try {
            //Toast.makeText(getBaseContext(), getBaseContext().getFilesDir().getPath(), Toast.LENGTH_LONG).show();
            if ( ! new java.io.File( getBaseContext().getFilesDir().getPath() + "/bin/python" ).exists() ) {//VidSingService
                Toast.makeText(getBaseContext(), getString( R.string.appBeingInitialized ), Toast.LENGTH_LONG).show();
                //download files
                if ( U_D.isOnline( getBaseContext() ) ) startService( new Intent( this, VidSingService.class ).putExtra("inicio", true ) );
                else Toast.makeText(getBaseContext(), getString( R.string.errorMessNoInternet ), Toast.LENGTH_LONG).show();
            }
            else{
                //Toast.makeText(getBaseContext(), "Verificando actualización", Toast.LENGTH_LONG).show();
                //new RetrieveFeedTask(this,this, new String[] { "actYOU" } ).execute( );
//                Toast.makeText(getBaseContext(), "Ready!!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText( getBaseContext(), getString( R.string.errorFirstExecution ) + e.toString(), Toast.LENGTH_LONG ).show();
            e.printStackTrace();
        }
    }

//    private void execute( String... params ){
//        new RetrieveFeedTask(this, params ).execute();
//    }

    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        try{
            video = WebViewVideosFound.get( position );
            video = video.substring( video.indexOf("\n") + 1 );
            selectOptionSelectedVideo();
        }
        catch ( Exception e ){
            Toast.makeText( getBaseContext(), getString( R.string.errorTitle ) + ": " + e.toString(), Toast.LENGTH_SHORT ).show();
        }
    }

    public void openVideo( ){
        Intent i = new Intent( Intent.ACTION_VIEW );
        i.setData( Uri.parse( video ) );
        this.startActivity(i);
    }

    public void btnCargarClick() {
        loadUrl();
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if ( i == KeyEvent.KEYCODE_ENTER ) loadUrl();
        return false;
    }

    private void loadUrl(){
        String URL_s = txtURL.getText().toString();
        if ( ! URL_s.equals("") ) {
            webView.loadUrl( URL_s );
            //Ocultar teclDO
            InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtURL.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        //Para ocultar el menu cuando presiono una opción
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if ( drawer.isDrawerOpen( GravityCompat.START ) ){
            drawer.closeDrawer( GravityCompat.START );
        }
        else {
            View frame_body_main = findViewById(R.id.frame_body_main);
            View tab1 = findViewById(R.id.tab1);
            View tab2 = findViewById(R.id.tab2);
            //same application as if you selected the conf
            if (getFragmentManager().findFragmentById(R.id.contenedor) != null) {//, as a result there's a fragment
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.contenedor)).commit();//clean "all the fragments" in the container :D
                tab1.setVisibility(View.VISIBLE);
                tab2.setVisibility(View.GONE);
                frame_body_main.setVisibility(View.VISIBLE);
                navigationView.setCheckedItem(R.id.m_item_web);
            } else {
                if (tab2.getVisibility() == View.VISIBLE) {
                    tab1.setVisibility(View.VISIBLE);
                    tab2.setVisibility(View.GONE);
                    navigationView.setCheckedItem(R.id.m_item_web);
                } else if (tab1.getVisibility() == View.VISIBLE) {
                    if ( webView.canGoBack() ) webView.goBack();//if the user has surfaced to many webView pages and he really wants to go back
                    else super.onBackPressed();
                }
            }
        }
    }

    private void selectOptionSelectedVideo(){
        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
        builder.setTitle( getString( R.string.selectOption ) );
        //final CharSequence[] items = {"Añadir", "Añadir y eliminar"};
        builder.setItems( new CharSequence[]{ getString( R.string.selectTV ) , getString( R.string.openAnotherApp ) }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int res) {
                //1= url servidor, 2=campo, 3= url
//                String url_servidor =  PreferenceManager.getDefaultSharedPreferences( getApplicationContext() ).getString( Preferences.prefFontSize, "https://diegowebpage.000webhostapp.com");
                sendNormalVideo( res );  /*execute( new String[] { "pass", url_servidor, "url", WebViewVideosFound.get(position) } );*/
                /*else openVideo( WebViewVideosFound.get(position) );*//*execute( new String[] { "pass", url_servidor, "url_d", WebViewVideosFound.get(position) } );*/
            }
        });
        builder.show();
    }

    private void selectSmartTV( final int what ) {
        //clean the lists before adding new devices to it
        servicesFound = new ArrayList<>();
        friendlyNames = new ArrayList<>();
        arrayAdapterTVsFound = new ArrayAdapter( getBaseContext(), android.R.layout.simple_list_item_1, friendlyNames);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        //getting the layout to set it as the custom title
        LayoutInflater inflater = getLayoutInflater();
        View viewx = inflater.inflate(R.layout.loading_layout, null);
        //set the name to the title of the dialog
        ( (TextView) viewx.findViewById(R.id.txtProgressBar) ).setText( getString( R.string.selectTV ) );
        builder.setCustomTitle( viewx )
                .setAdapter(arrayAdapterTVsFound, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//                                    keepSearchingClients = true;//stop keep searching a TV
                        //set value to the variable so that i won't send params to the method
                        TVSelected = "http:/" + servicesFound.get( which ).getRemoteIp().toString() + ":8080/ws/app/VidSing";
                        if ( what == 0 ) createHandler( 0, null );
                        else createHandler( 1, null );
                    }
                })
                .setPositiveButton(getString( R.string.emulatorOption ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TVSelected =  "http://192.168.42.88:8080/ws/app/VidSing";
                        if ( what == 0 ) createHandler( 0, null );
                        else createHandler( 1, null );
                    }
                })
                .setNegativeButton(getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        client.stopDiscovery();
                        //keepSearchingClients = true;//detengo busqueda---IMPORTANTE, BLUCLE ENDLESS
                    }
                });
        //builder.setNeutralButton("NEUTRAL", null);
        //builder.setPositiveButtonIcon(getResources().getDrawable(android.R.drawable.ic_menu_call, getTheme()));
        //builder.setIcon(getResources().getDrawable(R.drawable.ic_menu_manage, getTheme()));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Log.d("XXXXXX", "Creating client");
        client = SsdpClient.create();
        DiscoveryRequest networkStorageDevice = DiscoveryRequest.builder()
                .serviceType("urn:samsung.com:service:MultiScreenService:1")//header established in documentation
                .build();
        client.discoverServices(networkStorageDevice, new DiscoveryListener() {
            @Override
            public void onServiceDiscovered(SsdpService service) {
                Log.d("XXXXXX", "found service: " + service);
                try {
                    URL obj = new URL( service.getLocation() );
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                    int responseCode = con.getResponseCode();
                    Log.d("XXXX", "GET response Code :: " + responseCode);
                    if ( responseCode == HttpURLConnection.HTTP_OK ) { // success
                        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream() ) );
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null)
                            response.append(inputLine);
                        in.close();
                        // print result
                        String responseString = response.toString();
                        Log.d("XXXX", responseString);
                        //stored in the array
                        boolean serviceFoundIsNoRepeated = true;
                        for ( SsdpService serviceStored : servicesFound ){//i don't know if the friendly name can be repeated; therefore, i'm working with .getLocation() instead... i do think that i could work with all the .toString() xd
                            if ( serviceStored.getLocation().equals( service.getLocation() ) ){
                                serviceFoundIsNoRepeated = false;
                                break;
                            }
                        }
                        if ( serviceFoundIsNoRepeated ) {//if it's not repeated in the ArrayList, add it
                            String friendNameFound = responseString.substring(responseString.indexOf("<friendlyName>") + 14, responseString.indexOf("</friendlyName>"));
                            servicesFound.add( service );
                            friendlyNames.add( friendNameFound );

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    arrayAdapterTVsFound.notifyDataSetChanged();
                                }
                            });
                            Log.d("XXXX", "SERVICE ADDED!" + service );
                        }
                        Log.d("XXXX", "GET DONE!");
                    } else {
                        Log.d("XXXX", "GET request not worked");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("XXXX", e.toString());
                }
            }

            @Override
            public void onServiceAnnouncement(SsdpServiceAnnouncement announcement) {
                Log.d("XXXXXX", "Service announced something: " + announcement);
            }

            @Override
            public void onFailed(Exception ex) {
                Log.d("XXXXXX", "FAILED: " + ex.toString());
            }
        });
    }

    private void sendNormalVideo( int res ){
        if ( res == 0 ){
            selectSmartTV( 1 );
        }
        else openVideo();
    }

    //param what:
    //0, the source is youtube
    //1, the source is internet
    //2, insect mimetype
    private void createHandler(int what, @Nullable String urlToCheck ){
        HandlerThread thread = new HandlerThread("ServiceStartArguments", android.os.Process.THREAD_PRIORITY_BACKGROUND );
        thread.start();
        Looper mServiceLooper = thread.getLooper();
        MyHandler handler = new MyHandler( mServiceLooper );
        Message msg = handler.obtainMessage();
        msg.what = what;
        if ( what == 2 ){
            msg.obj = urlToCheck;
        }
        handler.sendMessage( msg );
//        if ( what == 0 || what == 1 ){
//            handler.sendMessage( msg );
//        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        View frame_body_main = findViewById( R.id.frame_body_main );
        View tab1 = findViewById( R.id.tab1 );
        View tab2 = findViewById( R.id.tab2 );
        int id = item.getItemId();
        if ( id == R.id.m_item_videos ) {
            tab1.setVisibility(View.GONE);
            tab2.setVisibility(View.VISIBLE);
        }
        else if ( id == R.id.m_item_web ) {
            tab1.setVisibility(View.VISIBLE);
            tab2.setVisibility(View.GONE);
        }
        else if ( id == R.id.m_item_conf ) {
            frame_body_main.setVisibility( View.GONE );
            getFragmentManager().beginTransaction().replace( R.id.contenedor, new Preferences() ).commit();
        }
        else if ( id == R.id.m_item_help ){
            String pdf = "https://github.com/diego1campos/ffmpeg/raw/master/MANUAL%20DE%20USUARIO.pdf";
            webView.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + pdf);
            tab1.setVisibility(View.VISIBLE);
            tab2.setVisibility(View.GONE);
        }
        //if you didn't selected conf since you have just selected it
        if ( getFragmentManager().findFragmentById(R.id.contenedor) != null ){//, as a result there's a fragment
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.contenedor)).commit();//clean "all the fragments" in the container :D
            frame_body_main.setVisibility(View.VISIBLE);
        }
        //Para ocultar el menu cuando presiono una opción
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final class MyHandler extends Handler {

        private int what;

        public MyHandler( Looper looper ){
            super( looper );
        }

        @Override
        public void handleMessage(Message msg) {
            what = msg.what;
            switch ( what ){
                case 0:
                    getLyrics();
                    break;
                case 1:
                    sendDataToSmartTV();
                    break;
                case 2:
                    checkUrl( msg.obj.toString() );
                    break;
                default:
                    Toast.makeText( getBaseContext(), getString( R.string.notfound ), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        private void checkUrl( String urlToCheck ){
            try {
                java.net.URL url = new java.net.URL( urlToCheck );
                URLConnection u = url.openConnection();
                //                    Log.d("TIGGY2", msg.obj.toString() );
                type = u.getHeaderField("Content-Type");
                if ( type != null ) {
                    if ( type.indexOf("video") != -1 || type.equals("application/x-mpegurl") || type.equals("application/vnd.apple.mpegurl") ) {
                        boolean videoURLFoundIsNoRepeated = true;
                        for ( String videoURL : WebViewVideosFound )
                            if ( ! videoURL.equals( urlToCheck ) ) videoURLFoundIsNoRepeated = false;
                        if ( videoURLFoundIsNoRepeated ) {
                            Toast.makeText( getBaseContext(), getString( R.string.videoFound ) + type, Toast.LENGTH_LONG ).show();
                            WebViewVideosFound.add( "Mime-type: " + type + "\n" + urlToCheck );
                            arrayAdapterWebViewVideosFound.notifyDataSetChanged();
                        }
                    }
                }
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
        }

        private void getLyrics(){
            try {
                //obtener datos musica
                //getLyrics
                Log.d("XXXX", "Getting lyrics :o");
                LyricHandler.Find(new Track( title ), new LyricFinderListener() {
                    @Override
                    public void OnFound(LyricSaver lyricSaver) {
                        Log.d("XXXX", "Found -> " + lyricSaver);
                        Log.d("XXXX", lyricSaver.getLyricTxt());
                        lyrics = lyricSaver.getLyricTxt();
                        sendDataToSmartTV();
                    }

                    @Override
                    public void OnNotFound(Track track) {
                        System.out.println("NotFound -> " + track);
                        Log.d("XXXX", "NotFound -> " + track);
                        //if lyrics are not found, it will be display de title of the song
                        sendDataToSmartTV();
                    }
                });

            }
            catch( Exception e ){
                e.printStackTrace();
                Log.d("XXXX", e.toString() );
            }
        }

        private HttpURLConnection createRequest( String url, int typeRequest ){//0=POST, 1=GET
            HttpURLConnection connection = null;
            try {
                URL obj = new URL( url );
                connection = (HttpURLConnection) obj.openConnection();
                if( typeRequest == 0 ) connection.setRequestMethod("POST");
                else connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); //set timeout to 4 seconds
                //headers needed
                //for ( Map.Entry<String, String> entry : requestProperties.entrySet() ) connection.setRequestProperty( entry.getKey(), entry.getValue() );
            }
            catch( Exception e ){
                e.printStackTrace();
                Log.d("XXXXPP", e.toString() );
            }
            return connection;
        }

        private void sendDataToSmartTV(){
            try{
                //KNOW IF THE APP IS RUNNING
                HttpURLConnection con = createRequest( TVSelected + "/info", 1 );
                //con.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                int responseCode = con.getResponseCode();
                con.disconnect();
                Log.d("XXXX", "POST connect Response Code :: " + responseCode);
                if ( responseCode == HttpURLConnection.HTTP_OK ) {
                    //ESTABLISH THE CONNECTION BETWEEN THE APP AND THE SMART TV
                    con = createRequest( TVSelected + "/connect", 0 );
                    //con.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                    //headers needed
                    con.setRequestProperty("SLDeviceID", "12345");
                    con.setRequestProperty("VendorID", "VenderMe");
                    con.setRequestProperty("DeviceName", "ANDROID");
                    con.setRequestProperty("GroupID", "feiGroup");
                    con.setRequestProperty("ProductID", "SMARTDev");
                    responseCode = con.getResponseCode();
                    con.disconnect();
                    Log.d("XXXX", "POST connect Response Code :: " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) { // success
                        Log.d("XXXX", "SUCCESS POST connect");
                        //IT'S REALLY NEEDED TO WAIT. I do think that My TV is pretty slow therefore, we need to wait till the tv process the connection
                        Thread.sleep(2000);
                        con = createRequest(TVSelected + "/queue", 0 );
                        con.setRequestMethod("POST");
                        //conn.setRequestProperty("User-Agent", "Mozilla/5.0");//"curl/7.20.1 (i686-pc-cygwin) libcurl/7.20.1 OpenSSL/0.9.8r zlib/1.2.5 libidn/1.18 libssh2/1.2.5");
                        //headers needed
                        con.setRequestProperty("Content-Type", "application/json");
                        con.setRequestProperty("SLDeviceID", "12345");
                        //JSON data
                        JSONObject data = new JSONObject();
                        data.put("video", video);
                        if (what == 0) {
//                            data.put("audio", audio);
                            data.put("title", title);
                            data.put("lyrics", lyrics);
                            data.put("fontSize", PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(Preferences.prefFontSize, "26"));
                        } else {
                            Log.d("XXX", "what" + what);
                        }
                        //send JSON
                        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                        wr.write(data.toString());
                        wr.flush();

                        //read response from server :')
                        int responseCodes = con.getResponseCode();
                        con.disconnect();
                        Log.d("XXXX", "POST quee Response Code :: " + responseCodes);
                        if (responseCodes == HttpURLConnection.HTTP_OK) { // success
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog(getString( R.string.successTitle ), getString( R.string.successMessVideoSent) );
                                }
                            });
                            Log.d("XXXX", "SUCCESS POST quee");
                        } else {
                            Log.d("XXXX", "POST quee not worked");
                        }
                    } else {
                        Log.d("XXXX", "POST connect not worked");
                    }
                } else {
                    Log.d("XXXX", "GET info application response not OK");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog(getString(R.string.errorTitle), getString(R.string.errorMessAppNotBeingExecuted));
                        }
                    });
                }
            }
            catch( Exception e ){
                e.printStackTrace();
                Log.d("XXXXS", e.toString() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(getString( R.string.errorTitle ), getString( R.string.errorMessTimeExecutionLimit ) );
                    }
                });
            }
        }

        private void showDialog( String title, String message ){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle( title );
            builder.setMessage( message );
            builder.setPositiveButton(getString( R.string.accept ), null);
            builder.show();
        }
    }
}