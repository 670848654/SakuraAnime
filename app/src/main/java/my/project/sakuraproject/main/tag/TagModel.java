package my.project.sakuraproject.main.tag;

import android.util.Log;

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
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TagModel extends BaseModel implements TagContract.Model {
    private List<MultiItemEntity> list = new ArrayList<>();

    @Override
    public void getData(TagContract.LoadDataCallback callback) {
        getHtml(callback, "");
    }

    private void getHtml(TagContract.LoadDataCallback callback, String RedirectedStr) {
        callback.log(Sakura.TAG_API + RedirectedStr);
        new HttpGet(Sakura.TAG_API + RedirectedStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Document doc = Jsoup.parse(response.body().string());
                    if (hasRedirected(doc))
                        getHtml(callback, getRedirectedStr(doc));
                    else if (hasRefresh(doc)) getHtml(callback, "");
                    else {
                        Elements tagTitles = doc.select("div.dtit");
                        Elements tagItems = doc.select("div.link");
                        Log.e("size1", tagTitles.size()+"");
                        Log.e("size2", tagItems.size()+"");
                        if (tagTitles.size() == tagItems.size()) {
                            setTagData(tagTitles, tagItems);
                            callback.success(list);
                        } else callback.error(Utils.getString(R.string.parsing_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }

            }
        });
    }

    private void setTagData(Elements tagTitles, Elements tagItems) {
        for (int i = 1, tagSize = tagTitles.size(); i < tagSize ; i++) {
            TagHeaderBean tagHeaderBean = new TagHeaderBean(tagTitles.get(i).text());
            Elements itemElements = tagItems.get(i).select("a");
            for (int j = 0, itemSize = itemElements.size(); j < itemSize; j++) {
                tagHeaderBean.addSubItem(
                        new TagBean(
                                tagHeaderBean.getTitle() + " - " + itemElements.get(j).text(),
                                itemElements.get(j).text(),
                                itemElements.get(j).attr("href")
                        )
                );
            }
            list.add(tagHeaderBean);
        }
    }
}
