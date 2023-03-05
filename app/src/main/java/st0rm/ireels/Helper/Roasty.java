package st0rm.ireels.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import st0rm.ireels.R;



public class Roasty {

    public static void showToast(Context context, int drawableRes, String msg) {

        Toast toast = new Toast(context);
        View v = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        TextView txtView = v.findViewById(R.id.toastTitle);
        txtView.setText(msg);

        if (drawableRes != -1) {
            txtView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);
        }

        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView((ViewGroup) v.findViewById(R.id.toastRoot));
        toast.show();

    }



}