package my.project.sakuraproject.main.video;

import com.alibaba.fastjson.JSONObject;

import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface DanmuContract {

    interface Model {
        void getDanmu(String title, String drama, DanmuContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessDanmuView(JSONObject danmus);

        void showSuccessDanmuXmlView(String content);

        void showErrorDanmuView(String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successDanmu(JSONObject danmus);

        void successDanmuXml(String content);

        void errorDanmu(String msg);
    }
}
