package com.example.shustrik.vkdocs.vk;


import android.database.Cursor;

import com.example.shustrik.vkdocs.common.DBConverter;

/**
 * Common class for document holder entity (dialog or community)
 */
public class MyVKEntityImpl implements MyVKEntity {
    private int peerId;

    private String peerName;

    private String previewUrl;

    public enum SrcType {
        GROUP,
        DIALOG
    }

    public MyVKEntityImpl(int peerId, String peerName, String previewUrl) {
        this.peerId = peerId;
        this.peerName = peerName;
        this.previewUrl = previewUrl;
    }

    public MyVKEntityImpl(Cursor cursor, SrcType type) {
        if (type == SrcType.GROUP) {
            peerId = cursor.getInt(DBConverter.COL_GROUP_ID);
            peerName = cursor.getString(DBConverter.COL_GROUP_TITLE);
            previewUrl = cursor.getString(DBConverter.COL_GROUP_PREVIEW);
        } else if (type == SrcType.DIALOG) {
            peerId = cursor.getInt(DBConverter.COL_DIALOG_PEER_ID);
            peerName = cursor.getString(DBConverter.COL_DIALOG_TITLE);
            previewUrl = cursor.getString(DBConverter.COL_DIALOG_PREVIEW);
        }
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
