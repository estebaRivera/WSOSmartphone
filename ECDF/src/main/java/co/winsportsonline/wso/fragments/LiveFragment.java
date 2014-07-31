package co.winsportsonline.wso.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import co.winsportsonline.wso.R;
import co.winsportsonline.wso.datamodel.LiveStream;
import co.winsportsonline.wso.datamodel.LiveStreamSchedule;
import co.winsportsonline.wso.datamodel.Media;
import co.winsportsonline.wso.delegates.ImageChooserDelegate;
import co.winsportsonline.wso.delegates.VideoDelegate;
import co.winsportsonline.wso.dialogs.MessageDialog;
import co.winsportsonline.wso.dialogs.PostDialog;
import co.winsportsonline.wso.services.ServiceManager;
import co.winsportsonline.wso.utils.AnimationManager;

/**
 * Created by Franklin  Cruz on 17-02-14.
 */
public class LiveFragment extends Fragment {

    public static final String LIVE_LEFT_HEADER_URL_FORMATSTR = "http://winsportsonline.com/assets/img/event/large/large-";
    public static final String LIVE_RIGHT_HEADER_URL_FORMATSTR = "http://winsportsonline.com/assets/img/event/large/large-";

    public static final String LIVE_THUMBNAIL_VISIT_IMAGE_URL = "http://winsportsonline.com/assets/img/event/small/small-";
    public static final String LIVE_THUMBNAIL_LOCAL_IMAGE_URL ="http://winsportsonline.com/assets/img/event/small/small-";

    public static final String LIVE_LOCAL_TEAM_URL_FORMATSTR = "http://winsportsonline.com/assets/img/event/large/large-";
    public static final String LIVE_VISIT_TEAM_URL_FORMATSTR = "http://winsportsonline.com/assets/img/event/large/large-";

    private LinearLayout nextShowContainer;

    private final int LEFT_SCROLL = 1;
    private final int RIGHT_SCROLL = 2;
    private final int NONE_SCROLL = 0;

    private int numCell = 0 ;
    private int count = 0;
    private int totalCell;
    private final int LIMITS = 20;

    private List<LiveStreamSchedule> liveStreamSchedules = new ArrayList<LiveStreamSchedule>();
    private LiveStreamSchedule nextShow;

    private LiveStreamSchedule mediaCorreo = new LiveStreamSchedule();

    private View rootView;
    private View prevShow = null;
    private View prevShare = null;
    private View prevShowLive = null;
    private View prevShareLive = null;
    private HorizontalScrollView slider;
    private LinearLayout containerSlider;

    //private LinearLayout

    private View showLive;
    private View shareLive;
    private View shareGone;
    private View showLiveSeconds;
    private View shareLiveSeconds;
    private View shareGoneSeconds;
    private RelativeLayout show_1;
    private RelativeLayout show_2;
    private ImageView btnShare;
    private ImageView btnShareSeconds;
    private HashMap<String,View> hashMap = new HashMap<String,View>();
    private HashMap<String,View> viewRemove = new HashMap<String,View>();
    private Resources res;
    private Drawable d;
    private TimerTask timerTask;
    private Timer timer;

    private long dateEndShowActual1;
    private long dateEndShowActual2;

    private long dateStartShowActual1;
    private long dateStartShowActual2;

    private TextView page2_1;
    private TextView page2_2;
    private TextView timeIndicator1;
    private TextView timeIndicator2;
    private boolean leftScroll = false;
    private boolean rightScroll = true;
    private boolean isShareShow = true;
    private boolean isShareShowSeconds = true;
    private boolean twoShowLive = false;
    private int loadedSources = 0;


    private VideoDelegate videoDelegate;

    public void setVideoSelectedDelegate(VideoDelegate delegate) {
        this.videoDelegate = delegate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_live, container, false);

        res = getResources();
        d = res.getDrawable(R.drawable.share_icon_b);

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        show_1 = (RelativeLayout) rootView.findViewById(R.id.show_1);
        show_2 = (RelativeLayout) rootView.findViewById(R.id.show_2);

        sizeScreen();

        slider = (HorizontalScrollView) rootView.findViewById(R.id.slider_live);
        containerSlider = (LinearLayout)rootView.findViewById(R.id.container_slider);
        final HorizontalScrollView scrollView = (HorizontalScrollView) rootView.findViewById(R.id.scroll);
        //moveScroll();
        //show_2.setVisibility(View.GONE);
        shareLive = rootView.findViewById(R.id.share_container_live);
        shareLiveSeconds = rootView.findViewById(R.id.share_container_live);

        showLive = show_1.findViewById(R.id.top_container);
        btnShare = (ImageView) show_1.findViewById(R.id.btn_share_);
        shareGone = show_1.findViewById(R.id.share_gone);

        showLiveSeconds = show_2.findViewById(R.id.top_container);
        btnShareSeconds = (ImageView) show_2.findViewById(R.id.btn_share_);
        shareGoneSeconds = show_2.findViewById(R.id.share_gone);


        page2_1 = (TextView) show_2.findViewById(R.id.slide_1);
        page2_2 = (TextView) show_2.findViewById(R.id.slide_2);

        // Colores
        //Naranjo   FF6002
        //Gris      383838

        page2_1.setBackgroundColor(Color.parseColor("#383838"));
        page2_2.setBackgroundColor(Color.parseColor("#FF6002"));

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isShareShow){
                    isShareShow = false;
                    if(twoShowLive)
                        timer.cancel();
                    shareGone.setVisibility(View.VISIBLE);
                    hideShareLive(shareLive, showLive);
                }
                else{
                    isShareShow = true;
                    if(twoShowLive)
                        moveScroll();
                    shareGone.setVisibility(View.GONE);
                    displayShareLive(shareLive,showLive);
                }
            }
        });
        shareGone.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShareShow){
                    isShareShow = false;
                    if(twoShowLive)
                        timer.cancel();
                    shareGone.setVisibility(View.VISIBLE);
                    hideShareLive(shareLive, showLive);
                }
                else{
                    isShareShow = true;
                    if(twoShowLive)
                        moveScroll();
                    shareGone.setVisibility(View.GONE);
                    displayShareLive(shareLive,showLive);
                }
            }
        });
        ////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////
        btnShareSeconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isShareShowSeconds){
                    isShareShowSeconds = false;
                    if(twoShowLive)
                        timer.cancel();
                    shareGoneSeconds.setVisibility(View.VISIBLE);
                    hideShareLive(shareLiveSeconds, showLiveSeconds);
                }
                else{
                    isShareShowSeconds = true;
                    if(twoShowLive)
                        moveScroll();
                    shareGoneSeconds.setVisibility(View.GONE);
                    displayShareLive(shareLiveSeconds,showLiveSeconds);
                }
            }
        });
        shareGoneSeconds.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShareShowSeconds){
                    isShareShowSeconds = false;
                    timer.cancel();
                    shareGoneSeconds.setVisibility(View.VISIBLE);
                    hideShareLive(shareLiveSeconds, showLiveSeconds);
                }
                else{
                    isShareShowSeconds = true;
                    moveScroll();
                    shareGoneSeconds.setVisibility(View.GONE);
                    displayShareLive(shareLiveSeconds,showLiveSeconds);
                }
            }
        });

        nextShowContainer = (LinearLayout) rootView.findViewById(R.id.container_next_shows);

        final View commingShowContainer = rootView.findViewById(R.id.comming_show_container);

        final LayoutInflater inflaterFinal = inflater;
        final ServiceManager serviceManager = new ServiceManager(getActivity());
        serviceManager.loadLiveStreamList(new ServiceManager.DataLoadedHandler<LiveStream>() {
            @Override
            public void loaded(List<LiveStream> data) {

                loadedSources = 0;
                final int totalSources = data.size();

                liveStreamSchedules.clear();
                for (int i = 0; i < data.size(); ++i) {

                    serviceManager.loadLiveStreamSchedule(data.get(i), new ServiceManager.DataLoadedHandler<LiveStreamSchedule>() {
                        @Override
                        public void loaded(List<LiveStreamSchedule> data) {


                                liveStreamSchedules.addAll(data);
                                totalCell = liveStreamSchedules.size();
                                Collections.sort(liveStreamSchedules, new Comparator<LiveStreamSchedule>() {
                                    @Override
                                    public int compare(LiveStreamSchedule lhs, LiveStreamSchedule rhs) {
                                        if(lhs.getStartDate().getTime() > rhs.getStartDate().getTime()) {
                                            return 1;
                                        }
                                        else if(lhs.getStartDate().getTime() < rhs.getStartDate().getTime()) {
                                            return -1;
                                        }
                                        else {
                                            return 0;
                                        }
                                    }
                                });

                                Resources res = getResources();
                                ImageView image = (ImageView) rootView.findViewById(R.id.image_programa);
                                TextView equipo1 = (TextView) rootView.findViewById(R.id.label_title);
                                TextView equipo2 = (TextView) rootView.findViewById(R.id.label_time);
                                image.setBackgroundColor(Color.TRANSPARENT);
                                equipo1.setText("");
                                equipo2.setText("");

                                ++loadedSources;
                                if (liveStreamSchedules.size() > 0 && loadedSources == totalSources) {

                                    List<LiveStreamSchedule> matchLiveList = countMatchLive();
                                    nextShow = liveStreamSchedules.get(0);
                                    Date now = new Date();

                                    if(videoDelegate != null) {
                                        videoDelegate.onLiveShowBegin(liveStreamSchedules.get(0));
                                    }

                                    if(matchLiveList.size()> 1){
                                        twoShowLive = true;
                                        dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                        dateEndShowActual2 = liveStreamSchedules.get(1).getEndDate().getTime();

                                        dateStartShowActual1 = liveStreamSchedules.get(0).getStartDate().getTime();
                                        dateStartShowActual2 = liveStreamSchedules.get(1).getStartDate().getTime();

                                        displayNextShow(liveStreamSchedules.get(0));
                                        diplayTwoShowLive(liveStreamSchedules.get(1));
                                        LinearLayout l = (LinearLayout) show_1.findViewById(R.id.indice_slide);
                                        l.setVisibility(View.VISIBLE);
                                        moveScroll();

                                    }
                                    else{
                                        show_2.setVisibility(View.GONE);
                                        dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                        dateStartShowActual1 = liveStreamSchedules.get(0).getStartDate().getTime();
                                        displayNextShow(liveStreamSchedules.get(0));
                                        LinearLayout l = (LinearLayout) show_1.findViewById(R.id.indice_slide);
                                        l.setVisibility(View.GONE);
                                    }

                                    nextShowContainer.removeAllViews();

                                    count++;
                                    //for (numCell = 1 ; numCell < (LIMITS * count) && numCell < totalCell; ++numCell) {
                                    for (numCell = 1 ; numCell < totalCell; ++numCell) {
                                        createLiveMediaCell(liveStreamSchedules.get(numCell));
                                    }
                                    TimerUpdate timerUpdate = new TimerUpdate();
                                    timerUpdate.execute();
                                }
                            progress.dismiss();
                        }
                       @Override
                        public void error(String error) {
                            super.error(error);
                            loadedSources++;
                            if (liveStreamSchedules.size() > 0 && loadedSources == totalSources) {

                                List<LiveStreamSchedule> matchLiveList = countMatchLive();

                                nextShow = liveStreamSchedules.get(0);

                                Date now = new Date();

                                if(videoDelegate != null) {
                                    videoDelegate.onLiveShowBegin(liveStreamSchedules.get(0));
                                }
                                if(matchLiveList.size()> 1){
                                    twoShowLive = true;
                                    displayNextShow(liveStreamSchedules.get(0));
                                    diplayTwoShowLive(liveStreamSchedules.get(1));

                                    dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                    dateEndShowActual2 = liveStreamSchedules.get(1).getEndDate().getTime();

                                    dateStartShowActual1 = liveStreamSchedules.get(0).getStartDate().getTime();
                                    dateStartShowActual2 = liveStreamSchedules.get(1).getStartDate().getTime();

                                }
                                else{
                                    show_2.setVisibility(View.GONE);
                                    displayNextShow(liveStreamSchedules.get(0));
                                    dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                    dateStartShowActual1 = liveStreamSchedules.get(0).getStartDate().getTime();

                                    LinearLayout l = (LinearLayout) show_1.findViewById(R.id.indice_slide);
                                    l.setVisibility(View.GONE);
                                }
                                nextShowContainer.removeAllViews();
                                Log.e("Error",""+totalCell);
                                for (numCell = 1 ; numCell < (LIMITS * count) &&  numCell < totalCell; ++numCell) {
                                    createLiveMediaCell(liveStreamSchedules.get(numCell));
                                }
                                TimerUpdate timerUpdate = new TimerUpdate();
                                timerUpdate.execute();
                            }
                            progress.dismiss();
                        }
                    });
                }
            }
        });
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (managerScroll(scrollView.getScrollX())){

                    case LEFT_SCROLL:   leftScroll = false;
                                        rightScroll = true;
                                        updateView(); break;

                    case RIGHT_SCROLL: leftScroll = true;
                                       rightScroll = false;
                                       updateView(); break;

                    case NONE_SCROLL: //Log.e("Scroll", "NONE");break;
                        break;

                }

                return false;
            }
        });
        return rootView;
    }
    private int widthScroll(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int width = (int)((size.x * 85)/100);

        return (numCell-2)* width;
    }

    private List<LiveStreamSchedule> countMatchLive() {
        List<LiveStreamSchedule> list = new ArrayList<LiveStreamSchedule>();
        Long now = new Date().getTime();
        for (LiveStreamSchedule lss : liveStreamSchedules) {
            if (lss.getStartDate().getTime() < now && lss.getEndDate().getTime() > now) {
                list.add(lss);
            }
        }
        return list;
    }
    private void updateView(){
        int top;
        int bottom;
        if(numCell-4 > 0 ){
            bottom = numCell - 4;
        }
        else{
            int count = numCell;
            bottom = 0;
            while(count == 0){
                ++bottom;
                --count;
            }
        }
        Log.e("Bottom","valor :"+bottom);
        if(numCell + 4 < totalCell || numCell + 4 == totalCell){
            top = numCell + 4;
        }
        else{
            int count = numCell;
            top = numCell;
            while(count == totalCell - 1){
                ++top;
                --count;
            }
        }
        for(int i = 0; i < totalCell; i++){
            if((i > bottom || i == bottom) && ( i < top || i == top) ){
                //Log.e("addView","Celda actual"+numCell);
                if(!hashMap.containsKey(""+i)){
                    if(viewRemove.containsKey(""+i)){
                        hashMap.put(""+i,viewRemove.get(""+i));
                        viewRemove.remove(""+i);
                    }
                }
            }
            else{
                if(!viewRemove.containsKey(""+i)){
                    viewRemove.put(""+i,hashMap.get(""+i));
                    hashMap.remove(""+i);
                }
            }
        }
    }
    private void removeView(){

        for(int i = 0; i < totalCell; i++){
            if(i < (numCell-3) && i > (numCell+3) ){
                //Log.e("RemoveView",""+numCell);
                //nextShowContainer.removeView(hashMap.get(""+i));

            }
        }
    }
    private void addView(){
        for(int i = 0; i < totalCell; i++){
            if(i > (numCell-4) && i < (numCell+4) ){
                //Log.e("addView",""+hashMap.get("+i").getTag());
                 //nextShowContainer.addView(hashMap.get(""+i),i);
            }
        }
    }
    private int managerScroll(int scrollCoordinate){

        if((scrollCoordinate > widthScroll()) && rightScroll == true){
            //count++;
            if(numCell< totalCell || numCell == totalCell){
                Log.e("Scroll","RIGHT");
                return RIGHT_SCROLL;
            }
        }
        if((scrollCoordinate < 120 && scrollCoordinate > 50)&& leftScroll == true){
            //count++;
            if(numCell> 0){
                Log.e("Scroll","LEFT");
                return LEFT_SCROLL;
            }
        }
        return NONE_SCROLL;
    }
    private void displayNextShow(final LiveStreamSchedule media) {

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");

        //final RelativeLayout rootView = (RelativeLayout)getLayoutInflater(null).inflate(R.layout.show_live, null);

        TextView titleTextView = (TextView)show_1.findViewById(R.id.label_title);
        TextView timeTextView = (TextView) show_1.findViewById(R.id.label_time);
        timeIndicator1 = (TextView)show_1.findViewById(R.id.label_delta);
        TextView proximo = (TextView) rootView.findViewById(R.id.proximos_encuetros);

        ImageView arriba = (ImageView) show_1.findViewById(R.id.margen_arriba);
        ImageView abajo = (ImageView) show_1.findViewById(R.id.margen_abajo);

        ImageView play = (ImageView) show_1.findViewById(R.id.button_play_1);

        proximo.setTypeface(light);
        proximo.setText("A CONTINUACIÓN");

        titleTextView.setText(media.getName());
        titleTextView.setTypeface(bold);

        Long t = media.getStartDate().getTime();
        Long now = new Date().getTime();
        Long delta = now - t ;
        int min = (int)(delta / 60000);
        int horas = 0 ,minutos = 0;
        boolean formato = false;
        if(min > 59){
            formato = true;
            horas = min / 60;
            minutos = min % 60;
        }

        Log.e("Hora",".->"+min+" Min");
        Log.e("Hora","-->"+horas+":"+minutos);

        if(!formato){
            timeIndicator1.setText("Comenzó hace " + min + " Min");
        }
        else{
            if(minutos > 10){
                timeIndicator1.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
            }
            else{
                timeIndicator1.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
            }
        }

        timeIndicator1.setTypeface(light);

        arriba.setBackgroundColor(Color.WHITE);
        abajo.setBackgroundColor(Color.WHITE);

        DateFormat df =  new SimpleDateFormat("HH:mm' Hrs.'", Locale.ENGLISH);

        timeTextView.setTypeface(light);
        timeTextView.setText(df.format(media.getStartDate()));

        final AQuery aq = new AQuery(rootView);

        String[] splited = null;

        if(media.getCode()!=null){

            splited = media.getCode().split("_vs_");
            aq.id(R.id.image_programa).image(LIVE_LOCAL_TEAM_URL_FORMATSTR+""+media.getCode()+".jpg");

        }
        else{
            if(media.getName() != null)
                splited = media.getName().split(" v/s ");
        }

        View facebookButton = show_1.findViewById(R.id.facebook_button);

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", media.getName());

                PostDialog postDialog = new PostDialog(text, media.getName(), String.format(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode()
                        +".jpg", LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode()+".jpg"),
                        PostDialog.FACEBOOK_SHARE);
                postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
            }
        });

        View twitterButton = show_1.findViewById(R.id.twitter_button);

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", media.getName());

                PostDialog postDialog = new PostDialog(text, media.getName(), "", PostDialog.TWITTER_SHARE);
                postDialog.show(getActivity().getSupportFragmentManager(), "dialog");

            }
        });


        View emailButton = show_1.findViewById(R.id.mail_button);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (videoDelegate != null) {

                    String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", media.getName());

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");

                    text = text +" www.winsportsonline.com";

                    i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                    i.putExtra(Intent.EXTRA_TEXT, text);

                    Bitmap image = aq.getCachedImage(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode() + ".jpg");

                    File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                    try {
                        image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));
                        if (image != null) {
                            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                        }
                        getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        Log.e("Error sdf","--> "+e.getMessage());
                    }
                }
            }
        });

        View clipboardButton = show_1.findViewById(R.id.clipboard_button);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Win Sports", "http://www.winsportsonline.com"));

                Toast.makeText(getActivity(), "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (videoDelegate != null) {

                    SharedPreferences sp = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
                    boolean permission = false;
                    List<String> countries = null;
                    try{
                        countries = media.getAccessRules().getGeo().getCountries();
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.e("COUNTRIES","Error: "+e.getMessage());
                    }
                    if(countries != null && countries.size() > 0){
                        for(String country : countries){
                            if(country.equalsIgnoreCase(sp.getString("country_code",""))){
                                permission = true;
                            }
                        }
                    }else{
                        permission = true;
                    }
                    if(permission){
                        videoDelegate.onVideoSelected(media);
                    }else{
                        MessageDialog messageDialog = new MessageDialog("ESTE CONTENIDO NO ESTÁ DISPONIBLE PARA TU UBICACIÓN.");
                        messageDialog.show(getActivity().getFragmentManager(),"dialog");
                    }
                }
            }
        });

    }

    private void diplayTwoShowLive(final LiveStreamSchedule media){

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");

        TextView titleTextView = (TextView)show_2.findViewById(R.id.label_title);
        TextView timeTextView = (TextView) show_2.findViewById(R.id.label_time);
        timeIndicator2 = (TextView)show_2.findViewById(R.id.label_delta);


        ImageView arriba = (ImageView) show_2.findViewById(R.id.margen_arriba);
        ImageView abajo = (ImageView) show_2.findViewById(R.id.margen_abajo);

        ImageView play = (ImageView) show_2.findViewById(R.id.button_play_1);


        titleTextView.setText(media.getName());
        titleTextView.setTypeface(bold);

        Long t = media.getStartDate().getTime();
        Long now = new Date().getTime();
        Long delta = now - t ;
        int min = (int)(delta / 60000);

        int horas = 0 ,minutos = 0;
        boolean formato = false;
        if(min > 59){
            formato = true;
            horas = min / 60;
            minutos = min % 60;
        }

        Log.e("Hora",".->"+min+" Min");
        Log.e("Hora","-->"+horas+":"+minutos);

        if(!formato){
            timeIndicator2.setText("Comenzó hace "+min+" Min");
        }
        else{
            if(minutos > 10){
                timeIndicator2.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
            }
            else{
                timeIndicator2.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
            }
        }

        timeIndicator2.setTypeface(light);

        arriba.setBackgroundColor(Color.WHITE);
        abajo.setBackgroundColor(Color.WHITE);

        DateFormat df =  new SimpleDateFormat("HH:mm' Hrs.'", Locale.ENGLISH);

        timeTextView.setTypeface(light);
        timeTextView.setText(df.format(media.getStartDate()));

        final AQuery aq = new AQuery(rootView);

        String[] splited = null;

        if(media.getCode()!=null){

            ImageView image = (ImageView) show_2.findViewById(R.id.image_programa);
            splited = media.getCode().split("_vs_");
            aq.id(image).image(LIVE_LOCAL_TEAM_URL_FORMATSTR+""+media.getCode()+".jpg");

        }
        else{
            if(media.getName() != null)
                splited = media.getName().split(" v/s ");
        }

        View facebookButton = show_2.findViewById(R.id.facebook_button);

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", media.getName());

                PostDialog postDialog = new PostDialog(text, media.getName(), String.format(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode()
                        +".jpg", LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode()+".jpg"),
                        PostDialog.FACEBOOK_SHARE);
                postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
            }
        });

        View twitterButton = show_2.findViewById(R.id.twitter_button);

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", media.getName());

                PostDialog postDialog = new PostDialog(text, media.getName(), "", PostDialog.TWITTER_SHARE);
                postDialog.show(getActivity().getSupportFragmentManager(), "dialog");

            }
        });


        View emailButton = show_2.findViewById(R.id.mail_button);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (videoDelegate != null) {

                    String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", media.getName());

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");

                    text = text +" www.winsportsonline.com";

                    i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                    i.putExtra(Intent.EXTRA_TEXT, text);

                    Bitmap image = aq.getCachedImage(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode() + ".jpg");

                    File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                    try {
                        image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));
                        if (image != null) {
                            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                        }
                        getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        View clipboardButton = show_2.findViewById(R.id.clipboard_button);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Win Sports", "http://www.winsportsonline.com"));

                Toast.makeText(getActivity(), "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (videoDelegate != null) {

                    SharedPreferences sp = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
                    boolean permission = false;
                    List<String> countries = null;
                    try{
                        countries = media.getAccessRules().getGeo().getCountries();
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.e("COUNTRIES","Error: "+e.getMessage());
                    }
                    if(countries != null && countries.size() > 0){
                        for(String country : countries){
                            if(country.equalsIgnoreCase(sp.getString("country_code",""))){
                                permission = true;
                            }
                        }
                    }else{
                        permission = true;
                    }
                    if(permission){
                        videoDelegate.onVideoSelected(media);
                    }else{
                        MessageDialog messageDialog = new MessageDialog("ESTE CONTENIDO NO ESTÁ DISPONIBLE PARA TU UBICACIÓN.");
                        messageDialog.show(getActivity().getFragmentManager(),"dialog");
                    }
                }
            }
        });
    }


    private void createLiveMediaCell(final LiveStreamSchedule media) {

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");

        final RelativeLayout v = (RelativeLayout)getLayoutInflater(null).inflate(R.layout.live_show_cell, null);
        final AQuery aq = new AQuery(v);

        TextView title = (TextView)v.findViewById(R.id.title_label);
        title.setTypeface(bold);

        TextView t = (TextView) v.findViewById(R.id.share_text);
        t.setTypeface(light);

        if(media.getName()!= null)
            if(media.getName().length()<23)
                title.setText(media.getName().toUpperCase());
            else
                title.setText(media.getName().substring(0,22).toUpperCase()+"...");
        else
            title.setText("WIN SPORTS ONLINE");

        TextView time = (TextView)v.findViewById(R.id.time_label);

        String format = "MMM dd, HH:mm 'Hrs'";
        DateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);

        time.setText(df.format(media.getStartDate()));
        time.setTypeface(light);

        final String[] splited;

        if(media.getCode()!= null) {
            splited = media.getCode().split("_vs_");

            View imageFullShare = v.findViewById(R.id.share_image_full);
            imageFullShare.setVisibility(View.VISIBLE);

            View imageFull = v.findViewById(R.id.image_full);
            imageFull.setVisibility(View.VISIBLE);

            aq.id(R.id.image_full).image(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode()+".jpg");
            aq.id(R.id.share_image_full).image(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode()+".jpg");

            ImageView image = (ImageView) v.findViewById(R.id.image_full);
            ImageView image2 = (ImageView) v.findViewById(R.id.share_image_full);

            image.setClickable(false);
            image.setFocusable(false);
            image.setEnabled(false);

            image2.setClickable(false);
            image2.setFocusable(false);
            image2.setEnabled(false);

        }
        else{

            splited = media.getName().split(" vs ");
        }

        if(splited != null){}

            if (splited.length > 1) {

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.VISIBLE);

                if(!media.getCode().equalsIgnoreCase("default")){
                    View vsLabel = v.findViewById(R.id.vs_label);
                    vsLabel.setVisibility(View.VISIBLE);
                }
            }

            View splitView = v.findViewById(R.id.split_image_container);
            splitView.setVisibility(View.GONE);

            View splitViewShare = v.findViewById(R.id.share_split_image_container);
            splitViewShare.setVisibility(View.GONE);


            final View show = v.findViewById(R.id.show_container);
            final View share = v.findViewById(R.id.share_container);
            final DateFormat dfS = new SimpleDateFormat("HH:mm 'HRS el' dd 'de' MMM");

            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Do nothing!!!
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    hideShare(share,show);
                    prevShare = null;
                    prevShow = null;

                }
            });

            ImageButton shareButton = (ImageButton)v.findViewById(R.id.share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View sender) {

                    if(prevShare != null && prevShow != null) {
                        hideShare(prevShare, prevShow);
                    }

                    displayShare(share,show);
                    prevShare = share;
                    prevShow = show;

                }
            });

            View facebookButton = v.findViewById(R.id.facebook_button);
            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String text = String.format("%s a las %s por http://goo.gl/IpRqp3", media.getName(), dfS.format(media.getStartDate()));

                    PostDialog postDialog = new PostDialog(text, media.getName(), String.format(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode()+".jpg", splited[0]),
                            PostDialog.FACEBOOK_SHARE);
                    postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            });

            View twitterButton = v.findViewById(R.id.twitter_button);

            twitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(splited.length > 1) {
                        String text = String.format("%s a las %s por http://goo.gl/IpRqp3", media.getName(), dfS.format(media.getStartDate()));

                        PostDialog postDialog = new PostDialog(text, media.getName(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("%s a las %s por http://goo.gl/IpRqp3H", media.getName(), dfS.format(media.getStartDate()));

                        PostDialog postDialog = new PostDialog(text, media.getName(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }

                }
            });


            View emailButton = v.findViewById(R.id.mail_button);

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (videoDelegate != null) {

                        String text = String.format("%s a las %s por http://goo.gl/IpRqp3", media.getName(), dfS.format(media.getStartDate()));

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        text = text +" www.winsportsonline.com";
                        i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                        i.putExtra(Intent.EXTRA_TEXT, text);

                        //mediaCorreo = media;
                        //CargaImagenes nuevaTarea = new CargaImagenes();
                        //nuevaTarea.execute(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode() + ".jpg");
                        //Log.e("Esteban","Rivera");
                        Bitmap image = aq.getCachedImage(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode() + ".jpg");

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {
                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));
                            if (image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }
                            getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            CargaImagenes nuevaTarea = new CargaImagenes();
                            nuevaTarea.execute(LIVE_LEFT_HEADER_URL_FORMATSTR + media.getCode() + ".jpg");
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            View clipboardButton = v.findViewById(R.id.clipboard_button);
            clipboardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Win Sports", "http://www.winsportsonline.com"));

                    Toast.makeText(getActivity(), "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show();
                }
            });

            final ImageButton reminderButton = (ImageButton)v.findViewById(R.id.remind_button);
            SharedPreferences prefs = getActivity().getSharedPreferences("com.winsportsonline", Context.MODE_PRIVATE);
            if(prefs.getBoolean("reminder_" + media.getEventId(), false)) {
                reminderButton.setAlpha((float)0.4);
                reminderButton.setEnabled(false);

            }

            reminderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(createReminder(media)){
                        AnimationManager.press(reminderButton);
                        reminderButton.setEnabled(false);
                    }
                }
            });

            View view = v;
            view.setTag(""+numCell);
            nextShowContainer.addView(v);

            if(!hashMap.containsKey(""+numCell)){
                hashMap.put(""+numCell,view);
            }


            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
            Display display = getActivity().getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            int width = (int)((size.x * 85)/100);
            int height = (int) (width * 220)/ 360;

            params.setMargins(5, 20, 5, 5);
            params.height = height;
            params.width = width;
            v.setClipChildren(false);
            v.setLayoutParams(params);

            v.setEnabled(false);
            v.setClickable(false);
            v.setFocusable(false);
            v.setSoundEffectsEnabled(false);

        adapterScreen800(v);

    }

    public void adapterScreen800(View v){
        Point outSize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(outSize);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
        int height = outSize.y;
        if(height <= 800){

            TextView title = (TextView)v.findViewById(R.id.title_label);
            title.setTextSize(14);

            TextView t = (TextView) v.findViewById(R.id.share_text);
            t.setTextSize(11);

            ImageButton recordar = (ImageButton) v.findViewById(R.id.remind_button);
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) recordar.getLayoutParams();

            ImageButton compartir = (ImageButton) v.findViewById(R.id.share_button);
            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) compartir.getLayoutParams();

            params1.height = 34;
            params2.height = 34;

            params1.width = 34;
            params2.width = 34;

            recordar.setLayoutParams(params1);
            compartir.setLayoutParams(params2);

            int widthParams = ((outSize.x * 85)/100);
            int heightParams = (widthParams * 220)/ 440; // 360
            params.setMargins(5, 20, 5, 5);
            params.height = heightParams;
            params.width = widthParams;

            RelativeLayout infoContainer = (RelativeLayout) v.findViewById(R.id.info_layout);
            RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) infoContainer.getLayoutParams();
            params3.height = 58;
            infoContainer.setLayoutParams(params3);
            v.setLayoutParams(params);
        }
    }

    private void sizeScreen(){

        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) show_1.getLayoutParams();
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) show_2.getLayoutParams();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x ;

        params1.width = width;
        params2.width = width;

        show_1.setLayoutParams(params1);
        show_2.setLayoutParams(params2);
    }

    private void moveScroll(){

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Colores
        //Naranjo   FF6002
        //Gris      383838

        final int width = size.x ;
        final int[] count = {0};
        timerTask = new TimerTask()
        {
            public void run()
            {
                int posicion =  slider.getScrollY();

                if(count[0] % 2 == 0){
                    slider.smoothScrollBy(width,0);
                }
                else{
                    slider.smoothScrollBy(-width,0);
                }
                count[0]++;

            }
        };

        // Aquí se pone en marcha el timer cada segundo.
        timer = new Timer();
        // Dentro de 0 milisegundos avísame cada 8000 milisegundos
        timer.scheduleAtFixedRate(timerTask, 8000, 8000);

    }
    private void displayShare(View share, View show) {

        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(share, "y",share.getMeasuredHeight(), 0.0f);
        rotationShow.setDuration(500);

        show.setPivotY(0);
        show.setPivotX(show.getMeasuredWidth() / 2.0f);
        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(show, "y", 0.0f, -show.getMeasuredHeight());
        rotationShare.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.start();
    }

    private void hideShare(View share, View show) {
        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(show, "y",-show.getMeasuredHeight(), 0.0f);
        rotationShow.setDuration(500);

        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(share, "y", 0.0f, share.getMeasuredHeight());
        rotationShare.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.start();
    }

    private void displayShareLive(final View share, View show) {
        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(share, "scaleY",1, 0);
        rotationShare.setDuration(500);
        show.setPivotY(0);
        show.setPivotX(show.getMeasuredWidth() / 2.0f);

        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(show, "y", -140, 0);
        rotationShow.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        //share.setVisibility(View.GONE);
        //animatorSet.playTogether(rotationShow, rotationShare);
        //animatorSet.play(rotationShare);
        animatorSet.play(rotationShow);
        animatorSet.start();
    }

    private void hideShareLive(final View share, View show) {
        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(share, "scaleY",0, 1);
        rotationShare.setDuration(500);
        show.setPivotY(0);
        show.setPivotX(show.getMeasuredWidth() / 2.0f);

        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(show, "y", 0.0f, -140);
        rotationShow.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        share.setVisibility(View.VISIBLE);
        //animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.play(rotationShow);
        //animatorSet.play(rotationShare);
        animatorSet.start();

    }

    public void refresh(){


        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        Log.e("Refresh","Refresh");
        final ServiceManager serviceManager = new ServiceManager(getActivity());
        serviceManager.loadLiveStreamList(new ServiceManager.DataLoadedHandler<LiveStream>() {
            @Override
            public void loaded(List<LiveStream> data) {

                loadedSources = 0;
                final int totalSources = data.size();

                liveStreamSchedules.clear();
                for (int i = 0; i < data.size(); ++i) {

                    serviceManager.loadLiveStreamSchedule(data.get(i), new ServiceManager.DataLoadedHandler<LiveStreamSchedule>() {
                        @Override
                        public void loaded(List<LiveStreamSchedule> data) {


                            liveStreamSchedules.addAll(data);
                            totalCell = liveStreamSchedules.size();
                            Collections.sort(liveStreamSchedules, new Comparator<LiveStreamSchedule>() {
                                @Override
                                public int compare(LiveStreamSchedule lhs, LiveStreamSchedule rhs) {
                                    if(lhs.getStartDate().getTime() > rhs.getStartDate().getTime()) {
                                        return 1;
                                    }
                                    else if(lhs.getStartDate().getTime() < rhs.getStartDate().getTime()) {
                                        return -1;
                                    }
                                    else {
                                        return 0;
                                    }
                                }
                            });

                            Resources res = getResources();
                            ImageView image = (ImageView) rootView.findViewById(R.id.image_programa);
                            TextView equipo1 = (TextView) rootView.findViewById(R.id.label_title);
                            TextView equipo2 = (TextView) rootView.findViewById(R.id.label_time);
                            image.setBackgroundColor(Color.TRANSPARENT);
                            equipo1.setText("");
                            equipo2.setText("");

                            ++loadedSources;
                            if (liveStreamSchedules.size() > 0 && loadedSources == totalSources) {

                                List<LiveStreamSchedule> matchLiveList = countMatchLive();
                                nextShow = liveStreamSchedules.get(0);
                                Date now = new Date();

                                if(videoDelegate != null) {
                                    videoDelegate.onLiveShowBegin(liveStreamSchedules.get(0));
                                }

                                if(matchLiveList.size()> 1){
                                    twoShowLive = true;

                                    dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                    dateEndShowActual2 = liveStreamSchedules.get(1).getEndDate().getTime();

                                    displayNextShow(liveStreamSchedules.get(0));
                                    diplayTwoShowLive(liveStreamSchedules.get(1));
                                    LinearLayout l = (LinearLayout) show_1.findViewById(R.id.indice_slide);
                                    l.setVisibility(View.VISIBLE);
                                    moveScroll();

                                }
                                else{
                                    show_2.setVisibility(View.GONE);
                                    dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                    displayNextShow(liveStreamSchedules.get(0));
                                    LinearLayout l = (LinearLayout) show_1.findViewById(R.id.indice_slide);
                                    l.setVisibility(View.GONE);
                                }

                                nextShowContainer.removeAllViews();
                                count++;
                                for (numCell = 1 ; numCell < totalCell; ++numCell) {
                                    createLiveMediaCell(liveStreamSchedules.get(numCell));
                                }
                            }
                            progress.dismiss();
                        }
                        @Override
                        public void error(String error) {
                            super.error(error);
                            loadedSources++;
                            if (liveStreamSchedules.size() > 0 && loadedSources == totalSources) {

                                List<LiveStreamSchedule> matchLiveList = countMatchLive();

                                nextShow = liveStreamSchedules.get(0);

                                if(videoDelegate != null) {
                                    videoDelegate.onLiveShowBegin(liveStreamSchedules.get(0));
                                }
                                if(matchLiveList.size()> 1){

                                    twoShowLive = true;
                                    dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                    dateEndShowActual2 = liveStreamSchedules.get(1).getEndDate().getTime();

                                    displayNextShow(liveStreamSchedules.get(0));
                                    diplayTwoShowLive(liveStreamSchedules.get(1));

                                }
                                else{
                                    show_2.setVisibility(View.GONE);
                                    displayNextShow(liveStreamSchedules.get(0));
                                    dateEndShowActual1 = liveStreamSchedules.get(0).getEndDate().getTime();
                                    LinearLayout l = (LinearLayout) show_1.findViewById(R.id.indice_slide);
                                    l.setVisibility(View.GONE);
                                }
                                nextShowContainer.removeAllViews();
                                Log.e("Error",""+totalCell);
                                for (numCell = 1 ; numCell < (LIMITS * count) &&  numCell < totalCell; ++numCell) {
                                    createLiveMediaCell(liveStreamSchedules.get(numCell));
                                }
                            }
                            progress.dismiss();
                        }
                    });
                    progress.dismiss();
                }
            }
        });

    }
    private boolean createReminder(LiveStreamSchedule liveStreamSchedule){

        SharedPreferences prefs = getActivity().getSharedPreferences("com.winsportsonline", Context.MODE_PRIVATE);

        if(prefs.getBoolean("reminder_" + liveStreamSchedule.getEventId(), false)) {
            Toast.makeText(getActivity(), "El recordatorio ya ha sido creado", Toast.LENGTH_SHORT).show();
            return false;
        }

        int  id_calendars[] = getCalendar(getActivity());

        if(id_calendars.length == 0) {
            Toast.makeText(getActivity(), "Calendario no disponible", Toast.LENGTH_SHORT).show();
            return false;
        }

        long calID = id_calendars[0];

        long startMillis = 0;

        long endMillis = 0;

        Calendar cal = Calendar.getInstance();

        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(liveStreamSchedule.getStartDate());
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(liveStreamSchedule.getEndDate());
        endMillis = endTime.getTimeInMillis();

        TimeZone timeZone = TimeZone.getDefault();

        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, liveStreamSchedule.getName());

        //TODO: Add description?
        //values.put(CalendarContract.Events.DESCRIPTION, "");

        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());

        values.put(CalendarContract.Events.ALL_DAY,0);


        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        long eventID = Long.parseLong(uri.getLastPathSegment());

        ContentValues reminderValues = new ContentValues();

        reminderValues.put(CalendarContract.Reminders.MINUTES, 3);
        reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
        reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

        cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

        prefs.edit().putBoolean("reminder_" + liveStreamSchedule.getEventId(), true).commit();

        Toast.makeText(getActivity(), "Recordatorio creado", Toast.LENGTH_SHORT).show();

        return true;
    }

    public int [] getCalendar(Context c) {

        String projection[] = {"_id", "calendar_displayName"};

        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = c.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);

        int aux[] = new int[0];

        if (managedCursor.moveToFirst()){

            aux = new int[managedCursor.getCount()];

            int cont= 0;
            do {
                aux[cont] = managedCursor.getInt(0);
                cont++;
            } while(managedCursor.moveToNext());

            managedCursor.close();
        }
        return aux;

    }

    private Bitmap descargarImagen (String imageHttpAddress){
        URL imageUrl = null;
        Bitmap imagen = null;
        try{
            imageUrl = new URL(imageHttpAddress);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.connect();
            imagen = BitmapFactory.decodeStream(conn.getInputStream());
        }catch(IOException ex){
            ex.printStackTrace();
        }

        return imagen;
    }

    private File guardarImagen(Context context, String nombre, Bitmap imagen){

        ContextWrapper cw = new ContextWrapper(context);
        File dirImages = cw.getDir("Win Sports Online", Context.MODE_PRIVATE);
        dirImages.mkdirs();

        File myPath = new File(dirImages, nombre + ".png");
        myPath.mkdirs();

        try
        {
            String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.txt";
            Log.e("rootPath","--> "+rootPath);

            File file=new File(rootPath);
            if(!file.exists()){
                Log.e("rootPath 2","--> "+rootPath);
                file.mkdirs();
            }

           OutputStreamWriter fout =
                    new OutputStreamWriter(
                            new FileOutputStream(file));

            fout.write("Texto de prueba.");
            fout.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a tarjeta SD");
        }

        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(myPath);
            imagen.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        }catch (FileNotFoundException ex){
            Log.e("Error 1","--> "+ex.getMessage());
            ex.printStackTrace();
        }catch (IOException ex){
            Log.e("Error 2","--> "+ex.getMessage());
            ex.printStackTrace();
        }
        return myPath;
    }
    private class CargaImagenes extends AsyncTask<String, Void, Bitmap> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub
            Log.i("doInBackground" , "Entra en doInBackground");
            String url = params[0];
            Bitmap imagen = descargarImagen(url);
            return imagen;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub

            if(result != null){
                Log.e("Image", "Image descargada");
            }

            final DateFormat dfS = new SimpleDateFormat("HH:mm 'HRS el' dd 'de' MMM");

            //imgImagen.setImageBitmap(result);
            //pDialog.dismiss();

            File  mFile = guardarImagen(getActivity(), mediaCorreo.getCode(),result);
            Uri u = null;
            u = Uri.fromFile(mFile);
            String text = String.format("%s a las %s por www.winsportsonline.com ", mediaCorreo.getName(), dfS.format(mediaCorreo.getStartDate()));

            Intent i = new Intent(Intent.ACTION_SEND);
            //i.setType("message/rfc822");
            i.setType("text/html");
            i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
            i.putExtra(Intent.EXTRA_TEXT, text);

            try{
                i.setType("image/png");
                i.putExtra(Intent.EXTRA_STREAM,u);
                startActivity(Intent.createChooser(i, "Send email..."));
            }
            catch(Exception e){
                Log.e("zzdf","--> "+e.getMessage());
            }
        }
    }

    public class TimerUpdate extends AsyncTask<Void, Long, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            if(!twoShowLive){
                int cont= 0, contador= 0;
                long inicio = 0, delta = 0, deltaminutos, fin;
                inicio = System.currentTimeMillis();
                do{
                    fin = System.currentTimeMillis();
                    delta = fin - dateEndShowActual1;
                    //Log.e("Date End",""+new Date(dateEndShowActual1).toString());
                    //delta = fin - (inicio + 60000);
                    deltaminutos = (fin - inicio) / 1000;
                    cont++;
                    if(deltaminutos % 60 == 0){
                        contador++;
                    }
                    if(contador > 59){
                        contador = 0;
                        publishProgress(inicio);
                    }
                //}while(contador <= 59);
                }while(delta <= 0);

            }else{

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... values) {

            if(!twoShowLive){
                Long t = dateStartShowActual1;
                Long now = new Date().getTime();
                Long delta = now - t;
                int min = (int)(delta / 60000);
                int horas = 0 ,minutos = 0;
                boolean formato = false;

                if(min > 59){
                    formato = true;
                    horas = min / 60;
                    minutos = min % 60;
                }
                //Log.e("Hora",".->"+min+" Min");
                //Log.e("Hora","-->"+horas+":"+minutos);

                if(!formato){
                    timeIndicator1.setText("Comenzó hace " + min + " Min");
                }
                else{
                    if(minutos > 10){
                        timeIndicator1.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
                    }
                    else{
                        timeIndicator1.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
                    }
                }
            }
            else{
                Long t1 = dateStartShowActual1;
                Long t2 = dateStartShowActual2;
                Long now = new Date().getTime();
                Long delta1 = now - t1;
                Long delta2 = now - t2;
                int min = (int)(delta1 / 60000);
                int min2 = (int)(delta2 / 60000);
                int horas = 0 ,minutos = 0;
                int horas2 = 0 ,minutos2 = 0;
                boolean formato = false;
                boolean formato2 = false;

                if(min > 59){
                    formato = true;
                    horas = min / 60;
                    minutos = min % 60;
                }

                if(min2 > 59){
                    formato2 = true;
                    horas2 = min2 / 60;
                    minutos2 = min2 % 60;
                }

                if(!formato){
                    timeIndicator1.setText("Comenzó hace " + min + " Min");
                }
                else{
                    if(minutos > 10){
                        timeIndicator1.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
                    }
                    else{
                        timeIndicator1.setText("Comenzó hace "+horas+" Hrs "+minutos+" Min");
                    }
                }

                if(!formato2){
                    timeIndicator2.setText("Comenzó hace " + min2 + " Min");
                }
                else{
                    if(minutos > 10){
                        timeIndicator2.setText("Comenzó hace "+horas2+" Hrs "+minutos2+" Min");
                    }
                    else{
                        timeIndicator2.setText("Comenzó hace "+horas2+" Hrs "+minutos2+" Min");
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refresh();
        }
    }
}
