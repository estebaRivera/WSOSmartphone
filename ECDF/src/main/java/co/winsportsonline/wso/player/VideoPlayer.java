package co.winsportsonline.wso.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by Esteban- on 10-06-14.
 */
public class VideoPlayer extends VideoView implements MediaController.MediaPlayerControl {
    public VideoPlayer(Context context) {
        super(context);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void start() {
        super.start();

    }

}
