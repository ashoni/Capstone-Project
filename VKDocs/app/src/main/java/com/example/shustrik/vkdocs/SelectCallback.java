package com.example.shustrik.vkdocs;

public interface SelectCallback {
    void onDialogSelected(int peerId, CharSequence name);
    void onCommunitySelected(int peerId, CharSequence name);
    void setToggleListener(boolean state);
}
