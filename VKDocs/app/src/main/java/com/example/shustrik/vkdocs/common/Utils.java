package com.example.shustrik.vkdocs.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utils {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM yyyy");

    private Utils() {
    }

    public static String sizeToString(long size) {
        if (size < 1024) {
            return size + "b";
        } else if (size < 1048576) {
            return (size / 1024) + "Kb";
        } else {
            return (size / 1048576) + "Mb";
        }
    }

    public static String convertDate(int date) {
        Date d =  new java.util.Date((long)date*1000);
        return simpleDateFormat.format(d);
    }

    public static String getFileMimeType(File file) {
        String[] arr = file.getName().split("\\.");
        if (arr.length == 0)
            return "text/plain";
        String mimeType = android.webkit.MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(arr[arr.length - 1]);
        return mimeType == null ? "text/plain" : null;
    }

    public static String join(List<Integer> ids) {
        if (ids.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ids.get(0));
        for (int i = 1; i < ids.size(); i ++) {
            sb.append(",");
            sb.append(ids.get(i));
        }
        return sb.toString();
    }
}
