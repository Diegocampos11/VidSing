package com.example.alumno.inspect_mimetype;

import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class httpHandler {

    public static String post(String... params){//(1= ip, 2=campo, 3= url) - 1
        try {
            HttpClient httpclient = new DefaultHttpClient();
            /*Creamos el objeto de HttpClient que nos permitira conectarnos mediante peticiones http*/
            HttpPost httppost = new HttpPost( params[0] );
            /*El objeto HttpPost permite que enviemos una peticion de tipo POST a una URL especificada*/
            //AÑADIR PARAMETROS
            List<NameValuePair> params_to_web = new ArrayList<NameValuePair>();
            params_to_web.add( new BasicNameValuePair( params[1], params[2] ) );
            /*params.add(new BasicNameValuePair("info","Otro mensaje"));*/
            /*Una vez añadidos los parametros actualizamos la entidad de httppost, esto quiere decir en pocas palabras anexamos los parametros al objeto para que al enviarse al servidor envien los datos que hemos añadido*/
            httppost.setEntity( new UrlEncodedFormEntity( params_to_web ) );

            /*Finalmente ejecutamos enviando la info al server*/
            HttpResponse resp = httpclient.execute(httppost);
            HttpEntity ent = resp.getEntity();/*y obtenemos una respuesta*/

            String text = EntityUtils.toString(ent);

            return text;
        }
        catch(Exception e) { return "error: " + e;}
    }

}
