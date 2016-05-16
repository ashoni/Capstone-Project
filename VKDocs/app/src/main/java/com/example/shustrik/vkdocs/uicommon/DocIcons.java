package com.example.shustrik.vkdocs.uicommon;

import com.example.shustrik.vkdocs.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps VK document types to icons
 */
public class DocIcons {
    private static Map<Integer, Integer> icons = new HashMap<>();

    private DocIcons() {}

    static {
        icons.put(1, R.drawable.ic_subject_black_24dp);
        icons.put(2, R.drawable.ic_unarchive_black_24dp);
        icons.put(3, R.drawable.ic_image_black_24dp);
        icons.put(4, R.drawable.ic_image_black_24dp);
        icons.put(5, R.drawable.ic_headset_black_24dp);
        icons.put(6, R.drawable.ic_movie_creation_black_24dp);
        icons.put(7, R.drawable.ic_book_black_24dp);
        icons.put(8, R.drawable.ic_insert_drive_file_black_24dp);
    }

    public static int getIconId(Integer t) {
        if (t == null || t < 1 || t > 7) {
            t = 7;
        }
        return icons.get(t);
    }

    public static int getChatIcon() {
        return R.drawable.ic_forum_black_18dp;
    }
}
