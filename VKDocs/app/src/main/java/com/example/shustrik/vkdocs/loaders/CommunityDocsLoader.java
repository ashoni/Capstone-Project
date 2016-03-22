package com.example.shustrik.vkdocs.loaders;


import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.vk.MyVKDocsArray;
import com.example.shustrik.vkdocs.vk.MyVKWallDocs;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

//Переделать обновление: в 0 не сбрасывать, если обновиться не удалось, вернуть значения переменных в исходное состояние,
// не трогать адаптер. В остальных классах также. Если удалось - всё обновить.
//Дальше сделать загрузку и персонализировать меню. Убедиться, что параллельная загрузка работает.
//(файл помечается как доступный оффлайн)
public class CommunityDocsLoader implements CustomLoader, DocListAdapter.LoadMore {
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
        prevCommPack = new ReqParamPack(commPack);
        prevWallPack = new ReqParamPack(wallPack);
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
    public void load() {
        if (from == 0) {
            loadCommunityDocs();
        } else if (from == 1) {
            loadCommunityWallDocs();
        }
    }


    private void loadCommunityWallDocs() {
        VKRequests.getDocsFromWall(new VKRequestCallback<MyVKWallDocs>() {
            @Override
            public void onSuccess(MyVKWallDocs wallDocs) {
                //Проверить внутренние исключения
                updateAdapterState(wallPack, false);
                if (wallPack.isFirstLoad()) {
                    if (isRefreshing) {
                        adapter.swapData(wallDocs.getResults());
                    } else {
                        adapter.addData(wallDocs.getResults());
                    }
                    wallPack.setTotal(wallDocs.getCount() < 500 ? wallDocs.getCount() : 500);
                } else {
                    adapter.addData(wallDocs.getResults());
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
                    loadCommunityWallDocs();
                } else {
                    updateAdapterState(commPack, false);
                    if (commPack.isFirstLoad()) {
                        adapter.swapData(documents);
                        commPack.setTotal(documents.getTotal());
                    } else {
                        adapter.addData(documents);
                    }
                    commPack.updateLoaded(documents.size());
                }
                if (commPack.isFinished()) {
                    Log.w("ANNA", "Done with docs-1");
                    from = 1;
                }
                adapter.notifyLoadingComplete();
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
            //Потому что нельзя так копировать. фуфуфу.
            commPack = prevCommPack;
            wallPack = prevWallPack;
            prevCommPack = null;
            prevWallPack = null;
        }
    }

    class ReqParamPack {
        private int count = 30;
        private int offset = 0;
        private int total = -1;

        public ReqParamPack(ReqParamPack pack) {
            count = pack.count;
            offset = pack.offset;
            total = pack.total;
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
