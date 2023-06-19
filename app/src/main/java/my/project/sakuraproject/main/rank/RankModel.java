package my.project.sakuraproject.main.rank;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.SiliSiliRankBean;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RankModel implements RankContract.Model {
    @Override
    public void getData(RankContract.LoadDataCallback callback) {
        new HttpGet(Sakura.DOMAIN + Api.SILISILI_RANK, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String source = response.body().string();
                    List<SiliSiliRankBean> siliSiliRankBeans = ImomoeJsoupUtils.getRankList(source);
                    if (siliSiliRankBeans.size() > 0)
                        callback.success(siliSiliRankBeans);
                    else
                        callback.error(Utils.getString(R.string.parsing_error));
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    };
}
