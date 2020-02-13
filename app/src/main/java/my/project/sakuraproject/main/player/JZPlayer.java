package my.project.sakuraproject.main.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import cn.jzvd.JZDataSource;
import cn.jzvd.JzvdStd;
import my.project.sakuraproject.R;

public class JZPlayer extends JzvdStd {
    private Context context;
    private CompleteListener listener;
    private TouchListener touchListener;

    public JZPlayer(Context context) {
        super(context);
    }

    public JZPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Context context, CompleteListener listener, TouchListener touchListener) {
        this.context = context;
        this.listener = listener;
        this.touchListener = touchListener;
    }

    public interface CompleteListener {
        void complete();
    }

    public interface TouchListener {
        void touch();
    }

    @Override
    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {
        super.setUp(jzDataSource, screen, mediaInterfaceClass);
        batteryTimeLayout.setVisibility(GONE);
        Glide.with(context).load(R.drawable.baseline_view_module_white_48dp).into(fullscreenButton);
        Glide.with(context).load(R.drawable.ic_close_white_48dp).apply(new RequestOptions().fitCenter()).into(backButton);
        backButton.setPadding(0, 0, 20, 0);
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
        onStateAutoComplete();
        listener.complete();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchListener.touch();
        return super.onTouch(v, event);
    }
}
