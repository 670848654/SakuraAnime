package my.project.sakuraproject.main.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import cn.jzvd.JZDataSource;
import cn.jzvd.JzvdStd;
import my.project.sakuraproject.R;

public class JZPlayer extends JzvdStd {
    private Context context;
    private CompleteListener listener;

    public JZPlayer(Context context) {
        super(context);
    }

    public JZPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Context context, CompleteListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface CompleteListener {
        void complete();
    }

    @Override
    public void setUp(JZDataSource jzDataSource, int screen) {
        super.setUp(jzDataSource, screen);
        batteryTimeLayout.setVisibility(GONE);
        Glide.with(context).load(R.drawable.baseline_view_module_white_48dp).into(fullscreenButton);
        Glide.with(context).load(R.drawable.baseline_close_white_48dp).apply(new RequestOptions().fitCenter()).into(backButton);
        backButton.setPadding(0, 0, 15, 0);
        changeButtonSize(backButton, 70);
    }

    public void changeButtonSize(View view, int size) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = size;
        lp.width = size;
    }

    public void startPIP() {
        fullscreenButton.setVisibility(INVISIBLE);
        backButton.setVisibility(INVISIBLE);
        titleTextView.setVisibility(INVISIBLE);
    }

    public void exitPIP() {
        fullscreenButton.setVisibility(VISIBLE);
        backButton.setVisibility(VISIBLE);
        titleTextView.setVisibility(VISIBLE);
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        listener.complete();
    }
}
