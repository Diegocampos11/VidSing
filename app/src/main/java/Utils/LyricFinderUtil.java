package Utils;

import Helpers.Official.*;
import Models.Lyrics;
import org.jsoup.Jsoup;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Models.Lyrics.NO_RESULT;

public class LyricFinderUtil {

    public static Lyrics getLyric(String wholeTitle ){
        try {
            Lyrics lyrics = new Lyrics(NO_RESULT);
            lyrics = Genius.Find( wholeTitle );
            if(IsFound(lyrics)) {
                return NormalizeLyric(lyrics);
            }
            else {
                return lyrics;
            }
        }
        catch (Exception e){
            return new Lyrics(NO_RESULT);
        }

    }

    private static Lyrics NormalizeLyric(Lyrics lyrics){
        String ly= RemoveSingersName(Normalizer(LineSeparator(
                lyrics.getText())));
        lyrics.setText(ly);
        return lyrics;
    }

    private static boolean IsFound(Lyrics lyrics){
        switch (lyrics.getFlag()){
            case Lyrics.NO_RESULT:
            case Lyrics.ERROR:
            case Lyrics.NEGATIVE_RESULT:
            case Lyrics.SEARCH_ITEM:
                return false;
            case Lyrics.POSITIVE_RESULT:return true;
            default:return false;
        }
    }


    private static String LineSeparator(String html){
        return html.replaceAll("<[^>]*>","/n");
    }

    private static List<String> ExtractSingersName(String lyric){
        //[Ariana Grande:] for example
        Matcher matcher = Pattern.compile("\\[([^]]+)").matcher(lyric);

        List<String> tags = new ArrayList<>();

        int pos = -1;
        while (matcher.find(pos+1)){
            pos = matcher.start();
            tags.add(matcher.group(1));
        }

        return tags;
    }

    private static String RemoveSingersName(String lyric){
        String ret= lyric;

        for (String singer:ExtractSingersName(lyric)) {
            ret=ret.replace("["+singer+"]","");
        }
        return ret;
    }

    private static String Normalizer(String lyric) {
        return Jsoup.parse(lyric).text();
    }
}
