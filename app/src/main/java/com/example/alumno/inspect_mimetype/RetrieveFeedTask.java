package com.example.alumno.inspect_mimetype;

import android.os.AsyncTask;
import android.widget.Toast;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Alumno on 10/11/2017.
 */

public class RetrieveFeedTask extends AsyncTask<String, Void, String>{

    private String type = "";
    private String [] params;

    public AsyncResponse delegate = null;

    // Define interfaz.
    public interface AsyncResponse {
        void processFinish(String output, String url);
    }

    public RetrieveFeedTask(AsyncResponse delegate, String... params){
        this.delegate = delegate;
        this.params = params;
    }

    @Override
    protected String doInBackground(String... paramss) {
        if ( params[0].equals("content") ) {
            try {
                java.net.URL url = new java.net.URL( params[1] );
                URLConnection u = url.openConnection();
                //long length = Long.parseLong(u.getHeaderField("Content-Length"));
                type = u.getHeaderField("Content-Type");
            } catch (Exception e) {
                type = e.toString();
            }
        }
        else{
            //1= ip, 2=campo, 3= url
            type = httpHandler.post( new String[] { params[1] + "/ajax/ajax.php", params[2], params[3] } );
        }
        return type;
    }

    @Override
    protected void onPostExecute(String result) {
        if ( params[0].equals("content") ) delegate.processFinish( type, params[1] );
        else delegate.processFinish( "result", result );
    }

}