package co.winsportsonline.wso.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import co.winsportsonline.wso.R;
import co.winsportsonline.wso.datamodel.Media;
import co.winsportsonline.wso.datamodel.Thumbnail;
import co.winsportsonline.wso.delegates.VideoDelegate;
import co.winsportsonline.wso.dialogs.MessageDialog;
import co.winsportsonline.wso.dialogs.PostDialog;
import co.winsportsonline.wso.services.ServiceManager;

/**
 * Created by Franklin Cruz on 26-02-14.
 */
public class VodFragment extends Fragment {

    public final String LIVE_LEFT_HEADER_URL_FORMATSTR = "http://winsportsonline.com/assets/img/event/large/large-";

    public final String VOD_CATEGORY_HIGHLIGHT      = "destacado";
    public final String VOD_CATEGORY_LAST_PROGRAM   = "Programas";
    public final String VOD_CATEGORY_LAST_MATCHES   = "Fútbol";

    private LinearLayout highlightsContainer;
    private LinearLayout lastProgramsContainer;
    private LinearLayout lastMatchesContainer;

    private List<Media> mediaList;
    private List<Media> lastMatchesList = null;
    private List<Media> lastShowsList = null;
    private List<Media> highliightsList = null;


    private View prevShow = null;
    private View prevShare = null;
    private View rootView;

    private VideoDelegate videoDelegate;

    private boolean higlightsLoaded = false;
    private boolean matchesLoaded = false;
    private boolean showsLoaded = false;

    public void setVideoDelegate(VideoDelegate delegate) {
        this.videoDelegate = delegate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_vod, container, false);


        lastProgramsContainer = (LinearLayout)rootView.findViewById(R.id.last_matches_container);
        lastMatchesContainer = (LinearLayout)rootView.findViewById(R.id.last_programs_container);

        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");
        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");

        TextView programas = (TextView) rootView.findViewById(R.id.last_programs_title_label);
        TextView partidos = (TextView) rootView.findViewById(R.id.last_matches_title_label);

        //destacados.setTypeface(light);
        programas.setTypeface(light);
        partidos.setTypeface(light);

        higlightsLoaded = false;
        matchesLoaded = false;
        showsLoaded = false;

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);


        ServiceManager serviceManager = new ServiceManager(getActivity());

        serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_LAST_MATCHES }, new ServiceManager.DataLoadedHandler<Media>() {
            @Override
            public void loaded(List<Media> data) {
                mediaList = data;
                lastMatchesList = data;

                displayLastMatches();

                matchesLoaded = true;
                if( matchesLoaded && showsLoaded) {
                    progress.dismiss();
                }
            }
        });

        serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_LAST_PROGRAM }, new ServiceManager.DataLoadedHandler<Media>() {
            @Override
            public void loaded(List<Media> data) {
                mediaList = data;
                lastShowsList = data;

                displayLastPrograms();

                showsLoaded = true;
                if(matchesLoaded && showsLoaded) {

                    progress.dismiss();
                }
            }
        });
        return rootView;
    }

    private void displayLastPrograms() {

        for (int i = 0; i < lastShowsList.size(); ++i) {
            final Media m = lastShowsList.get(i);

            View v = getActivity().getLayoutInflater().inflate(R.layout.highlight_cell, null);
            lastProgramsContainer.addView(v);

            Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");
            Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");

            TextView equipo1 = (TextView)v.findViewById(R.id.title_label);
            TextView vs = (TextView)v.findViewById(R.id.title_label_2);
            TextView equipo2 = (TextView)v.findViewById(R.id.title_label_3);
            equipo1.setTypeface(bold);
            equipo2.setTypeface(bold);
            vs.setTypeface(light);

            final String splited[] = m.getTitle().replace(" - ", "\n").split("v/s");

            if(m.getTitle().length()<27){
                if(splited.length > 1){

                    equipo2.setVisibility(View.VISIBLE);
                    vs.setVisibility(View.VISIBLE);
                    equipo1.setText(splited[0].replace(" - ", "\n").toUpperCase());
                    equipo2.setText(splited[1].replace(" - ", "\n").toUpperCase());
                    vs.setText(" VS ");
                }
                else{
                    equipo1.setText(m.getTitle().replace(" - ", "\n").toUpperCase());
                }
            }
            else{
                if(splited.length > 1){

                    equipo2.setVisibility(View.VISIBLE);
                    vs.setVisibility(View.VISIBLE);
                    equipo1.setText(splited[0].replace(" - ", "\n").toUpperCase());
                    equipo2.setText(splited[1].replace(" - ", "\n").toUpperCase().substring(0,9)+"...");
                    vs.setText(" VS ");
                }
                else{
                    equipo1.setText(m.getTitle().replace(" - ", "\n").toUpperCase().substring(0,24)+"...");
                }

            }

            TextView timeLabel = (TextView)v.findViewById(R.id.time_label);
            timeLabel.setText(String.format("%d Min.", m.getDuration() / 60));
            timeLabel.setTypeface(light);

            TextView te = (TextView) v.findViewById(R.id.share_text);
            te.setTypeface(light);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();

            Display display = getActivity().getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            int width = (int)((size.x * 80)/100);
            //int height = (size.y * 35)/100 ;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;

            params.setMargins(0,0,40,0);
            params.height = height;
            params.width = width;

            v.setLayoutParams(params);

            ImageButton shareButton = (ImageButton)v.findViewById(R.id.share_button);

            Point outSize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(outSize);
            int screenHeight = outSize.y;
            if(screenHeight <= 600){
                equipo1.setTextSize(14);
                equipo2.setTextSize(14);
                vs.setTextSize(14);
                timeLabel.setVisibility(View.GONE);
                ViewGroup.LayoutParams params2 = timeLabel.getLayoutParams();

                params2.height = 28;
                params2.width = 28;

                timeLabel.setLayoutParams(params2);
            }

            final View show = v.findViewById(R.id.show_container);
            final View share = v.findViewById(R.id.share_container);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(prevShare != null && prevShow != null) {
                        hideShare(prevShare,prevShow);
                    }

                    displayShare(share,show);
                    prevShare = share;
                    prevShow = show;
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


            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (videoDelegate != null) {

                        SharedPreferences sp = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
                        boolean permission = false;
                        List<String> countries = null;
                        try{
                            countries = m.getAccessRules().getGeo().getCountries();
                        }catch(Exception e){
                            e.printStackTrace();
                            Log.e("COUNTRIES", "Error: " + e.getMessage());
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
                            videoDelegate.onVideoSelected(m);
                        }else{
                            MessageDialog messageDialog = new MessageDialog("ESTE CONTENIDO NO ESTÁ DISPONIBLE PARA TU UBICACIÓN.");
                            messageDialog.show(getActivity().getFragmentManager(),"dialog");
                        }
                    }
                }
            });

            final AQuery aq = new AQuery(v);
            Thumbnail t = m.getDefaultThumbnail();
            final String thumbnailUrl;
            if (t != null) {

                thumbnailUrl = t.getUrl();

                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);


                aq.id(R.id.preview_image).image(t.getUrl());
                aq.id(R.id.share_image_full).image(t.getUrl());
            }
            else if(m.getThumbnails() != null && m.getThumbnails().size() > 0) {

                thumbnailUrl = m.getThumbnails().get(0).getUrl();

                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(m.getThumbnails().get(0).getUrl());
                aq.id(R.id.share_image_full).image(m.getThumbnails().get(0).getUrl());
            }
            else {
                thumbnailUrl = "";
            }

            View facebookButton = v.findViewById(R.id.facebook_button);

            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo  %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }
            });

            View twitterButton = v.findViewById(R.id.twitter_button);

            twitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }

                }
            });


            View emailButton = v.findViewById(R.id.mail_button);

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {

                        String text = String.format("Estoy viendo  %s por Win Sports Online", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        text = text +" www.winsportsonline.com";
                        i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }

                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
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
        }

    }

    private void displayLastMatches() {
        for (int i = 0; i < lastMatchesList.size(); ++i) {
            final Media m = lastMatchesList.get(i);

            View v = getActivity().getLayoutInflater().inflate(R.layout.highlight_cell, null);
            lastMatchesContainer.addView(v);

            Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");
            Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");

            TextView equipo1 = (TextView)v.findViewById(R.id.title_label);
            TextView vs = (TextView)v.findViewById(R.id.title_label_2);
            TextView equipo2 = (TextView)v.findViewById(R.id.title_label_3);
            equipo1.setTypeface(bold);
            equipo2.setTypeface(bold);
            vs.setTypeface(light);

            final String splited[] = m.getTitle().replace(" - ", "\n").split("v/s");

            if(m.getTitle().length()<27){
                if(splited.length > 1){
                    //equipo1.setText(m.getTitle().replace(" - ", "\n").toUpperCase());
                    equipo2.setVisibility(View.VISIBLE);
                    vs.setVisibility(View.VISIBLE);
                    equipo1.setText(splited[0].replace(" - ", "\n").toUpperCase());
                    equipo2.setText(splited[1].replace(" - ", "\n").toUpperCase());
                    vs.setText(" VS ");
                }
                else{
                    equipo1.setText(m.getTitle().replace(" - ", "\n").toUpperCase());
                }
            }
            else{
                if(splited.length > 1){

                    equipo2.setVisibility(View.VISIBLE);
                    vs.setVisibility(View.VISIBLE);
                    equipo1.setText(splited[0].replace(" - ", "\n").toUpperCase());
                    equipo2.setText(splited[1].replace(" - ", "\n").toUpperCase().substring(0,9)+"...");
                    vs.setText(" VS ");
                }
                else{
                    equipo1.setText(m.getTitle().replace(" - ", "\n").toUpperCase().substring(0,24)+"...");
                }

            }

            TextView te = (TextView) v.findViewById(R.id.share_text);
            te.setTypeface(light);

            TextView timeLabel = (TextView)v.findViewById(R.id.time_label);
            timeLabel.setText(String.format("%d Min.", m.getDuration() / 60));
            timeLabel.setTypeface(light);

            ImageButton shareButton = (ImageButton)v.findViewById(R.id.share_button);

            Point outSize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(outSize);
            int screenHeight = outSize.y;
            if(screenHeight <= 600){
                equipo1.setTextSize(14);
                equipo2.setTextSize(14);
                vs.setTextSize(14);
                timeLabel.setVisibility(View.GONE);
                ViewGroup.LayoutParams params = timeLabel.getLayoutParams();

                params.height = 28;
                params.width = 28;

                timeLabel.setLayoutParams(params);
            }

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
            Display display = getActivity().getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            int width = (int)((size.x * 80)/100);
            //int height = (size.y * 32)/100 ;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;

            params.setMargins(0,0,40,0);
            /*int width = (int)((size.x * 80)/100);
            int height = 390 ;

            params.setMargins(5,20,40,5);*/
            params.height = height;
            params.width = width;

            v.setLayoutParams(params);

            final View show = v.findViewById(R.id.show_container);
            final View share = v.findViewById(R.id.share_container);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(prevShare != null && prevShow != null) {
                        hideShare(prevShare,prevShow);
                    }

                    displayShare(share,show);
                    prevShare = share;
                    prevShow = show;
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

            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if (videoDelegate != null) {
                        videoDelegate.onVideoSelected(m);
                    }*/

                    if (videoDelegate != null) {

                        SharedPreferences sp = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
                        boolean permission = false;
                        List<String> countries = null;
                        try{
                            countries = m.getAccessRules().getGeo().getCountries();
                        }catch(Exception e){
                            e.printStackTrace();
                            Log.e("COUNTRIES", "Error: " + e.getMessage());
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
                            videoDelegate.onVideoSelected(m);
                        }else{
                            MessageDialog messageDialog = new MessageDialog("ESTE CONTENIDO NO ESTÁ DISPONIBLE PARA TU UBICACIÓN.");
                            messageDialog.show(getActivity().getFragmentManager(),"dialog");
                        }
                    }
                }
            });


            final AQuery aq = new AQuery(v);
            final String thumbnailUrl;
            Thumbnail t = m.getDefaultThumbnail();
            if (t != null) {
                thumbnailUrl = t.getUrl();
                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(t.getUrl());
                aq.id(R.id.share_image_full).image(t.getUrl());
            }
            else if(m.getThumbnails() != null && m.getThumbnails().size() > 0) {
                thumbnailUrl = m.getThumbnails().get(0).getUrl();
                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(m.getThumbnails().get(0).getUrl());
                aq.id(R.id.share_image_full).image(m.getThumbnails().get(0).getUrl());
            }
            else {
                thumbnailUrl = "";
            }

            View facebookButton = v.findViewById(R.id.facebook_button);

            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        //PostDialog postDialog = new PostDialog(text, m.getTitle(), LIVE_LEFT_HEADER_URL_FORMATSTR + m.getCode()+".jpg", PostDialog.FACEBOOK_SHARE);

                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo  %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }
            });

            View twitterButton = v.findViewById(R.id.twitter_button);

            twitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo  %s por Win Sports Online", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }

                }
            });


            View emailButton = v.findViewById(R.id.mail_button);

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        text = text +" www.winsportsonline.com";

                        i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {

                        String text = String.format("Estoy viendo %s por Win Sports Online", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
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
                    clipboard.setPrimaryClip(ClipData.newPlainText("Win Sports Online", "http://www.winsportsonline.com"));

                    Toast.makeText(getActivity(), "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
}
