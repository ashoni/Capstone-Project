package com.example.shustrik.vkdocs.vk;

import com.vk.sdk.api.VKError;

/**
 * Callback to process result of request to VK server
 */
public interface VKRequestCallback<T> {
    void onSuccess(T obj);

    void onError(VKError e);
}
