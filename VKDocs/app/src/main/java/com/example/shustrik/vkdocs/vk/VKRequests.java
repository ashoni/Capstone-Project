package com.example.shustrik.vkdocs.vk;

import android.app.Activity;
import android.util.Log;

import com.example.shustrik.vkdocs.common.Utils;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiGroups;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiDocument;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKDocsArray;
import com.vk.sdk.api.model.VKUsersArray;
import com.vk.sdk.api.model.VKWallPostResult;

import org.json.JSONException;

import java.io.File;
import java.util.List;


/**
 * THINK OF
 * a) implement your own Optional and Either
 * b) http://tools.android.com/tech-docs/new-build-system/resource-shrinking
 */
public class VKRequests {

    private VKRequests() {
    }


    public static void login(Activity activity, String... scope) {
        VKSdk.login(activity, scope);
    }


    //Заменить названия полей на константы из классов
    public static void getUserInfo(List<Integer> userIds, final VKRequestCallback<VKUsersArray> callback) {
        VKRequest request = new VKRequest("users.get",
                VKParameters.from("user_ids", Utils.join(userIds), "fields", "photo_100"));
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.d("ANNA", "Response " + response.json.toString());
                VKUsersArray vkUsersArray = new VKUsersArray();
                try {
                    vkUsersArray.parse(response.json);
                    callback.onSuccess(vkUsersArray);
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    public static void getUserInfo(int userId, final VKRequestCallback<VKApiUser> callback) {
        VKRequest request = new VKRequest("users.get",
                VKParameters.from("user_ids", userId, "fields", "photo_50,photo_100,photo_200"));
        getUserInfo(request, callback);
    }

    public static void getUserInfo(final VKRequestCallback<VKApiUser> callback) {
        VKRequest request = new VKRequest("users.get",
                VKParameters.from("fields", "photo_50,photo_100,photo_200"));
        getUserInfo(request, callback);
    }

    private static void getUserInfo(VKRequest request, final VKRequestCallback<VKApiUser> callback) {
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.d("ANNA", "Response " + response.json.toString());
                VKUsersArray vkUsersArray = new VKUsersArray();
                try {
                    vkUsersArray.parse(response.json);
                    callback.onSuccess(vkUsersArray.get(0));
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    public static void getCommunityInfo(List<Integer> communityIds, final VKRequestCallback<VKApiCommunityArray> callback) {
        final VKRequest request = new VKRequest("groups.get", VKParameters.from("group_ids", Utils.join(communityIds)));
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                VKApiCommunityArray communityArray = new VKApiCommunityArray();
                try {
                    communityArray.parse(response.json);
                    callback.onSuccess(communityArray);
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    public static void getDocs(final VKRequestCallback<MyVKDocsArray> callback) {
        VKRequest request = new VKRequest("docs.get");
        getDocs(request, callback);
    }


    public static void getDocs(final VKRequestCallback<MyVKDocsArray> callback, long ownerId) {
        VKRequest request = new VKRequest("docs.get", VKParameters.from("owner_id", ownerId));
        getDocs(request, callback);
    }


    public static void getDocs(final VKRequestCallback<MyVKDocsArray> callback,
                               long ownerId, int count, int offset) {
        VKRequest request = new VKRequest("docs.get",
                VKParameters.from("owner_id", ownerId, "count", count, "offset", offset));
        Log.w("ANNA", "get docs " + request.toString());
        getDocs(request, callback);
    }


    private static void getDocs(VKRequest request, final VKRequestCallback<MyVKDocsArray> callback) {
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.w("ANNA", response.json.toString());
                MyVKDocsArray vkDocsArray = new MyVKDocsArray();
                try {
                    vkDocsArray.parse(response.json);
                    callback.onSuccess(vkDocsArray);
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }


    public static void getDocsFromWall(final VKRequestCallback<MyVKWallDocs> callback,
                                       int ownerId, int offset, int count) {
        VKRequest request = new VKRequest("wall.get",
                VKParameters.from("owner_id", ownerId, "offset", offset, "count", count));

        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.w("ANNA", response.json.toString());
                try {
                    callback.onSuccess(MyVKApiDocument.getDocsFromWall(response.json));
                } catch (Exception e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }


    /**
     * https://vk.com/dev/errors
     * 10 Произошла внутренняя ошибка сервера.
     */
    public static void getDialogs(final VKRequestCallback<MyVKDialogsArray> callback,
                                  int offset, int count) {
        VKRequest request = new VKRequest("messages.getDialogs",
                VKParameters.from("offset", offset, "count", count));
        Log.w("ANNA", request.toString());
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.w("ANNA", response.responseString);
                MyVKDialogsArray vkDialogsArray = new MyVKDialogsArray();
                try {
                    vkDialogsArray.parse(response.json);
                    callback.onSuccess(vkDialogsArray);
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    /**
     * count положительное число, максимальное значение 200, по умолчанию 30
     */
    public static void getAttachments(final VKRequestCallback<MyVKDocsAttachments> callback,
                                      long peerId, String startFrom, int count) {
        VKRequest request = new VKRequest("messages.getHistoryAttachments",
                VKParameters.from("peer_id", peerId, "media_type", "doc",
                        "start_from", startFrom, "count", count));
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.w("ANNA", "Response "  + response.json.toString());
                try {
                    callback.onSuccess(MyVKApiDocument.getDocsFromAttachments(response.json));
                } catch (Exception e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    public static void globalSearch(final VKRequestCallback<VKDocsArray> callback,
                                    String q, int offset, int count) {
        final VKRequest request = new VKRequest("messages.search",
                VKParameters.from("q", q, "offset", offset, "count", count));
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.d("ANNA", "Response " + response.json.toString());
                VKDocsArray vkDocsArray = new VKDocsArray();
                try {
                    vkDocsArray.parse(response.json);
                    callback.onSuccess(vkDocsArray);
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    public static void getCommunities(final VKRequestCallback<VKApiCommunityArray> callback,
                                      int offset, int count) {
        VKRequest request = VKApi.groups().get(VKParameters.from("offset", offset, "count", count, "extended", 1));
        Log.w("ANNA", request.toString());
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.d("ANNA", "Response " + response.json.toString());
                VKApiCommunityArray vkApiCommunityArray = new VKApiCommunityArray();
                try {
                    vkApiCommunityArray.parse(response.json);
                    callback.onSuccess(vkApiCommunityArray);
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    public static void addToUserDocs(final VKRequestCallback<Void> callback,
                                     long ownerId, long docId) {
        VKRequest request = new VKRequest("docs.add",
                VKParameters.from("owner_id", ownerId, "doc_id", docId));
        executeWithEmptyResult(request, callback);
    }

    public static void addToUserDocs(final VKRequestCallback<Void> callback,
                                     long ownerId, long docId, String accessKey) {
        VKRequest request = new VKRequest("docs.add",
                VKParameters.from("owner_id", ownerId, "doc_id", docId, "access_key", accessKey));
        executeWithEmptyResult(request, callback);
    }


    public static void upload(final VKRequestCallback<VKAttachments> callback, File doc) {
        VKRequest request = VKApi.docs().uploadDocRequest(doc);
    }

    public static void uploadToGroup(final VKRequestCallback<VKAttachments> callback,
                                     File doc, long groupId) {
        VKRequest request = VKApi.docs().uploadDocRequest(doc, groupId);
    }

    /**
     * Если не работает, перечитать исходники, лучше отдебажить. Там несколько операций, но вроде
     * как они все реализованы.
     */
    private static void upload(VKRequest request, final VKRequestCallback<VKDocsArray> callback) {
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                Log.d("ANNA", "Response "  + response.json.toString());
                VKDocsArray vkDocsArray = new VKDocsArray();
                try {
                    vkDocsArray.parse(response.json);
                    callback.onSuccess(vkDocsArray);
                } catch (JSONException e) {
                    callback.onError(new VKError(10));
                }
            }
        });
    }

    /**
     * add tags?
     */
    public static void edit(final VKRequestCallback<Void> callback,
                            long ownerId, long docId, String title) {
        VKRequest request = new VKRequest("docs.edit",
                VKParameters.from("owner_id", ownerId, "doc_id", docId, "title", title));
        executeWithEmptyResult(request, callback);

    }

    public static void delete(final VKRequestCallback<Void> callback, long ownerId, long docId) {
        VKRequest request = new VKRequest("docs.delete",
                VKParameters.from("owner_id", ownerId, "doc_id", docId));
        executeWithEmptyResult(request, callback);
    }


    private static void executeWithEmptyResult(VKRequest request, final VKRequestCallback<Void> callback) {
        request.executeWithListener(new MyVKRequestListener(callback) {
            @Override
            public void onComplete(VKResponse response) {
                callback.onSuccess(null);
            }
        });

    }

    private static abstract class MyVKRequestListener extends VKRequest.VKRequestListener {
        private VKRequestCallback callback;

        public MyVKRequestListener(VKRequestCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onError(VKError error) {
            Log.d("ANNA", "Error " + error);
            callback.onError(error);
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            Log.d("ANNA", "Attempt failed");
        }
    }
}
