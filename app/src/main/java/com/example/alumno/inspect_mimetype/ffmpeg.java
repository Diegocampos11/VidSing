package com.example.alumno.inspect_mimetype;

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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ffmpeg extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    public ffmpeg(){}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private String url = "";
        private boolean inicio = false;
        //private int notId = 0;//inicio variable para realizar la configuracion de inicio o no xd

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
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                if ( ! inicio ) {
                    //ORIHGINAL...//or... :D Process sh = Runtime.getRuntime().exec( new String[]{"python", "youtube-dl", "-s", "-f", "bestaudio[ext=m4a]", "-g", "--no-check-certificate", url }, new String[] { "PATH=/data/data/com.yoump3/files/bin", "LD_LIBRARY_PATH=/data/data/com.yoump3/files/lib" }, null);
                    //EN CONSOLA VIRTUAL ES NECESARIO ESCRIBIR EXPORT ANTES :o youtube-dl -s -f bestaudio[ext=m4a] -g https://www.youtube.com/watch?v=mGQFZxIuURE > /data/data/com.vidsing/p.txt
                    Process sh = Runtime.getRuntime().exec(new String[]{"python", getBaseContext().getFilesDir().getPath() + "/youtube-dl", "-s", "-f", "bestaudio[ext=m4a]", "-g", "--no-check-certificate", url}, new String[]{"PATH=" + getBaseContext().getFilesDir().getPath() + "/bin", "LD_LIBRARY_PATH=" + getBaseContext().getFilesDir().getPath() + "/lib"}, null);

                    //try{
                        //Process sh = Runtime.getRuntime().exec("su", new String[]{"PATH=$PATH:" + getBaseContext().getFilesDir().getPath() + "/bin", "LD_LIBRARY_PATH=" + getBaseContext().getFilesDir().getPath() + "/lib"}, null);
                        //DataOutputStream outputStream = new DataOutputStream(sh.getOutputStream());

                        //outputStream.writeBytes("python " + getBaseContext().getFilesDir().getPath() + "/youtube-dl -s -f bestaudio[ext=m4a] -g --no-check-certificate" + url );
                        //outputStream.flush();

                        //outputStream.writeBytes("exit\n");
                        //outputStream.flush();
                        //sh.waitFor();
                    /*}catch(IOException e){
                        throw new Exception(e);
                    }catch(InterruptedException e){
                        throw new Exception(e);
                    }*/
                    //or... :D
                    //Process sh = Runtime.getRuntime().exec( new String[]{"python", "-m", "youtube_dl", "-s", "-f", "bestaudio[ext=m4a]", "-g", "--no-check-certificate", url }, new String[] { "PATH=" + getBaseContext().getFilesDir().getPath() + "/bin", "LD_LIBRARY_PATH=" + getBaseContext().getFilesDir().getPath() + "/lib" }, null);
                    //inputstream
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sh.getInputStream()))) {//el parentesis en el try es por que automaticamente cerrará el flujo ya que esta clase BUffered... implementa la interfaz  java.lang.AutoCloseable
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                    }
                    //inputstream
                    sh.waitFor();
                    Toast.makeText(getApplicationContext(), "finish! xd " + stringBuilder.toString(), Toast.LENGTH_LONG).show();
                    String retorno = httpHandler.post(new String[]{"https://diegowebpage.000webhostapp.com/ajax/ajax.php", "url_d", stringBuilder.toString()});

                    //ENVIAR RESPUESTA A ACTIVITY

                    final Intent activityIntent = new Intent("2");//accion a ejecutar en la activity
                    activityIntent.putExtra("res", stringBuilder.toString());
                    getApplicationContext().sendBroadcast(activityIntent);

                    //ENVIAR RESPUESTA A ACTIVITY

                    Toast.makeText(getApplicationContext(), "finish! xd " + (retorno.equals("[false]") ? "Añadido con éxito :')" : "No añadido :o"), Toast.LENGTH_LONG).show();
                    Log.d("INFO :D", stringBuilder.toString());
                    //os.close();
                    //z
                    //System.out.println( "finish! xd" );
                    //showNotification( ( new Intent(Intent.ACTION_VIEW) ), url, notId, false );
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
                                    Runtime.getRuntime().exec("chmod -R 777 " + getBaseContext().getFilesDir().getPath() );
                                    Runtime.getRuntime().exec("chmod -R a+x " + getBaseContext().getFilesDir().getPath() );
                                    //Runtime.getRuntime().exec("chmod -R u+x " + getBaseContext().getFilesDir().getPath() );
                                    Runtime.getRuntime().exec("rm -f " + getBaseContext().getFilesDir().getPath() + "/TERMUX.zip" );
                                    Toast.makeText( getApplicationContext(), "Todo correcto!! :D", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        catch(Exception e){
                            //System.out.println( "Error task down_unzip " + e.toString() );
                            Log.d( "ERROR", e.toString() );
                            e.printStackTrace();
                            Toast.makeText( getApplicationContext(), "Error task down_unzip ", Toast.LENGTH_LONG).show();
                        }
                        //return result;
                    }
                    //else return download_result;
                }
                // Stop the service using the startId, so that we don't stop
                // the service in the middle of handling another job
                stopSelf( msg.arg1 );
            } catch (	java.lang.SecurityException e) {
                Log.d( "ERROR", "SSS"+e.toString() );
                e.printStackTrace();
                Toast.makeText( getApplicationContext(), "SSSSSContact with Diego :o, download error", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.d( "ERROR", e.toString() );
                e.printStackTrace();
                Toast.makeText( getApplicationContext(), "Contact with Diego :o, download error", Toast.LENGTH_LONG).show();
            } catch (InterruptedException e) {
                Log.d( "ERROR", e.toString() );
                e.printStackTrace();
                Toast.makeText( getApplicationContext(), "Contact with Diego :o, download error", Toast.LENGTH_LONG).show();
            }
            //ip del servidor, _POST['videoInfo'], valor a enviar
            /*String retorno = httpHandler.post( new String[] { "https://pruebas-diego-campos.c9users.io/VideoInfo.php", "videoInfo", url } );
            Log.d( "YOUMP3", url );
            Log.d( "YOUMP3", retorno );
            Toast.makeText( getBaseContext(), retorno, Toast.LENGTH_LONG ).show();*/
        }
    }

    public void showNotification(Intent i, String nombre_song, int notId, boolean error ) {
        Notification.Builder mBuilder;
        NotificationManager mNotifyMgr =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        int icono = R.drawable.ic_launcher_foreground;
        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
        //reproduce automatimcamente la cancion despues de descargarla :o
        //getApplicationContext().startActivity(i);
        mBuilder =new Notification.Builder( getApplicationContext() )
                .setContentIntent( pendingIntent )
                .setLargeIcon( BitmapFactory.decodeResource( getResources(), icono) )
                .setSmallIcon(icono)
                .setContentTitle( ( error ) ? "Error al descargar" : "Descarga terminada" )
                .setContentText( ( error ) ? "Espacio insuficiente para almacenar la canción" : nombre_song )
                .setVibrate(new long[] {100, 250, 100, 500})
                .setAutoCancel(true);
        mNotifyMgr.notify( notId, mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        //
        boolean inicio = intent.getExtras().getBoolean("inicio" );//si es primera vez que la ejecuto :)
        if ( inicio ) mServiceHandler = new ServiceHandler( mServiceLooper, inicio );//,
        else mServiceHandler = new ServiceHandler( mServiceLooper, intent.getExtras().getString("url") );
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage( msg );
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        System.out.println("Service stopping");
    }
}
