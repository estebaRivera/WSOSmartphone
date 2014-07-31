package co.winsportsonline.wso.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import java.util.List;

import co.winsportsonline.wso.R;
import co.winsportsonline.wso.adapters.FilteredVodArrayAdapter;
import co.winsportsonline.wso.datamodel.Filter;
import co.winsportsonline.wso.datamodel.Media;
import co.winsportsonline.wso.delegates.FilteredArrayAdapterDelegate;
import co.winsportsonline.wso.delegates.VideoDelegate;
import co.winsportsonline.wso.dialogs.MessageDialog;
import co.winsportsonline.wso.services.ServiceManager;

/**
 * Created by Franklin Cruz on 27-02-14.
 */
public class FilteredFragment extends Fragment {

    private Filter filter;

    //private StickyGridHeadersGridView gridView;

    private ListView gridView;
    private TextView titleTextView;
    private List<Media> media = null;
    private LinearLayout noResult;

    private VideoDelegate videoDelegate;

    public void setVideoDelegate(VideoDelegate delegate) {
        this.videoDelegate = delegate;
    }

    public FilteredFragment() { }

    public FilteredFragment(Filter filter) {
        this.filter = filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        //loadData();
    }

    public FilteredFragment(List<Media> media) {
        this.media = media;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_filtered, container, false);

        //gridView = (StickyGridHeadersGridView)rootView.findViewById(R.id.gridview);
        gridView = (ListView) rootView.findViewById(R.id.gridview);

        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");
        titleTextView = (TextView)rootView.findViewById(R.id.filters_title_label);
        titleTextView.setTypeface(light);

        noResult = (LinearLayout)rootView.findViewById(R.id.no_result);

        if(media != null) {
            loadSearch();
        }else {
            loadData();
        }

        return rootView;
    }

    private void loadData() {

        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");
        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");

        if (gridView == null) {
            return;
        }

        Filter topFilter = filter;
        String titleText = null;
        do {

            if (titleText == null) {
                titleText =  topFilter.getName() ;
            }
            else {
                titleText = topFilter.getName() + " | " + titleText;
            }

            topFilter = topFilter.getParent();
        }while (topFilter != null);

        titleTextView.setText(titleText.toUpperCase());
        titleTextView.setTypeface(light);

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        //gridView.setAreHeadersSticky(false);

        ServiceManager serviceManager = new ServiceManager(getActivity());
        serviceManager.loadVODMediaByCategoryId(filter.getCategories().toArray(new String[0]), new ServiceManager.DataLoadedHandler<Media>() {
            @Override
            public void loaded(List<Media> data) {
                List<Media> media = data;

                FilteredVodArrayAdapter adapter = new FilteredVodArrayAdapter(getActivity(), getActivity()
                        .getApplicationContext(), media);

                adapter.setDelegate(new FilteredArrayAdapterDelegate() {
                    @Override
                    public void onShowViewClicked(Media media) {

                        /*if(videoDelegate != null) {
                            SharedPreferences sp = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
                            boolean permission = false;
                            List<String> countries = null;
                            try {
                                countries = media.getAccessRules().getGeo().getCountries();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("COUNTRIES", "Error: " + e.getMessage());
                            }
                            if (countries != null && countries.size() > 0) {
                                for (String country : countries) {
                                    if (country.equalsIgnoreCase(sp.getString("country_code", ""))) {
                                        permission = true;
                                    }
                                }
                            } else {
                                permission = true;
                            }
                            if (permission) {
                                progress.dismiss();
                                videoDelegate.onVideoSelected(media);
                            } else {
                                progress.dismiss();
                                MessageDialog messageDialog = new MessageDialog("ESTE CONTENIDO NO ESTÁ DISPONIBLE PARA TU UBICACIÓN.");
                                messageDialog.show(getActivity().getFragmentManager(), "dialog");
                            }
                        }*/
                        if(videoDelegate != null) {
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
                                //progress.dismiss();
                                permission = true;
                            }
                            if(permission){
                                progress.dismiss();
                                videoDelegate.onVideoSelected(media);
                            }else{
                                progress.dismiss();
                                MessageDialog messageDialog = new MessageDialog("ESTE CONTENIDO NO ESTÁ DISPONIBLE EN TU UBICACIÓN.");
                                messageDialog.show(getActivity().getFragmentManager(),"dialog");
                            }
                        }
                    }
                });

                gridView.setAdapter(adapter);

                progress.dismiss();
            }

            @Override
            public void error(String error) {
                progress.dismiss();
            }
        });
    }

    private void loadSearch(){
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");

        if (gridView == null) {
            return;
        }

        titleTextView.setTypeface(light);
        if(media.size() == 0){
            gridView.setVisibility(View.GONE);
            titleTextView.setVisibility(View.GONE);
            noResult.setVisibility(View.VISIBLE);
            TextView no_result_label = (TextView)noResult.findViewById(R.id.text);
            no_result_label.setTypeface(light);
            return;
        }
        titleTextView.setText("Resultado");

        FilteredVodArrayAdapter adapter = new FilteredVodArrayAdapter(getActivity(), getActivity().getApplicationContext(), media);

        adapter.setDelegate(new FilteredArrayAdapterDelegate() {
            @Override
            public void onShowViewClicked(Media media) {
                if(videoDelegate != null) {
                    SharedPreferences sp = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
                    boolean permission = false;
                    List<String> countries = null;
                    try {
                        countries = media.getAccessRules().getGeo().getCountries();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("COUNTRIES", "Error: " + e.getMessage());
                    }
                    if (countries != null && countries.size() > 0) {
                        for (String country : countries) {
                            if (country.equalsIgnoreCase(sp.getString("country_code", ""))) {
                                permission = true;
                            }
                        }
                    } else {
                        permission = true;
                    }
                    if (permission) {
                        videoDelegate.onVideoSelected(media);
                    } else {
                        MessageDialog messageDialog = new MessageDialog("ESTE CONTENIDO NO ESTÁ DISPONIBLE PARA TU UBICACIÓN.");
                        messageDialog.show(getActivity().getFragmentManager(), "dialog");
                    }
                }
            }
        });

        gridView.setAdapter(adapter);
    }
}
