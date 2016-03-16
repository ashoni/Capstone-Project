package com.example.shustrik.vkdocs;

public interface SelectCallback {
    void onDialogSelected(int peerId);
    void onCommunitySelected(int peerId);
    void setToggleListener(boolean state);
}
