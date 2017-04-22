/*
 * Copyright 2015. 1. 23. Lee Dong-Hyuk.
 * All rights reserved.
 */

package com.activednc.webappmaker.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Lee Dong-Hyuk
 */
public class MediaHelper {
	private static final String FILE_FORMAT = "yyyyMMdd_kkmmss";

	/** The img file name. */
	public static final String CAPTURE_DIR = "Temp";

	private PackageManager mPackageManager;

	public MediaHelper(PackageManager manager) {

		mPackageManager = manager;
	}

	public Intent getPickIntent(ArrayList<Uri> outputFileList, String pickerTitle) {

		Intent chooserIntent = Intent.createChooser(getGalleryIntent(), pickerTitle);
		Intent[] imageIntents = getCameraIntents(outputFileList, MediaStore.ACTION_IMAGE_CAPTURE, "jpg");
		Intent[] videoIntents = getCameraIntents(outputFileList, MediaStore.ACTION_VIDEO_CAPTURE, "mp4");
		Intent[] finalIntent = Arrays.copyOf(imageIntents, imageIntents.length + videoIntents.length);
		System.arraycopy(videoIntents, 0, finalIntent, imageIntents.length, videoIntents.length);
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, finalIntent);

		return chooserIntent;
	}

	public Intent getMoviePlayerIntent(String url) {

		if(TextUtils.isEmpty(url)) {
			return null;
		}

		Intent galleryIntent = new Intent(Intent.ACTION_VIEW);
		galleryIntent.setDataAndType(Uri.parse(url), "video/*");

		return galleryIntent;
	}

	public Intent getGalleryIntent() {

		Intent galleryIntent = null;

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			galleryIntent = new Intent(Intent.ACTION_PICK);
			galleryIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
			galleryIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		} else {
			galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
			galleryIntent.setType("*/*");
		}

		return galleryIntent;
	}

	public Intent getImageIntent(ArrayList<Uri> outputFileList) {

		final Uri imageUri = makeFileUri("jpg");
		outputFileList.add(imageUri);

		Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileList);

		return takeImageIntent;
	}

	public Intent[] getCameraIntents(ArrayList<Uri> outputFileList, String action, String extension) {

		Intent takeCameraIntent = new Intent(action);
		List<ResolveInfo> listImage = mPackageManager.queryIntentActivities(takeCameraIntent, 0);

		Intent[] result = null;
		if(listImage != null && listImage.isEmpty() == false) {
			result = new Intent[listImage.size()];
			for(int index = 0; index < listImage.size(); index++) {
				final ResolveInfo res = listImage.get(index);
				final String packageName = res.activityInfo.packageName;

				final Uri fileUri = makeFileUri(extension);
				outputFileList.add(fileUri);

				final Intent intent = new Intent(takeCameraIntent);
				intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
				intent.setPackage(packageName);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				result[index] = intent;
			}
		} else {
			result = new Intent[]{};
		}

		return result;
	}

	public Uri makeFileUri(String extension) {

		String folderName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + CAPTURE_DIR;
		String fileName = new SimpleDateFormat(FILE_FORMAT, Locale.KOREA).format(System.currentTimeMillis());
		String fullName = folderName + File.separator + fileName + "." + extension;

		File folder = new File(folderName);
		if(folder.exists() == false) {
			folder.mkdirs();
		}

		File file = new File(fullName);
		return Uri.fromFile(file);
	}
}
