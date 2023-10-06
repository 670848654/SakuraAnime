package my.project.sakuraproject.main.setting;

import android.content.Intent;
import android.os.Handler;
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
    TextView domain_default;
    @BindView(R.id.player_default)
    TextView player_default;
    /*@BindView(R.id.x5_state_title)
    TextView x5_state_title;
    @BindView(R.id.x5_state)
    TextView x5_state;*/
    @BindView(R.id.footer)
    LinearLayout footer;
    private String url;
    private String[] playerItems = Utils.getArray(R.array.player);
    private String[] x5Items = {"启用","禁用"};
    private boolean isImomoe;
    @BindView(R.id.check_favorite_update)
    TextView checkFavoriteUpdateView;
    private String[] checkFavoriteUpdateItems = {"启用","停用"};
    @BindView(R.id.download_number)
    TextView downloadNumber;
    private String[] downloadNumbers = Utils.getArray(R.array.download_numbers);
    private AlertDialog alertDialog;
    private String downloadUrl;
    private Call downCall;
    @BindView(R.id.show)
    CoordinatorLayout show;
    @BindView(R.id.danmu_select)
    TextView danmuSelectView;
    private String[] danmuItems = {"开", "关"};
    @BindView(R.id.kernel_default)
    TextView kernelDefaultView;
    private String[] playerKernelItems = {"ExoPlayer", "IjkPlayer"};
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
        switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
            case 0:
                player_default.setText(playerItems[0]);
                break;
            case 1:
                player_default.setText(playerItems[1]);
                break;
        }
        int playerKernel = (Integer) SharedPreferencesUtils.getParam(this, "player_kernel", 0);
        kernelDefaultView.setText(playerKernelItems[playerKernel]);
        /*if (Utils.getX5State())
            x5_state_title.append(Html.fromHtml("<font color=\"#259b24\">加载成功</font>"));
        else
            x5_state_title.append(Html.fromHtml("<font color=\"#e51c23\">加载失败</font>"));
        if (Utils.loadX5())
            x5_state.setText(x5Items[0]);
        else
            x5_state.setText(x5Items[1]);*/
        checkFavoriteUpdateView.setText((Boolean) SharedPreferencesUtils.getParam(this, "check_favorite_update", true) ? checkFavoriteUpdateItems[0] : checkFavoriteUpdateItems[1]);
        domain_default.setText(Sakura.DOMAIN);
        downloadNumber.setText(((Integer) SharedPreferencesUtils.getParam(this, "download_number", 0) + 1) + "");
        danmuSelectView.setText((Boolean) SharedPreferencesUtils.getParam(this, "open_danmu", true) ? danmuItems[0] : danmuItems[1]);
    }

    @OnClick({R.id.set_domain, R.id.set_player, R.id.set_player_kernel, R.id.set_favorite_update, R.id.set_download_number, R.id.remove_downloads, R.id.set_danmu})
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
        EditText editText = view.findViewById(R.id.domain);
        String defaultUrl = isImomoe ? (String) SharedPreferencesUtils.getParam(this, "imomoe_domain", Utils.getString(R.string.imomoe_url)) : (String) SharedPreferencesUtils.getParam(this, "domain", Utils.getString(R.string.domain_url));
        spinner.setSelection(defaultUrl.startsWith("https") ? 1 : 0, true);
        editText.setText(defaultUrl.replaceAll("http://", "").replaceAll("https://", ""));
        builder.setPositiveButton(Utils.getString(R.string.page_positive_edit), null);
        builder.setNegativeButton(Utils.getString(R.string.page_negative), null);
        builder.setNeutralButton(Utils.getString(R.string.page_def), null);
        builder.setCancelable(false);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = editText.getText().toString();
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
                    domain_default.setText(url);
                    alertDialog.dismiss();
                    EventBus.getDefault().post(new Refresh(0));
//                    application.showSuccessToastMsg(Utils.getString(R.string.set_domain_ok));
                    CustomToast.showToast(this, Utils.getString(R.string.set_domain_ok), CustomToast.SUCCESS);
                } else editText.setError(Utils.getString(R.string.set_domain_error2));
            } else editText.setError(Utils.getString(R.string.set_domain_error1));
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
//            setResult(0x20);
            if (isImomoe)
                SharedPreferencesUtils.setParam(SettingActivity.this, "imomoe_domain", Utils.getString(R.string.imomoe_url));
            else
                SharedPreferencesUtils.setParam(SettingActivity.this, "domain", Utils.getString(R.string.domain_url));
            Sakura.setApi();
            domain_default.setText(Sakura.DOMAIN);
            EventBus.getDefault().post(new Refresh(0));
            alertDialog.dismiss();
        });
    }

    public void setDefaultPlayer() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.select_player));
        builder.setSingleChoiceItems(playerItems, (Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0), (dialog, which) -> {
            player_default.setText(playerItems[which]);
            SharedPreferencesUtils.setParam(getApplicationContext(), "player", which);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setPlayerKernel() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.set_player_kernel));
        builder.setSingleChoiceItems(playerKernelItems, (Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player_kernel", 0), (dialog, which) -> {
            kernelDefaultView.setText(playerKernelItems[which]);
            SharedPreferencesUtils.setParam(getApplicationContext(), "player_kernel", which);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setCheckFavoriteUpdateState() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.page_title));
        builder.setSingleChoiceItems(checkFavoriteUpdateItems, (Boolean) SharedPreferencesUtils.getParam(this, "check_favorite_update", true) ? 0 : 1, (dialog, which) -> {
            switch (which) {
                case 0:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "check_favorite_update", true);
                    checkFavoriteUpdateView.setText(checkFavoriteUpdateItems[0]);
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "check_favorite_update", false);
                    checkFavoriteUpdateView.setText(checkFavoriteUpdateItems[1]);
                    break;
            }
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
            downloadNumber.setText(downloadNumbers[which]);
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
        builder.setTitle(Utils.getString(R.string.danmu_title));
        builder.setSingleChoiceItems(danmuItems, (Boolean) SharedPreferencesUtils.getParam(this, "open_danmu", true) ? 0 : 1, (dialog, which) -> {
            switch (which) {
                case 0:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "open_danmu", true);
                    danmuSelectView.setText(danmuItems[0]);
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "open_danmu", false);
                    danmuSelectView.setText(danmuItems[1]);
                    break;
            }
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void checkUpdate() {
        alertDialog = Utils.getProDialog(this, R.string.check_update_text);
        new Handler().postDelayed(() -> new HttpGet(Api.CHECK_UPDATE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Utils.cancelDialog(alertDialog);
                    application.showSnackbarMsgAction(show, Utils.getString(R.string.ck_network_error_start), Utils.getString(R.string.try_again), v -> checkUpdate());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String newVersion = obj.getString("tag_name");
                    if (newVersion.equals(Utils.getASVersionName()))
                        runOnUiThread(() -> {
                            Utils.cancelDialog(alertDialog);
                            CustomToast.showToast(SettingActivity.this, Utils.getString(R.string.no_new_version), CustomToast.SUCCESS);
                        });
                    else {
                        downloadUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        String body = obj.getString("body");
                        runOnUiThread(() -> {
                            Utils.cancelDialog(alertDialog);
                            Utils.findNewVersion(SettingActivity.this,
                                    newVersion,
                                    body,
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        Utils.putTextIntoClip(downloadUrl);
//                                        application.showSuccessToastMsg(Utils.getString(R.string.url_copied));
                                        CustomToast.showToast(SettingActivity.this, Utils.getString(R.string.url_copied), CustomToast.SUCCESS);
                                        Utils.viewInBrowser(SettingActivity.this, downloadUrl);
                                    },
                                    (dialog, which) -> dialog.dismiss()
                            );
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
//                    application.showErrorToastMsg(Utils.getString(R.string.ck_error_start));
                    CustomToast.showToast(SettingActivity.this, Utils.getString(R.string.ck_error_start), CustomToast.ERROR);
                    Utils.cancelDialog(alertDialog);
                }
            }
        }), 1000);
    }
}
