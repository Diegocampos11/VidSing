package com.vidsing;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ffmpeg extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    public ffmpeg(){}//needed

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }//needed

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        //
        boolean inicio = intent.getExtras().getBoolean("inicio" );//si es primera vez que la ejecuto :)
        if ( inicio ) mServiceHandler = new ServiceHandler( mServiceLooper, true );//,
        else mServiceHandler = new ServiceHandler( mServiceLooper, intent.getExtras().getString("url") );
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage( msg );
        return START_REDELIVER_INTENT;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private String url = "";
        private boolean inicio = false;

        public ServiceHandler(Looper looper, String url){
            super(looper);
            this.url = url;
        }

        public ServiceHandler(Looper looper, boolean inicio){
            super(looper);
            this.inicio = inicio;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if ( ! inicio ) {
                    //ORIHGINAL...//or... :D Process sh = Runtime.getRuntime().exec( new String[]{"python", "youtube-dl", "-s", "-f", "bestaudio[ext=m4a]", "-g", "--no-check-certificate", url }, new String[] { "PATH=/data/data/com.yoump3/files/bin", "LD_LIBRARY_PATH=/data/data/com.yoump3/files/lib" }, null);
                    //EN CONSOLA VIRTUAL ES NECESARIO ESCRIBIR EXPORT ANTES :o youtube-dl -s -f bestaudio[ext=m4a] -g https://www.youtube.com/watch?v=mGQFZxIuURE > /data/data/com.vidsing/p.txt
                    /*
                        PARAMS command:
                        -s, --simulate                   Do not download the video and do not write
                        -e, --get-title                  Simulate, quiet but print title
                        -f, --format FORMAT              Video format code, see the "FORMAT
                        -g, --get-url                    Simulate, quiet but print URL<<---PRETTY NECESSARY jaja
                        --no-check-certificate           Suppress HTTPS certificate validation<<<--- Me peta de certificado no encontrado o algo asi xd
                     */
                    //PATH variable worked till android 6 maybe... in 8 and 9 didn't work-->Process sh = Runtime.getRuntime().exec(new String[]{"python", getBaseContext().getFilesDir().getPath() + "/youtube-dl", "-s", "-e", "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]", "-g", "--no-check-certificate", url}, new String[]{"PATH=" + getBaseContext().getFilesDir().getPath() + "/bin", "LD_LIBRARY_PATH=" + getBaseContext().getFilesDir().getPath() + "/lib"}, null);
                    Process sh = Runtime.getRuntime().exec(new String[]{ getBaseContext().getFilesDir().getPath() + "/bin/python", getBaseContext().getFilesDir().getPath() + "/youtube-dl", "-s", "-e", "-f", "18", "-g", "--no-check-certificate", url}, new String[]{ "LD_LIBRARY_PATH=" + getBaseContext().getFilesDir().getPath() + "/lib" }, null);
                    //inputstream so as to get the result of the command
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sh.getInputStream()))) {//el parentesis en el try es por que automaticamente cerrará el flujo ya que esta clase BUffered... implementa la interfaz  java.lang.AutoCloseable
                        while ( (line = bufferedReader.readLine() ) != null) {
                            stringBuilder.append( line + "\n" );//I will use the salto de linea to separate the URL's
                        }
                    }
                    sh.waitFor();
                    //ENVIAR RESPUESTA A ACTIVITY
                    final Intent activityIntent = new Intent("2");//accion a ejecutar en la activity
                    activityIntent.putExtra("res", stringBuilder.toString() );
                    getApplicationContext().sendBroadcast( activityIntent );
                }
                else{//primera ejecucion
                    String download_result = U_D.download( getBaseContext(), "https://github.com/diego1campos/ffmpeg/raw/master/TERMUX.zip", "TERMUX.zip" );//TERMUX649.zip
                    if ( download_result.equals( "ok" ) ){
                        String result = U_D.unzip( getBaseContext() );
                        try{
                            if ( result.equals("ok") ){//https://storage.googleapis.com/sacred-drive-234208.appspot.com/654987234/57c3f123194dfa82f0ae20c63d4e664018f59e9e708f32860f23313439020625-oPaOs0Uc45oPzFLqIX0r.mp4
                                result = U_D.download( getBaseContext(), "https://youtube-dl.org/downloads/latest/youtube-dl", "youtube-dl" );
                                if ( result.equals("ok") ) {
                                    //Runtime.getRuntime().exec("chmod -R 755 /data/data/com.vidsing");
                                    //Runtime.getRuntime().exec("chmod -R 755 " + getBaseContext().getFilesDir().getPath() );

                                    Runtime.getRuntime().exec("chmod -R 777 /data/data/com.vidsing");
                                    Runtime.getRuntime().exec("rm -f " + getBaseContext().getFilesDir().getPath() + "/TERMUX.zip" );
                                    Toast.makeText( getApplicationContext(), getString( R.string.firstExecutionSuccess ), Toast.LENGTH_LONG ).show();
                                }
                            }
                        }
                        catch(Exception e){
                            //System.out.println( "Error task down_unzip " + e.toString() );
                            Log.d( "ERROR", e.toString() );
                            e.printStackTrace();
                            showDialog( getString( R.string.errorTitle ), getString( R.string.errorUnzipDown ) );
                        }
                        //return result;
                    }
                    //else return download_result;
                }
                // Stop the service using the startId, so that we don't stop
                // the service in the middle of handling another job
                stopSelf( msg.arg1 );
            } catch ( Exception e) {
                Log.d( "ERROR", "SSS"+e.toString() );
                e.printStackTrace();
                showDialog( getString( R.string.errorTitle ), getString( R.string.errorDownload ) );
            }
        }

        private void showDialog( String title, String message ){
            AlertDialog.Builder builder = new AlertDialog.Builder( getBaseContext() );
            builder.setTitle( title );
            builder.setMessage( message );
            builder.setPositiveButton(getString( R.string.accept ), null);
            builder.show();
        }
    }
}
