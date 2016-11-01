package com.simbaliu.vcyp.view;

import android.content.Context;
import android.widget.Button;

import com.simbaliu.vcyp.R;

/**
 * 悬浮窗
 * Created by LiuXing on 2016/10/27.
 */
public class FloatView extends Button {
    public FloatView(Context context) {
        super(context);
        setBackgroundResource(R.mipmap.icon_main_menu_order_water);
    }

    public void isSpeeking(boolean isSpeeking) {
        if (isSpeeking) {
            setBackgroundResource(R.mipmap.icon_main_menu_order_milk);
        } else {
            setBackgroundResource(R.mipmap.icon_main_menu_order_water);
        }
    }

}
