package langotec.numberq.client.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import langotec.numberq.client.R;

public class ShowDialog {

    private ProgressDialog dialog;

    public ShowDialog(Context context, final AsyncTask asyncTask){
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getResources().getString(R.string.dialog_loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().
                getString(R.string.menu_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                asyncTask.cancel(true);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void dismiss(){
        dialog.dismiss();
    }
}
