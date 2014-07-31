package co.winsportsonline.wso.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONObject;

import co.winsportsonline.wso.R;
import co.winsportsonline.wso.activities.MainActivity;
import co.winsportsonline.wso.custom.EditTextView;
import co.winsportsonline.wso.datamodel.User;
import co.winsportsonline.wso.services.ServiceManager;


/**
 * Created by Franklin Cruz on 17-02-14.
 */
public class LoginFragment extends Fragment {

    private TextView subtitulo;
    private TextView loginText;
    private EditTextView user;
    private EditTextView pass;

    private Button loginButtonFacebook;
    private Button loginButton;

    private RelativeLayout r;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

        SharedPreferences sp = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
        if((sp.getString("userdata", null) != null || (sp.getString("name", null) != null && sp.getString("id", null) != null)) && sp.getString("country_code", null) != null) {
            ServiceManager serviceManager = new ServiceManager(getActivity().getApplicationContext());
            //Log.e("user data ",sp.getString("userdata", null));
            //Log.e("name ",sp.getString("name", null));
            //Log.e("id",sp.getString("id", null));
            serviceManager.geoData();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        r = (RelativeLayout) rootView.findViewById(R.id.container_login);

        AQuery aq = new AQuery(rootView);

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Bold.otf");
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");

        subtitulo = (TextView) rootView.findViewById(R.id.login_subtitulo);
        loginText = (TextView) rootView.findViewById(R.id.login_text);

        user = (EditTextView) rootView.findViewById(R.id.login_user);
        pass = (EditTextView) rootView.findViewById(R.id.login_pass);

        final ProgressDialog progress = new ProgressDialog(getActivity());

        subtitulo.setTypeface(light);
        loginText.setTypeface(light);
        user.setTypeface(light);
        pass.setTypeface(light);

        final ServiceManager serviceManager = new ServiceManager(getActivity());

        loginButtonFacebook = (Button)rootView.findViewById(R.id.login_button_facebook);
        loginButtonFacebook.setTypeface(bold);
        loginButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                progress.setContentView(R.layout.progress_dialog);
                progress.setCancelable(false);
                progress.setCanceledOnTouchOutside(false);
                serviceManager.loginFacebook(getActivity(), new AjaxCallback<JSONObject>(){
                    @Override
                    public void callback(String url, JSONObject user, AjaxStatus status) {
                        try{
                            SharedPreferences prefs = getActivity().getSharedPreferences("co.winsportsonline.wso", Context.MODE_PRIVATE);
                            final SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("id", user.getString("id"));
                            editor.putString("name", user.getString("name"));
                            serviceManager.geoData();
                            serviceManager.saveFacebookToken(new AjaxCallback<JSONObject>() {
                                @Override
                                public void callback(String url, JSONObject object, AjaxStatus status) {
                                    try{
                                        editor.putString("access_token", object.getJSONObject("data").getString("access_token"));
                                        editor.commit();
                                        progress.dismiss();
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }catch(Exception e){
                                        e.getStackTrace();
                                        Log.e("loginFacebook",e.getMessage());
                                        Toast.makeText(getActivity(),"Ocurrió un error al iniciar sesión con facebook.",Toast.LENGTH_LONG).show();
                                        progress.dismiss();
                                    }
                                }
                            });
                        }catch(Exception e){
                            progress.dismiss();
                        }
                    }
                });
                /*ServiceManager serviceManager = new ServiceManager(getActivity());
                serviceManager.login(usernameTextbox.getText().toString(), passwordTextbox.getText().toString(), new ServiceManager.DataLoadedHandler<User>() {
                    @Override
                    public void loaded(User data) {*/

                        //Intent intent = new Intent(getActivity(), MainActivity.class);
                        //startActivity(intent);
                  //  }
               // });

            }
        });

        InputFilter filter = new InputFilter(){
            @Override
            public CharSequence filter(CharSequence charSequence, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isSpaceChar(charSequence.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        user.setFilters(new InputFilter[]{filter});
        pass.setFilters(new InputFilter[]{filter});

        loginButton = (Button)rootView.findViewById(R.id.login_button);
        loginButton.setTypeface(bold);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                progress.setContentView(R.layout.progress_dialog);
                progress.setCancelable(false);
                progress.setCanceledOnTouchOutside(false);

                if(user.getText().toString().trim().length() > 0 && pass.getText().toString().length() > 0) {
                    try{
                        serviceManager.loginStandard(user.getText().toString(), pass.getText().toString(), new ServiceManager.DataLoadedHandler<User>() {
                            @Override
                            public void loaded(User data) {
                                serviceManager.geoData();
                                progress.dismiss();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }

                            @Override
                            public void error(String error) {
                                Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                progress.dismiss();
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.e("ServiceManager 1","Connection error: "+e.getMessage());
                        Toast.makeText(getActivity().getApplicationContext(),"Ups! Intenta de nuevo...",Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Usuario y/o contraseña invalido.",Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            }
        });
        //adapterScreen800();
        return rootView;
    }

    public void adapterScreen800(){
        Point outSize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(outSize);
        int height = outSize.y;
        Log.e("Largo","--> "+height);
        if(height <= 800){
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) r.getLayoutParams();
            params.setMargins(0,0,0,0);
            r.setLayoutParams(params);
            RelativeLayout.LayoutParams parametros;
            parametros = (RelativeLayout.LayoutParams) loginButtonFacebook.getLayoutParams();
            parametros.setMargins(0,15,0,0);
            parametros.height = 45;
            loginButtonFacebook.setLayoutParams(parametros);
            //loginButtonFacebook.setHeight(45);
           /* loginButton.setHeight(40);
            user.setHeight(40);
            pass.setHeight(40);*/
            parametros = (RelativeLayout.LayoutParams) loginButton.getLayoutParams();
            parametros.setMargins(0,10,0,20);
            parametros.height = 40;
            loginButton.setLayoutParams(parametros);

            parametros = (RelativeLayout.LayoutParams) user.getLayoutParams();
            parametros.setMargins(0,10,0,0);
            parametros.height = 40;
            user.setLayoutParams(parametros);

            parametros = (RelativeLayout.LayoutParams) pass.getLayoutParams();
            parametros.setMargins(0,10,0,0);
            parametros.height = 40;
            pass.setLayoutParams(parametros);

            /*loginButtonFacebook.setHeight(45);
            loginButton.setHeight(20);
            user.setHeight(40);
            pass.setHeight(40);*/
        }
    }
}
