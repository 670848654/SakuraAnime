package my.project.sakuraproject.main.tag;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.bean.TagHeaderBean;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TagModel implements TagContract.Model {
    private List<MultiItemEntity> list = new ArrayList<>();

    @Override
    public void getData(TagContract.LoadDataCallback callback) {
        new HttpGet(Sakura.TAG_API, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Document doc = Jsoup.parse(response.body().string());
                    Elements tagList = doc.select("div.ters > p");
                    if (tagList.size() > 0) {
                        //字母索引
                        setData("字母索引", tagList.get(0).select("a"));
                        //年份
                        setYearData("年份", tagList.get(1).select("a"));
                        //地区
                        setRegionData();
                        //语言
                        setLanguageData();
                        //类型
                        setData("动漫类型", tagList.get(2).select("a"));
                        callback.success(list);
                    } else callback.error(Utils.getString(R.string.parsing_error));
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }

            }
        });
    }

    private void setData(String title, Elements tag) {
        TagHeaderBean tagHeaderBean = new TagHeaderBean(title);
        for (int i = 0; i < tag.size(); i++) {
            tagHeaderBean.addSubItem(
                    new TagBean(
                            tag.get(i).text(),
                            tag.get(i).attr("href"),
                            title + " - ",
                            true));
        }
        list.add(tagHeaderBean);
    }

    private void setYearData(String title, Elements year) {
        TagHeaderBean tagHeaderBean = new TagHeaderBean(title);
        for (int i = 0; i < year.size(); i++) {
            if (year.get(i).text().startsWith("2")) {
                tagHeaderBean.addSubItem(
                        new TagBean(
                                year.get(i).text(),
                                year.get(i).attr("href"),
                                title + " - ",
                                true));
            }
        }
        list.add(tagHeaderBean);
    }

    private void setRegionData() {
        TagHeaderBean tagHeaderBean = new TagHeaderBean("地区");
        tagHeaderBean.addSubItem(new TagBean("日本", "/japan/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("大陆", "/china/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("美国", "/american/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("英国", "/england/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("韩国", "/korea/", "地区 - ", true));
        list.add(tagHeaderBean);
    }

    private void setLanguageData() {
        TagHeaderBean tagHeaderBean = new TagHeaderBean("语言");
        tagHeaderBean.addSubItem(new TagBean("日语", "/29/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("国语", "/30/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("粤语", "/31/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("英语", "/32/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("韩语", "/33/", "地区 - ", true));
        tagHeaderBean.addSubItem(new TagBean("方言", "/34/", "地区 - ", true));
        list.add(tagHeaderBean);
    }
}
