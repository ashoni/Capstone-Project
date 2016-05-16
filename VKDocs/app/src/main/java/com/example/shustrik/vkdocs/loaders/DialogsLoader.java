package com.example.shustrik.vkdocs.loaders;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.adapters.LoadMore;
import com.example.shustrik.vkdocs.adapters.VKEntityListAdapter;
import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.sync.VKDocsSyncAdapter;
import com.example.shustrik.vkdocs.vk.MyVKApiDialog;
import com.example.shustrik.vkdocs.vk.MyVKDialogsArray;
import com.example.shustrik.vkdocs.vk.MyVKEntity;
import com.example.shustrik.vkdocs.vk.MyVKEntityImpl;
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


/**
 * Loads dialog list (after dialog list is loaded, loads information about its participants)
 */
public class DialogsLoader implements CustomLoader, LoadMore, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int START_COUNT = 40;
    private final int DIALOG_LOADER = 19;

    private VKEntityListAdapter adapter;
    private List<MyVKEntity> dialogs = new ArrayList<>();
    private SwipeRefreshLayout swipe;

    private int offset = 0;
    private int count = 20;
    private boolean isRefreshing = false;
    private long ownerId;
    private MainActivity activity;
    private LoaderManager loaderManager;

    private String query;
    private boolean isSearch = false;

    private static DialogsLoader instance;

    public static DialogsLoader getInstance() {
        return instance;
    }

    public static DialogsLoader initAndGetInstance(MainActivity activity, VKEntityListAdapter adapter, SwipeRefreshLayout swipe,
                                                   LoaderManager loaderManager, long ownerId) {
        instance = new DialogsLoader(activity, adapter, swipe, loaderManager, ownerId);
        return instance;
    }

    private DialogsLoader(MainActivity activity, VKEntityListAdapter adapter, SwipeRefreshLayout swipe,
                          LoaderManager loaderManager, long ownerId) {
        this.adapter = adapter;
        adapter.setLoadMore(this);
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
        this.loaderManager = loaderManager;
        this.ownerId = ownerId;
        this.activity = activity;
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        swipe.setRefreshing(true);
        VKDocsSyncAdapter.syncImmediately(activity);
    }

    private void onRefreshFailed() {
        updateAdapterState();
        adapter.onRefreshFailed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri dialogsUri = DocsContract.DialogEntry.buildDialogsUri(ownerId);
        if (id == DIALOG_LOADER) {
            return new CursorLoader(activity,
                    dialogsUri,
                    DBConverter.DIALOG_COLUMNS,
                    null,
                    null,
                    DocsContract.DialogEntry.sortDateDesc());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        updateAdapterState();
        List<MyVKEntity> dialogs = new ArrayList<>();
        while (cursor.moveToNext()) {
            MyVKEntity entity = new MyVKEntityImpl(cursor, MyVKEntityImpl.SrcType.DIALOG);
            if (isSearch && query != null) {
                if (entity.getPeerName().toLowerCase().contains(query.toLowerCase())) {
                    dialogs.add(entity);
                }
            } else {
                dialogs.add(entity);
            }
        }
        offset = dialogs.size();
        adapter.swapData(dialogs);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapData(null);
    }

    @Override
    public void initLoader() {
        offset = 0;
        if (!isRefreshing) {
            adapter.setLoading(true);
        }
        loaderManager.restartLoader(DIALOG_LOADER, null, this);
    }

    @Override
    public void cancelSearch() {
        isSearch = false;
        query = "";
        initLoader();
    }

    @Override
    public void load() {
        loadDialogs(offset, count, new Callback() {
            @Override
            public void process(List<MyVKApiDialog> newDialogs, boolean isSuccess) {
                updateAdapterState();
                adapter.notifyLoadingComplete();
                if (!dialogs.isEmpty()) {
                    if (isSearch && query != null) {
                        for (MyVKEntity entity : newDialogs) {
                            if (entity.getPeerName().contains(query)) {
                                dialogs.add(entity);
                            }
                        }
                    } else {
                        dialogs.addAll(newDialogs);
                    }
                    offset += newDialogs.size();
                    adapter.swapData(dialogs);
                }
                if (dialogs.size() < count) {
                    adapter.notifyLoadFinished();
                }
            }
        });
    }

    public interface Callback {
        void process(List<MyVKApiDialog> dialogs, boolean isSuccess);
    }

    public static void loadDialogs(int offset, int count, final Callback callback) {
        VKRequests.getDialogs(new VKRequestCallback<MyVKDialogsArray>() {
            @Override
            public void onSuccess(MyVKDialogsArray receivedDialogs) {
                if (receivedDialogs.size() > 0) {

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
                    loadUsersInfo(userIds, dialogsCount, userMap, groupMap, receivedDialogs, callback);
                    loadCommunitiesInfo(groupIds, dialogsCount, userMap, groupMap, receivedDialogs, callback);
                } else {
                    callback.process(new ArrayList<MyVKApiDialog>(), true);
                }
            }

            @Override
            public void onError(VKError e) {
                callback.process(null, false);
            }
        }, offset, count);
    }

    private static void loadUsersInfo(final Set<Integer> userIds,
                                      final AtomicInteger dialogsWaiting,
                                      final Map<Integer, VKApiUser> userMap,
                                      final Map<Integer, VKApiCommunity> groupMap,
                                      final MyVKDialogsArray receivedDialogs,
                                      final Callback callback) {
        VKRequests.getUserInfo(new ArrayList<>(userIds), new VKRequestCallback<VKUsersArray>() {
            @Override
            public void onSuccess(VKUsersArray obj) {
                for (VKApiUser user : obj) {
                    userMap.put(user.id, user);
                }
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting, callback);
            }

            @Override
            public void onError(VKError e) {
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting, callback);
            }
        });
    }

    private static void loadCommunitiesInfo(final Set<Integer> groupIds,
                                            final AtomicInteger dialogsWaiting,
                                            final Map<Integer, VKApiUser> userMap,
                                            final Map<Integer, VKApiCommunity> groupMap,
                                            final MyVKDialogsArray receivedDialogs,
                                            final Callback callback) {
        VKRequests.getCommunityInfo(new ArrayList<>(groupIds), new VKRequestCallback<VKApiCommunityArray>() {
            @Override
            public void onSuccess(VKApiCommunityArray obj) {
                for (VKApiCommunity group : obj) {
                    groupMap.put(group.id, group);
                }
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting, callback);
            }

            @Override
            public void onError(VKError e) {
                notifyDialogReady(userMap, groupMap, receivedDialogs, dialogsWaiting, callback);
            }
        });
    }

    private static void notifyDialogReady(Map<Integer, VKApiUser> userMap,
                                          Map<Integer, VKApiCommunity> groupMap,
                                          MyVKDialogsArray receivedDialogs,
                                          AtomicInteger dialogsWaiting,
                                          Callback callback) {
        if (dialogsWaiting.decrementAndGet() == 0) {
            callback.process(updateDialogs(userMap, groupMap, receivedDialogs), true);
        }
    }

    private static List<MyVKApiDialog> updateDialogs(Map<Integer, VKApiUser> userMap,
                                                     Map<Integer, VKApiCommunity> groupMap,
                                                     MyVKDialogsArray receivedDialogs) {
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
        return completeDialogs;
    }

    private void updateAdapterState() {
        if (isRefreshing) {
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else if (offset == 0) {
            adapter.setLoading(false);
        }
    }

    public void onSyncUpdate(boolean isSuccess) {
        if (isSuccess) {
            loaderManager.restartLoader(DIALOG_LOADER, null, this);
        } else {
            if (isRefreshing) {
                onRefreshFailed();
            }
        }
    }


    @Override
    public void search(String query) {
        if (query == null || query.isEmpty()) {
            isSearch = false;
            this.query = "";
            initLoader();
        } else {
            isSearch = true;
            this.query = query;
            initLoader();
        }
    }
}
