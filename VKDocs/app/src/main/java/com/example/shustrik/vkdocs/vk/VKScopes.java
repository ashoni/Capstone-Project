package com.example.shustrik.vkdocs.vk;


/**
 * https://vk.com/dev/permissions
 */
public enum VKScopes {
    FRIENDS(2, "friends"),
    DOCS(131072, "docs"),
    GROUPS(262144, "groups"),
    MESSAGES(4096, "messages");

    private int code;
    private String name;

    VKScopes(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getMask() {
        return code;
    }

    public String getName() {
        return name;
    }
}
