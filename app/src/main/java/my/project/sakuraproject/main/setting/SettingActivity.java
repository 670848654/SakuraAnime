package my.project.sakuraproject.main.setting;

import android.content.Intent;
import android.text.Html;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.r0adkll.slidr.Slidr;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.setting.user.ApiActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
import my.project.sakuraproject.util.Utils;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.domain_default)
    TextView domain_default;
    @BindView(R.id.player_default)
    TextView player_default;
    @BindView(R.id.api)
    TextView api;
    @BindView(R.id.x5_state_title)
    TextView x5_state_title;
    @BindView(R.id.x5_state)
    TextView x5_state;
    @BindView(R.id.footer)
    LinearLayout footer;
    private String url;
    private String[] playerItems = Utils.getArray(R.array.player);
    private String [] x5Items = {"启用","禁用"};

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
        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initViews();
        getUserCustomSet();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Utils.getString(R.string.setting_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initViews() {
        LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.getNavigationBarHeight(this));
        footer.findViewById(R.id.footer).setLayoutParams(Params);
    }

    public void getUserCustomSet() {
        api.setText(DatabaseUtil.queryAllApi().size() + "");
        switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
            case 0:
                player_default.setText(playerItems[0]);
                break;
            case 1:
                player_default.setText(playerItems[1]);
                break;
        }
        if (Utils.getX5State())
            x5_state_title.append(Html.fromHtml("<font color=\"#259b24\">加载成功</font>"));
        else
            x5_state_title.append(Html.fromHtml("<font color=\"#e51c23\">加载失败</font>"));
        if (Utils.loadX5())
            x5_state.setText(x5Items[0]);
        else
            x5_state.setText(x5Items[1]);
        domain_default.setText(Sakura.DOMAIN);
    }

    @OnClick({R.id.set_domain, R.id.set_player, R.id.set_api_source,R.id.set_x5})
    public void onClick(RelativeLayout layout) {
        switch (layout.getId()) {
            case R.id.set_domain:
                setDomain();
                break;
            case R.id.set_player:
                setDefaultPlayer();
                break;
            case R.id.set_api_source:
                startActivity(new Intent(this, ApiActivity.class));
                break;
            case R.id.set_x5:
                if (Utils.getX5State())
                    setX5State();
                else
                    application.showErrorToastMsg("X5内核未能加载成功，无法设置");
                break;
        }
    }

    public void setDomain() {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        String defaultUrl = (String) SharedPreferencesUtils.getParam(this, "domain", Utils.getString(R.string.domain_url));
        editText.setText(defaultUrl.replaceAll("http://", "").replaceAll("https://", ""));
        builder.setPositiveButton(Utils.getString(R.string.page_positive_edit), null);
        builder.setNegativeButton(Utils.getString(R.string.page_negative), null);
        builder.setNeutralButton(Utils.getString(R.string.page_def), null);
        builder.setTitle(Utils.getString(R.string.domain_title));
        builder.setCancelable(false);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = editText.getText().toString();
            if (!text.equals("")) {
                if (Patterns.WEB_URL.matcher(text).matches()) {
                    setResult(0x20);
                    if (text.endsWith("/")) text = text.substring(0, text.length() - 1);
                    url += text;
                    SharedPreferencesUtils.setParam(SettingActivity.this, "domain", url);
                    Sakura.DOMAIN = url;
                    Sakura.setApi();
                    domain_default.setText(url);
                    alertDialog.dismiss();
                    application.showSuccessToastMsg(Utils.getString(R.string.set_domain_ok));
                } else editText.setError(Utils.getString(R.string.set_domain_error2));
            } else editText.setError(Utils.getString(R.string.set_domain_error1));
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            setResult(0x20);
            Sakura.DOMAIN = Utils.getString(R.string.domain_url);
            Sakura.setApi();
            SharedPreferencesUtils.setParam(SettingActivity.this, "domain", Sakura.DOMAIN);
            domain_default.setText(Sakura.DOMAIN);
            alertDialog.dismiss();
        });
    }

    public void setDefaultPlayer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Utils.getString(R.string.select_player));
        builder.setSingleChoiceItems(playerItems, (Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0), (dialog, which) -> {
            switch (which) {
                case 0:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "player", 0);
                    player_default.setText(playerItems[0]);
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "player", 1);
                    player_default.setText(playerItems[1]);
                    break;
            }
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setX5State() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择内核状态");
        builder.setSingleChoiceItems(x5Items, Utils.loadX5() ? 0 : 1, (dialog, which) -> {
            switch (which){
                case 0:
                    SharedPreferencesUtils.setParam(getApplicationContext(),"loadX5",true);
                    x5_state.setText(x5Items[0]);
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(getApplicationContext(),"loadX5",false);
                    x5_state.setText(x5Items[1]);
                    break;
            }
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        api.setText(DatabaseUtil.queryAllApi().size() + "");
    }
}
