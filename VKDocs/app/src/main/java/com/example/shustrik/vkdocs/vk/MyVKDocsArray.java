package com.example.shustrik.vkdocs.vk;

import com.vk.sdk.api.model.VKApiModel;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * List of MyVKApiDocument objects
 */
public class MyVKDocsArray extends VKList<MyVKApiDocument> {
    private Integer total;

    @Override
    public VKApiModel parse(JSONObject response) throws JSONException {
        fill(response, MyVKApiDocument.class);
        parseRest(response);
        return this;
    }

    public void parseRest(JSONObject from) {
        try {
            total = from.optJSONObject("response").getInt("count");
        } catch (Exception e) {
            total = 0;
        }
    }

    public Integer getTotal() {
        return total;
    }
}
