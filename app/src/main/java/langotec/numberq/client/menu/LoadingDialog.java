package langotec.numberq.client.menu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;

public class LoadingDialog {

    private ProgressDialog dialog;
    private WeakReference<Context> weakReference;
    private AsyncTask task;

    public LoadingDialog(final WeakReference<Context> weakReference){
        this.weakReference = weakReference;
        dialog = new ProgressDialog(weakReference.get());
        dialog.setCancelable(false);
        dialog.setMessage(weakReference.get().getString(R.string.dialog_loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                weakReference.get().getString(R.string.menu_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        task.cancel(true);
                    }
                });
        dialog.show();
        dialog.getButton(ProgressDialog.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);
    }

    public void setMessage(String message){
        dialog.setMessage(message);
    }

    public void setCancel(AsyncTask task){
        this.task = task;
        dialog.getButton(ProgressDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
    }

    public void closeDialog(){
        dialog.dismiss();
    }
}
