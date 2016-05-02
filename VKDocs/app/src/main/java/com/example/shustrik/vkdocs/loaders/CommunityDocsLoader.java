package com.example.shustrik.vkdocs.loaders;


import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.adapters.LoadMore;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.example.shustrik.vkdocs.vk.MyVKDocsArray;
import com.example.shustrik.vkdocs.vk.MyVKWallDocs;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;
import java.util.List;

//Переделать обновление: в 0 не сбрасывать, если обновиться не удалось, вернуть значения переменных в исходное состояние,
// не трогать адаптер. В остальных классах также. Если удалось - всё обновить.
//Дальше сделать загрузку и персонализировать меню. Убедиться, что параллельная загрузка работает.
//(файл помечается как доступный оффлайн)
public class CommunityDocsLoader implements CustomLoader, LoadMore {
    public static String TAG = "ANNA_DDL";

    private static final int COMM_COUNT = 30;
    private static final int WALL_COUNT = 50;
    private int peerId;
    private int from = 0;

    private ReqParamPack commPack;
    private ReqParamPack wallPack;
    private ReqParamPack prevCommPack;
    private ReqParamPack prevWallPack;

    private DocListAdapter adapter;
    private SwipeRefreshLayout swipe;
    private boolean isRefreshing = false;

    private String query;
    private boolean isSearch = false;


    public CommunityDocsLoader(DocListAdapter adapter, int peerId, SwipeRefreshLayout swipe) {
        this.adapter = adapter;
        adapter.setLoadMore(this);
        this.peerId = peerId;
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);

        commPack = new ReqParamPack(COMM_COUNT);
        wallPack = new ReqParamPack(WALL_COUNT);
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        prevCommPack = new ReqParamPack(commPack, COMM_COUNT);
        prevWallPack = new ReqParamPack(wallPack, WALL_COUNT);
        swipe.setRefreshing(true);
        initLoader();
    }

    @Override
    public void initLoader() {
        commPack = new ReqParamPack(COMM_COUNT);
        wallPack = new ReqParamPack(WALL_COUNT);
        from = 0;
        if (!isRefreshing) {
            adapter.setLoading(true);
        }
        loadCommunityDocs();
    }

    @Override
    public void cancelSearch() {
        isSearch = false;
        query = "";
        initLoader();
    }

    @Override
    public void load() {
        Log.w("ANNA", "loooooad");
        if (from == 0) {
            loadCommunityDocs();
        } else if (from == 1) {
            loadCommunityWallDocs();
        }
    }


    private void loadCommunityWallDocs() {
        Log.w("ANNA", "load wall");
        VKRequests.getDocsFromWall(new VKRequestCallback<MyVKWallDocs>() {
            @Override
            public void onSuccess(MyVKWallDocs wallDocs) {
                Log.w("ANNA", "On success");
                //Проверить внутренние исключения
                updateAdapterState(wallPack, false);

                Log.w("ANNA", "Situation " + isSearch + " " + query);
                List<MyVKApiDocument> nDocs = new ArrayList<>();
                if (isSearch && query != null) {
                    Log.w("ANNA", "tutochki");
                    for (MyVKApiDocument doc : wallDocs.getResults()) {
                        if (doc.title.toLowerCase().contains(query.toLowerCase())) {
                            nDocs.add(doc);
                        }
                    }
                } else {
                    nDocs.addAll(wallDocs.getResults());
                }
                Log.w("ANNA", "Size " + nDocs.size());

                if (wallPack.isFirstLoad()) {
                    if (isRefreshing) {
                        adapter.swapData(nDocs);
                    } else {
                        Log.w("ANNA", "another add");
                        if (adapter.getItemCount() == 0) {
                            adapter.swapData(nDocs);
                        } else {
                            adapter.addData(nDocs);
                        }
                    }
                    wallPack.setTotal(wallDocs.getCount() < 500 ? wallDocs.getCount() : 500);
                } else {
                    Log.w("ANNA", "add data");
                    adapter.addData(nDocs);
                }
                wallPack.updateLoaded(wallDocs.getResults().size());
                if (wallPack.isFinished()) {
                    Log.w("ANNA", "Done with docs-1");
                    from = 2;
                    adapter.notifyLoadFinished();
                }
                adapter.notifyLoadingComplete();
            }

            @Override
            public void onError(VKError e) {
                //message
                updateAdapterState(wallPack, true);
                Log.w(TAG, e.toString());
                adapter.notifyLoadingComplete();
            }
        }, -peerId, wallPack.getOffset(), wallPack.getCount());
    }


    private void loadCommunityDocs() {
        VKRequests.getDocs(new VKRequestCallback<MyVKDocsArray>() {
            @Override
            public void onSuccess(MyVKDocsArray documents) {
                if (commPack.isFirstLoad() && documents.size() == 0) {
                    from = 1;
                    Log.w("ANNA", "Go for wall");
                    loadCommunityWallDocs();
                } else {
                    updateAdapterState(commPack, false);
                    List<MyVKApiDocument> goodDocs = new ArrayList<>();
                    if (isSearch && query != null) {
                        for (MyVKApiDocument doc : documents) {
                            if (doc.title.toLowerCase().contains(query.toLowerCase())) {
                                goodDocs.add(doc);
                            }
                        }
                    } else {
                        goodDocs.addAll(documents);
                    }
                    if (commPack.isFirstLoad()) {
                        adapter.swapData(goodDocs);
                        commPack.setTotal(documents.getTotal());
                    } else {
                        adapter.addData(goodDocs);
                    }
                    commPack.updateLoaded(documents.size());
                }
                if (commPack.isFinished()) {
                    Log.w("ANNA", "Done with docs-1");
                    Log.w("ANNA", "Go for wall");
                    from = 1;
                }
                if (isSearch && commPack.isFinished()) {
                    loadCommunityWallDocs();
                } else {
                    adapter.notifyLoadingComplete();
                }
            }

            @Override
            public void onError(VKError e) {
                updateAdapterState(commPack, true);
                Log.w(TAG, e.toString());
                adapter.notifyLoadingComplete();
            }
        }, -peerId, commPack.getCount(), commPack.getOffset());
    }

    private void updateAdapterState(ReqParamPack pack, boolean error) {
        if (isRefreshing) {
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else if (pack.isFirstLoad()) {
            adapter.setLoading(false);
        }
        if (error) {
            commPack = new ReqParamPack(prevCommPack, COMM_COUNT);
            wallPack = new ReqParamPack(prevWallPack, WALL_COUNT);
            prevCommPack = null;
            prevWallPack = null;
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
//            List<MyVKEntity> searchList = new ArrayList<>();
//            for (MyVKEntity community : dialogs) {
//                if (community.getPeerName().contains(query)) {
//                    searchList.add(community);
//                }
//            }
//            dialogs = searchList;
//            adapter.swapData(dialogs);
            initLoader();
        }
    }

    class ReqParamPack {
        private int count = 30;
        private int offset = 0;
        private int total = -1;

        public ReqParamPack(ReqParamPack pack, int defCount) {
            //attempt to read on null
            if (pack != null) {
                count = pack.count;
                offset = pack.offset;
                total = pack.total;
            } else {
                this.count = defCount;
                this.offset = 0;
                this.total = -1;
            }
        }

        public ReqParamPack(int count) {
            this.count = count;
            this.offset = 0;
            this.total = -1;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public boolean isFirstLoad() {
            return total == -1;
        }

        public boolean isFinished() {
            return offset >= total;
        }

        public void updateLoaded(int c) {
            offset += c;
        }
    }
}
