package langotec.numberq.client.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import langotec.numberq.client.R;

public class LoadingDialog {

    private ProgressDialog dialog;

    public LoadingDialog(Context context, final AsyncTask task){
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.dialog_loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.menu_cancel),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                task.cancel(true);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void closeDialog(){
        dialog.dismiss();
    }
}
