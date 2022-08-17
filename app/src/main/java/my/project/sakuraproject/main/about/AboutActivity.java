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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.LogAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.LogBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
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
        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initViews();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
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
        sourceTitleView.setText(Utils.isImomoe() ? Utils.getString(R.string.imomoe_url) : Utils.getString(R.string.domain_url));
        cache.setText(Environment.getExternalStorageDirectory() + Utils.getString(R.string.cache_text));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.update_log));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_log, null);
        RecyclerView logs = view.findViewById(R.id.rv_list);
        logs.setLayoutManager(new LinearLayoutManager(this));
        LogAdapter logAdapter = new LogAdapter(createUpdateLogList());
        logs.setAdapter(logAdapter);
        builder.setPositiveButton(Utils.getString(R.string.page_positive), null);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
    }

    public List createUpdateLogList() {
        List logsList = new ArrayList();
        logsList.add(new LogBean("版本：2.3.8_1", "添加播放界面长按2倍速功能\n添加对获取弹幕API异常时的处理\n修复在收藏、历史记录中由于番剧地址发生改变导致访问详情界面闪退的问题\n修复番剧地址改变导致历史记录出现闪退的问题"));
        logsList.add(new LogBean("版本：2.3.8", "添加播放界面长按2倍速功能\n添加对获取弹幕API异常时的处理\n修复在收藏、历史记录中由于番剧地址发生改变导致访问详情界面闪退的问题"));
        logsList.add(new LogBean("版本：2.3.7", "修复malimali源部分番剧无法播放的问题，如：咒术回战\n初步添加对弹幕支持，使用@MaybeQHL提供的弹幕API\n**可在设置中进行弹幕开关，可能会出现崩溃的情况"));
        logsList.add(new LogBean("版本：2.3.6", "修复malimali源播放时闪退的问题\n**由于malimali源播放地址变更，从历史记录中进行播放出现闪退时需手动删除该历史记录"));
        logsList.add(new LogBean("版本：2.3.5", "修复malimali源部分图片显示失败的问题\n修复malimali源分类解析方法\n修复删除下载的视频后下载其他视频时出现TaskId重复导致下载异常闪退的问题，如已出现需删除所有下载记录（设置->清除所有下载记录）\n**malimali源站点搜索功能异常，需等待站点修复"));
        logsList.add(new LogBean("版本：2.3.4", "修复malimali源站点UI改版（又变回原来的UI了。。。）导致所有相关解析出错的问题\n修复在下载列表界面中出现下载成功但显示下载失败的问题"));
        logsList.add(new LogBean("版本：2.3.3", "malimali域名变更为https://www.malimali6.com\n修复malimali源站点UI改版导致所有相关解析出错的问题\nmalimali源新增当视频地址是链接到腾讯视频等视频网站资源时，在播放失败时选择网页嗅探时加入了几个免费的VIP视频解析接口（不保证长期有效），下载该类型的视频时同理，但不保证一定能嗅探成功\n**由于malimali源站点改版导致部分番剧的地址发生变化可能导致在我的收藏、历史记录等界面操作时出现崩溃的情况，如出现请手动删除相关信息"));
        logsList.add(new LogBean("版本：2.3.2", "修复在下载过程中应用异常退出后再进行继续下载时TS转换器被回收导致下载完成后无法合并的问题\n修复Aria下载框架使用不当存在内存泄露的问题\n*本次更新后建议先手动删除所有下载失败的剧集，否则可能出现数据错乱...\n**选择多集批量下载时有时会出现未创建下载任务的情况，如出现该问题请删除未执行下载剧集后自行重新下载"));
        logsList.add(new LogBean("版本：2.3.1", "尽可能修复M3U8下载时可能出现下载失败的情况（下载时不建议批量下载过多的视频）\n*目前发现MaliMali源有些视频因资源问题导致无法播放，可尝试下载，如果能下载成功可尝试使用你的外置播放器进行播放"));
        logsList.add(new LogBean("版本：2.3.0", "由于imomoe源访问变成dmh8.com（怕是凉了），番剧画质差且视频中夹带非法广告，现移除imomoe源解析替换为质量不错的malimali源，更新后将清空所有imomoe源的存储信息（下载除外）。\n修复伪装为图片格式的M3U8下载完成后合并的文件无法播放的问题。"));
        logsList.add(new LogBean("版本：2.2.0", "修改首页UI，移除侧滑栏，不再将时间表作为首页展示内容\n修复下载M3U8视频失败时无法删除任务的问题\n移除设置同时下载数量功能（功能存在问题），暂设置默认下载数量为1\n修复投屏功能目前发现的一些问题\n修复我的追番列表、历史记录、下载列表中的番剧图片地址更换而无法显示的问题，更新新的图片地址存入数据库\n修改历史记录、下载列表中列表数据UI呈现样式"));
        logsList.add(new LogBean("版本：2.1.0", "新增追番更新检测，有更新的番剧将排在列表最前面（仅支持Yhdm源）\n新增观看历史记录\n新增剧集下载功能\n新增保存播放进度"));
        logsList.add(new LogBean("版本：2.0.0", "修复Yhdm源一些剧集无法解析的问题\n修复Imomoe源的一个Bug\n内置播放器添加播放时可隐藏底部进度条的选项（在播放器中设置）\n内置播放器优化相关操作"));
        logsList.add(new LogBean("版本：1.9.9", "Yhdm域名变更为http://www.yhdm.so，Imomoe域名变更为http://www.imomoe.la\n添加1.25、1.75倍速播放\n修复其他已知问题"));
        logsList.add(new LogBean("版本：1.9.8_2", "修复Imomoe源一些番剧播放时崩溃的问题\n修复其他已知问题"));
        logsList.add(new LogBean("版本：1.9.8_1", "修复Yhdm源动漫专题无法正常加载的问题"));
        logsList.add(new LogBean("版本：1.9.8", "支持Android 11\n修复已知问题，部分UI变更\n新增对樱花动漫网（Imomoe）的支持，首页点击标题栏可切换（可能存在Bug）"));
        logsList.add(new LogBean("版本：1.9.7_1", "修复ExoPlayer不支持（http -> https | https -> http）重定向导致部分番剧无法正常播放的问题"));
        logsList.add(new LogBean("版本：1.9.7", "修复已知问题\n新增视频投屏功能"));
        logsList.add(new LogBean("版本：1.9.6_2", "修复图片无法正常显示的问题\n修复动漫分类界面浮动按钮在有导航栏的设备上被遮挡的问题\n内置播放器新增倍数播放（0.5X - 3X）"));
        logsList.add(new LogBean("版本：1.9.6_1", "域名变更为http://www.yhdm.io"));
        logsList.add(new LogBean("版本：1.9.6", "动漫分类界面改动\n内置播放器快进、后退参数可设置（5s，10s，15s，30s），播放器界面点击“设置”图标，在弹窗界面中配置"));
        logsList.add(new LogBean("版本：1.9.5", "修复部分设备（平板）无法正常播放的问题"));
        logsList.add(new LogBean("版本：1.9.4_redirection_fix1", "再次尝试修复由于网站重定向导致某些获数据取异常的Bug ┐(´д｀)┌"));
        logsList.add(new LogBean("版本：1.9.4", "尝试修复由于网站重定向导致获数据取异常的Bug"));
        logsList.add(new LogBean("版本：1.9.3", "修复番剧详情加载失败闪退Bug"));
        logsList.add(new LogBean("版本：1.9.2", "番剧详情界面布局修改"));
        logsList.add(new LogBean("版本：1.9.1", "修正动漫分类列表"));
        logsList.add(new LogBean("版本：1.9.0", "修复一些Bug\n优化番剧详情界面\n内置播放器新增屏幕锁定、快进、后退操作"));
        logsList.add(new LogBean("版本：1.8.9","修复解析时弹窗不关闭的问题"));
        logsList.add(new LogBean("版本：1.8.8","修复已知问题"));
        logsList.add(new LogBean("版本：1.8.7","部分界面UI改动\n修复番剧详情界面显示问题"));
        logsList.add(new LogBean("版本：1.8.6_b","修复内置播放器播放完毕后程序崩溃的问题"));
        logsList.add(new LogBean("版本：1.8.6_a","修复内置播放器使用Exo内核无限加载的问题"));
        logsList.add(new LogBean("版本：1.8.6","修复一些错误\n修复内置视频播放器存在的一些问题"));
        logsList.add(new LogBean("版本：1.8.5","修复新解析方案资源未释放导致视频声音外放的Bug"));
        logsList.add(new LogBean("版本：1.8.4","修复视频播放器白额头的Bug\n增加新的解析方案，尽量减少使用webView（Test）"));
        logsList.add(new LogBean("版本：1.8.3","修复一些Bug"));
        logsList.add(new LogBean("版本：1.8.2","默认禁用X5内核，X5内核更新后会导致应用闪退（Android 10)，你可以在自定义设置中打开，若发生闪退则关闭该选项"));
        logsList.add(new LogBean("版本：1.8.1","修复某些设备导航栏的显示问题"));
        logsList.add(new LogBean("版本：1.8","修复一些Bugs\n修正部分界面布局\n适配沉浸式导航栏《仅支持原生导航栏，第三方魔改UI无效》（Test）"));
        logsList.add(new LogBean("版本：1.7","修复一些Bugs\n修正部分界面布局\n新增亮色主题（Test）"));
        logsList.add(new LogBean("版本：1.6","修复更新SDK后导致崩溃的严重问题"));
        logsList.add(new LogBean("版本：1.5","升级SDK版本为29（Android 10）"));
        logsList.add(new LogBean("版本：1.4","修复搜索界面无法加载更多动漫的Bug"));
        logsList.add(new LogBean("版本：1.3","部分UI变更，优化体验\n修复存在的一些问题"));
        logsList.add(new LogBean("版本：1.2","修复番剧详情剧集列表的一个显示错误\n新增视频播放解析方法，通过目前使用情况，理论上能正常播放樱花动漫网站大部分视频啦（有一些无法播放的视频绝大多数是网站自身原因）"));
        logsList.add(new LogBean("版本：1.1","修复一个Bug\n修复一个显示错误\n修复部分番剧无法正常播放的问题（兼容性待测试）"));
        logsList.add(new LogBean("版本：1.0","第一个版本（业余时间花了两个下午时间编写，可能存在许多Bug~）"));
        return logsList;
    }


}
