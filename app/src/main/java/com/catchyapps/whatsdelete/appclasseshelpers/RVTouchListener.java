package com.catchyapps.whatsdelete.appclasseshelpers;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RVTouchListener implements RecyclerView.OnItemTouchListener {

    private final GestureDetector gestureDetector;
    private final ClickListener clickListener;

    public RVTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null) {
                    clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child), e);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
            int position = rv.getChildAdapterPosition(child);
            // If we should not handle (e.g. tap on share/delete), don't call onClick and don't consume
            if (!clickListener.shouldHandleClick(child, position, e)) {
                return false;
            }
            clickListener.onClick(child, position, e);
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface ClickListener {
        void onClick(View view, int position, MotionEvent e);
        void onLongClick(View view, int position, MotionEvent e);
        /** Return false to let the event go to children (e.g. share/delete icons). Default true. */
        default boolean shouldHandleClick(View view, int position, MotionEvent e) {
            return true;
        }
    }
}
