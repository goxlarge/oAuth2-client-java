package goxlarge.auth.client.ports.utils;

public class Formater {
    public static String getHumanTime(long ts) {
        long ms = ts%1000;
        if(ts < 1000) {
            return ms + "ms";
        }
        long s = (ts/1000)%60;
        if(ts < 60*1000) {
            return s + "s" + ms + "ms";
        }
        long m = (ts/1000/60)%60;
        if(ts < 60*60*1000) {
            return m + "m" + s + "s";
        }
        long h = (ts/1000/60/60);
        return h + "h" + m + "m";
    }
}
