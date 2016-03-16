package com.example.shustrik.vkdocs.vk;

import com.vk.sdk.api.VKError;

public interface VKRequestCallback<T> {
    void onSuccess(T obj);

    void onError(VKError e);
}
