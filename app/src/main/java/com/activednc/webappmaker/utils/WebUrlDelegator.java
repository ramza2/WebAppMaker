package com.activednc.webappmaker.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.net.URISyntaxException;

public class WebUrlDelegator {
	
	private Context context;
	
	public WebUrlDelegator(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	public boolean delegateUrl(String url){
		if(url.endsWith(".m3u8")){
			delegateVideoUrl(url);
			return true;
		}else if (url.startsWith("sms:") || url.startsWith("mailto:")) {// 문자 보내기
			delegateSendToUrl(url);
			return true;
		} else if (url.startsWith("tel:")) {// 전화 걸기
			delegeteDialUrl(url);
			return true;
		} else if (url.startsWith("intent:")) {
			try {
				Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				Intent existPackage = context.getPackageManager().getLaunchIntentForPackage(intent.getPackage());
				if (existPackage != null) {
					context.startActivity(intent);
				} else {
					Intent marketIntent = new Intent(Intent.ACTION_VIEW);
					marketIntent.setData(Uri.parse("market://details?id="+intent.getPackage()));
					context.startActivity(marketIntent);
				}
				return true;
			}catch (Exception e) {
				e.printStackTrace();
			}
		} else if (url.startsWith("market://")) {
			try {
				Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				if (intent != null) {
					context.startActivity(intent);
				}
				return true;
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}else if(url.startsWith("kakaolink:") || url.startsWith("myp:") || url.startsWith("line:") || url.startsWith("rtsp://") || url.startsWith("bandapp://")){
			delegateExtraUrl(url);
			return true;
		}
		return false;
	}
	
	private void delegateVideoUrl(String url){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(url), "video/*");
		try{
			context.startActivity(intent);
		}catch(ActivityNotFoundException e){
			System.out.println("Activity Not Found.");
		}
	}

	private void delegateSendToUrl(String url){
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
		context.startActivity(intent);
	}

	private void delegeteDialUrl(String url){
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
		context.startActivity(intent);
	}
	
	private void delegateApplinkUrl(String url){
		String packageName = (url.split("//", 2))[1];
		PackageManager pm = context.getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage(packageName);
		if (intent != null) {
			context.startActivity(intent);
		} else {
			Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
			marketLaunch.setData(Uri.parse("market://details?id=" + packageName));
			context.startActivity(marketLaunch);
		}
	}
	
	private void delegateExtraUrl(String url){
		try {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(i);
		} catch (ActivityNotFoundException e) {
			if (url.startsWith("kakaolink:")) {
				Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
				marketLaunch.setData(Uri.parse("market://details?id=com.kakao.talk"));
				context.startActivity(marketLaunch);
			} else if (url.startsWith("myp:")) {
				Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
				marketLaunch.setData(Uri.parse("market://details?id=net.daum.android.air"));
				context.startActivity(marketLaunch);
			} else if (url.startsWith("line:")) {
				Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
				marketLaunch.setData(Uri.parse("market://details?id=jp.naver.line.android"));
				context.startActivity(marketLaunch);
			}else if (url.startsWith("bandapp:")) {
				Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
				marketLaunch.setData(Uri.parse("market://details?id=com.nhn.android.band"));
				context.startActivity(marketLaunch);
			}
		}
	}
}
