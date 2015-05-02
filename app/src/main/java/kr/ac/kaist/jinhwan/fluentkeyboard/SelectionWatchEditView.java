package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

public class SelectionWatchEditView extends EditText{
    private SelectionWatcher selectionWatcher;

    public SelectionWatchEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addSelectionWatcher(SelectionWatcher selectionWatcher){
        this.selectionWatcher = selectionWatcher;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        Log.d("editText", "onSelectionChanged called");
        if(selectionWatcher!=null){
            Log.d("editText", "watcher not null. ready to call watcher");
            selectionWatcher.onSelectionChanged(selStart, selEnd);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public static interface SelectionWatcher {
        public void onSelectionChanged(int selStart, int selEnd);
    }
}
