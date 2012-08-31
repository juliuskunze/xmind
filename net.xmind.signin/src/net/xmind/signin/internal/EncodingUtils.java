package net.xmind.signin.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EncodingUtils {

    public static String urlEncode(Object object) {
        String text = object == null ? "" : String.valueOf(object); //$NON-NLS-1$
        try {
            return URLEncoder.encode(text, "utf-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    public static String format(String pattern, Object... values) {
        Object[] encodedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            encodedValues[i] = EncodingUtils.urlEncode(values[i]);
        }
        return String.format(pattern, encodedValues);
    }

}
