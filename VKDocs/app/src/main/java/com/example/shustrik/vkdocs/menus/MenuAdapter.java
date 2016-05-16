package com.example.shustrik.vkdocs.menus;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.shustrik.vkdocs.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for custom menu view
 */
class MenuAdapter extends ArrayAdapter<MenuItem> {
    private int layoutResourceId;
    private List<MenuItem> data;
    private Activity activity;
    private DocItemMenuListener listener;
    private DocMenu docMenu;

    public MenuAdapter(Activity activity,
                       int layoutResourceId,
                       List<MenuItem> data,
                       DocItemMenuListener listener,
                       DocMenu docMenu) {
        super(activity, layoutResourceId, data);
        this.activity = activity;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
        this.listener = listener;
        this.docMenu = docMenu;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuViewHolder menuViewHolder;
        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            menuViewHolder = new MenuViewHolder(convertView,
                    data.get(position).isCheckable(),
                    data.get(position).getItemId(),
                    listener);
            convertView.setTag(menuViewHolder);
        } else {
            menuViewHolder = (MenuViewHolder) convertView.getTag();
        }

        menuViewHolder.setIcon(data.get(position).getIcon());
        menuViewHolder.setTitle(data.get(position).getTitle());
        menuViewHolder.setOfflineAvailable(docMenu.isOffline());

        return convertView;
    }

    class MenuViewHolder {
        @Bind(R.id.icon)
        ImageView icon;
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.switch_element)
        Switch checkableSwitch;

        private boolean isOffline;


        public MenuViewHolder(View view, boolean isCheckable,
                              final int menuId, final DocItemMenuListener listener) {
            ButterKnife.bind(this, view);

            if (isCheckable) {
                checkableSwitch.setVisibility(View.VISIBLE);
                checkableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        listener.onClick(menuId, isChecked);
                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isOffline = !isOffline;
                        checkableSwitch.setChecked(isOffline);
                    }
                });
            } else {
                checkableSwitch.setVisibility(View.INVISIBLE);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(menuId);
                    }
                });
            }
        }

        public void setIcon(Drawable drawable) {
            icon.setImageDrawable(drawable);
        }

        public void setTitle(CharSequence titleText) {
            title.setText(titleText);
        }

        public void setOfflineAvailable(boolean isOffline) {
            this.isOffline = isOffline;
            checkableSwitch.setChecked(isOffline);
        }
    }
}
