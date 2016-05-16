package com.example.shustrik.vkdocs;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.shustrik.vkdocs.adapters.CustomAdapter;
import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.adapters.VKEntityListAdapter;
import com.example.shustrik.vkdocs.download.DefaultDownloader;
import com.example.shustrik.vkdocs.download.DocDownloaderHolder;
import com.example.shustrik.vkdocs.download.DocNotificationManager;
import com.example.shustrik.vkdocs.download.GoogleDriveDownloader;
import com.example.shustrik.vkdocs.fragments.MainActivityFragment;
import com.example.shustrik.vkdocs.loaders.CommunitiesLoader;
import com.example.shustrik.vkdocs.loaders.CommunityDocsLoader;
import com.example.shustrik.vkdocs.loaders.CustomLoader;
import com.example.shustrik.vkdocs.loaders.DialogDocsLoader;
import com.example.shustrik.vkdocs.loaders.DialogsLoader;
import com.example.shustrik.vkdocs.loaders.GlobalLoader;
import com.example.shustrik.vkdocs.loaders.MyDocsLoader;
import com.example.shustrik.vkdocs.sync.VKDocsSyncAdapter;
import com.example.shustrik.vkdocs.uicommon.FabManager;
import com.example.shustrik.vkdocs.uicommon.OpenFileDialog;
import com.example.shustrik.vkdocs.uicommon.OpenFileDialogPermissionsManager;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.example.shustrik.vkdocs.vk.VKScopes;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiUser;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SelectCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String USER_FNAME = "first_name";
    public static final String USER_LNAME = "last_name";
    public static final String USER_ID = "user_id";
    public static final String PREFS = "settings";
    public static final String LOGIN = "login";

    private static final int REQUEST_CODE_RESOLUTION = 3;

    public static final int MY_DOCS = 0;
    public static final int DIALOGS = 1;
    public static final int COMMUNITIES = 2;
    public static final int DIALOG_DOCS = 3;
    public static final int COMMUNITY_DOCS = 4;
    public static final int GLOBAL = 5;
    private static final int REQUEST_CODE_OPENER = 33;
    private static final String USER_PIC = "USER_PIC";
    private static MainActivityFragment curFragment;
    private static int dialogPeer = -1;
    private static int communityPeer = -1;

    private Tracker mTracker;

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

    private DriveId mFileId;
    private boolean isWaitingForGDdownload = false;
    private GoogleApiClient mGoogleApiClient;

    private SharedPreferences prefs;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((ApplicationWithVK)getApplication()).getDefaultTracker();

        prefs = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(LOGIN, false)) {
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

        final Activity thisActivity = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FabManager(thisActivity, coordinatorLayout, new FabManager.FabCallback() {
                    @Override
                    public void onLocalUpload() {
                        OpenFileDialogPermissionsManager.checkPermissions(MainActivity.this,
                                new OpenFileDialogPermissionsManager.Callback() {
                                    @Override
                                    public void performIfGranted() {
                                        openFileChooser();
                                    }
                                });
                    }

                    @Override
                    public void onDriveUpload() {
                        IntentSender intentSender = Drive.DriveApi
                                .newOpenFileActivityBuilder()
                                .setMimeType(new String[]{"text/plain", "text/html", "image/jpeg",
                                        "text/xml", "text/plain", "application/pdf", "image/png",
                                        "image/gif", "image/bmp", "application/msword", "audio/mpeg",
                                        "application/zip", "application/rar", "application/tar"

                                })
                                .build(mGoogleApiClient);
                        try {
                            startIntentSenderForResult(intentSender,
                                    REQUEST_CODE_OPENER, null, 0, 0, 0);

                        } catch (IntentSender.SendIntentException e) {

                        }
                    }
                }).show();
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
                        setFragment(getMyDocsFragment(), true);
                        return true;
                    case R.id.dialogs_docs:
                        setFragment(getDialogsFragment(), false);
                        return true;
                    case R.id.group_docs:
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
        updateUserInfo(prefs.getString(USER_FNAME, ""), prefs.getString(USER_LNAME, ""),
                prefs.getString(USER_PIC, ""));

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
            doMySearch(query);
        }
    }

    private void openFileChooser() {
        OpenFileDialog fileDialog = new OpenFileDialog(MainActivity.this)
                .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                    @Override
                    public void OnSelectedFile(String filePath) {
                        final File f = new File(filePath);
                        DocNotificationManager.createUploadingNotification(MainActivity.this, f.getName());
                        VKRequests.upload(new VKRequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void obj) {
                                snack(getString(R.string.upload_success, f.getName()), Snackbar.LENGTH_LONG);
                                VKDocsSyncAdapter.syncImmediately(MainActivity.this);
                                DocNotificationManager.dismissNotification(MainActivity.this, f.getName().hashCode());
                            }

                            @Override
                            public void onError(VKError e) {
                                snack(getString(R.string.upload_mistake, f.getName()), Snackbar.LENGTH_LONG);
                                DocNotificationManager.dismissNotification(MainActivity.this, f.getName().hashCode());
                            }
                        }, f, f.getName());
                    }
                });
        fileDialog.show();
    }

    private void doMySearch(String query) {
        curFragment.search(query);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DocDownloaderHolder.attach(this);
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private MainActivityFragment getMyDocsFragment() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Documents")
                .setAction("Choose")
                .setLabel("My Documents")
                .build());
        return MainActivityFragment.getInstance(MY_DOCS, false, getString(R.string.no_docs_found),
                getString(R.string.loading_docs), getString(R.string.my_documents_title));
    }

    private MainActivityFragment getDialogDocsFragment(String title) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Documents")
                .setAction("Choose")
                .setLabel("Dialog Documents")
                .build());
        return MainActivityFragment.getInstance(DIALOG_DOCS, true, getString(R.string.no_docs_found),
                getString(R.string.loading_docs), title);
    }

    private MainActivityFragment getDialogsFragment() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Documents")
                .setAction("Choose")
                .setLabel("Dialogs")
                .build());
        return MainActivityFragment.getInstance(DIALOGS, false, getString(R.string.no_dialogs_found),
                getString(R.string.loading_dialogs), getString(R.string.dialog_documents_title));
    }

    private MainActivityFragment getCommunitiesFragment() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Documents")
                .setAction("Choose")
                .setLabel("Communities")
                .build());
        return MainActivityFragment.getInstance(COMMUNITIES, false, getString(R.string.no_groups_found),
                getString(R.string.loading_groups), getString(R.string.community_documents_title));
    }

    private MainActivityFragment getGlobalFragment() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Documents")
                .setAction("Choose")
                .setLabel("Global")
                .build());
        return MainActivityFragment.getInstance(GLOBAL, false, getString(R.string.no_docs_found),
                getString(R.string.loading_docs), getString(R.string.global_docs));
    }

    private MainActivityFragment getCommunityDocsFragment(String title) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Documents")
                .setAction("Choose")
                .setLabel("Community Documents")
                .build());
        return MainActivityFragment.getInstance(COMMUNITY_DOCS, true, getString(R.string.no_docs_found),
                getString(R.string.loading_docs), title);
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
        CustomLoader loader = new GlobalLoader(adapter, mSwipeRefreshLayout);
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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        final MenuItem searchMI = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMI,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        curFragment.backToList();
                        return true;
                    }
                });

        return true;
    }

    private void updateUserInfo(String firstName, String lastName, String imgPath) {
        if (navigationView != null && username != null) {
            username.setText(String.format("%s %s", firstName, lastName));
        }
        if (imgPath.isEmpty()) {
            Picasso.with(this)
                    .load(R.drawable.no_photo)
                    .resize((int) getResources().getDimension(R.dimen.image_size),
                            (int) getResources().getDimension(R.dimen.image_size))
                    .centerCrop()
                    .into((ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image));
        } else {
            Picasso.with(this)
                    .load(imgPath)
                    .resize((int) getResources().getDimension(R.dimen.image_size),
                            (int) getResources().getDimension(R.dimen.image_size))
                    .centerCrop()
                    .placeholder(R.drawable.no_photo)
                    .error(R.drawable.error)
                    .into((ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context context = this;
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                final SharedPreferences.Editor e = prefs.edit();
                e.putBoolean(LOGIN, true);
                e.commit();
                final String firstName = prefs.getString(USER_FNAME, "");
                final String lastName = prefs.getString(USER_LNAME, "");
                final String pic = prefs.getString(USER_PIC, "");
                VKDocsSyncAdapter.initializeSyncAdapter(context);
                VKRequests.getUserInfo(new VKRequestCallback<VKApiUser>() {
                    @Override
                    public void onSuccess(VKApiUser userInfo) {
                        if (!firstName.equals(userInfo.first_name) ||
                                !lastName.equals(userInfo.last_name) ||
                                !pic.equals(userInfo.photo_100)) {
                            e.putString(USER_FNAME, userInfo.first_name);
                            e.putString(USER_LNAME, userInfo.last_name);
                            e.putString(USER_PIC, userInfo.photo_100);
                            e.putInt(USER_ID, userInfo.id);
                            e.commit();
                            updateUserInfo(userInfo.first_name, userInfo.last_name, userInfo.photo_100);
                        }
                        userId = userInfo.id;
                    }

                    @Override
                    public void onError(VKError e) {
                        snack(getString(R.string.user_info_error), Snackbar.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onError(VKError error) {
            }
        })) {
            if (requestCode == REQUEST_CODE_OPENER) {
                if (resultCode == RESULT_OK) {
                    mFileId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    if (!mGoogleApiClient.isConnected()) {
                        isWaitingForGDdownload = true;
                        mGoogleApiClient.connect();
                    } else {
                        downloadFileFromDrive();
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void downloadFileFromDrive() {
        isWaitingForGDdownload = false;
        GoogleDriveDownloader.download(mGoogleApiClient, mFileId, this, new GoogleDriveDownloader.Callback() {
            @Override
            public void onDownloadSuccess(final File f, String name) {
                DocNotificationManager.createUploadingNotification(MainActivity.this, f.getName());
                VKRequests.upload(new VKRequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void obj) {
                        snack(getString(R.string.upload_success, f.getName()), Snackbar.LENGTH_LONG);
                        DocNotificationManager.dismissNotification(MainActivity.this, f.getName().hashCode());
                        VKDocsSyncAdapter.syncImmediately(MainActivity.this);
                    }

                    @Override
                    public void onError(VKError e) {
                        snack(getString(R.string.upload_mistake, f.getName()), Snackbar.LENGTH_LONG);
                        DocNotificationManager.dismissNotification(MainActivity.this, f.getName().hashCode());
                    }
                }, f, name);
            }

            @Override
            public void onDownloadFail(String reason) {
            }
        });
    }

    @Override
    public void onDialogSelected(int peerId, CharSequence name) {
        dialogPeer = peerId;
        setFragment(getDialogDocsFragment(name.toString()), false);
    }

    @Override
    public void onCommunitySelected(int peerId, CharSequence name) {
        communityPeer = peerId;
        setFragment(getCommunityDocsFragment(name.toString()), false);
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
                    snack(getString(R.string.lack_download_permissions), Snackbar.LENGTH_SHORT);
                }
            }
            case OpenFileDialogPermissionsManager.READ_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFileChooser();
                } else {
                    snack(getString(R.string.lack_download_permissions), Snackbar.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (isWaitingForGDdownload) {
            downloadFileFromDrive();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
        }
    }


    public void setTitle(String name) {
        toolbar.setTitle(name);
    }

    public ActionBarDrawerToggle getToggle() {
        return actionBarDrawerToggle;
    }
}
