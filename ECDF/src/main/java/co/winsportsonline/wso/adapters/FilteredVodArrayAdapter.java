package co.winsportsonline.wso.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleArrayAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import co.winsportsonline.wso.R;
import co.winsportsonline.wso.datamodel.Media;
import co.winsportsonline.wso.datamodel.Thumbnail;
import co.winsportsonline.wso.delegates.FilteredArrayAdapterDelegate;
import co.winsportsonline.wso.dialogs.PostDialog;

/**
 * Created by Franklin Cruz on 27-02-14.
 */
public class FilteredVodArrayAdapter extends StickyGridHeadersSimpleArrayAdapter {

    private LayoutInflater inflater;

    private View prevShow = null;
    private View prevShare = null;

    private FilteredArrayAdapterDelegate delegate;

    private FragmentActivity activity;

    public FragmentActivity getActivity() {
        return  activity;
    }

    public void setDelegate(FilteredArrayAdapterDelegate delegate) {
        this.delegate = delegate;
    }

    public FilteredVodArrayAdapter(FragmentActivity activity, Context context, List<Media> items) {
        super(context,items,R.layout.filtered_vod_section_header, R.layout.last_programs_cell);

        inflater = LayoutInflater.from(context);
        this.activity = activity;
    }

    @Override
    public long getHeaderId(int position) {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.filtered_vod_section_header, parent, false);
            holder = new HeaderViewHolder();
            holder.textView = (TextView)convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder)convertView.getTag();
        }

        holder.textView.setText("");

        return convertView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");
            Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");
            convertView = mInflater.inflate(mItemResId, parent, false);
            holder = new ViewHolder();

            holder.titleTextView = (TextView)convertView.findViewById(R.id.title_label);
            holder.titleTextView2 = (TextView)convertView.findViewById(R.id.title_label_2);
            holder.titleTextView3 = (TextView)convertView.findViewById(R.id.title_label_3);
            holder.timeTextView = (TextView)convertView.findViewById(R.id.time_label);
            holder.titleTextView.setTypeface(bold);
            holder.titleTextView2.setTypeface(light);
            holder.titleTextView3.setTypeface(bold);
            holder.timeTextView.setTypeface(light);
            holder.shareButton = (ImageButton)convertView.findViewById(R.id.share_button);
            holder.previewImage = (ImageView)convertView.findViewById(R.id.preview_image);
            holder.shareView = convertView.findViewById(R.id.share_container);

            holder.showView = convertView.findViewById(R.id.show_container);
            holder.compartir = (TextView) convertView.findViewById(R.id.share_text);
            holder.compartir.setTypeface(light);

            convertView.setTag(holder);

            Point outSize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(outSize);
            int screenHeight = outSize.y;
            if(screenHeight <= 600){
                holder.titleTextView.setTextSize(13);
                holder.titleTextView2.setTextSize(13);
                holder.titleTextView3.setTextSize(13);
                holder.timeTextView.setTextSize(11);

            }

        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        final Media item = getItem(position);

        final AQuery aq = new AQuery(convertView);
        final String thumbnailUrl;

        Thumbnail thumbnail = item.getDefaultThumbnail();

        if(thumbnail != null) {
            thumbnailUrl = thumbnail.getUrl();
            View imageFullShare = convertView.findViewById(R.id.share_image_full);
            imageFullShare.setVisibility(View.VISIBLE);

            View splitViewShare = convertView.findViewById(R.id.share_split_image_container);
            splitViewShare.setVisibility(View.GONE);

            View vsLabelShare = convertView.findViewById(R.id.share_vs_label);
            vsLabelShare.setVisibility(View.GONE);

            aq.id(R.id.share_image_full).image(thumbnail.getUrl());
            aq.id(R.id.preview_image).image(thumbnail.getUrl());
        }
        else if(item.getThumbnails() != null && item.getThumbnails().size() > 0) {
            thumbnailUrl = item.getThumbnails().get(0).getUrl();
            View imageFullShare = convertView.findViewById(R.id.share_image_full);
            imageFullShare.setVisibility(View.VISIBLE);

            View splitViewShare = convertView.findViewById(R.id.share_split_image_container);
            splitViewShare.setVisibility(View.GONE);

            View vsLabelShare = convertView.findViewById(R.id.share_vs_label);
            vsLabelShare.setVisibility(View.GONE);

            aq.id(R.id.share_image_full).image(item.getThumbnails().get(0).getUrl());
            aq.id(R.id.preview_image).image(item.getThumbnails().get(0).getUrl());
        }
        else {
            thumbnailUrl = "";
        }


        View facebookButton = convertView.findViewById(R.id.facebook_button);

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(item.belongsToCategoryByName("Partido")) {
                    String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", item.getTitle());

                    PostDialog postDialog = new PostDialog(text, item.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                    postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }
                else {

                    String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", item.getTitle());

                    PostDialog postDialog = new PostDialog(text, item.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                    postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            }
        });

        View twitterButton = convertView.findViewById(R.id.twitter_button);

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(item.belongsToCategoryByName("Partido")) {
                    String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", item.getTitle());

                    PostDialog postDialog = new PostDialog(text, item.getTitle(), "", PostDialog.TWITTER_SHARE);
                    postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }
                else {

                    String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", item.getTitle());

                    PostDialog postDialog = new PostDialog(text, item.getTitle(), "", PostDialog.TWITTER_SHARE);
                    postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }

            }
        });


        View emailButton = convertView.findViewById(R.id.mail_button);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(item.belongsToCategoryByName("Partido")) {
                    String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Win Sports Online", item.getTitle());
                    text = text +" www.winsportsonline.com";

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


                        getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    String text = String.format("Estoy viendo EN VIVO %s por Win Sports Online", item.getTitle());

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");

                    i.putExtra(Intent.EXTRA_SUBJECT, "Win Sports Online");
                    i.putExtra(Intent.EXTRA_TEXT   , text);

                    Bitmap image = aq.image(thumbnailUrl).getCachedImage(thumbnailUrl);

                    File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                    try {

                        image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                        if(image != null) {
                            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                        }


                        getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        View clipboardButton = convertView.findViewById(R.id.clipboard_button);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Win Sports", "http://www.winsportsonline.com"));

                Toast.makeText(getActivity(), "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        });

        String splited[] = item.getTitle().replace(" - ", "\n").split("v/s");
        if(splited.length > 1){
            holder.titleTextView2.setVisibility(View.VISIBLE);
            holder.titleTextView3.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(splited[0].toUpperCase().replace(" ",""));
            if(item.getTitle().length()< 26){

                holder.titleTextView3.setText(splited[1].toUpperCase().replace(" ",""));
           }
            else{
                if(splited[1].length()> 11)
                    holder.titleTextView3.setText(splited[1].replace(" ","").toUpperCase().substring(0,10)+"...");
                else
                    holder.titleTextView3.setText(splited[1].replace(" ","").toUpperCase().substring(0,7)+"...");
           }
        }
        else{
            if(item.getTitle().length()< 26)
                holder.titleTextView.setText(item.getTitle().toUpperCase());
            else
                holder.titleTextView.setText(item.getTitle().toUpperCase().substring(0,25)+"...");
        }


        holder.timeTextView.setText(String.format("%d Min.", item.getDuration() / 60));

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prevShare != null && prevShow != null) {
                    hideShare(prevShare,prevShow);
                }

                displayShare(holder.shareView,holder.showView);
                prevShare = holder.shareView;
                prevShow = holder.showView;
            }
        });

        holder.shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShare(holder.shareView,holder.showView);
                prevShare = null;
                prevShow = null;
            }
        });

        /*holder.previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (delegate != null) {
                    delegate.onShowViewClicked(item);
                }
            }
        });*/
        holder.showView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (delegate != null) {
                    //holder.showView.setEnabled(false);
                    delegate.onShowViewClicked(item);
                }
            }
        });

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)convertView.getLayoutParams();
        Display display = getActivity().getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int width = (int)((size.x * 95)/100);
        int height = (int) (width * 150)/ 360;

        params.height = height;
        params.width = width;

        convertView.setLayoutParams(params);

        Point outSize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(outSize);
        int screenHeight = outSize.y;
        if(screenHeight <= 600){
            holder.titleTextView.setTextSize(13);
            holder.titleTextView2.setTextSize(13);
            holder.titleTextView3.setTextSize(13);
            holder.timeTextView.setTextSize(11);

        }
        return convertView;
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
