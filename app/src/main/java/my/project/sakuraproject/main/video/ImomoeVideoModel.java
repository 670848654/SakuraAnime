package my.project.sakuraproject.main.video;

import java.io.IOException;

import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ImomoeVideoModel extends BaseModel implements ImomoeVideoContract.Model {

    @Override
    public void getData(String url, ImomoeVideoContract.LoadDataCallback callback) {
        parserImomoe(url, callback);
    }

    private void parserImomoe( String url, ImomoeVideoContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, true);
                String playUrl = ImomoeJsoupUtils.getImomoeApiPlayUrl(source);
                if (playUrl.isEmpty()) callback.error();
                else callback.success(playUrl);
            }
        });
    }
}
