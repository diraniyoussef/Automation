package de.dirani.readButton;

import android.os.SystemClock;
import android.view.View;
import android.widget.Switch;

import java.util.Map;
import java.util.WeakHashMap;

/**         This is left for reference only. Not used...
 * A Debounced OnClickListener
 * Rejects clicks that are too close together in time.
 * This class is safe to use as an OnClickListener for multiple views, and will debounce each one separately.
 */
public abstract class DebouncedOnClickListener implements View.OnClickListener {
    private Switch switch1;
    private final long minimumInterval;
    private Map<View, Long> lastClickMap;

    /**
     * Implement this in your subclass instead of onClick
     * @param v The view that was clicked
     */
    public abstract void onDebouncedClick(View v);

    /**
     * The one and only constructor
     * @param minimumIntervalMsec The minimum allowed time between clicks - any click sooner than this after a previous click will be rejected
     */
    protected DebouncedOnClickListener(long minimumIntervalMsec) {
        this.minimumInterval = minimumIntervalMsec;
        this.lastClickMap = new WeakHashMap<View, Long>();
    }

    @Override
    public void onClick(View clickedView) {
        Long previousClickTimestamp = lastClickMap.get(clickedView);
        long currentTimestamp = SystemClock.uptimeMillis();

        lastClickMap.put(clickedView, currentTimestamp);

        if(previousClickTimestamp == null || Math.abs(currentTimestamp - previousClickTimestamp.longValue()) > minimumInterval) {
            onDebouncedClick(clickedView);
        }
    }
}

//how to use it?
/*
//inside OnResume() you can put
        switch3.setOnClickListener(new DebouncedOnClickListener1(2000));
//inside the MainActivity you put
    private class DebouncedOnClickListener1 extends DebouncedOnClickListener {

protected DebouncedOnClickListener1(long minimumIntervalMsec) {
    super(minimumIntervalMsec);
}

    @Override
    public void onDebouncedClick(View v){
        Log.i("switch3", "it's starting");




    }

}

 */