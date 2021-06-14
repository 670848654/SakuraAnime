package my.project.sakuraproject.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.ApiBean;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.util.Utils;

public class DatabaseUtil {
    private static SQLiteDatabase db;
    private static String DB_PATH = Environment.getExternalStorageDirectory() + "/SakuraAnime/Database/sakura.db";

    /**
     * 创建tables
     */
    public static void CREATE_TABLES() {
        db = SQLiteDatabase.openOrCreateDatabase(DB_PATH, null);
        db.execSQL("create table if not exists f_favorite(id integer primary key autoincrement, f_title text, f_url text, f_desc text, f_img text)");
        db.execSQL("create table if not exists f_anime(id integer primary key autoincrement, f_id text, f_title text)");
        db.execSQL("create table if not exists f_index(id integer primary key autoincrement, f_pid text, f_url text)");
        db.execSQL("create table if not exists f_api(id integer primary key autoincrement, f_id text, f_title text, f_url text)");
    }

    /**
     * 关闭数据库连接
     */
    public static void closeDB() {
        db.close();
    }

    /**
     * 新增点击过的番剧名称
     *
     * @param title
     */
    public static void addAnime(String title) {
        if (!checkAnime(title))
            db.execSQL("insert into f_anime values(?,?,?)",
                    new Object[]{null, UUID.randomUUID().toString(), title});
    }

    /**
     * 检查番剧名称是否存在
     *
     * @param title
     * @return
     */
    public static boolean checkAnime(String title) {
        String Query = "select * from f_anime where f_title =?";
        Cursor cursor = db.rawQuery(Query, new String[]{title});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    /**
     * 获取番剧fid
     *
     * @param title
     * @return
     */
    public static String getAnimeID(String title) {
        String Query = "select * from f_anime where f_title =?";
        Cursor cursor = db.rawQuery(Query, new String[]{title});
        cursor.moveToNext();
        return cursor.getString(1);
    }

    /**
     * 新增点击过的剧集名称
     *
     * @param fid 父id
     * @param url 播放地址
     */
    public static void addIndex(String fid, String url) {
        String imomoeUlr = BaseModel.getDomain(true);
        String yhdmUrl  = BaseModel.getDomain(false);
        url = url.contains(imomoeUlr) ? url.substring(imomoeUlr.length()) : url.substring(yhdmUrl.length());
        if (!checkIndex(fid, url))
            db.execSQL("insert into f_index values(?,?,?)",
                    new Object[]{null, fid, url});
    }

    /**
     * 检查剧集名称是否存在
     *
     * @param fid 父id
     * @param url 播放地址
     * @return
     */
    private static boolean checkIndex(String fid, String url) {
        String Query = "select * from f_index where f_pid =? and f_url =?";
        Cursor cursor = db.rawQuery(Query, new String[]{fid, url});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    /**
     * 检查当前fid所有剧集
     *
     * @param fid 番剧ID
     * @return
     */
    public static String queryAllIndex(String fid) {
        StringBuffer buffer = new StringBuffer();
        String Query = "select * from f_index where f_pid =?";
        Cursor c = db.rawQuery(Query, new String[]{fid});
        while (c.moveToNext()) {
            buffer.append(c.getString(2));
        }
        c.close();
        return buffer.toString();
    }

    /**
     * 分页查询用户收藏的番剧
     */
    public static List<AnimeListBean> queryFavoriteByLimit(int offset, int limit) {
        List<AnimeListBean> list = new ArrayList<>();
//        Cursor c = db.rawQuery("select * from f_favorite order by id desc_bg", null);
        String parameter = "%s,%s";
        Cursor c = db.query("f_favorite", null, null, null,null,null,"id DESC",
                String.format(parameter, offset, limit));
        while (c.moveToNext()) {
            AnimeListBean bean = new AnimeListBean();
            bean.setTitle(c.getString(1));
            bean.setUrl(c.getString(2));
            bean.setDesc(c.getString(3));
            bean.setImg(c.getString(4));
            list.add(bean);
        }
        c.close();
        return list;
    }

    /**
     *  查询用户收藏总数
     * @return
     */
    public static int queryFavoriteCount() {
        int count;
        String QueryCount = "select * from f_favorite";
        Cursor cursor = db.rawQuery(QueryCount, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * 收藏or删除收藏
     *
     * @param bean
     * @return true 收藏成功 false 移除收藏
     */
    public static boolean favorite(AnimeListBean bean, boolean isImomoe) {
        if (isImomoe) bean.setTitle(bean.getTitle()+Utils.getString(R.string.imomoe));
        if (checkFavorite(bean.getTitle())) {
            deleteFavorite(bean.getTitle());
            return false;
        } else {
            addFavorite(bean);
            return true;
        }
    }

    /**
     * 添加到收藏
     *
     * @param bean
     */
    private static void addFavorite(AnimeListBean bean) {
        String imomoeUlr = BaseModel.getDomain(true);
        String yhdmUrl  = BaseModel.getDomain(false);
        String url =  bean.getUrl().contains(imomoeUlr) ?  bean.getUrl().substring(imomoeUlr.length()) :  bean.getUrl().substring(yhdmUrl.length());
        db.execSQL("insert into f_favorite values(?,?,?,?,?)",
                new Object[]{null,
                        bean.getTitle(),
                        url,
                        bean.getDesc(),
                        bean.getImg()
                });
    }

    /**
     * 更新收藏信息
     * @param bean
     */
    public static void updateFavorite(AnimeListBean bean) {
        String Query = "select * from f_favorite where f_title =?";
        String title = bean.getUrl().contains("/view/") ?  bean.getTitle() + Utils.getString(R.string.imomoe) : bean.getTitle();
        Cursor cursor = db.rawQuery(Query, new String[] { title });
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String imomoeUlr = BaseModel.getDomain(true);
            String yhdmUrl  = BaseModel.getDomain(false);
            String url =  bean.getUrl().contains(imomoeUlr) ?  bean.getUrl().substring(imomoeUlr.length()) :  bean.getUrl().substring(yhdmUrl.length());
            db.execSQL("update f_favorite set f_title=?, f_url=?, f_desc=?, f_img=? where id=?",
                    new Object[]{
                            title,
                            url,
                            bean.getDesc(),
                            bean.getImg(),
                            id});
        }
        cursor.close();
    }

    /**
     * 删除收藏
     *
     * @param title
     */
    public static void deleteFavorite(String title) {
        db.execSQL("delete from f_favorite where f_title=?", new String[]{title});
    }

    /**
     * 检查番剧是否收藏
     *
     * @param title
     * @return
     */
    public static boolean checkFavorite(String title) {
        String Query = "select * from f_favorite where f_title =?";
        Cursor cursor = db.rawQuery(Query, new String[]{title});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    /**
     * 查询用户自定义api
     *
     * @return
     */
    public static List<ApiBean> queryAllApi() {
        List<ApiBean> list = new ArrayList<>();
        Cursor c = db.rawQuery("select * from f_api order by id desc", null);
        while (c.moveToNext()) {
            ApiBean bean = new ApiBean();
            bean.setId(c.getString(1));
            bean.setTitle(c.getString(2));
            bean.setUrl(c.getString(3));
            list.add(bean);
        }
        c.close();
        return list;
    }

    /**
     * 新增api
     *
     * @param bean
     */
    public static void addApi(ApiBean bean) {
        db.execSQL("insert into f_api values(?,?,?,?)",
                new Object[]{null,
                        bean.getId(),
                        bean.getTitle(),
                        bean.getUrl()});
    }

    /**
     * 删除api
     *
     * @param id
     */
    public static void deleteApi(String id) {
        db.execSQL("delete from f_api where f_id=?", new String[]{id});
    }

    /**
     * 修改api
     *
     * @param id
     * @param title
     * @param url
     */
    public static void updateApi(String id, String title, String url) {
        db.execSQL("update f_api set f_title=?,f_url=? where f_id=?", new String[]{title, url, id});
    }
}