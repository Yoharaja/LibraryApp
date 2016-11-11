package com.chehara.mycheharalibrary.widget;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chehara.mycheharalibrary.R;


public class CustomDialog {



	public static AlertDialog buildAlertDialogTitle(final Context context,
			String text, String txtTitle) {
		Builder alertDialogBuilder = new Builder(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogRoot = inflater.inflate(R.layout.layout_custom_dialog, null);
		TextView title = (TextView) dialogRoot
				.findViewById(R.id.textViewdialogTitle);
		title.setText(text);
		alertDialogBuilder.setTitle(txtTitle);
		alertDialogBuilder.setView(dialogRoot);

		alertDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				AlertDialog alertDialog = (AlertDialog) dialog;
				Typeface roboto = Typeface.createFromAsset(context.getAssets(),
						"fonts/Roboto-Light.ttf");
				Button buttonP = alertDialog
						.getButton(DialogInterface.BUTTON_POSITIVE);
				buttonP.setTypeface(roboto);
			}
		});
		return alertDialog;
	}


}
