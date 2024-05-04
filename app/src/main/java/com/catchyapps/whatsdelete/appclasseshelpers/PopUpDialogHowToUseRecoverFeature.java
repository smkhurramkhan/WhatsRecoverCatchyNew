package com.catchyapps.whatsdelete.appclasseshelpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.catchyapps.whatsdelete.R;

public class PopUpDialogHowToUseRecoverFeature {

    private final Context context;

    public PopUpDialogHowToUseRecoverFeature(Context context) {
        this.context = context;
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.how_to_use_recover_feature_dialog, null);
        builder.setView(view);
        TextView msgTxt = view.findViewById(R.id.tv_message);
        msgTxt.setText(Html.fromHtml(context.getResources().getString(R.string.how_it_work_desc)));

        builder.setTitle("How it Works?").setPositiveButton("Close", (dialog, which) -> {
            new MyAppSharedPrefs(context).setShowHowWorkDialog();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
