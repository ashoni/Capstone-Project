package com.example.shustrik.vkdocs.vk;

public enum VKDocTypes {
    TEXT(1),
    ARCHIVED(2),
    GIF(3),
    PIC(4),
    AUDIO(5),
    VIDEO(6),
    BOOK(7),
    UNKNOWN(8);

    private int code;

    VKDocTypes(int code) {
        this.code = code;
    }
}
