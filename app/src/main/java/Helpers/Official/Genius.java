
package Helpers.Official;

import android.util.Log;

import Models.Lyrics;
import Utils.HelperType;

import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

//import static Utils.SearchUtil.google;

public class Genius {

    //@Reflection
    public static final String domain = "genius.com";

    public static Lyrics Find(String wholeTitle ) {
        String url = "";
        try{
            String geniusRequest = "https://api.genius.com/search?access_token=FfgPFFMHnRqh4d0Yo4NvxkdqDMK1BnlWCxm7GnXUSLtS9dLB2u3ZVhkEB2h6qeZX&q=" + wholeTitle.replaceAll(" ", "%20").replaceAll( "&", "%26" );
            Log.d("XXXX", "GEnius Request" + geniusRequest);
            URL obj = new URL( geniusRequest );
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            //headers needed
            //conn.setRequestProperty("Content-Type", "application/json");
            int responseCodes = conn.getResponseCode();
            Log.d("XXXX", "GET GENIUS Response Code :: " + responseCodes);
            if ( responseCodes == HttpURLConnection.HTTP_OK ) { // success
                Log.d("XXXX", "GET GENIUS XD");
                BufferedReader in = new BufferedReader(new InputStreamReader( conn.getInputStream() ) );
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                String responseString = response.toString();
                Log.d("XXXX", responseString);
                //pass to json
                JSONObject jsonRes = new JSONObject( response.toString() );
                url = ( ( JSONObject ) jsonRes.getJSONObject("response").getJSONArray("hits").get(0) ).getJSONObject("result").getString("url");
                Log.d("XXXX", url );
                //( (JSONObject) jsonRes.get("response") ).get("hits").result.url;
            } else {
                Log.d("XXXX", "GET GENIUS not worked");
            }
        }
        catch( Exception e ){
            Log.d("XXXX", e.toString() );
            e.printStackTrace();
        }
        //
        return fromURL(url, wholeTitle, wholeTitle);
    }

    public static Lyrics Find2(String originalArtist, String originalTitle) throws UnsupportedEncodingException {

        String query=originalTitle+" "+originalArtist+" lyrics genius";


        String searchURL = "https://www.google.com.sv/" + URLEncoder
                .encode(query, StandardCharsets.UTF_8.toString())+"&num="+10;

        Document doc = null;
        try {
            doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
            Elements results = doc.select("h3.r > a");

            for (Element result : results) {
                String linkHref = result.attr("href").replace("/url?q=",
                        "");
                if(linkHref.contains(HelperType.genius))
                    return fromURL(linkHref,originalArtist,originalTitle);
            }
            return new Lyrics(Lyrics.NO_RESULT);
        } catch (IOException e) {
            return new Lyrics(Lyrics.ERROR);
        }
    }

    public static Lyrics fromURL(String url, String artist, String title) {
        Document lyricsPage;
        String text;
        try {
            lyricsPage = Jsoup.connect(url).userAgent(HttpConnection.DEFAULT_UA).get();
            Elements lyricsDiv = lyricsPage.select(".lyrics");
            if (lyricsDiv.isEmpty())
                throw new StringIndexOutOfBoundsException();
            else
                text = Jsoup.clean(lyricsDiv.html(), Whitelist.none().addTags("br")).trim();
        } catch (HttpStatusException e) {
            return new Lyrics(Lyrics.NO_RESULT);
        } catch (IOException | StringIndexOutOfBoundsException e) {
            return new Lyrics(Lyrics.ERROR);
        }
        if (artist == null) {
            title = lyricsPage.getElementsByClass("text_title").get(0).text();
            artist = lyricsPage.getElementsByClass("text_artist").get(0).text();
        }
        Lyrics result = new Lyrics(Lyrics.POSITIVE_RESULT);
        if ("[Instrumental]".equals(text))
            result = new Lyrics(Lyrics.NEGATIVE_RESULT);
        if (!isProbablyArabic(text)) {
            Pattern pattern = Pattern.compile("\\[.+]");
            StringBuilder builder = new StringBuilder();
            for (String line : text.split("<br> ")) {
                String strippedLine = line.replaceAll("\\s", "");
                if (!pattern.matcher(strippedLine).matches() && !(strippedLine.isEmpty() && builder.length() == 0))
                    builder.append(line.replaceAll("\\P{Print}", "")).append("<br/>");
            }
            if (builder.length() > 5)
                builder.delete(builder.length() - 5, builder.length());
            result.setArtist(artist);
            result.setTitle(title);
            result.setText(Normalizer.normalize(builder.toString(), Normalizer.Form.NFD));
            result.setURL(url);
            result.setSource("Genius");
            return result;
        } else {
            result = new Lyrics(Lyrics.POSITIVE_RESULT);
            result.setArtist(artist);
            result.setTitle(title);
            result.setText(Normalizer.normalize(text, Normalizer.Form.NFD));
            result.setURL(url);
            result.setSource("Genius");
        }
        return result;
    }

    private static boolean isProbablyArabic(String s) {

        for (int i = 0; i < s.length();) {
            int c = s.codePointAt(i);
            if (c >= 0x0600 && c <= 0x06E0)
                return true;
            i += Character.charCount(c);
        }
        return false;
    }

}
