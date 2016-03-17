package com.example.shustrik.vkdocs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shustrik.vkdocs.adapters.CursorDocListAdapter;
import com.example.shustrik.vkdocs.adapters.VKEntityListAdapter;
import com.example.shustrik.vkdocs.adapters.SpecDocListAdapter;
import com.example.shustrik.vkdocs.download.DocDownloader;
import com.example.shustrik.vkdocs.fragments.MainActivityFragment;
import com.example.shustrik.vkdocs.loaders.CommunitiesLoader;
import com.example.shustrik.vkdocs.loaders.CommunityDocsLoader;
import com.example.shustrik.vkdocs.loaders.DialogDocsLoader;
import com.example.shustrik.vkdocs.loaders.DialogsLoader;
import com.example.shustrik.vkdocs.loaders.MyDocsLoader;
import com.example.shustrik.vkdocs.sync.VKDocsSyncAdapter;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.example.shustrik.vkdocs.vk.VKScopes;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiUser;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Разобраться, как syncadapter оповещает активити
 * <p/>
 * Если что, navigation проверять здесь:
 * http://www.android4devs.com/2015/06/navigation-view-material-design-support.html
 * Проверить по
 * https://www.google.com/design/spec/patterns/navigation-drawer.html#
 */
public class MainActivity extends AppCompatActivity implements SelectCallback {
    private SharedPreferences prefs;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.navigation_view)
    NavigationView navigationView;

    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    TextView username;

    private ActionBarDrawerToggle actionBarDrawerToggle;


    public static final String USER_FNAME = "first_name";
    public static final String USER_LNAME = "last_name";
    public static final String USER_ID = "user_id";
    public static final String PREFS = "settings";
    public static final String TAG = "ANNA_MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("login", false)) {
            VKRequests.login(this,
                    VKScopes.DOCS.getName(),
                    VKScopes.FRIENDS.getName(),
                    VKScopes.GROUPS.getName(),
                    VKScopes.MESSAGES.getName());
        }

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    default:
                    case R.id.my_docs:
                        setFragment(getMyDocsFragment(), true);
                        return true;
                    case R.id.dialogs_docs:
                        setFragment(getDialogsFragment(), false);
                        return true;
                    case R.id.group_docs:
                        setFragment(getCommunitiesFragment(), false);
                        return true;
                    case R.id.global_docs:
                        Toast.makeText(getApplicationContext(), "Global", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });


        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.open_menu, R.string.close_menu) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (savedInstanceState == null) {
            setFragment(getMyDocsFragment(), true);
        }

        username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_username);
        Log.w("ANNA", "USERNAME: " + username);
        updateUsername(prefs.getString(USER_FNAME, ""), prefs.getString(USER_LNAME, ""));

        mSwipeRefreshLayout.setColorSchemeColors(R.color.blue, R.color.orange, R.color.purple, R.color.green);
    }

    private MainActivityFragment getMyDocsFragment() {
        DocDownloader docDownloader = new DocDownloader(this);
        CursorDocListAdapter adapter = new CursorDocListAdapter(this, docDownloader, R.menu.my_docs_options);
        return MainActivityFragment.getInstance(
                adapter,
                new MyDocsLoader(this, getSupportLoaderManager(), adapter, mSwipeRefreshLayout),
                docDownloader,
                false,
                "No documents found",
                "Loading documents..."
        );
    }

    private MainActivityFragment getDialogDocsFragment(int peerId) {
        DocDownloader docDownloader = new DocDownloader(this);
        SpecDocListAdapter adapter = new SpecDocListAdapter(this, docDownloader, R.menu.dialog_docs_options);
        return MainActivityFragment.getInstance(
                adapter,
                new DialogDocsLoader(adapter, peerId, mSwipeRefreshLayout),
                docDownloader,
                true,
                "No documents found",
                "Loading documents..."
        );
    }

    private MainActivityFragment getDialogsFragment() {
        VKEntityListAdapter adapter = new VKEntityListAdapter(this, new VKEntityListAdapter.OnClickHandler() {
            @Override
            public void onClick(VKEntityListAdapter.DialogViewHolder vh) {
                onDialogSelected(vh.getPeerId());
            }
        });
        return MainActivityFragment.getInstance(
                adapter,
                new DialogsLoader(adapter, mSwipeRefreshLayout),
                null,
                false,
                "No dialogs found",
                "Loading dialogs..."
        );
    }

    private MainActivityFragment getCommunitiesFragment() {
        VKEntityListAdapter adapter = new VKEntityListAdapter(this, new VKEntityListAdapter.OnClickHandler() {
            @Override
            public void onClick(VKEntityListAdapter.DialogViewHolder vh) {
                onCommunitySelected(vh.getPeerId());
            }
        });
        return MainActivityFragment.getInstance(
                adapter,
                new CommunitiesLoader(adapter, mSwipeRefreshLayout),
                null,
                false,
                "No communities found",
                "Loading communities..."
        );
    }

    private MainActivityFragment getCommunityDocsFragment(int peerId) {
        DocDownloader docDownloader = new DocDownloader(this);
        SpecDocListAdapter adapter = new SpecDocListAdapter(this, docDownloader, R.menu.community_docs_options);
        return MainActivityFragment.getInstance(
                adapter,
                new CommunityDocsLoader(adapter, peerId, mSwipeRefreshLayout),
                docDownloader,
                true,
                "No documents found",
                "Loading documents..."
        );
    }

    @Override
    public void setToggleListener(boolean state) {
        actionBarDrawerToggle.setDrawerIndicatorEnabled(state);
    }


    private void setFragment(Fragment fragment, boolean isFab) {
        setFragment(fragment, isFab, null);
    }


    private void setFragment(Fragment fragment, boolean isFab, String backStackName) {
        if (isFab) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, fragment)
                .addToBackStack(backStackName)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUsername(String firstName, String lastName) {
        if (navigationView != null && username != null) {
            username.setText(firstName + " " + lastName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context context = this;
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.w(TAG, "Authorisation result: " + res.toString());
                final SharedPreferences.Editor e = prefs.edit();
                e.putBoolean("login", true);
                e.commit();
                final String firstName = prefs.getString(USER_FNAME, "");
                final String lastName = prefs.getString(USER_LNAME, "");
                VKDocsSyncAdapter.initializeSyncAdapter(context);
                VKRequests.getUserInfo(new VKRequestCallback<VKApiUser>() {
                    @Override
                    public void onSuccess(VKApiUser userInfo) {
                        if (!firstName.equals(userInfo.first_name) || !lastName.equals(userInfo.last_name)) {
                            Log.w("ANNA", "New username " + userInfo.first_name + userInfo.last_name);
                            e.putString(USER_FNAME, userInfo.first_name);
                            e.putString(USER_LNAME, userInfo.last_name);
                            e.putInt(USER_ID, userInfo.id);
                            e.commit();
                            updateUsername(userInfo.first_name, userInfo.last_name);
                        }
                    }

                    @Override
                    public void onError(VKError e) {
                        Log.w("ANNA", "User info error");
                    }
                });
            }

            @Override
            public void onError(VKError error) {
                Log.w(TAG, "Authorisation error: " + error.toString());
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDialogSelected(int peerId) {
        setFragment(getDialogDocsFragment(peerId), false);
    }

    @Override
    public void onCommunitySelected(int peerId) {
        setFragment(getCommunityDocsFragment(peerId), false);
    }

    public void snack(String text, int length) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, text, length);
        snackbar.show();
    }


}
