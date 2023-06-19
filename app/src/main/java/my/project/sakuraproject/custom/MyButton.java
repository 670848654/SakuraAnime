package my.project.sakuraproject.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyButton extends MaterialButton {

    public MyButton(@NonNull Context context) { super(context); }

    public MyButton(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

    public MyButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) { super.onFocusChanged(focused, direction, previouslyFocusedRect); }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) { if(hasWindowFocus) super.onWindowFocusChanged(hasWindowFocus); }

    @Override
    public boolean isFocused() { return true; }
}
