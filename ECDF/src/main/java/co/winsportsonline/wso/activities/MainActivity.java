package co.winsportsonline.wso.activities;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.util.AQUtility;
import com.longtailvideo.jwplayer.JWPlayer;
import com.longtailvideo.jwplayer.JWPlayerView;

import org.json.JSONObject;

import java.util.List;

import co.winsportsonline.wso.R;
import co.winsportsonline.wso.datamodel.Filter;
import co.winsportsonline.wso.datamodel.LiveStreamSchedule;
import co.winsportsonline.wso.datamodel.Media;
import co.winsportsonline.wso.datamodel.TokenIssue;
import co.winsportsonline.wso.delegates.ImageChooserDelegate;
import co.winsportsonline.wso.delegates.SlideMenuDelegate;
import co.winsportsonline.wso.delegates.VideoDelegate;
import co.winsportsonline.wso.dialogs.ImagePickerDialog;
import co.winsportsonline.wso.dialogs.MessageDialog;
import co.winsportsonline.wso.fragments.FilteredFragment;
import co.winsportsonline.wso.fragments.LiveFragment;
import co.winsportsonline.wso.fragments.SlideMenu;
import co.winsportsonline.wso.fragments.VodFragment;
import co.winsportsonline.wso.services.ServiceManager;
import co.winsportsonline.wso.utils.VideoType;

/**
 * Created by Franklin Cruz on 17-02-14.
 */
public class MainActivity extends ActionBarActivity implements
        JWPlayer.OnFullscreenListener, JWPlayer.OnPlayListener, JWPlayer.OnBufferListener, JWPlayer.OnIdleListener,
        JWPlayer.OnQualityChangeListener, JWPlayer.OnQualityLevelsListener,JWPlayer.OnErrorListener {

    private static final String MEDIA_HLS_BASE_URL = "https://mdstrm.com/video/";

    private SlideMenu slideMenu;
    private boolean isSlideMenuOpen = false;
    private boolean isOnLiveStream = false;

    private View blockOverlay;

    private LiveFragment liveFragment;
    private VodFragment vodFragment;
    private FilteredFragment filteredFragment;

    private ImageButton menuButton;
    private ImageButton helpButton;
    private Button liveButton;
    private Button vodButton;

    private final int MAX = 5;
    private int intentos = 0;
    private boolean isWifiEnabled;
    private boolean is3GEnabled;

    private VideoDelegate videoDelegate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

       final ServiceManager serviceManager = new ServiceManager(getApplicationContext());
        serviceManager.reLogin(new ServiceManager.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(JSONObject jsonObject) {
                Intent intent = new Intent(getApplication(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intentos = 0;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dataNetwork();

        videoDelegate = new VideoDelegate() {
            @Override
            public void onVideoSelected(Media media) {
                displayMedia(media);
            }

            @Override
            public void onLiveShowBegin(LiveStreamSchedule media) {
               //displayLiveThumbnail(media);
            }

            @Override
            public void displayImageChooser(String image1, String image2, ImageChooserDelegate delegate) {
                hideSlideMenu();

                ImagePickerDialog newFragment = new ImagePickerDialog(image1,image2);
                newFragment.setDelegate(delegate);
                newFragment.show(getSupportFragmentManager(), "dialog");
            }

            @Override
            public void onVideoSelected(LiveStreamSchedule media) {
                //super.onVideoSelected(media);

                displayLiveThumbnail(media,1);
            }
        };

        blockOverlay = findViewById(R.id.block_overlay);
        blockOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSlideMenu();
            }
        });

        slideMenu = (SlideMenu)getSupportFragmentManager().findFragmentById(R.id.slide_menu);

        slideMenu.setDelegate(new SlideMenuDelegate() {

            @Override
            public void onSearchSelected(SlideMenu slidemenu, List<Media> media, ProgressDialog progress) {
                liveButton.setSelected(false);
                liveButton.setBackgroundColor(Color.TRANSPARENT);
                vodButton.setSelected(true);
                vodButton.setBackgroundColor(Color.parseColor("#D46419"));
                loadVodContent(media);
                progress.dismiss();
            }

            @Override
            public void onFilterSelected(SlideMenu slidemenu, Filter filter) {
                liveButton.setSelected(false);
                liveButton.setBackgroundColor(Color.TRANSPARENT);
                vodButton.setSelected(true);
                vodButton.setBackgroundColor(Color.parseColor("#D46419"));
                loadVodContent(filter);
            }

            @Override
            public void onVodSelected(SlideMenu slidemenu) {
                liveButton.setSelected(false);
                liveButton.setBackgroundColor(Color.TRANSPARENT);
                vodButton.setSelected(true);
                vodButton.setBackgroundColor(Color.parseColor("#D46419"));
                loadVodContent();
                hideSlideMenu();
            }

            @Override
            public void onLiveSelected(SlideMenu slidemenu) {
                liveButton.setSelected(true);
                liveButton.setBackgroundColor(Color.parseColor("#D46419"));
                vodButton.setSelected(false);
                vodButton.setBackgroundColor(Color.TRANSPARENT);
                loadLiveContent();
                hideSlideMenu();
            }
        });

        createActionBar();

        if (savedInstanceState == null) {

            loadLiveContent();
            findViewById(R.id.main_container).bringToFront();

        }
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }

    }


    public void dataNetwork(){

        ConnectivityManager conMan = ((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE));
        isWifiEnabled = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        /*is3GEnabled = !(conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
            && conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getReason().equals("dataDisabled"));*/
    }

    private void displayLiveThumbnail(final LiveStreamSchedule live, final int index) {
        if (!isOnLiveStream) {
            return;
        }
        final ProgressDialog progress = new ProgressDialog(this);
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.issueTokenForLive(live.getStream().getLiveStreamId(), index, new ServiceManager.DataLoadedHandler<TokenIssue>() {
            @Override
            public void loaded(TokenIssue data) {

                if (data.getStatus().equalsIgnoreCase("OK")) {
                    String url = String.format("https://mdstrm.com/live-stream-playlist/%s.m3u8?access_token=%s", live.getStream().getLiveStreamId(), data.getAccessToken());

                    Intent intent = new Intent( MainActivity.this, PlayerActivity.class);
                    intent.putExtra("media", url);
                    startActivity(intent);
                    progress.dismiss();
                }
                else if(data.getStatus().equalsIgnoreCase("ERROR")){
                    Toast.makeText(getApplicationContext(),data.getStatus(), Toast.LENGTH_LONG).show();
                    progress.dismiss();
                }else{
                    MessageDialog messageDialog = new MessageDialog(data.getStatus());
                    messageDialog.show(getFragmentManager(), "dialog");
                    progress.dismiss();
                }
            }

            @Override
            public void error(String error) {
                MessageDialog messageDialog = new MessageDialog(error);
                messageDialog.show(getFragmentManager(), "dialog");
                progress.dismiss();
            }
        });
    }

    private void displayMedia(final Media media) {

        final ProgressDialog progress = new ProgressDialog(this);
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        ServiceManager serviceManager = new ServiceManager(this);


        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(outSize);
        final int screenHeight = outSize.y;

        final Intent intent = new Intent( MainActivity.this, PlayerActivity.class);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){*/
            serviceManager.issueTokenForMedia(media.getMediaId(), new ServiceManager.DataLoadedHandler<TokenIssue>() {
                @Override
                public void loaded(TokenIssue data) {
                    if(data.getStatus().equalsIgnoreCase("OK")) {

                        String url = null;
                        if(isWifiEnabled == true){
                            if(screenHeight <= 600){

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                                    url = String.format("%s%s.m3u8?access_token=%s",MEDIA_HLS_BASE_URL, media.getMediaId(), data.getAccessToken());
                                }
                                else{
                                    url = String.format("%s?access_token=%s",media.getMetaLabel(VideoType.MEDIA).getUrl(), data.getAccessToken());
                                }

                            }
                            else{
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                                    url = String.format("%s%s.m3u8?access_token=%s",MEDIA_HLS_BASE_URL, media.getMediaId(), data.getAccessToken());
                                }
                                else{
                                    url = String.format("%s?access_token=%s",media.getMetaLabel(VideoType.ALTA).getUrl(), data.getAccessToken());
                                }

                            }

                        }
                        else{
                            if(screenHeight <= 600){

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                                    url = String.format("%s%s.m3u8?access_token=%s",MEDIA_HLS_BASE_URL, media.getMediaId(), data.getAccessToken());
                                }
                                else{
                                    url = String.format("%s?access_token=%s",media.getMetaLabel(VideoType.BAJA).getUrl(), data.getAccessToken());
                                }

                            }
                            else{
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                                    url = String.format("%s%s.m3u8?access_token=%s",MEDIA_HLS_BASE_URL, media.getMediaId(), data.getAccessToken());
                                }
                                else{
                                    url = String.format("%s?access_token=%s",media.getMetaLabel(VideoType.MEDIA).getUrl(), data.getAccessToken());
                                }

                            }
                        }
                        intent.putExtra("media",url);
                        startActivity(intent);
                        progress.dismiss();
                    }
                    else if(data.getStatus().equalsIgnoreCase("ERROR")){
                        Toast.makeText(getApplicationContext(),data.getStatus(), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }else{
                        MessageDialog messageDialog = new MessageDialog(data.getStatus());
                        messageDialog.show(getFragmentManager(), "dialog");
                        progress.dismiss();
                    }
                }
            });
       // }
       /* else{
                serviceManager.issueTokenForMedia(media.getMediaId(), new ServiceManager.DataLoadedHandler<TokenIssue>() {
                @Override
                public void loaded(TokenIssue data) {
                    if(data.getStatus().equalsIgnoreCase("OK")) {
                        String url = String.format("%s%s.m3u8?access_token=%s",MEDIA_HLS_BASE_URL, media.getMediaId(), data.getAccessToken());
                        //Intent intent = new Intent( MainActivity.this, PlayerActivity.class);
                        intent.putExtra("media",url);
                        startActivity(intent);
                    }
                    //"error":"invalid_grant","error_description":"Invalid or expired token"
                    else if(data.getStatus().equalsIgnoreCase("error")){
                        Log.e("TOKEN",""+data.getMessage());
                        Toast.makeText(getApplicationContext(),data.getStatus(), Toast.LENGTH_LONG).show();

                    }
                    progress.dismiss();
                }
            });*/

       // }
    }

    private void createActionBar() {


        Typeface light = Typeface.createFromAsset(getAssets(), "fonts/Oswald-Light.otf");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        View view = inflater.inflate(R.layout.actionbar, null);

        menuButton = (ImageButton)view.findViewById(R.id.item_menu);
        helpButton = (ImageButton)view.findViewById(R.id.item_help);

        liveButton = (Button)view.findViewById(R.id.actionbar_live_button);
        vodButton = (Button)view.findViewById(R.id.actionbar_vod_button);
        liveButton.setTypeface(light);
        vodButton.setTypeface(light);

        liveButton.setSelected(true);
        vodButton.setSelected(false);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSlideMenuOpen) {
                    hideSlideMenu();
                }
                else {
                    showSlideMenu();
                }

            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideSlideMenu();

                /*getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new HelpFragment())
                        .addToBackStack(null)
                        .commit();*/
            }
        });


        liveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveButton.setSelected(false);
                liveButton.setBackgroundColor(Color.parseColor("#D46419"));
                vodButton.setSelected(true);
                vodButton.setBackgroundColor(Color.TRANSPARENT);

                loadLiveContent();

                hideSlideMenu();
            }
        });


        vodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveButton.setSelected(false);
                liveButton.setBackgroundColor(Color.TRANSPARENT);
                vodButton.setSelected(true);
                vodButton.setBackgroundColor(Color.parseColor("#D46419"));

                loadVodContent();

                hideSlideMenu();
            }
        });


        actionBar.setCustomView(view,layout);
    }

    private void showSlideMenu() {
        float slideMenuPosition;
        float menuButtonPosition;

        blockOverlay.setVisibility(View.VISIBLE);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        View slideMenuContainer = findViewById(R.id.slide_menu_container);

        // anchoMenu = slideMenuContainer.getWidth();


        //slideMenuPosition = 0;
        //menuButtonPosition = 5;
        int anchoMenu = slideMenuContainer.getMeasuredWidth();
        slideMenuPosition = width - anchoMenu + 31;
        menuButtonPosition = width - anchoMenu + 5;
        isSlideMenuOpen = true;

        ObjectAnimator animatorSlideMenu = ObjectAnimator.ofFloat(slideMenuContainer,View.X,slideMenuPosition);
        //ObjectAnimator animatorMenuButton = ObjectAnimator.ofFloat(menuButton,View.X,menuButtonPosition);
        //ObjectAnimator animatorHelpButton = ObjectAnimator.ofFloat(helpButton,"x",helpButtonPosition);

        AnimatorSet animSet = new AnimatorSet();
        //animSet.playTogether(animatorSlideMenu, animatorMenuButton);
        animSet.play(animatorSlideMenu);
        animSet.start();
    }

    private void hideSlideMenu() {

        float slideMenuPosition;
        float menuButtonPosition;

        blockOverlay.setVisibility(View.GONE);

        View slideMenuContainer = findViewById(R.id.slide_menu_container);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        Log.e("showSlideMenu","ancho :"+width +" alto:"+height);

        //slideMenuPosition = -slideMenuContainer.getMeasuredWidth();// -600
        //menuButtonPosition = 5;

        slideMenuPosition = width ;
        menuButtonPosition = width - menuButton.getWidth();

        isSlideMenuOpen = false;

        ObjectAnimator animatorSlideMenu = ObjectAnimator.ofFloat(slideMenuContainer,View.X,slideMenuPosition);
        //ObjectAnimator animatorMenuButton = ObjectAnimator.ofFloat(menuButton,View.X,menuButtonPosition);
        //ObjectAnimator animatorHelpButton = ObjectAnimator.ofFloat(helpButton,"x",helpButtonPosition);

        AnimatorSet animSet = new AnimatorSet();
        //animSet.playTogether(animatorSlideMenu, animatorMenuButton);
        animSet.play(animatorSlideMenu);
        animSet.start();


    }

    private void loadVodContent() {

        if (vodFragment == null) {
            vodFragment = new VodFragment();
            vodFragment.setVideoDelegate(videoDelegate);
        }

        isOnLiveStream = false;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, vodFragment)
                .commit();
    }

    private void loadVodContent(Filter filter) {

        filteredFragment = new FilteredFragment(filter);
        filteredFragment.setVideoDelegate(videoDelegate);


        isOnLiveStream = false;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, filteredFragment)
                .commit();

        hideSlideMenu();
    }

    private void loadVodContent(List<Media> media){
        filteredFragment = new FilteredFragment(media);
        filteredFragment.setVideoDelegate(videoDelegate);

        isOnLiveStream = false;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, filteredFragment)
                .commit();

        hideSlideMenu();
    }


    private void loadLiveContent() {

        if (liveFragment == null) {
            liveFragment = new LiveFragment();
            liveFragment.setVideoSelectedDelegate(videoDelegate);
        }

        isOnLiveStream = true;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, liveFragment)
                .commit();
    }

    @Override
    public void onBuffer() {

    }

    @Override
    public void onFullscreen(boolean state) {


    }

    @Override
    public void onIdle() {

    }

    @Override
    public void onPlay() {
        intentos = 0;
    }

    @Override
    public void onQualityChange(JWPlayer.QualityLevel currentQuality) {

    }

    @Override
    public void onQualityLevels(JWPlayer.QualityLevel[] levels) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AQUtility.cleanCacheAsync(this);
    }

    @Override
    public void onError(String message) {
        if(intentos< MAX){
            //.play();
            intentos++;
        }
        Toast.makeText(getApplicationContext(),"Frank la cagamos ", Toast.LENGTH_LONG).show();
    }
}
