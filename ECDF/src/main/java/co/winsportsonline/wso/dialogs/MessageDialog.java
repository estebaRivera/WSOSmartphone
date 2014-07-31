package co.winsportsonline.wso.dialogs;

import android.app.DialogFragment;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import co.winsportsonline.wso.R;

/**
 * Created by Esteban- on 09-06-14.
 */
public class MessageDialog extends DialogFragment {

    private String message;

    public MessageDialog(String message) {
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, getTheme());
        this.message = message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.message_dialog, container, false);
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Oswald-Light.otf");

        TextView messageLabel = (TextView)rootView.findViewById(R.id.message_label);
        messageLabel.setTypeface(light);
        messageLabel.setText(message);

        Button btn_play = (Button)rootView.findViewById(R.id.btn_ok);
        btn_play.setTypeface(light);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);

        return rootView;
    }
}
