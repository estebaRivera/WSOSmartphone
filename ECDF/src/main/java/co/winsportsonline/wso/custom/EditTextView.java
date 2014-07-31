package co.winsportsonline.wso.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Esteban- on 16-06-14.
 */
public class EditTextView extends EditText {

    public EditTextView(Context context) {
        super(context);
    }

    public EditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode== KeyEvent.KEYCODE_ENTER)
        {
            // Just ignore the [Enter] key
            return false;
        }
        // Handle all other keys in the default way
        return super.onKeyDown(keyCode, event);
    }
}
