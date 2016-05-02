package com.example.shustrik.vkdocs.vk;

import android.database.Cursor;

import com.example.shustrik.vkdocs.common.DBConverter;
import com.vk.sdk.api.model.VKApiDocument;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyVKApiDocument extends VKApiDocument {
    private long date;
    private int type;
    private boolean isOffline = false;
    private VKApiDocument doc;

    public MyVKApiDocument(JSONObject from) throws JSONException {
        parse(from);
        parseRest(from);
    }

    public MyVKApiDocument(Cursor cursor) {
        id = cursor.getInt(DBConverter.COL_DOC_ID);
        owner_id = cursor.getInt(DBConverter.COL_DOC_OWNER_ID);
        title = cursor.getString(DBConverter.COL_TITLE);
        size = cursor.getInt(DBConverter.COL_SIZE);
        date = cursor.getInt(DBConverter.COL_DATE);
        url = cursor.getString(DBConverter.COL_URL);
        isOffline = (cursor.getInt(DBConverter.COL_OFFLINE) == 1);
        photo_130 = cursor.getString(DBConverter.COL_PREVIEW_URL);
        type = cursor.getInt(DBConverter.COL_TYPE);
    }

    public static MyVKDocsAttachments getDocsFromAttachments(JSONObject from) throws JSONException {
        JSONObject response = from.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        final List<MyVKApiDocument> results = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject att = array.getJSONObject(i);
            final JSONObject doc = att.getJSONObject("doc");
            results.add(new MyVKApiDocument(doc));
        }
        return new MyVKDocsAttachments(results, response.optString("next_from"));
    }

    public static MyVKWallDocs getDocsFromWall(JSONObject from) throws JSONException {
        JSONObject response = from.optJSONObject("response");
        int count = response.getInt("count");
        JSONArray array = response.optJSONArray("items");
        final List<MyVKApiDocument> results = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject att = array.getJSONObject(i);
            final JSONArray arr = att.optJSONArray("attachments");
            if (arr == null) {
                continue;
            }
            for (int j = 0; j < arr.length(); j++) {
                final JSONObject attItem = arr.getJSONObject(j);
                final JSONObject doc = attItem.optJSONObject("doc");
                if (doc != null) {
                    results.add(new MyVKApiDocument(doc));
                }
            }
        }
        return new MyVKWallDocs(count, results);
    }

    public void parseRest(JSONObject from) {
        date = from.optLong("date");
        type = from.optInt("type");
    }

    public long getDate() {
        return date;
    }

    public int getFileType() {
        return type;
    }

    public VKApiDocument getDoc() {
        return doc;
    }


}
