package com.example.shustrik.vkdocs.vk;

import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyVKApiDialog extends VKApiDialog implements MyVKEntity {
    private int peerId;
    private String dialogName;
    private String previewUrl;
    private Peer peerType;

    private List<Integer> peerIds = new ArrayList<>();

    public MyVKApiDialog(JSONObject from) throws JSONException {
        parse(from);
        if (!from.isNull("message")) {
            parseRest(from.getJSONObject("message"));
        }
    }


    public void parseRest(JSONObject from) {
        if (from.isNull("chat_id")) {
            peerId = message.user_id;
            if (peerId > 0) {
                peerType = Peer.USER;
            } else {
                peerType = Peer.GROUP;
            }
        } else {
            peerId = from.optInt("chat_id") + 2000000000;
            peerType = Peer.CHAT;
            JSONArray array = from.optJSONArray("chat_active");
            for (int i = 0; i < array.length(); i++) {
                try {
                    peerIds.add(array.getInt(i));
                } catch (JSONException e) {

                }
            }
        }
    }

    @Override
    public int getPeerId() {
        return peerId;
    }

    public Peer getPeerType() {
        return peerType;
    }

    public void setDialogParams(VKApiUser user) {
        dialogName = user.first_name + " " + user.last_name;
        previewUrl = user.photo_200;
    }

    public void setDialogParams(VKApiCommunity community) {
        dialogName = community.name;
        previewUrl = community.photo_200;
    }

    public void setDialogParams(List<VKApiUser> users) {
        StringBuilder sb = new StringBuilder();
        sb.append(users.get(0).first_name);
        sb.append(" ");
        sb.append(users.get(0).last_name);
        for (int i = 1; i < users.size() && i < 3; i++) {
            sb.append(", ");
            sb.append(users.get(i).first_name);
            sb.append(" ");
            sb.append(users.get(i).last_name);
        }
        if (users.size() > 3) {
            sb.append("...(");
            sb.append(users.size());
            sb.append(")");
        }
        dialogName = sb.toString();
        previewUrl = null;
    }

    @Override
    public String getPeerName() {
        return dialogName;
    }

    @Override
    public String getPreviewUrl() {
        return previewUrl;
    }

    public List<Integer> getPeerIds() {
        return peerIds;
    }

    public enum Peer {
        USER,
        GROUP,
        CHAT
    }
}
