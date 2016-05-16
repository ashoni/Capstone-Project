package com.example.shustrik.vkdocs.widget;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.uicommon.DocIcons;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WidgetIntentService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            private List<MyVKApiDocument> docs = new ArrayList<>();

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                docs.clear();
                data = getContentResolver().query(DocsContract.DocumentEntry.buildDocConstUri(),
                        DBConverter.DOC_CONST_COLUMNS, null, null, DocsContract.DocumentEntry.sortDateDesc());
                if (data != null) {
                    while (data.moveToNext()) {
                        docs.add(new MyVKApiDocument(data));
                    }
                }

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null ||
                        !data.moveToPosition(position)) {
                    return null;
                }

                data.moveToPosition(position);

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_doc_list_item);

                MyVKApiDocument doc = docs.get(position);
                String title = doc.title;
                String src = docs.get(position).photo_130;

                views.setTextViewText(R.id.list_item_title, title);
                try {
                    Bitmap b = Picasso.with(getApplicationContext()).load(src).get();
                    views.setImageViewBitmap(R.id.list_item_preview, b);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (src != null && !src.isEmpty()) {
                    try {
                        Bitmap b = Picasso.with(getApplicationContext()).load(src).get();
                        views.setImageViewBitmap(R.id.list_item_preview, b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    views.setImageViewResource(R.id.list_item_preview,
                            DocIcons.getIconId(doc.getFileType()));
                }

                final Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget_doc_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_doc_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(DBConverter.COL_DOC_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

