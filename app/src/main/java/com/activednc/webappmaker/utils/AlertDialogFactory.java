package com.activednc.webappmaker.utils;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;

import com.activednc.webappmaker.R;

/**
 * A factory for creating AlertDialog objects.
 * 
 * @author 전창현
 * @date 2014-01-03
 */
public class AlertDialogFactory {

	/**
	 * Creates a new AlertDialog object.
	 *
	 * @param context the context
	 * @param title the title
	 * @param message the message
	 * @param onClickListener 다이알로그 버튼 클릭시 리스너
	 * @return the alert dialog
	 */
	public static AlertDialog createConfirmDialog(Context context, String title, String message,
												  OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setMessage(message)
				.setCancelable(false).setNegativeButton(R.string.confirm, onClickListener);
		return builder.create();
	}

	/**
	 * Creates a new AlertDialog object.
	 *
	 * @param context the context
	 * @param title the title
	 * @param message the message
	 * @param yesBtnText Positive 버튼의 표시 문자열
	 * @param noBtnText Negative 버튼의 표시 문자열
	 * @param onClickListener 다이알로그 버튼 클릭시 리스너
	 * @return the alert dialog
	 */
	public static AlertDialog createYesNoDialog(Context context, String title, String message, String yesBtnText,
			String noBtnText, OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setMessage(message)
				.setCancelable(false).setPositiveButton(yesBtnText, onClickListener)
				.setNegativeButton(noBtnText, onClickListener);
		return builder.create();
	}
}
