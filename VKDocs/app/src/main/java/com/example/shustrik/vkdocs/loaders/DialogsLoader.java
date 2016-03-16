package com.example.shustrik.vkdocs.loaders;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.shustrik.vkdocs.adapters.VKEntityListAdapter;
import com.example.shustrik.vkdocs.vk.MyVKApiDialog;
import com.example.shustrik.vkdocs.vk.MyVKDialogsArray;
import com.example.shustrik.vkdocs.vk.MyVKEntity;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


//ДОБАВИТЬ LOADMORE
public class DialogsLoader implements CustomLoader {
    public static final String TAG = "ANNA_DL";
    private VKEntityListAdapter adapter;
    private List<MyVKEntity> dialogs = new ArrayList<>();
    private SwipeRefreshLayout swipe;

    private int offset = 0;
    private int count = 20;
    private boolean isRefreshing = false;


    public DialogsLoader(VKEntityListAdapter adapter, SwipeRefreshLayout swipe) {
        this.adapter = adapter;
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        swipe.setRefreshing(true);
        adapter.swapData(null);
        initLoader();
    }

    @Override
    public void initLoader() {
        offset = 0;
        if (!isRefreshing) {
            adapter.setLoading(true);
        }
        loadDialogs(offset, count);
    }

    private void loadDialogs(int offset, int count) {
        VKRequests.getDialogs(new VKRequestCallback<MyVKDialogsArray>() {
            @Override
            public void onSuccess(MyVKDialogsArray receivedDialogs) {
                if (receivedDialogs.size() > 0) {
                    Log.w("ANNA", "Received informations on: " + receivedDialogs.size());

                    Set<Integer> userIds = new HashSet<>();
                    Set<Integer> groupIds = new HashSet<>();
                    for (MyVKApiDialog dialog : receivedDialogs) {
                        if (dialog.getPeerType() == MyVKApiDialog.Peer.USER) {
                            userIds.add(dialog.getPeerId());
                        } else if (dialog.getPeerType() == MyVKApiDialog.Peer.GROUP) {
                            groupIds.add(-dialog.getPeerId());
                        } else {
                            userIds.addAll(dialog.getPeerIds());
                        }
                    }
                    final Map<Integer, VKApiUser> userMap = new HashMap<>();
                    final Map<Integer, VKApiCommunity> groupMap = new HashMap<>();

                    AtomicInteger dialogsCount = new AtomicInteger(2);
                    loadUsersInfo(userIds, dialogsCount, userMap, groupMap, receivedDialogs);
                    loadCommunitiesInfo(groupIds, dialogsCount, userMap, groupMap, receivedDialogs);
                } else {
                    Log.w(TAG, "No more dialogs");
                }
            }

            @Override
            public void onError(VKError e) {
                Log.w(TAG, "Upload problems");
                //SNACK?
            }
        }, offset, count);
    }

    private void loadUsersInfo(final Set<Integer> userIds,
                               final AtomicInteger dialogsWaiting,
                               final Map<Integer, VKApiUser> userMap,
                               final Map<Integer, VKApiCommunity> groupMap,
                               final MyVKDialogsArray receivedDialogs) {
        VKRequests.getUserInfo(new ArrayList<>(userIds), new VKRequestCallback<VKUsersArray>() {
            @Override
            public void onSuccess(VKUsersArray obj) {
                for (VKApiUser user : obj) {
                    userMap.put(user.id, user);
                }
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting);
            }

            @Override
            public void onError(VKError e) {
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting);
            }
        });
    }

    private void loadCommunitiesInfo(final Set<Integer> groupIds,
                                     final AtomicInteger dialogsWaiting,
                                     final Map<Integer, VKApiUser> userMap,
                                     final Map<Integer, VKApiCommunity> groupMap,
                                     final MyVKDialogsArray receivedDialogs) {
        VKRequests.getCommunityInfo(new ArrayList<>(groupIds), new VKRequestCallback<VKApiCommunityArray>() {
            @Override
            public void onSuccess(VKApiCommunityArray obj) {
                for (VKApiCommunity group : obj) {
                    groupMap.put(group.id, group);
                }
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting);
            }

            @Override
            public void onError(VKError e) {
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting);
            }
        });
    }

    private void notifyDialogReady(Map<Integer, VKApiUser> userMap,
                                   Map<Integer, VKApiCommunity> groupMap,
                                   MyVKDialogsArray receivedDialogs,
                                   AtomicInteger dialogsWaiting) {
        if (dialogsWaiting.decrementAndGet() == 0) {
            updateDialogs(userMap, groupMap, receivedDialogs);
            adapter.swapData(dialogs);
        }
    }

    private void updateDialogs(Map<Integer, VKApiUser> userMap,
                               Map<Integer, VKApiCommunity> groupMap,
                               MyVKDialogsArray receivedDialogs) {
        updateAdapterState();
        List<MyVKApiDialog> completeDialogs = new ArrayList<>();
        for (MyVKApiDialog dialog : receivedDialogs) {
            if (dialog.getPeerType() == MyVKApiDialog.Peer.USER && userMap.containsKey(dialog.getPeerId())) {
                dialog.setDialogParams(userMap.get(dialog.getPeerId()));
                completeDialogs.add(dialog);
            } else if (dialog.getPeerType() == MyVKApiDialog.Peer.GROUP && groupMap.containsKey(-dialog.getPeerId())) {
                dialog.setDialogParams(groupMap.get(-dialog.getPeerId()));
                completeDialogs.add(dialog);
            } else {
                boolean got = false;
                List<VKApiUser> chatUsers = new ArrayList<>();
                for (int peerId : dialog.getPeerIds()) {
                    if (userMap.containsKey(peerId)) {
                        got = true;
                        chatUsers.add(userMap.get(peerId));
                    }
                }
                if (got) {
                    dialog.setDialogParams(chatUsers);
                    completeDialogs.add(dialog);
                }
            }
        }
        if (!completeDialogs.isEmpty()) {
            dialogs.addAll(completeDialogs);
            adapter.swapData(dialogs);
        }
    }

    private void updateAdapterState() {
        if (isRefreshing) {
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else if (offset == 0) {
            adapter.setLoading(false);
        }
    }
}
