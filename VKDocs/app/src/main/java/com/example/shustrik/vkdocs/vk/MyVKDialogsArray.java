package com.example.shustrik.vkdocs.vk;

import com.vk.sdk.api.model.VKApiModel;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shustrik on 10.03.2016.
 */
public class MyVKDialogsArray extends VKList<MyVKApiDialog> {
    @Override
    public VKApiModel parse(JSONObject response) throws JSONException {
        fill(response, MyVKApiDialog.class);
        return this;
    }
}
