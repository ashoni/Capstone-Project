package com.example.shustrik.vkdocs.vk;


public class MyVKEntityImpl implements MyVKEntity {
    private int peerId;

    private String peerName;

    private String previewUrl;

    public MyVKEntityImpl(int peerId, String peerName, String previewUrl) {
        this.peerId = peerId;
        this.peerName = peerName;
        this.previewUrl = previewUrl;
    }

    @Override
    public int getPeerId() {
        return peerId;
    }

    @Override
    public String getPeerName() {
        return peerName;
    }

    @Override
    public String getPreviewUrl() {
        return previewUrl;
    }
}
