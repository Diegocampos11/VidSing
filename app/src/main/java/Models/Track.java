package Models;

import java.io.Serializable;
import java.util.Objects;

public class Track implements Serializable {

    private String TrackID ;
    private String ArtistIDs ;
    private String AlbumID ;
    private String AlbumName ;
    private String wholeTitle;
    private int DurationMS ;

    public Track( String wholeTitle ){
        this.wholeTitle = wholeTitle;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Track)) return false;
        Track track = (Track) o;
        return Objects.equals(TrackID, track.TrackID) &&
                Objects.equals( wholeTitle, track.wholeTitle );
    }

    @Override
    public int hashCode() {
        return Objects.hash(TrackID, ArtistIDs, AlbumID, AlbumName, wholeTitle, DurationMS);
    }

    @Override
    public String toString() {
        return "Track{" +
                "TrackID='" + TrackID + '\'' +
                ", ArtistIDs='" + ArtistIDs + '\'' +
                ", AlbumID='" + AlbumID + '\'' +
                ", AlbumName='" + AlbumName + '\'' +
                ", wholeTitle='" + wholeTitle + '\'' +
                ", DurationMS=" + DurationMS +
                '}';
    }

    public String getTrackID() {
        return TrackID;
    }

    public void setTrackID(String trackID) {
        TrackID = trackID;
    }

    public String getArtistIDs() {
        return ArtistIDs;
    }

    public void setArtistIDs(String artistIDs) {
        ArtistIDs = artistIDs;
    }

    public String getAlbumID() {
        return AlbumID;
    }

    public void setAlbumID(String albumID) {
        AlbumID = albumID;
    }

    public String getAlbumName() {
        return AlbumName;
    }

    public void setAlbumName(String albumName) {
        AlbumName = albumName;
    }

    public String getWholeTitle() {
        return wholeTitle;
    }

    public void setWholeTitle(String wholeTitle) {
        this.wholeTitle = wholeTitle;
    }

    public int getDurationMS() {
        return DurationMS;
    }

    public void setDurationMS(int durationMS) {
        DurationMS = durationMS;
    }
}
