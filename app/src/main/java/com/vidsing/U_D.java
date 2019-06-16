package com.vidsing;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by darka on 7/1/2018.
 */

public class U_D {
    /*private static File _zipFile = new File("/data/data/com.yoump3/VidSingService.zip");
    private static InputStream _zipFileStream;
    private static final String ROOT_LOCATION = "/data/data/com.yoump3/VidSingService";
    private static final String TAG = "UNZIPUTIL";*/

    public static String unzip(Context context ){
        File zipFile = new File(  context.getFilesDir().getPath() + "/TERMUX.zip");
        File targetDirectory = new File( context.getFilesDir().getPath() );//com.yoump3.files
        targetDirectory.mkdirs();
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(
                    new BufferedInputStream( new FileInputStream(zipFile) ) );
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ( ( ze = zis.getNextEntry() ) != null ) {
                Log.d( "YOUMP3", ze.getName() );
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if ( !dir.isDirectory() && !dir.mkdirs() )
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if ( ze.isDirectory() )
                    continue;
                FileOutputStream fout = new FileOutputStream( file );
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } catch ( Exception e ){
            e.printStackTrace();
            return "bad";
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
                return "bad";
            }
        }
        Log.d( "YOUMP3", "Unzip finished!! :D" );
        return "ok";
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public static String download(Context context, String website, String nombreAGuardar ){
        int count;
        try {
            URL url = new URL( website );
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream( new File( context.getFilesDir().getPath(), nombreAGuardar ) );

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            return "ok";

        } catch (Exception e) {
            return "Error download " + e.getMessage();
        }
    }
}


/*original
*
*
*
* */