package com.ujuz.suspend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.ujuz.suspend.scroll.ScrollHeaderListener;

/**
 * @author ameng
 * Create on 2020-06-02 14:01
 * Link:amengnn1029@gmail.com
 */
public class HeaderContainer extends LinearLayout {

    int initY = 0;
    int scrollY = 0;
    private ScrollHeaderListener scrollHeaderListener;


    public HeaderContainer(Context context) {
        super(context);
    }

    public HeaderContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollHeaderListener(ScrollHeaderListener scrollHeaderListener) {
        this.scrollHeaderListener = scrollHeaderListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        scrollHeaderListener.setMotionEventAction(event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                scrollY = (int) (event.getY() - initY);
                scrollHeaderListener.onScrollListener(scrollY);

                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }


}
