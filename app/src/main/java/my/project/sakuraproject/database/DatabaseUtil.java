package my.project.sakuraproject.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.arialyy.aria.core.Aria;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.DownloadBean;
import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.bean.HistoryBean;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.util.Utils;

public class DatabaseUtil {
    private static SQLiteDatabase db;
    private static String DB_PATH = Environment.getExternalStorageDirectory() + "/SakuraAnime/Database/sakura.db";
    private static String YHDM_DOMAIN = BaseModel.getDomain(false);
    private static String IMOMOE_DOMIAN = BaseModel.getDomain(true);
    /**
     * 创建tables
     */
    public static void CREATE_TABLES() {
        db = SQLiteDatabase.openOrCreateDatabase(Utils.hasFilePermission() ? DB_PATH : Utils.getPrivateDbPath(), null);
        db.execSQL("create table if not exists T_ANIME(F_INDEX integer primary key autoincrement, F_ID text, F_TITLE text, F_SOURCE integer)");
        db.execSQL("create table if not exists T_FAVORITE(F_INDEX integer primary key autoincrement, F_ID text, F_LINK_ID text, F_IMG_URL text, F_URL text, F_DESC text, F_LAST_PLAY_NUMBER text, F_LAST_UPDATE_NUMBER text, F_STATE integer)");
        db.execSQL("create table if not exists T_HISTORY(F_INDEX integer primary key autoincrement, F_ID text, F_LINK_ID text, F_DESC_URL text, F_IMG_URL text, F_VISIBLE integer, F_UPDATE_TIME text)");
        db.execSQL("create table if not exists T_HISTORY_DATA(F_INDEX integer primary key autoincrement, F_ID text, F_LINK_ID text, F_PLAY_SOURCE integer, F_PLAY_URL text, F_PLAY_NUMBER text, F_PROGRESS integer, F_DURATION integer, F_UPDATE_TIME text)");
        db.execSQL("create table if not exists T_DOWNLOAD(F_INDEX integer primary key autoincrement, F_ID text, F_LINK_ID text, F_IMG_URL text, F_DESC_URL, F_CREATE_TIME text, F_UPDATE_TIME text)");
        db.execSQL("create table if not exists T_DOWNLOAD_DATA(F_INDEX integer primary key autoincrement, F_ID text, F_LINK_ID text, F_PLAY_NUMBER text, F_COMPLETE integer, F_PATH text, F_FILE_SIZE integer, F_IMOMOE_SOURCE integer, F_TASK_ID integer, F_CREATE_TIME text, F_PROGRESS integer, F_DURATION integer)");
    }

    public static void deleteMaliMaliData() {
        // 2023年3月26日19:50:51 清空所有MALIMALI源数据
        db.execSQL("delete from T_FAVORITE where F_URL like '%/voddetail/%'", new String[]{});
        Cursor historyCursor = db.rawQuery("select * from T_HISTORY where F_DESC_URL like '%/voddetail/%'", new String[]{});
        if (historyCursor.getCount() > 0) {
            while (historyCursor.moveToNext()) {
                String historyId = historyCursor.getString(1);
                // 删除历史记录字表
                db.execSQL("delete from T_HISTORY_DATA where F_LINK_ID =?", new String[]{historyId});
                // 删除历史记录主表
                db.execSQL("delete from T_HISTORY where F_ID =?", new String[]{historyId});
            }
            historyCursor.close();
        }
    }

    public static void deleteImomoeData() {
        // 2022年5月29日20:39:00 清空imomoe的所有收藏、历史观看数据，不清除下载记录
        Cursor cursor = db.rawQuery("select * from T_FAVORITE where F_URL like '%/view/%' ", new String[]{});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String animeId = cursor.getString(2);
                // 删除收藏夹
                db.execSQL("delete from T_FAVORITE where F_LINK_ID =?", new String[]{animeId});
            }
            cursor.close();
        }
        Cursor historyCursor = db.rawQuery("select * from T_HISTORY where F_DESC_URL like '%/view/%'", new String[]{});
        if (historyCursor.getCount() > 0) {
            while (historyCursor.moveToNext()) {
                String historyId = historyCursor.getString(1);
                // 删除历史记录字表
                db.execSQL("delete from T_HISTORY_DATA where F_LINK_ID =?", new String[]{historyId});
                // 删除历史记录主表
                db.execSQL("delete from T_HISTORY where F_ID =?", new String[]{historyId});
            }
            historyCursor.close();
        }
    }

    /**
     * 旧数据转移
     */
    public static void dataTransfer() {
        // 2021年7月13日开始使用新表
        if (checkOldTable("f_anime") > 0) oldTableDataToNewTable();
    }

    /**
     * 检查表是否存在
     *
     * @param tableName
     * @return
     */
    public static int checkOldTable(@NonNull String tableName) {
        String Query = "SELECT * FROM sqlite_master WHERE type='table' AND name=?";
        Cursor cursor = db.rawQuery(Query, new String[]{tableName});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * 老数据移动至新表
     */
    public static void oldTableDataToNewTable() {
        db.beginTransaction();
        String animeQuery = "select * from f_anime";
        Cursor animeCursor = db.rawQuery(animeQuery, new String[]{});
        while (animeCursor.moveToNext()) {
            String F_ID = animeCursor.getString(1); // 番剧ID
            String F_TITLE = animeCursor.getString(2); // 番剧名称
            // 存入番剧表
            db.execSQL("insert into T_ANIME values(?,?,?,?)",
                    new Object[]{
                            null,
                            F_ID,
                            F_TITLE.replaceAll("imomoe", ""),
                            F_TITLE.contains("imomoe") ? 1 : 0}
            );
            // 存入历史记录表
            String F_HISTORY_ID = UUID.randomUUID().toString();
            db.execSQL("insert into T_HISTORY values(?,?,?,?,?,?,?)",
                    new Object[]{
                            null,
                            F_HISTORY_ID,
                            F_ID,
                            "",
                            "",
                            0,
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}
            );
            String indexQuery = "select * from f_index where f_pid = ?";
            Cursor indexCursor = db.rawQuery(indexQuery, new String[]{F_ID});
            while (indexCursor.moveToNext()) {
                // 存入播放过的剧集信息到历史记录子表
                db.execSQL("insert into T_HISTORY_DATA values(?,?,?,?,?,?,?,?,?)",
                        new Object[]{
                                null,
                                UUID.randomUUID().toString(),
                                F_HISTORY_ID,
                                0,
                                indexCursor.getString(2),
                                "",
                                0,
                                0,
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}
                );
            }
            indexCursor.close();
            String favoriteQuery = "select * from f_favorite where f_title = ?";
            Cursor favoriteCursor = db.rawQuery(favoriteQuery, new String[]{F_TITLE});
            int count = favoriteCursor.getCount();
            if (count > 0) {
                favoriteCursor.moveToNext();
                // 存入追番表
                db.execSQL("insert into T_FAVORITE values(?,?,?,?,?,?,?,?,?)",
                        new Object[]{
                                null,
                                UUID.randomUUID().toString(),
                                F_ID,
                                favoriteCursor.getString(4),
                                favoriteCursor.getString(2),
                                favoriteCursor.getString(3),
                                "",
                                "",
                                0}
                );
            }
            favoriteCursor.close();

        }
        animeCursor.close();
        // 全部执行完毕后 删除旧表
        db.execSQL("DROP TABLE IF EXISTS f_anime");
        db.execSQL("DROP TABLE IF EXISTS f_api");
        db.execSQL("DROP TABLE IF EXISTS f_index");
        db.execSQL("DROP TABLE IF EXISTS f_favorite");
        db.setTransactionSuccessful();
        db.endTransaction();
        Toast.makeText(Sakura.getInstance(), "数据迁移完毕", Toast.LENGTH_SHORT).show();
    }

    public static void openDB() { if (null != db) db = SQLiteDatabase.openOrCreateDatabase(Utils.hasFilePermission() ? DB_PATH : Utils.getPrivateDbPath(), null);}

    /**
     * 关闭数据库连接
     */
    public static void closeDB() {
        if (null != db) db.close();
    }

    /**
     * 新增点击过的番剧名称
     * @param title 番剧名称
     * @param source 来源 0 yhdm 1 imomoe
     */
    public static void addAnime(String title, int source) {
        if (!checkAnime(title, source)) {
            String animeId = UUID.randomUUID().toString();
            // 新增番剧数据
            db.execSQL("insert into T_ANIME values(?,?,?,?)",
                    new Object[]{null, animeId, title, String.valueOf(source)});
        }
    }

    /**
     * 检查番剧名称是否存在
     *
     * @param title 番剧名称
     * @param source 来源 0 yhdm 1 imomoe
     * @return
     */
    public static boolean checkAnime(String title, int source) {
        String Query = "select * from T_ANIME where F_TITLE =? AND F_SOURCE =?";
        Cursor cursor = db.rawQuery(Query, new String[]{title, String.valueOf(source)});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    /**
     * 获取番剧ID
     *
     * @param title 番剧名称
     * @param source 来源 0 yhdm 1 imomoe
     * @return
     */
    public static String getAnimeID(String title, int source) {
        String Query = "select * from T_ANIME where F_TITLE =? AND F_SOURCE =?";
        Cursor cursor = db.rawQuery(Query, new String[]{title, String.valueOf(source)});
        cursor.moveToNext();
        return cursor.getString(1);
    }

    /**
     * 播放剧集时执行保存播放状态
     * 1.通过番剧ID查询追表表，查看当前观看的番剧是否在追番表中，如果查到则更新追番表该番剧的最后播放地址字段，
     * 然后通过该播放地址比对追番表中最后一次更新的播放地址，如果两者相同，则更新追番表该番剧更新状态为0（未更新）
     * 2.通过番剧ID查询历史记录表获取历史记录ID
     * 3.通过番剧ID更新历史记录表的最后更新时间
     * 4.通过历史记录ID、播放页地址查询历史记录子表，如果记录不存在，则新增记录，存在则更新历史记录子表
     * @param animeId 番剧ID
     * @param playUrl 播放页地址
     * @param playSource 播放源（仅用于imomoe， yhdm默认存0）
     * @param playNumber 播放集数名称
     */
    public static void addIndex(String animeId,  String playUrl, int playSource, String playNumber) {
        playUrl = playUrl.contains(IMOMOE_DOMIAN) ? playUrl.substring(IMOMOE_DOMIAN.length()) : playUrl.substring(YHDM_DOMAIN.length());
        // 查询收藏夹是否存在该剧集
        Cursor favoriteCursor = db.rawQuery("select * from T_FAVORITE where F_LINK_ID =?", new String[]{animeId});
        if (favoriteCursor.getCount() > 0) {
            favoriteCursor.moveToNext();
            // 收藏夹存在
            db.execSQL("update T_FAVORITE set F_LAST_PLAY_NUMBER=? where F_LINK_ID=?", new String[]{playUrl,animeId});
            if (playUrl.equals(favoriteCursor.getString(7))) {
                // 如果当前剧集是最新的一集 则变更更新状态为未更新
                db.execSQL("update T_FAVORITE set F_STATE= 0 where F_LINK_ID=?", new Object[]{animeId});
            }
            /*
            Cursor animeCursor = db.rawQuery("select * from T_ANIME where F_ID =?", new String[]{fid});
            animeCursor.moveToNext();
            int source = animeCursor.getInt(3);
            // 收藏夹存在
            switch (source) {
                case 0:
                    // yhdm 对比剧集地址
                    db.execSQL("update T_FAVORITE set F_LAST_PLAY_NUMBER=? where F_LINK_ID=?", new String[]{url,fid});
                    if (url.equals(favoriteCursor.getString(7))) {
                        // 如果当前剧集是最新的一集 则变更更新状态为未更新
                        db.execSQL("update T_FAVORITE set F_STATE= 0 where F_LINK_ID=?", new Object[]{fid});
                    }
                    break;
                case 1:
                    // imomoe 对比剧集文字
                    db.execSQL("update T_FAVORITE set F_LAST_PLAY_NUMBER=? where F_LINK_ID=?", new String[]{playNumber,fid});
                    if (playNumber.equals(favoriteCursor.getString(7))) {
                        // 如果当前剧集是最新的一集 则变更更新状态为未更新
                        db.execSQL("update T_FAVORITE set F_STATE= 0 where F_LINK_ID=?", new Object[]{fid});
                    }
                    break;
            }
            animeCursor.close();*/
        }
        favoriteCursor.close();
        // 查询在历史记录中是否存在
        Cursor historyCursor = db.rawQuery("select * from T_HISTORY where F_LINK_ID =?", new String[]{animeId});
        historyCursor.moveToNext();
        // 存在则更新历史记录
        db.execSQL("update T_HISTORY set F_VISIBLE= 1, F_UPDATE_TIME=? where F_LINK_ID=?",
                new Object[]{
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                        animeId
        });
        String historyId = historyCursor.getString(1);
        historyCursor.close();
        // 查询历史记录子表是否存在
        Cursor historyDataCursor = db.rawQuery("select * from T_HISTORY_DATA where F_LINK_ID =? AND F_PLAY_URL=?", new String[]{historyId, playUrl});
        if (historyDataCursor.getCount() == 0) {
            // 不存在则新增子表数据
            db.execSQL("insert into T_HISTORY_DATA values(?,?,?,?,?,?,?,?,?)",
                    new Object[]{
                            null,
                            UUID.randomUUID().toString(),
                            historyId,
                            playSource,
                            playUrl,
                            playNumber,
                            0,
                            0,
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                    });
        } else {
            db.execSQL("update T_HISTORY_DATA set F_PLAY_SOURCE =?, F_PLAY_NUMBER=?, F_UPDATE_TIME=? where F_LINK_ID=? AND F_PLAY_URL= ?",
                    new Object[]{
                            playSource,
                            playNumber,
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                            historyId,
                            playUrl
                    });
        }
    }

    /**
     * 通过番剧ID查询所有已播放过的剧集信息
     *
     * @param animeId 番剧ID
     * @return
     */
    public static String queryAllIndex(String animeId, boolean isHistory, int playSource) {
        Cursor cursor = db.rawQuery("select F_ID, F_DESC_URL from T_HISTORY WHERE F_LINK_ID = ? AND F_DESC_URL LIKE '%voddetail%'", new String[]{animeId});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String descNumber = cursor.getString(1).replaceAll("/voddetail/|.html", "");
                // 删除错误的数据
                db.execSQL("DELETE FROM T_HISTORY_DATA WHERE F_LINK_ID = ? AND F_PLAY_URL NOT LIKE '%"+descNumber+"%'", new String[] {
                        id
                });
            }
        }
        cursor.close();
        StringBuffer buffer = new StringBuffer();
        String Query = "";
        Cursor c;
        if (isHistory) {
            Query = "select t2.F_PLAY_URL from T_HISTORY t1 INNER JOIN T_HISTORY_DATA t2 ON t1.F_ID = t2.F_LINK_ID AND t2.F_PLAY_SOURCE=? where t1.F_LINK_ID =?";
            c = db.rawQuery(Query, new String[]{String.valueOf(playSource), animeId});
        } else {
            Query = "select t2.F_PLAY_URL from T_HISTORY t1 INNER JOIN T_HISTORY_DATA t2 ON t1.F_ID = t2.F_LINK_ID where t1.F_LINK_ID =?";
            c = db.rawQuery(Query, new String[]{animeId});
        }
        while (c.moveToNext()) {
            buffer.append(c.getString(0));
        }
        c.close();
        return buffer.toString();
    }

    /**
     * 分页查询用户收藏的番剧
     * @param offset 从第几条记录开始查询
     * @param limit 一次查询多少条记录（默认100）
     * @param updateOrder 是否开启追番更新
     * @return
     */
    public static List<AnimeListBean> queryFavoriteByLimit(int offset, int limit, boolean updateOrder) {
        List<AnimeListBean> list = new ArrayList<>();
        String parameter = "%s,%s";
        Cursor c = db.query("T_FAVORITE", null, null, null,null,null,updateOrder ? "F_STATE DESC, F_INDEX DESC" : "F_INDEX DESC",
                String.format(parameter, offset, limit));
        while (c.moveToNext()) {
            String animeID = c.getString(2);
            String animeQuery = "select * from T_ANIME where F_ID =?";
            Cursor animeCursor = db.rawQuery(animeQuery, new String[]{animeID});
            animeCursor.moveToNext();
            AnimeListBean bean = new AnimeListBean();
            bean.setAnimeId(animeCursor.getString(1));
            bean.setTitle(animeCursor.getString(2));
            bean.setImg(c.getString(3));
            bean.setUrl(c.getString(4));
            bean.setDesc(c.getString(5));
            bean.setState(updateOrder ? c.getInt(8) : 0);
            bean.setSource(animeCursor.getInt(3));
            list.add(bean);
            animeCursor.close();
        }
        c.close();
        return list;
    }

    /**
     *  查询追番表总数
     * @return
     */
    public static int queryFavoriteCount() {
        int count;
        String QueryCount = "select * from T_FAVORITE";
        Cursor cursor = db.rawQuery(QueryCount, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     *  查询追番表更新总数
     * @return
     */
    public static int queryFavoriteUpdateCount() {
        int count;
        Cursor cursor = db.rawQuery("select * from T_FAVORITE where F_STATE = 1", null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * 追番或删除追番
     *
     * @param bean
     * @return true 追番 false 弃番
     */
    public static boolean favorite(AnimeListBean bean, String animeId) {
        if (checkFavorite(animeId)) {
            deleteFavorite(animeId);
            return false;
        } else {
            addFavorite(bean, animeId);
            return true;
        }
    }

    /**
     * 添加到追番表
     * @param bean 番剧详情实体
     * @param animeId 番剧ID
     */
    private static void addFavorite(AnimeListBean bean, String animeId) {
        String url =  bean.getUrl().contains(IMOMOE_DOMIAN) ?  bean.getUrl().substring(IMOMOE_DOMIAN.length()) :  bean.getUrl().substring(YHDM_DOMAIN.length());
        db.execSQL("insert into T_FAVORITE values(?,?,?,?,?,?,?,?,?)",
                new Object[]{
                        null,
                        UUID.randomUUID().toString(),
                        animeId,
                        bean.getImg(),
                        url,
                        bean.getDesc(),
                        "",
                        "",
                        0
                });
    }

    /**
     * 更新追番信息
     * @param bean 番剧详情实体
     * @param animeId 番剧ID
     */
    public static void updateFavorite(AnimeListBean bean, String animeId) {
        String Query = "select * from T_FAVORITE where F_LINK_ID =?";
        Cursor cursor = db.rawQuery(Query, new String[] { animeId });
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            String id = cursor.getString(cursor.getColumnIndex("F_ID"));
            String url =  bean.getUrl().contains(IMOMOE_DOMIAN) ?  bean.getUrl().substring(IMOMOE_DOMIAN.length()) :  bean.getUrl().substring(YHDM_DOMAIN.length());
            db.execSQL("update T_FAVORITE set F_IMG_URL=?, F_URL=?, F_DESC=? where F_ID=?",
                    new Object[]{
                            bean.getImg(),
                            url,
                            bean.getDesc(),
                            id});
        }
        cursor.close();
    }

    /**
     * 弃番
     *
     * @param animeId 番剧ID
     */
    public static void deleteFavorite(String animeId) {
        db.execSQL("delete from T_FAVORITE where F_LINK_ID=?", new String[]{animeId});
    }

    /**
     * 检查番剧是否主福安
     *
     * @param animeId 番剧ID
     * @return
     */
    public static boolean checkFavorite(String animeId) {
        String Query = "select * from T_FAVORITE where F_LINK_ID =?";
        Cursor cursor = db.rawQuery(Query, new String[]{animeId});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    /**
     * 更新追番表
     * @param animeUpdateInfoBeans 番剧更新实体
     */
    public static void updateFavorite(List<AnimeUpdateInfoBean> animeUpdateInfoBeans) {
        for (AnimeUpdateInfoBean animeUpdateInfoBean : animeUpdateInfoBeans) {
            Cursor animeCursor = db.rawQuery("select * from T_ANIME where F_TITLE=? AND F_SOURCE=?",
                    new String[]{animeUpdateInfoBean.getTitle(), String.valueOf(animeUpdateInfoBean.getSource())});
            if (animeCursor.getCount() >0) {
                animeCursor.moveToNext();
                String animeId = animeCursor.getString(1);
                Cursor favoriteCursor = db.rawQuery("select * from T_FAVORITE where F_LINK_ID=?",
                        new String[]{animeId});
                Cursor historyCursor = db.rawQuery("select * from T_HISTORY where F_LINK_ID =?",
                        new String[]{animeId});
                if (historyCursor.getCount() > 0) {
                    historyCursor.moveToNext();
                    String historyId = historyCursor.getString(1);
                    Cursor historyDataCursor = db.rawQuery("select * from T_HISTORY_DATA where F_LINK_ID =? AND  F_PLAY_URL=?",
                            new String[]{historyId, animeUpdateInfoBean.getPlayNumber()});
                    if (historyDataCursor.getCount() > 0)
                        db.execSQL("update T_FAVORITE set F_LAST_PLAY_NUMBER=? where F_LINK_ID=?",
                                new String[]{animeUpdateInfoBean.getPlayNumber(), animeId});
                    historyDataCursor.close();
                }
                historyCursor.close();
                if (favoriteCursor.getCount() > 0) {
                    favoriteCursor.moveToNext();
                    String favoriteId = favoriteCursor.getString(1);
                    String lastPlayUrl = favoriteCursor.getString(6);
                    String lastUpdateUrl = favoriteCursor.getString(7);
                    if (lastPlayUrl.equals("") || lastUpdateUrl.equals("") || !lastUpdateUrl.equals(animeUpdateInfoBean.getPlayNumber())) // 如果没看过 或 最后观看的剧集不是最新的剧集 则提示有更新
                        db.execSQL("update T_FAVORITE set F_LAST_UPDATE_NUMBER=?, F_STATE = 1 where F_ID=?",
                                new String[]{animeUpdateInfoBean.getPlayNumber(),favoriteId});
                    else if (lastPlayUrl.equals(animeUpdateInfoBean.getPlayNumber())) // 如果看过最后更新的剧集则 不显示有更新
                        db.execSQL("update T_FAVORITE set F_LAST_UPDATE_NUMBER=?, F_STATE= 0 where F_ID=?",
                                new Object[]{animeUpdateInfoBean.getPlayNumber(), favoriteId});
                }
                favoriteCursor.close();
            }
            animeCursor.close();
        }
    }

    /**
     * 新增或更新历史记录
     * @param animeId 番剧ID
     * @param descUrl 详情地址
     * @param imgUrl 图片地址
     */
    public static void addOrUpdateHistory(String animeId, String descUrl, String imgUrl) {
        descUrl = descUrl.contains(IMOMOE_DOMIAN) ? descUrl.substring(IMOMOE_DOMIAN.length()) : descUrl.substring(YHDM_DOMAIN.length());
        Cursor historyCursor = db.rawQuery("select * from T_HISTORY where F_LINK_ID=?",
                new String[]{animeId});
        if (historyCursor.getCount() > 0) {
            // 存在则更新
            db.execSQL("update T_HISTORY set F_IMG_URL =?, F_DESC_URL=? where F_LINK_ID=?", new Object[] {
                    imgUrl,
                    descUrl,
                    animeId
            });
            historyCursor.close();
        } else {
            // 不存在则新增
            db.execSQL("insert into T_HISTORY values(?,?,?,?,?,?,?)", new Object[] {
                    null,
                    UUID.randomUUID().toString(),
                    animeId,
                    descUrl,
                    imgUrl,
                    0,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
            });
        }
    }

    /**
     * 更新历史记录
     * @param animeId 番剧ID
     * @param playUrl 播放地址
     * @param position 播放进度
     * @param duration 视频总长度
     */
    public static void updateHistory(String animeId, String playUrl, long position, long duration) {
        Cursor historyCursor = db.rawQuery("select * from T_HISTORY where F_LINK_ID=?", new String[]{animeId});
        historyCursor.moveToNext();
        String historyId = historyCursor.getString(1);
        db.execSQL("update T_HISTORY set F_UPDATE_TIME=? where F_ID=?",
                new Object[]{
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                        historyId
                });
        db.execSQL("update T_HISTORY_DATA set F_PROGRESS=?, F_DURATION=?, F_UPDATE_TIME=? where F_LINK_ID=? AND F_PLAY_URL=?",
                new Object[]{
                        position,
                        duration,
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                        historyId,
                        playUrl
                });
        historyCursor.close();
    }

    /**
     * 获取剧集播放进度
     * @param animeId 番剧ID
     * @param playUrl 播放地址
     * @return
     */
    public static long getPlayPosition(String animeId, String playUrl) {
        long position = 0;
        Cursor historyCursor = db.rawQuery("select * from T_HISTORY where F_LINK_ID=?", new String[]{animeId});
        historyCursor.moveToNext();
        String historyId = historyCursor.getString(1);
        Cursor historyDataCursor = db.rawQuery("select * from T_HISTORY_DATA where F_LINK_ID =? AND F_PLAY_URL=?", new String[]{
                historyId,
                playUrl
        });
        if (historyDataCursor.getCount() > 0) {
            historyDataCursor.moveToNext();
            position = historyDataCursor.getLong(6);
        }
        historyCursor.close();
        historyDataCursor.close();
        return position;
    }

    public static int queryHistoryCount() {
        Cursor cursor = db.rawQuery("SELECT * FROM T_HISTORY WHERE F_VISIBLE = 1" , null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * 获取历史记录
     * @param limit 一次查询多少条记录（默认100）
     * @param offset 从第几条记录开始查询
     * @return
     */
    public static List<HistoryBean> queryAllHistory(int limit, int offset) {
        List<HistoryBean> historyBeans = new ArrayList<>();
        // 隐藏错误的历史记录
        Cursor historyCursor = db.rawQuery("select t1.F_ID, COUNT(t2.F_ID) from T_HISTORY t1 \n" +
                "left join T_HISTORY_DATA t2 on t1.F_ID = t2.F_LINK_ID\n" +
                "where t1.F_VISIBLE = 1 \n" +
                "GROUP BY t1.F_ID", null);
        while (historyCursor.moveToNext()) {
            String historyId = historyCursor.getString(0);
            int count = historyCursor.getInt(1);
            if (count == 0) {
                db.execSQL("update T_HISTORY set F_VISIBLE = 0 where F_ID = ? ", new String[]{historyId}); // 当字表数据不存在时隐藏该历史记录
            }
        }
        historyCursor = db.rawQuery("select t2.F_ID F_ANIME_ID, t1.F_ID, t2.F_TITLE, t1.F_DESC_URL, t2.F_SOURCE, t1.F_IMG_URL, t1.F_UPDATE_TIME " +
                "from T_HISTORY t1 " +
                "left join T_ANIME t2 on t1.F_LINK_ID = t2.F_ID " +
                "where t1.F_VISIBLE = 1 " +
                "order by t1.F_UPDATE_TIME DESC limit ? offset ?", new String[]{String.valueOf(limit), String.valueOf(offset)});
        while (historyCursor.moveToNext()) {
            String historyId = historyCursor.getString(1);
            HistoryBean historyBean = new HistoryBean();
            historyBean.setAnimeId(historyCursor.getString(0));
            historyBean.setHistoryId(historyId);
            historyBean.setTitle(historyCursor.getString(2));
            historyBean.setDescUrl(historyCursor.getString(3));
            historyBean.setSource(historyCursor.getInt(4));
            historyBean.setImgUrl(historyCursor.getString(5));
            try {
                historyBean.setUpdateTime(Utils.isYesterday(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(historyCursor.getString(6))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Cursor historyDataCursor = db.rawQuery("select F_PLAY_SOURCE, F_PLAY_NUMBER, F_PLAY_URL, F_PROGRESS, F_DURATION \n" +
                    "from T_HISTORY_DATA where F_LINK_ID = ? order by F_UPDATE_TIME DESC LIMIT 1", new String[]{
                    historyId
            });
            historyDataCursor.moveToNext();
            historyBean.setPlaySource(historyDataCursor.getInt(0));
            historyBean.setDramaNumber(historyDataCursor.getString(1));
            historyBean.setDramaUrl(historyDataCursor.getString(2));
            historyBean.setProgress(historyDataCursor.getLong(3));
            historyBean.setDuration(historyDataCursor.getLong(4));
            historyBeans.add(historyBean);
            historyDataCursor.close();
        }
        historyCursor.close();
        return historyBeans;
    }

    /**
     * 隐藏历史记录
     * @param historyId 历史记录ID
     * @param isAll 是否全部隐藏
     */
    public static void deleteHistory(String historyId, boolean isAll) {
        if (isAll)
            db.execSQL("update T_HISTORY set F_VISIBLE = 0"); // 隐藏全部
        else
            db.execSQL("update T_HISTORY set F_VISIBLE = 0 where F_ID = ?", new String[]{historyId});
    }

    /**
     * 查询下载表是否存在记录
     * @param animeId
     * @return
     */
    public static boolean checkDownload(String animeId) {
        String Query = "select * from T_DOWNLOAD where F_LINK_ID =?";
        Cursor cursor = db.rawQuery(Query, new String[]{animeId});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    /**
     * 查询下载（番剧）列表总数
     * @return
     */
    public static int queryDownloadCount() {
        Cursor cursor = db.rawQuery("SELECT * FROM T_DOWNLOAD" , null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(1);
            Cursor dataCursor = db.rawQuery("SELECT * FROM T_DOWNLOAD_DATA where F_LINK_ID = ?" , new String[]{id});
            if (dataCursor.getCount() == 0)
                db.execSQL("delete from T_DOWNLOAD where F_ID = ?", new String[]{id}); // 删除异常的数据
            dataCursor.close();
        }
        cursor = db.rawQuery("SELECT * FROM T_DOWNLOAD" , null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * 查询下载列表总数
     * @return
     */
    public static int queryAllDownloadCount() {
        Cursor cursor = db.rawQuery("SELECT t2.* FROM T_DOWNLOAD t1 LEFT JOIN T_DOWNLOAD_DATA t2 ON t1.F_ID = t2.F_LINK_ID" , null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * 删除下载数据
     * @param id
     */
    public static void deleteDownload(String id) {
        db.execSQL("delete from T_DOWNLOAD where F_ID=?", new String[]{id});
    }

    /**
     * 新增下载信息
     * @param animeTitle
     * @param source
     * @param imgUrl
     * @param descUrl
     */
    public static void insertDownload(String animeTitle, int source, String imgUrl, String descUrl) {
        Log.e("info", animeTitle+">"+source);
        String animeId = getAnimeID(animeTitle, source);
        if (checkDownload(animeId))
            db.execSQL("update T_DOWNLOAD set F_UPDATE_TIME =? where F_LINK_ID=?", new Object[]{
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    animeId
            });
        else
            db.execSQL("insert into T_DOWNLOAD values (?,?,?,?,?,?,?)",
                    new Object[]{
                            null,
                            UUID.randomUUID().toString(),
                            animeId,
                            imgUrl,
                            descUrl,
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                    });
    }

    public static void insertDownloadData(String animeTitle, int source, String playNumber, int imomoeSource, long taskId) {
        String animeId = getAnimeID(animeTitle, source);
        Cursor cursor = db.rawQuery("select * from T_DOWNLOAD where F_LINK_ID=?", new String[]{animeId});
        cursor.moveToNext();
        String downloadId = cursor.getString(1);
        Cursor downloadDataCursor = db.rawQuery("select * from T_DOWNLOAD_DATA where F_LINK_ID = ? and F_PLAY_NUMBER =?",
                new String[]{downloadId, playNumber});
        if (downloadDataCursor.getCount() == 0) {
            db.execSQL("insert into T_DOWNLOAD_DATA values(?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{
                    null,
                    UUID.randomUUID().toString(),
                    downloadId,
                    playNumber,
                    0,
                    "",
                    "",
                    imomoeSource,
                    taskId,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    0,
                    0
            });
        } else {
            db.execSQL("update T_DOWNLOAD_DATA set F_COMPLETE = 0, F_PATH='', F_FILE_SIZE='' where F_LINK_ID=? AND F_TASK_ID=?",new Object[]{
                    downloadId,
                    taskId,
            });
        }
        cursor.close();
    }

    /**
     * 更新下载信息
     * @param animeTitle
     * @param source
     * @param path
     * @param fileSize
     */
    public static void updateDownloadSuccess(String animeTitle, int source, String path, long taskId, long fileSize) {
        String animeId = getAnimeID(animeTitle, source);
        Cursor cursor = db.rawQuery("select * from T_DOWNLOAD where F_LINK_ID=?", new String[]{animeId});
        cursor.moveToNext();
        String downloadId = cursor.getString(1);
        if (path.contains(".m3u8")) {
            path = path.replaceAll("m3u8", "mp4");
            File file = new File(path);
            db.execSQL("update T_DOWNLOAD_DATA set F_COMPLETE = 1, F_PATH=?, F_FILE_SIZE=?, F_TASK_ID = -99  where F_LINK_ID=? AND F_TASK_ID=?",
                    new Object[]{
                            path,
                            file.length(),
                            downloadId,
                            taskId
                    });
        } else
            db.execSQL("update T_DOWNLOAD_DATA set F_COMPLETE = 1, F_PATH=?, F_FILE_SIZE=?, F_TASK_ID = -99 where F_LINK_ID=? AND F_TASK_ID=?",
                    new Object[]{
                            path,
                            fileSize,
                            downloadId,
                            taskId
                    });
        cursor.close();
    }

    /**
     * 更新下载失败信息
     * @param animeTitle
     * @param source
     * @param path
     * @param fileSize
     */
    public static void updateDownloadError(String animeTitle, int source, String path, long taskId, long fileSize) {
        String animeId = getAnimeID(animeTitle, source);
        Cursor cursor = db.rawQuery("select * from T_DOWNLOAD where F_LINK_ID=?", new String[]{animeId});
        cursor.moveToNext();
        String downloadId = cursor.getString(1);
        db.execSQL("update T_DOWNLOAD_DATA set F_COMPLETE = 2, F_PATH=?, F_FILE_SIZE=? where F_LINK_ID=? AND F_TASK_ID=?",
                new Object[]{
                        path,
                        fileSize,
                        downloadId,
                        taskId
                });
        cursor.close();
    }


    /**
     * 更新下载信息
     */
    public static void updateDownloadState(long taskId) {
        db.execSQL("update T_DOWNLOAD_DATA set F_COMPLETE = 0 WHERE F_TASK_ID=?",
                new Object[]{
                        taskId
                });
    }

    /**
     * 查询所有下载任务
     * @param limit
     * @param offset
     * @return
     */
    public static List<DownloadBean> queryAllDownloads(int limit, int offset) {
        List<DownloadBean> downloadBeans = new ArrayList<>();
        if (queryDownloadCount() > 0) {
            Cursor downloadCursor = db.rawQuery("select * from T_DOWNLOAD order by F_UPDATE_TIME DESC limit ? offset ?",
                    new String[]{String.valueOf(limit), String.valueOf(offset)});
            while (downloadCursor.moveToNext()) {
                DownloadBean downloadBean = new DownloadBean();
                String downloadId = downloadCursor.getString(1);
                downloadBean.setDownloadId(downloadId);
                String animeId = downloadCursor.getString(2);
                Cursor animeCursor = db.rawQuery("select * from T_ANIME where F_ID = ?", new String[]{animeId});
                animeCursor.moveToNext();
                downloadBean.setAnimeTitle(animeCursor.getString(2));
                downloadBean.setSource(animeCursor.getInt(3));
                downloadBean.setImgUrl(downloadCursor.getString(3));
                downloadBean.setDescUrl(downloadCursor.getString(4));
                downloadBean.setDownloadDataSize(queryDownloadDataCount(downloadId));
                Cursor downloadDataCursor = db.rawQuery("select sum(F_FILE_SIZE) from T_DOWNLOAD_DATA where F_LINK_ID = ?", new String[]{downloadId});
                downloadDataCursor.moveToNext();
                downloadBean.setFilesSize(Utils.getNetFileSizeDescription(downloadDataCursor.getLong(0)));
                downloadDataCursor = db.rawQuery("select count(F_ID) from T_DOWNLOAD_DATA where F_LINK_ID = ? AND F_COMPLETE != 1", new String[]{downloadId});
                downloadDataCursor.moveToNext();
                downloadBean.setNoCompleteSize(downloadDataCursor.getInt(0));
                downloadBeans.add(downloadBean);
                animeCursor.close();
                downloadDataCursor.close();
            }
            downloadCursor.close();
        }
        return downloadBeans;
    }

    /**
     * 获取已下载的文件总大小
     * @param downloadId
     * @return
     */
    public static String queryDownloadFilesSize(String downloadId) {
        Cursor downloadDataCursor = db.rawQuery("select sum(F_FILE_SIZE) from T_DOWNLOAD_DATA where F_LINK_ID = ?", new String[]{downloadId});
        downloadDataCursor.moveToNext();
        String size = Utils.getNetFileSizeDescription(downloadDataCursor.getLong(0));
        downloadDataCursor.close();;
        return size;
    }

    /**
     * 获取当前任务下未完成的数量
     * @param downloadId
     * @return
     */
    public static int queryDownloadNotCompleteCount(String downloadId) {
        Cursor downloadDataCursor = db.rawQuery("select count(F_ID) from T_DOWNLOAD_DATA where F_LINK_ID = ? AND F_COMPLETE != 1", new String[]{downloadId});
        downloadDataCursor.moveToNext();
        int size = downloadDataCursor.getInt(0);
        downloadDataCursor.close();;
        return size;
    }

    /**
     * 获取下载的番剧下所有剧集总数
     * @param downloadId
     * @return
     */
    public static int queryDownloadDataCount(String downloadId) {
        Cursor cursor = db.rawQuery("SELECT * FROM T_DOWNLOAD_DATA where F_LINK_ID=?" , new String[]{downloadId});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * 查询当前下载任务状态
     * @param animeId
     * @param playNumber
     * @param nowSource
     * @return
     */
    public static int queryDownloadDataIsDownloadError(String animeId, String playNumber, int nowSource) {
        int complete = -1;
        Cursor cursor = db.rawQuery("select t2.F_COMPLETE from T_DOWNLOAD t1 LEFT JOIN T_DOWNLOAD_DATA t2 ON t1.F_ID = t2.F_LINK_ID where t1.F_LINK_ID = ? AND t2.F_PLAY_NUMBER=? AND t2.F_IMOMOE_SOURCE=?",
                new String[]{animeId, playNumber, String.valueOf(nowSource)});
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            return cursor.getInt(0);
        }
        cursor.close();
        return complete;
    }

    /**
     * 删除下载数据
     * @param id
     */
    public static void deleteDownloadData(String id) {
        db.execSQL("delete from T_DOWNLOAD_DATA where F_ID=?", new String[]{id});
    }

    /**
     * 查询下载的番剧下所有剧集
     * @param downloadId
     * @param limit
     * @param offset
     * @return
     */
    public static List<DownloadDataBean> queryDownloadDataByDownloadId(String downloadId, int limit, int offset) {
        List<DownloadDataBean> downloadDataBeans = new ArrayList<>();
        Cursor downloadDataCursor = db.rawQuery("select t1.F_ID, t3.F_TITLE, t1.F_PLAY_NUMBER, t1.F_COMPLETE, t1.F_PATH, t1.F_FILE_SIZE, t2.F_IMG_URL, t3.F_SOURCE, t1.F_IMOMOE_SOURCE, t1.F_TASK_ID, t1.F_PROGRESS, t1.F_DURATION from T_DOWNLOAD_DATA t1 left join T_DOWNLOAD t2 ON t1.F_LINK_ID = t2.F_ID left join T_ANIME t3 ON t2.F_LINK_ID = t3.F_ID WHERE t1.F_LINK_ID =? ORDER by t1.F_IMOMOE_SOURCE ASC, t1.F_PLAY_NUMBER ASC  limit ? offset ?",
                new String[]{downloadId, String.valueOf(limit), String.valueOf(offset)});
        while (downloadDataCursor.moveToNext()) {
            DownloadDataBean downloadDataBean = new DownloadDataBean();
            downloadDataBean.setId(downloadDataCursor.getString(0));
            downloadDataBean.setAnimeTitle(downloadDataCursor.getString(1));
            downloadDataBean.setPlayNumber(downloadDataCursor.getString(2));
            downloadDataBean.setComplete(downloadDataCursor.getInt(3));
            downloadDataBean.setPath(downloadDataCursor.getString(4));
            downloadDataBean.setFileSize(downloadDataCursor.getLong(5));
            downloadDataBean.setAnimeImg(downloadDataCursor.getString(6));
            downloadDataBean.setSource(downloadDataCursor.getInt(7));
            downloadDataBean.setImomoeSource(downloadDataCursor.getInt(8));
            downloadDataBean.setTaskId(downloadDataCursor.getLong(9));
            downloadDataBean.setProgress(downloadDataCursor.getLong(10));
            downloadDataBean.setDuration(downloadDataCursor.getLong(11));
            downloadDataBeans.add(downloadDataBean);
        }
        downloadDataCursor.close();
        return downloadDataBeans;
    }

    /**
     * 根据下载ID查询 番剧名称 番剧来源
     * @param taskId
     * @return
     */
    public static List<Object> queryDownloadAnimeInfo(long taskId) {
        Cursor cursor = db.rawQuery("select t3.F_TITLE, t3.F_SOURCE from T_DOWNLOAD_DATA t1 " +
                        "LEFT JOIN T_DOWNLOAD t2 ON t1.F_LINK_ID = t2.F_ID " +
                        "LEFT JOIN T_ANIME t3 ON t2.F_LINK_ID = t3.F_ID where t1.F_TASK_ID=?",
                new String[]{String.valueOf(taskId)});
        List<Object> objects = new ArrayList<>();
        cursor.moveToNext();
        if (cursor.getCount() > 0) {
            objects.add(cursor.getString(0));
            objects.add(cursor.getInt(1));
//            Log.e("????" , "taskId：" +taskId + ",番剧名称：" +cursor.getString(0) + cursor.getInt(1));
        }
        cursor.close();
        return objects;
    }

    /**
     * 根据番剧ID 番剧来源 播放剧集 查看本地是否存在
     * @param animeId
     * @param source
     * @param playNumber
     * @return
     */
    public static String getLocalVideoByAnimeInfo(String animeId, int source, String playNumber) {
        Cursor cursor = db.rawQuery("select t3.F_PATH from T_ANIME t1 LEFT JOIN T_DOWNLOAD t2 ON t1.F_ID = t2.F_LINK_ID LEFT JOIN T_DOWNLOAD_DATA t3 ON t2.F_ID = t3.F_LINK_ID where t1.F_ID=? and t1.F_SOURCE=? and t3.F_PLAY_NUMBER=?",
                new String[]{animeId, playNumber});
        cursor.moveToNext();
        String path = cursor.getString(0);
        if (path == null) return null;
        else {
            File file = new File(path);
            if (file.exists())
                return Uri.fromFile(file).toString();
            else return null;
        }
    }

    /**
     * 更新当前播放进度
     * @param downloadDataId
     */
    public static void updateDownloadDataProgressById(long position, long duration, String downloadDataId) {
        db.execSQL("update T_DOWNLOAD_DATA set F_PROGRESS=?, F_DURATION=? where F_ID=?",
                new Object[]{position, duration, downloadDataId});
    }

    /**
     * 获取当前播放进度
     * @param downloadDataId
     * @return
     */
    public static long queryDownloadDataProgressById(String downloadDataId) {
        Cursor cursor = db.rawQuery("select F_PROGRESS from T_DOWNLOAD_DATA where F_ID=?",
                new String[]{downloadDataId});
        cursor.moveToNext();
        long progress = cursor.getLong(0);
        cursor.close();
        return progress;
    }

    /**
     * 更新图片信息
     * @param id
     * @param imgUrl
     * @param type
     */
    public static void updateImg(String id, String imgUrl, int type) {
        switch (type) {
            case 0:
                // 更新收藏夹
                String Query = "select * from T_FAVORITE where F_LINK_ID =?";
                Cursor cursor = db.rawQuery(Query, new String[] { id });
                if (cursor.getCount() > 0) {
                    cursor.moveToNext();
                    String favoriteId = cursor.getString(cursor.getColumnIndex("F_ID"));
                    db.execSQL("update T_FAVORITE set F_IMG_URL=? where F_ID=?",
                            new Object[]{
                                    imgUrl,
                                    favoriteId});
                }
                cursor.close();
                break;
            case 1:
                // 更新历史记录
                db.execSQL("update T_HISTORY set F_IMG_URL =? where F_LINK_ID=?", new Object[]{
                        imgUrl,
                        id
                });
                break;
            case 2:
                db.execSQL("update T_DOWNLOAD set F_IMG_URL =? where F_ID=?", new Object[]{
                        imgUrl,
                        id
                });
                break;
        }
    }

    /**
     * 删除重复taskID的错误数据
     * @param context
     */
    public static void deleteDistinctData(Context context) {
        Cursor cursor = db.rawQuery("select F_TASK_ID from T_DOWNLOAD_DATA where F_TASK_ID in (select F_TASK_ID from T_DOWNLOAD_DATA group by F_TASK_ID having count(*) > 1) group by F_TASK_ID", null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int taskId = cursor.getInt(0);
                Aria.download(context).load(taskId).cancel();
            }
            cursor.close();
        }
        db.execSQL("delete from T_DOWNLOAD_DATA where F_TASK_ID in (select F_TASK_ID from T_DOWNLOAD_DATA group by F_TASK_ID having count(*) > 1) and F_COMPLETE = 0", new String[]{});
        db.execSQL("delete from T_DOWNLOAD where F_ID not in (select F_LINK_ID from T_DOWNLOAD_DATA group by F_LINK_ID)", new String[]{});
    }

    /**
     * 删除所有下载记录
     */
    public static void deleteAllDownloads() {
        db.execSQL("delete from T_DOWNLOAD");
        db.execSQL("delete from T_DOWNLOAD_DATA");
    }

    /**
     * 2022年8月3日09:28:04新增
     * 由于MALIMALI源地址变更需更新错误的播放记录
     */
    public static void updatePlayUrl() {
        Cursor cursor = db.rawQuery("select F_ID, F_PLAY_URL from T_HISTORY_DATA WHERE F_PLAY_URL like '%/play/%'", null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String url = cursor.getString(1).replaceAll("/play/", "/vodplay/");
                db.execSQL("UPDATE T_HISTORY_DATA SET F_PLAY_URL = ? WHERE F_ID = ?", new String[] {
                        url,
                        id
                });
            }
            cursor.close();
        }
    }

    /**
     * 删除数据库中不存在的任务
     * @param context
     * @param taskId
     */
    public static void deleteAbsentTask(Context context, long taskId) {
        Cursor cursor = db.rawQuery("select * from T_DOWNLOAD_DATA where F_TASK_ID = ?", new String[]{String.valueOf(taskId)});
        if (cursor.getCount() == 0) {
            // 数据库中不存在 删除任务
            Aria.download(context).load(taskId).ignoreCheckPermissions().cancel();
            Log.e("删除不存在的任务", "TaskId：" + taskId);
        }
        cursor.close();
    }
}