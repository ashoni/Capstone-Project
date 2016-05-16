package com.example.shustrik.vkdocs.vk;

import com.vk.sdk.api.model.VKApiModel;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * List of MyVKApiDialog objects
 */
public class MyVKDialogsArray extends VKList<MyVKApiDialog> {
    @Override
    public VKApiModel parse(JSONObject response) throws JSONException {
        fill(response, MyVKApiDialog.class);
        return this;
    }
}
