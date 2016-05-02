package com.example.shustrik.vkdocs;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shustrik.vkdocs.adapters.CustomAdapter;
import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.adapters.VKEntityListAdapter;
import com.example.shustrik.vkdocs.download.DefaultDownloader;
import com.example.shustrik.vkdocs.download.DocDownloaderHolder;
import com.example.shustrik.vkdocs.fragments.MainActivityFragment;
import com.example.shustrik.vkdocs.loaders.CommunitiesLoader;
import com.example.shustrik.vkdocs.loaders.CommunityDocsLoader;
import com.example.shustrik.vkdocs.loaders.CustomLoader;
import com.example.shustrik.vkdocs.loaders.DialogDocsLoader;
import com.example.shustrik.vkdocs.loaders.DialogsLoader;
import com.example.shustrik.vkdocs.loaders.GlobalLoader;
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
 * <p>
 * Если что, navigation проверять здесь:
 * http://www.android4devs.com/2015/06/navigation-view-material-design-support.html
 * Проверить по
 * https://www.google.com/design/spec/patterns/navigation-drawer.html#
 */
public class MainActivity extends AppCompatActivity implements SelectCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String USER_FNAME = "first_name";
    public static final String USER_LNAME = "last_name";
    public static final String USER_ID = "user_id";
    public static final String PREFS = "settings";
    public static final String TAG = "ANNA_MainActivity";
    public static final int MY_DOCS = 0;
    public static final int DIALOGS = 1;
    public static final int COMMUNITIES = 2;
    public static final int DIALOG_DOCS = 3;
    public static final int COMMUNITY_DOCS = 4;
    public static final int GLOBAL = 5;
    private static MainActivityFragment curFragment;
    private static int dialogPeer = -1;
    private static int communityPeer = -1;
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


//    @Override
//    protected void onStop() {
//        DocDownloaderHolder.detach();
//        super.onStop();
//    }
    private SharedPreferences prefs;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private long userId;

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
        } else {
            userId = prefs.getInt(USER_ID, -1);
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

        DefaultDownloader.init(this);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    default:
                    case R.id.my_docs:
                        toolbar.setTitle("My Documents");
                        setFragment(getMyDocsFragment(), true);
                        return true;
                    case R.id.dialogs_docs:
                        toolbar.setTitle("Dialog Documents");
                        setFragment(getDialogsFragment(), false);
                        return true;
                    case R.id.group_docs:
                        toolbar.setTitle("Community Documents");
                        setFragment(getCommunitiesFragment(), false);
                        return true;
                    case R.id.global_docs:
                        setFragment(getGlobalFragment(), false);
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

        DocDownloaderHolder.attach(this);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.w("ANNA", "Search ction");
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        curFragment.search(query);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DocDownloaderHolder.attach(this);
    }

    private MainActivityFragment getMyDocsFragment() {
        return MainActivityFragment.getInstance(MY_DOCS, false, "No documents found",
                "Loading documents...");
    }

    private MainActivityFragment getDialogDocsFragment() {
        return MainActivityFragment.getInstance(DIALOG_DOCS, true, "No documents found",
                "Loading documents...");
    }

    private MainActivityFragment getDialogsFragment() {
        return MainActivityFragment.getInstance(DIALOGS, false, "No dialogs found",
                "Loading dialogs...");
    }

    private MainActivityFragment getCommunitiesFragment() {
        return MainActivityFragment.getInstance(COMMUNITIES, false, "No communities found",
                "Loading communities...");
    }

    private MainActivityFragment getGlobalFragment() {
        return MainActivityFragment.getInstance(GLOBAL, false, "No communities found",
                "Loading communities...");
    }

    private MainActivityFragment getCommunityDocsFragment() {
        return MainActivityFragment.getInstance(COMMUNITY_DOCS, true, "No documents found",
                "Loading documents...");
    }

    public Pair<CustomAdapter, CustomLoader> createAdapterAndLoader(int t) {
        switch (t) {
            case MY_DOCS:
                return getMyDocsAdapterAndLoader();
            case DIALOGS:
                return getDialogsAdapterAndLoader();
            case DIALOG_DOCS:
                return getDialogDocsAdapterAndLoader();
            case COMMUNITIES:
                return getCommunitiesAdapterAndLoader();
            case COMMUNITY_DOCS:
                return getCommunityDocsAdapterAndLoader();
            case GLOBAL:
                return getGlobalDocsAdapterAndLoader();
            default:
                return null;
        }
    }

    private Pair<CustomAdapter, CustomLoader> getMyDocsAdapterAndLoader() {
        //CursorDocListAdapter adapter = new CursorDocListAdapter(this, R.menu.my_docs_options, MY_DOCS);
        DocListAdapter adapter = new DocListAdapter(this, R.menu.my_docs_options, MY_DOCS);
        CustomLoader loader = MyDocsLoader.initAndGetInstance(this, adapter, mSwipeRefreshLayout, getSupportLoaderManager());
        return new Pair<>((CustomAdapter) adapter, loader);
    }

    private Pair<CustomAdapter, CustomLoader> getDialogsAdapterAndLoader() {
        VKEntityListAdapter adapter = new VKEntityListAdapter(this, new VKEntityListAdapter.OnClickHandler() {
            @Override
            public void onClick(VKEntityListAdapter.DialogViewHolder vh) {
                onDialogSelected(vh.getPeerId(), vh.getName());
            }
        });
        CustomLoader loader = DialogsLoader.initAndGetInstance(this, adapter, mSwipeRefreshLayout, getSupportLoaderManager(), userId);
        return new Pair<>((CustomAdapter) adapter, loader);
    }

    private Pair<CustomAdapter, CustomLoader> getCommunitiesAdapterAndLoader() {
        VKEntityListAdapter adapter = new VKEntityListAdapter(this, new VKEntityListAdapter.OnClickHandler() {
            @Override
            public void onClick(VKEntityListAdapter.DialogViewHolder vh) {
                onCommunitySelected(vh.getPeerId(), vh.getName());
            }
        });
        CustomLoader loader = CommunitiesLoader.initAndGetInstance(this, adapter, mSwipeRefreshLayout, getSupportLoaderManager(), userId);
        return new Pair<>((CustomAdapter) adapter, loader);
    }

    private Pair<CustomAdapter, CustomLoader> getDialogDocsAdapterAndLoader() {
        DocListAdapter adapter = new DocListAdapter(this, R.menu.dialog_docs_options, DIALOG_DOCS);
        CustomLoader loader = new DialogDocsLoader(adapter, dialogPeer, mSwipeRefreshLayout);
        return new Pair<>((CustomAdapter) adapter, loader);
    }

    private Pair<CustomAdapter, CustomLoader> getCommunityDocsAdapterAndLoader() {
        DocListAdapter adapter = new DocListAdapter(this, R.menu.community_docs_options, COMMUNITY_DOCS);
        CustomLoader loader = new CommunityDocsLoader(adapter, communityPeer, mSwipeRefreshLayout);
        return new Pair<>((CustomAdapter) adapter, loader);
    }


    private Pair<CustomAdapter, CustomLoader> getGlobalDocsAdapterAndLoader() {
        DocListAdapter adapter = new DocListAdapter(this, R.menu.global_docs_options, COMMUNITY_DOCS);
        CustomLoader loader = new GlobalLoader(adapter, communityPeer, mSwipeRefreshLayout);
        return new Pair<>((CustomAdapter) adapter, loader);
    }

    @Override
    public void setToggleListener(boolean state) {
        actionBarDrawerToggle.setDrawerIndicatorEnabled(state);
    }

    private void setFragment(MainActivityFragment fragment, boolean isFab) {
        setFragment(fragment, isFab, null);
    }

    private void setFragment(MainActivityFragment fragment, boolean isFab, String backStackName) {
        curFragment = fragment;
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        final MenuItem searchMI = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMI,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        Log.w("ANNA", "Expand");
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        Log.w("ANNA", "Collapse");
                        curFragment.backToList();
                        return true;
                    }
                });

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
                        userId = userInfo.id;
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
    public void onDialogSelected(int peerId, CharSequence name) {
        Log.w("ANNA", "on dialog selected " + peerId);
        dialogPeer = peerId;
        toolbar.setTitle(name);
        setFragment(getDialogDocsFragment(), false);
    }

    @Override
    public void onCommunitySelected(int peerId, CharSequence name) {
        communityPeer = peerId;
        toolbar.setTitle(name);
        setFragment(getCommunityDocsFragment(), false);
    }

    public void snack(String text, int length) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, text, length);
        snackbar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case DefaultDownloader.DOWNLOAD_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DefaultDownloader.getInstance().download();
                } else {
                    snack("Not enough permissions to complete download", Snackbar.LENGTH_SHORT);
                }
            }
        }
    }
}
