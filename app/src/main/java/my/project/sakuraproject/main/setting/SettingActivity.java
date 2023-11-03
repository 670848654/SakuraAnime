package my.project.sakuraproject.main.setting;

import static my.project.sakuraproject.config.SettingEnum.*;

import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.arialyy.aria.core.Aria;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.config.SettingEnum;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.about.AboutActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.domain_default)
    TextView domainDefaultTV;
    @BindView(R.id.player_default)
    TextView playerDefaultTV;
    @BindView(R.id.footer)
    LinearLayout footer;
    private String url;
    private boolean isImomoe;
    @BindView(R.id.check_favorite_update)
    TextView checkFavoriteUpdateTV;
    @BindView(R.id.download_number)
    TextView downloadNumberTV;
    private String[] downloadNumbers = Utils.getArray(R.array.download_numbers);
    private AlertDialog alertDialog;
    private String downloadUrl;
    private Call downCall;
    @BindView(R.id.show)
    CoordinatorLayout show;
    @BindView(R.id.danmu_select)
    TextView danmuSelectTV;
    @BindView(R.id.kernel_default)
    TextView kernelDefaultTV;
    @BindView(R.id.check_update_default)
    TextView checkUpdateDefaultTV;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_setting;
    }

    @Override
    protected void init() {
//        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initViews();
        getUserCustomSet();
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    @Override
    protected void setConfigurationChanged() {

    }

    public void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Utils.getString(R.string.home_setting_item_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initViews() {
        LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.getNavigationBarHeight(this));
        footer.findViewById(R.id.footer).setLayoutParams(Params);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) show.getLayoutParams();
        params.setMargins(10, 0, 10, Utils.getNavigationBarHeight(this) - 5);
        show.setLayoutParams(params);
    }

    public void getUserCustomSet() {
        isImomoe = Utils.isImomoe();
        domainDefaultTV.setText(Sakura.DOMAIN);
        int setDefaultPlayer = (Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0);
        playerDefaultTV.setText(VIDEO_PLAYER.getItems()[setDefaultPlayer]);
        int setPlayerKernel = (Integer) SharedPreferencesUtils.getParam(this, "player_kernel", 0);
        kernelDefaultTV.setText(VIDEO_PLAYER_KERNEL.getItems()[setPlayerKernel]);
        int setStartCheckUpdate = (Integer) SharedPreferencesUtils.getParam(this, "start_check_update", 0);
        checkUpdateDefaultTV.setText(CHECK_APP_UPDATE.getItems()[setStartCheckUpdate]);
        checkFavoriteUpdateTV.setText((Boolean) SharedPreferencesUtils.getParam(this, "check_favorite_update", true) ? CHECK_AMINE_UPDATE.getItems()[0] : CHECK_AMINE_UPDATE.getItems()[1]);
        downloadNumberTV.setText(((Integer) SharedPreferencesUtils.getParam(this, "download_number", 0) + 1) + "");
        danmuSelectTV.setText((Boolean) SharedPreferencesUtils.getParam(this, "open_danmu", true) ? VIDEO_PLAYER_DANMU.getItems()[0] : VIDEO_PLAYER_DANMU.getItems()[1]);
    }

    @OnClick({R.id.set_domain, R.id.set_player, R.id.set_player_kernel, R.id.start_check_update, R.id.set_favorite_update, R.id.set_download_number, R.id.remove_downloads, R.id.set_danmu})
    public void onClick(RelativeLayout layout) {
        switch (layout.getId()) {
            case R.id.set_domain:
                setDomain();
                break;
            case R.id.set_player:
                setDefaultPlayer();
                break;
            case R.id.set_player_kernel:
                setPlayerKernel();
                break;
            case R.id.start_check_update:
                setCheckUpdate();
                break;
            case R.id.set_favorite_update:
                setCheckFavoriteUpdateState();
                break;
            case R.id.set_download_number:
                setDownloadNumber();
                break;
            case R.id.remove_downloads:
                removeDownloads();
                break;
            case R.id.set_danmu:
                setDanmu();
                break;
        }
    }

    public void setDomain() {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.domain_title));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_domain, null);
        Spinner spinner = view.findViewById(R.id.prefix);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                url = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        TextInputLayout textInputLayout = view.findViewById(R.id.domain);
        textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        String defaultUrl = isImomoe ? (String) SharedPreferencesUtils.getParam(this, "imomoe_domain", Utils.getString(R.string.imomoe_url)) : (String) SharedPreferencesUtils.getParam(this, "domain", Utils.getString(R.string.domain_url));
        spinner.setSelection(defaultUrl.startsWith("https") ? 1 : 0, true);
        textInputLayout.getEditText().setText(defaultUrl.replaceAll("http://", "").replaceAll("https://", ""));
        builder.setPositiveButton(Utils.getString(R.string.page_positive_edit), null);
        builder.setNegativeButton(Utils.getString(R.string.page_negative), null);
        builder.setNeutralButton(Utils.getString(R.string.page_def), null);
        builder.setCancelable(false);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = textInputLayout.getEditText().getText().toString();
            if (!text.equals("")) {
                if (Patterns.WEB_URL.matcher(text).matches()) {
//                    setResult(0x20);
                    if (text.endsWith("/")) text = text.substring(0, text.length() - 1);
                    url += text;
                    if (isImomoe)
                        SharedPreferencesUtils.setParam(SettingActivity.this, "imomoe_domain", url);
                    else
                        SharedPreferencesUtils.setParam(SettingActivity.this, "domain", url);
                    Sakura.setApi();
                    domainDefaultTV.setText(url);
                    alertDialog.dismiss();
                    EventBus.getDefault().post(new Refresh(0));
//                    application.showSuccessToastMsg(Utils.getString(R.string.set_domain_ok));
                    CustomToast.showToast(this, Utils.getString(R.string.set_domain_ok), CustomToast.SUCCESS);
                } else textInputLayout.getEditText().setError(Utils.getString(R.string.set_domain_error2));
            } else textInputLayout.getEditText().setError(Utils.getString(R.string.set_domain_error1));
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
//            setResult(0x20);
            if (isImomoe)
                SharedPreferencesUtils.setParam(SettingActivity.this, "imomoe_domain", Utils.getString(R.string.imomoe_url));
            else
                SharedPreferencesUtils.setParam(SettingActivity.this, "domain", Utils.getString(R.string.domain_url));
            Sakura.setApi();
            domainDefaultTV.setText(Sakura.DOMAIN);
            EventBus.getDefault().post(new Refresh(0));
            alertDialog.dismiss();
        });
    }

    public void setDefaultPlayer() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(VIDEO_PLAYER.getDialogTitle());
        builder.setSingleChoiceItems(VIDEO_PLAYER.getItems(), (Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0), (dialog, which) -> {
            playerDefaultTV.setText(VIDEO_PLAYER.getItems()[which]);
            SharedPreferencesUtils.setParam(getApplicationContext(), "player", which);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setPlayerKernel() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(VIDEO_PLAYER_KERNEL.getDialogTitle());
        builder.setSingleChoiceItems(VIDEO_PLAYER_KERNEL.getItems(), (Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player_kernel", 0), (dialog, which) -> {
            kernelDefaultTV.setText(VIDEO_PLAYER_KERNEL.getItems()[which]);
            SharedPreferencesUtils.setParam(getApplicationContext(), "player_kernel", which);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setCheckUpdate() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(CHECK_APP_UPDATE.getDialogTitle());
        builder.setSingleChoiceItems(CHECK_APP_UPDATE.getItems(), (Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "start_check_update", 0), (dialog, which) -> {
            checkUpdateDefaultTV.setText(CHECK_APP_UPDATE.getItems()[which]);
            SharedPreferencesUtils.setParam(getApplicationContext(), "start_check_update", which);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setCheckFavoriteUpdateState() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(CHECK_AMINE_UPDATE.getDialogTitle());
        builder.setSingleChoiceItems(CHECK_AMINE_UPDATE.getItems(), (Boolean) SharedPreferencesUtils.getParam(this, "check_favorite_update", true) ? 0 : 1, (dialog, which) -> {
            SharedPreferencesUtils.setParam(getApplicationContext(), "check_favorite_update", which == 0);
            checkFavoriteUpdateTV.setText(CHECK_AMINE_UPDATE.getItems()[which]);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setDownloadNumber() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle("设置同时下载任务数量");
        builder.setSingleChoiceItems(downloadNumbers, (Integer) SharedPreferencesUtils.getParam(this, "download_number", 0), (dialog, which) -> {
            SharedPreferencesUtils.setParam(this, "download_number", which);
            downloadNumberTV.setText(downloadNumbers[which]);
            Aria.get(this).getDownloadConfig().setMaxTaskNum(Integer.valueOf(downloadNumbers[which]));
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void removeDownloads() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setPositiveButton(Utils.getString(R.string.page_positive), null);
        builder.setNegativeButton(Utils.getString(R.string.cancel), null);
        builder.setTitle(Utils.getString(R.string.remove_downloads_title));
        builder.setMessage(Utils.getString(R.string.remove_downloads_content));
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            EventBus.getDefault().post(new Refresh(99));
            Aria.download(this).removeAllTask(false);
            DatabaseUtil.deleteAllDownloads();
            CustomToast.showToast(this, "已删除所有下载记录", CustomToast.DEFAULT);
            alertDialog.dismiss();
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> alertDialog.dismiss());
    }

    public void setDanmu() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(VIDEO_PLAYER_DANMU.getDialogTitle());
        builder.setSingleChoiceItems(VIDEO_PLAYER_DANMU.getItems(), (Boolean) SharedPreferencesUtils.getParam(this, "open_danmu", true) ? 0 : 1, (dialog, which) -> {
            SharedPreferencesUtils.setParam(getApplicationContext(), "open_danmu", which == 0);
            danmuSelectTV.setText(VIDEO_PLAYER_DANMU.getItems()[which]);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
