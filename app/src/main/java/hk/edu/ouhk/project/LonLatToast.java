package hk.edu.ouhk.project;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LonLatToast {
    private static final String TAG = "Toast";
    private static String msg = "";
    protected  static Toast toast = null;
    private  static  long oneTime = 0;
    private  static  long twoTime = 0;

    public  static void showToast(Context context, String message){
        View view = LayoutInflater.from(context).inflate(R.layout.toastview,null);
        TextView showText = view.findViewById(R.id.lonlat_toast);

        if(toast==null){
            toast = new Toast(context);
            toast.setView(view);
            showText.setText(message);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            Log.d(TAG, "showToast: create new toast");
            oneTime = System.currentTimeMillis();
        }else{
            twoTime = System.currentTimeMillis();
            if(message.equals(msg)){
                if(twoTime-oneTime>Toast.LENGTH_LONG){
                    showText.setText(msg);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    Log.d(TAG,"showToast: twoTime large than oneTime");
                }
                Log.d(TAG,"showToast: twoTime is not large than oneTime");
            }else{
                msg = message;
                toast.setView(view);
                showText.setText(msg);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
                Log.d(TAG,"showToast: newMsg not equals msg");
            }
        }
        oneTime = twoTime;
    }
}
