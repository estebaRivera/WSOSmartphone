package co.winsportsonline.wso.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.longtailvideo.jwplayer.JWPlayer;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.media.HLSMediaProvider;
import com.longtailvideo.jwplayer.media.NativeHLSMediaProvider;

import java.nio.Buffer;

import co.winsportsonline.wso.BuildConfig;
import co.winsportsonline.wso.R;
import co.winsportsonline.wso.datamodel.LiveStreamSchedule;
import co.winsportsonline.wso.datamodel.Media;
import co.winsportsonline.wso.datamodel.TokenIssue;
import co.winsportsonline.wso.delegates.ImageChooserDelegate;
import co.winsportsonline.wso.delegates.VideoDelegate;
import co.winsportsonline.wso.dialogs.ImagePickerDialog;
import co.winsportsonline.wso.services.ServiceManager;

/**
 * Created by Esteban- on 15-04-14.
 */
public class PlayerActivity extends ActionBarActivity{// implements
        //JWPlayer.OnFullscreenListener, JWPlayer.OnPlayListener, JWPlayer.OnBufferListener, JWPlayer.OnIdleListener,
        //JWPlayer.OnQualityChangeListener, JWPlayer.OnQualityLevelsListener, JWPlayer.OnErrorListener{

    private static final String MEDIA_HLS_BASE_URL = "https://mdstrm.com/video/";
    private VideoDelegate videoDelegate;
    private JWPlayerView playerView;
    private VideoView videoView;
    private View videoPreview;
    private String URL;
    private RelativeLayout contNative;
    private ProgressDialog progress;
    private final int MAX = 2;
    private int intentos = 0;
    private boolean isKitkat = false;
    private boolean isProgressDimiss = false;
    private long inicio, fin, delta;

    public PlayerActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        progress = new ProgressDialog(this);
        contNative = (RelativeLayout) findViewById(R.id.player_native);
        Bundle extras = getIntent().getExtras();
        URL = extras.getString("media");
        inicio = System.currentTimeMillis();
        progress.show();
        progress.setContentView(R.layout.progress_dialog_horizontal);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        contNative.setVisibility(View.VISIBLE);
        videoView = (VideoView) findViewById(R.id.video_view);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                fin = System.currentTimeMillis();
                delta = (fin - inicio)/ 1000;
                progress.dismiss();
                Log.e("Video ","Start");
                Log.e("Delay","--> "+delta);
            }

        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                if(intentos < MAX){
                    intentos++;
                    Toast.makeText(getApplicationContext(),"Error "+intentos,Toast.LENGTH_LONG).show();
                    displayMediaNative();
                    return true;
                }
                else{
                    finish();
                }
                return false;
            }
        });

        displayMediaNative();

    }

    private void displayMediaNative(){

        videoView.setVideoPath(URL);
        videoView.setMediaController(new MediaController(this));
        videoView.start();
    }
    private void displayMedia() {

        playerView.setFullscreen(true);
        playerView.release();
        playerView.load(URL);
        playerView.play();
    }


    @Override
    public void onBackPressed() {
        videoView.stopPlayback();
        finish();
    }

    @Override
    protected void onPostResume() {
        if(isKitkat && videoView.isPlaying() ){
            Log.e("sesñvsdfñb sdñFbfs","egrdg");
            progress.dismiss();
        }
        super.onPostResume();
    }

}
