package ca.bcit.comp3717.guardian.util;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ca.bcit.comp3717.guardian.R;

public class DialogBuilder {

    public static Dialog constructLoadingDialog(Context context, int dialogLayout) {
        Dialog loadingDialog = new Dialog(context);
        loadingDialog.setContentView(dialogLayout);
        loadingDialog.setCancelable(false);
        return loadingDialog;
    }

    public static AlertDialog constructAlertDialog(Context context, String title, String desc) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null);

        TextView etAlertTitle = dialogView.findViewById(R.id.textView_dialogAlert_title);
        TextView etAlertDesc = dialogView.findViewById(R.id.textView_dialogAlert_desc);
        etAlertTitle.setText(title);
        etAlertDesc.setText(desc);

        Button btnOk = dialogView.findViewById(R.id.button_dialogAlert_ok);
        mBuilder.setView(dialogView);

        final AlertDialog alertDialog = mBuilder.create();
        alertDialog.setCancelable(false);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }
}
