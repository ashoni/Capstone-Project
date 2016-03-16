package com.example.shustrik.vkdocs.vk;

import android.util.Log;

import com.vk.sdk.api.model.VKApiDocument;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyVKApiDocument extends VKApiDocument {
    private long date;
    private int type;
    private VKApiDocument doc;

    public MyVKApiDocument(JSONObject from) throws JSONException {
        parse(from);
        parseRest(from);
    }

    public static MyVKDocsAttachments getDocsFromAttachments(JSONObject from) throws JSONException {
        JSONObject response = from.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        final List<MyVKApiDocument> results = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject att = array.getJSONObject(i);
            final JSONObject doc = att.getJSONObject("doc");
            Log.w("ANNA", doc.toString());
            results.add(new MyVKApiDocument(doc));
        }
        return new MyVKDocsAttachments(results, response.optString("next_from"));
    }

    public static MyVKWallDocs getDocsFromWall(JSONObject from) throws JSONException {
        JSONObject response = from.optJSONObject("response");
        int count = response.getInt("count");
        Log.w("ANNA", "wall count " + count);
        JSONArray array = response.optJSONArray("items");
        Log.w("ANNA", "got from wall " + array.length());
        final List<MyVKApiDocument> results = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject att = array.getJSONObject(i);
            Log.w("ANNA", i + " post " + att.toString());
            final JSONArray arr = att.optJSONArray("attachments");
            if (arr == null) {
                continue;
            }
            Log.w("ANNA", "attachments " + arr.length());
            for (int j = 0; j < arr.length(); j++) {
                final JSONObject attItem = arr.getJSONObject(j);
                Log.w("ANNA", j + " att: " + attItem.toString());
                final JSONObject doc = attItem.optJSONObject("doc");
                if (doc != null) {
                    Log.w("ANNA", "doc!" + doc);
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
