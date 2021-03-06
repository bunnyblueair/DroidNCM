package io.bunnyblue.droidncm.finder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.List;

import io.bunnyblue.droidncm.R;
import io.bunnyblue.droidncm.finder.dummy.NCMFileContent;
import io.bunnyblue.droidncm.finder.task.AboutFragment;
import io.bunnyblue.droidncm.finder.task.FileConvertTask;
import io.bunnyblue.droidncm.finder.task.NCMFileFinder;
import io.bunnyblue.droidncm.utils.DocumentsUtils;

public class MainFinderActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static String TAG = "ncm";
    public NCMFileContent ncmFileContent = null;
    public AboutFragment aboutFragment = null;
    LocalFileFragment localFileFragment = new LocalFileFragment(ncmFileContent);
    String rootPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] paths = DocumentsUtils.getExtSdCardPath(this);
        if (paths != null && paths.length > 0) {
            rootPath = paths[0];
        }

        Log.d("ncm", "rootPath " + rootPath);
        setContentView(R.layout.activity_main_finder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File targetDir = new File(Environment.getExternalStorageDirectory(), "Music");
                if (!targetDir.exists()) {//create Music if not exist
                    targetDir.mkdir();
                }
                NCMFileFinder ncmFileFinder = new NCMFileFinder(MainFinderActivity.this);
                if (rootPath != null) {
                    Log.d("ncm", "rootPath " + rootPath);
                    ncmFileFinder.execute(new File(Environment.getExternalStorageDirectory(), "netease"), new File(rootPath));
                } else {
                    ncmFileFinder.execute(new File(Environment.getExternalStorageDirectory(), "netease"));
                }
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.container, localFileFragment).commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0x11);
            //  requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},0x11);
        }
        if (rootPath != null) {
            if (!DocumentsUtils.checkWritableRootPath(this, rootPath)) {
                showOpenDocumentTree();
            }
        }
        init();
        help();
    }

    private void init() {
        FloatingActionButton sortBtn = (FloatingActionButton) findViewById(R.id.sortBtn);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder sortDialog =
                        new AlertDialog.Builder(MainFinderActivity.this);
                //  SortTypeAdapter sortTypeAdapter=new SortTypeAdapter(MainFinderActivity.this);
                String[] sortTypeList = getResources().getStringArray(R.array.sortTypeList);
                SharedPreferences sharedPreferences = getSharedPreferences("ncm", Context.MODE_PRIVATE);
                final int defaultType = SortTypeHelper.getDefaultType(MainFinderActivity.this);
                final int[] typeRC = {defaultType};
                sortDialog.setSingleChoiceItems(sortTypeList, defaultType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: " + which);
                        typeRC[0] = which;
                    }
                });
                sortDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (typeRC[0] == defaultType) {

                            Toast.makeText(MainFinderActivity.this, R.string.tips_sort_type_nochange, Toast.LENGTH_SHORT).show();
                        } else {
                            SortTypeHelper.update(MainFinderActivity.this, typeRC[0]);
                            ncmFileContent.updateSortType(typeRC[0]);
                            ncmFileContent.requestSort();
                            localFileFragment.updateSort();

                            Toast.makeText(MainFinderActivity.this, R.string.tips_sort_type_changed, Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();

                    }
                });
                sortDialog.create().show();
            }
        });
    }

    void help() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????");
        builder.setMessage("????????????????????????\n???????????????????????????????????????");
        builder.setPositiveButton("????OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //  requestPermissions(new String[]{Androi});
    }


    private void showOpenDocumentTree() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            StorageManager sm = getSystemService(StorageManager.class);

            StorageVolume volume = sm.getStorageVolume(new File(rootPath));

            if (volume != null) {
                intent = volume.createAccessIntent(null);
            }
        }

        if (intent == null) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == DocumentsUtils.OPEN_DOCUMENT_TREE_CODE) {

            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                DocumentsUtils.saveTreeUri(this, rootPath, uri);
            }
        }
        if (resultCode != RESULT_OK) {
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment f : fragments) {
                f.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_finder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (ncmFileContent == null || ncmFileContent.getITEMS().isEmpty()) {
                Toast.makeText(this, "??????????????????????????????NCM??????????????????", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                doBatchConvert();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doBatchConvert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????");
        builder.setMessage(String.format("?????? ncm?????? %s ?????????????????????????????????", ncmFileContent.getITEMS().size()));
        builder.setPositiveButton("?????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FileConvertTask convertTask = new FileConvertTask(MainFinderActivity.this);

                convertTask.execute(ncmFileContent);
            }
        }).create().show();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.importFile) {
            //  if (getSupportFragmentManager().getPrimaryNavigationFragment() != localFileFragment) {

            getSupportFragmentManager().beginTransaction().replace(R.id.container, localFileFragment).commit();
            //  }

            // Handle the camera action
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_about) {
            //  if (getSupportFragmentManager().getPrimaryNavigationFragment() != aboutFragment) {
            if (aboutFragment == null) {
                aboutFragment = new AboutFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, aboutFragment).commit();

            //   }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // drawer.set
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateNCMFileList() {
        if (ncmFileContent == null || ncmFileContent.getITEMS().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("DroidNCM").setMessage("??????????????????").setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create().show();

        }
        localFileFragment.updateFileList(ncmFileContent);


    }
}
