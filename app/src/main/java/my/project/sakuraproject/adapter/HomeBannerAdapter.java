package my.project.sakuraproject.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.util.Utils;

public class HomeBannerAdapter extends BannerAdapter<HomeBean.HomeItemBean, HomeBannerAdapter.BannerViewHolder> {
    private Context context;
    public HomeBannerAdapter(Context context, List<HomeBean.HomeItemBean> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_item, parent, false);
        return new BannerViewHolder(v);
    }

    @Override
    public void onBindView(BannerViewHolder holder, HomeBean.HomeItemBean homeItemBean, int position, int size) {
        ImageView imageView = holder.imageView;
        imageView.setTag(R.id.imageid,  homeItemBean.getImg());
        if (Utils.isPad()) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else
            imageView.setScaleType(Utils.isImomoe() ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Utils.setDefaultImage(context, homeItemBean.getImg(), imageView);
        Glide.with(context)
                .load(homeItemBean.getImg())
                .fitCenter()
                .format(DecodeFormat.PREFER_RGB_565)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .apply(RequestOptions.bitmapTransform( new BlurTransformation(20, 8)))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.imageView.setBackground(resource);
                    }
                });
        holder.title.setText(homeItemBean.getTitle());
        holder.episodes.setText(homeItemBean.getEpisodes());
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final ImageView imageView;
        public final TextView episodes;
        public BannerViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            imageView = (ImageView) v.findViewById(R.id.img);
            episodes = (TextView) v.findViewById(R.id.episodes);
        }
    }
}