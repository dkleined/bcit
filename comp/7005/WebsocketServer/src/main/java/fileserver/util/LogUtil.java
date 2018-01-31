package fileserver.util;

import org.springframework.stereotype.Component;

import static java.lang.String.format;

/**
 * Utility to normalize logging.
 *
 * @author dklein
 */
@Component
public class LogUtil {

    public static String logStr(String str1, String str2, String str3) {
        return format("[{%s}][{%s}][{%s}]", str1, str2, str3);
    }

    public static String logStr(String str1, String str2) {
        return format("[{%s}][{%s}]", str1, str2);
    }
}
