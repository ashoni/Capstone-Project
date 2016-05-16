package com.vk.sdk.api.docs;

import android.util.Log;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.util.VKJsonHelper;
import com.vk.sdk.util.VKUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

public class VKUploadDocRequest extends VKUploadDocBase {
    private String title;

    public VKUploadDocRequest(File doc, String title) {
        super();
        this.mDoc = doc;
        this.mGroupId = 0;
        this.title = title;
        Log.w("ANNA", "set title " + title);
    }
    /**
     * Creates a VKUploadDocRequest instance.
     * @param doc file for upload to server
     */
    public VKUploadDocRequest(File doc) {
        super();
        this.mDoc = doc;
        this.mGroupId = 0;
        Log.w("ANNA", "Doc constructor");
    }

    /**
     * Creates a VKUploadDocRequest instance.
     * @param doc file for upload to server
     * @param groupId community ID (if the document will be uploaded to the community).
     */
    public VKUploadDocRequest(File doc, long groupId) {
        super();
        Log.w("ANNA", "group constructor");
        this.mDoc = doc;
        this.mGroupId = groupId;
    }

    @Override
    protected VKRequest getServerRequest() {
        if (mGroupId != 0)
            return VKApi.docs().getUploadServer(mGroupId);
        return VKApi.docs().getUploadServer();
    }

    @Override
    protected VKRequest getSaveRequest(JSONObject response) {
        VKRequest saveRequest;
        Log.w("ANNA", "get save request");
        try {
            Map<String, Object> params = VKJsonHelper.toMap(response);
            if (title != null && !title.isEmpty()) {
                params.put("title", title);
                Log.w("ANNA", params.toString());
                Log.w("ANNA", "title is " + params.toString());
            }
            saveRequest = VKApi.docs().save(new VKParameters(params));
        } catch (JSONException e) {
            return null;
        }
        if (mGroupId != 0)
            saveRequest.addExtraParameters(VKUtil.paramsFrom(VKApiConst.GROUP_ID, mGroupId));
        return saveRequest;
    }
}
