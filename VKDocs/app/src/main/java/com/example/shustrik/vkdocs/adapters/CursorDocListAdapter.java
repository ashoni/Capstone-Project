package com.example.shustrik.vkdocs.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.download.DocDownloader;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;

public class CursorDocListAdapter extends BaseDocListAdapter {
    private Cursor cursor;


    public CursorDocListAdapter(Context context, DocDownloader docDownloader, int menuId) {
        super(context, docDownloader, menuId);
    }


    @Override
    public void onBindViewHolder(BaseAdapterViewHolder holder, int position) {
        cursor.moveToPosition(position);
        setDocId(holder, cursor.getInt(DBConverter.COL_DOC_ID));
        setOwnerId(holder, cursor.getInt(DBConverter.COL_DOC_OWNER_ID));
        setTitle(holder, cursor.getString(DBConverter.COL_TITLE));
        setSize(holder, cursor.getInt(DBConverter.COL_SIZE));
        setDate(holder, cursor.getInt(DBConverter.COL_DATE));
        setUrl(holder, cursor.getString(DBConverter.COL_URL));
        setOffline(holder, cursor.getInt(DBConverter.COL_OFFLINE));
        setPreview(holder, cursor.getString(DBConverter.COL_PREVIEW_URL),
                cursor.getInt(DBConverter.COL_TYPE));

        setPosition(holder, position);
    }


    @Override
    public int getItemCount() {
        if (null == cursor) return 0;
        return cursor.getCount();
    }


    public void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
        recyclerView.setVisibility(getItemCount() != 0 && !loading ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(getItemCount() == 0 && !loading ? View.VISIBLE : View.GONE);
    }


    @Override
    void moveToPosition(int position) {
        cursor.moveToPosition(position);
    }

    @Override
    public MyVKApiDocument getDocumentOnMenuClick(int position) {
        //moveToPosition(getDocumentOnMenuPosition());
        //add create MyVKApiDocument from cursor to DBConverter
        //а нужно ли? это меню отсюда никогда не вызовется
        return null;
    }

    @Override
    public void rename(int position, String title) {
        moveToPosition(position);
        BaseAdapterViewHolder holder = (BaseAdapterViewHolder)recyclerView.findViewHolderForAdapterPosition(position);
        setTitle(holder, cursor.getString(DBConverter.COL_TITLE));
        notifyItemChanged(position);
    }
}
