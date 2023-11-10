package my.project.sakuraproject.main.about;

import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.LogAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.config.AboutEnum;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cache)
    TextView cache;
    @BindView(R.id.footer)
    LinearLayout footer;
    @BindView(R.id.source_title)
    TextView sourceTitleView;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_about;
    }

    @Override
    protected void init() {
//        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initViews();
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    @Override
    protected void setConfigurationChanged() {

    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.about));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initViews() {
        LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.getNavigationBarHeight(this));
        footer.findViewById(R.id.footer).setLayoutParams(Params);
        sourceTitleView.setText(Utils.isImomoe() ? (String) SharedPreferencesUtils.getParam(AboutActivity.this, "imomoe_domain", Utils.getString(R.string.imomoe_url)) : (String) SharedPreferencesUtils.getParam(AboutActivity.this, "domain", Utils.getString(R.string.domain_url)));
        cache.setText(!Utils.hasFilePermission() ? getFilesDir().getPath() : Environment.getExternalStorageDirectory() + Utils.getString(R.string.cache_text));
    }

    @OnClick({R.id.sakura,R.id.github})
    public void openBrowser(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.sakura:
                Utils.viewInChrome(this, Sakura.DOMAIN);
                break;
            case R.id.github:
                Utils.viewInChrome(this, Utils.getString(R.string.github_url));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_menu, menu);
        MenuItem updateLogItem = menu.findItem(R.id.update_log);
        MenuItem openSourceItem = menu.findItem(R.id.open_source);
        if (!Utils.getTheme()) {
            updateLogItem.setIcon(R.drawable.baseline_insert_chart_outlined_black_48dp);
            openSourceItem.setIcon(R.drawable.baseline_all_inclusive_black_48dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_log:
                showUpdateLogs();
                break;
            case R.id.open_source:
                if (Utils.isFastClick()) startActivity(new Intent(AboutActivity.this,OpenSourceActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showUpdateLogs() {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.update_log));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_log, null);
        RecyclerView logs = view.findViewById(R.id.rv_list);
        logs.setLayoutManager(new LinearLayoutManager(this));
        LogAdapter logAdapter = new LogAdapter(AboutEnum.getVersionList());
        logs.setAdapter(logAdapter);
        builder.setPositiveButton(Utils.getString(R.string.page_positive), null);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
    }
}
