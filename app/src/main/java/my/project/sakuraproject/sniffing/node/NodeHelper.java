package my.project.sakuraproject.sniffing.node;
import android.text.TextUtils;

/**
 * Created by fanchen on 2017/9/17.
 */
public class NodeHelper {

    public static String substring(String str, int start) {
        return substring(str, start, -1);
    }

    public static String substring(String str, int start, int end) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if(start < 0){
            return str;
        }
        if (end < 0) {
            return str.substring(start);
        }
        if (start >= 0 && start <= str.length()) {
            return str.substring(start, end);
        }
        return "";
    }

    public static String split(String str, String regex, int position) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        String[] array = str.split(regex);
        if(position >= array.length){
            return array[array.length-1];
        }
        if (position < 0) {
            return array[0];
        }
        return array[position];
    }
}