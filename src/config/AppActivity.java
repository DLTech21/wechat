package config;

import bean.UserEntity;
import bean.UserInfo;

import com.donal.wechat.R;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import service.IMChatService;
import tools.AppContext;
import tools.AppManager;
import tools.BaseActivity;
import tools.Logger;

/**
 * wechat
 *
 * @author donal
 *
 */
public class AppActivity extends BaseActivity implements AppActivitySupport{
	protected WCApplication appContext;
	protected Context context = null;
	protected SharedPreferences preferences;
	protected ProgressDialog pg = null;
	protected NotificationManager notificationManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext =  (WCApplication)getApplication();
		context = this;
		preferences = getSharedPreferences(CommonValue.LOGIN_SET, 0);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public WCApplication getWcApplication() {
		return appContext;
	}

	@Override
	public void stopService() {
		Intent chatServer = new Intent(context, IMChatService.class);
		context.stopService(chatServer);
	}

	@Override
	public void startService() {
		Intent chatServer = new Intent(context, IMChatService.class);
		context.startService(chatServer);
	}

	@Override
	public boolean validateInternet() {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			openWirelessSet();
			return false;
		} else {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		openWirelessSet();
		return false;
	}
	
	public void openWirelessSet() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder
				.setTitle(R.string.prompt)
				.setMessage(context.getString(R.string.check_connection))
				.setPositiveButton(R.string.menu_settings,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								Intent intent = new Intent(
										Settings.ACTION_WIRELESS_SETTINGS);
								context.startActivity(intent);
							}
						})
				.setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});
		dialogBuilder.show();
	}

	@Override
	public boolean hasInternetConnected() {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(context.CONNECTIVITY_SERVICE);
		if (manager != null) {
			NetworkInfo network = manager.getActiveNetworkInfo();
			if (network != null && network.isConnectedOrConnecting()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void isExit() {
		new AlertDialog.Builder(context).setTitle("确定退出吗?")
		.setNeutralButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				appContext.exit();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();
	}

	@Override
	public boolean hasLocationGPS() {
		LocationManager manager = (LocationManager) context
				.getSystemService(context.LOCATION_SERVICE);
		if (manager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean hasLocationNetWork() {
		LocationManager manager = (LocationManager) context
				.getSystemService(context.LOCATION_SERVICE);
		if (manager
				.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void checkMemoryCard() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			new AlertDialog.Builder(context)
					.setTitle(R.string.prompt)
					.setMessage("请检查内存卡")
					.setPositiveButton(R.string.menu_settings,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
									Intent intent = new Intent(
											Settings.ACTION_SETTINGS);
									context.startActivity(intent);
								}
							})
					.setNegativeButton("退出",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
									appContext.exit();
								}
							}).create().show();
		}
	}

	@Override
	public void showToast(String text, int longint) {
		Toast.makeText(context, text, longint).show();
	}

	@Override
	public void showToast(String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public ProgressDialog getProgressDialog() {
		return pg;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public SharedPreferences getLoginUserSharedPre() {
		return preferences;
	}
	
	public void saveLoginConfig(UserEntity loginConfig) {
		preferences.edit().putString(CommonValue.USERID, loginConfig.userInfo.userId).commit();
		preferences.edit().putString(CommonValue.APIKEY, loginConfig.apiKey).commit();
	}
	
	public UserEntity getLoginConfig() {
		UserEntity user = new UserEntity();
		UserInfo loginConfig = new UserInfo();
		loginConfig.userId = (preferences.getString(CommonValue.USERID, null));
		user.userInfo = loginConfig;
		return user;
	}

	@Override
	public void setNotiType(int iconId, String contentTitle,
			String contentText, Class activity, String from) {
		Intent notifyIntent = new Intent(this, activity);
		notifyIntent.putExtra("to", from);
		PendingIntent appIntent = PendingIntent.getActivity(this, 0,
				notifyIntent, 0);
		Notification myNoti = new Notification();
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		myNoti.icon = iconId;
		myNoti.tickerText = contentTitle;
		myNoti.defaults = Notification.DEFAULT_SOUND;
		myNoti.setLatestEventInfo(this, contentTitle, contentText, appIntent);
		notificationManager.notify(0, myNoti);
	}
	
	public void closeInput() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null && this.getCurrentFocus() != null) {
			inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
