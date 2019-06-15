package com.project.travel.story.Utility;



import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

public class GPSUtility {

    private static StringBuilder sb = new StringBuilder(20);

    /**
     * returns ref for latitude which is S or N.
     * @param latitude
     * @return S or N
     */
    public static String latitudeRef(double latitude) {
        return latitude<0.0d?"S":"N";
    }

    /**
     * returns ref for latitude which is W or E.
     * @param longitude
     * @return W or E
     */
    public static String longitudeRef(double longitude) {
        return longitude<0.0d?"W":"E";
    }

    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     *  79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     * @param latitude could be longitude.
     * @return
     */
    synchronized public static final String convert(double latitude) {
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude*1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000");
        return sb.toString();
    }

    synchronized public static final void saveExifData(String path, Double latitude, Double longitude, String description) throws IOException {
        ExifInterface exif = new ExifInterface(path);
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convert(latitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef(latitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convert(longitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,longitudeRef(longitude));
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description);
        exif.saveAttributes();
    }
}
