package ui;




import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.donal.wechat.R;

import tools.AppContext;
import tools.AppManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import cn.sharesdk.framework.ShareSDK;

import config.AppActivity;
import config.CommonValue;


/**
 * wechat
 *
 * @author donal
 *
 */
public class Welcome extends AppActivity{
	
	public static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.welcome_page, null);
		setContentView(view);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		double screenSize = diagonalPixels / (160*dm.density);
		appContext.saveScreenSize(screenSize);
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}
			
		});
	}
	
	private void redirectTo(){     
		if(!appContext.isLogin()){
//			if(!showWhatsNewOnFirstLaunch()){
				Intent intent = new Intent(this,Login.class);
				startActivity(intent);
				AppManager.getAppManager().finishActivity(this);
//			}
		}
		else {
			Intent intent = new Intent(this, Tabbar.class);
	        startActivity(intent);
	        AppManager.getAppManager().finishActivity(this);
		}
    }
	
	private boolean showWhatsNewOnFirstLaunch() {
	    try {
		      PackageInfo info = getPackageManager().getPackageInfo(CommonValue.PackageName, 0);
		      int currentVersion = info.versionCode;
		      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		      int lastVersion = prefs.getInt(KEY_HELP_VERSION_SHOWN, 0);
		      if (currentVersion > lastVersion) {
			        prefs.edit().putInt(KEY_HELP_VERSION_SHOWN, currentVersion).commit();
//			        Intent intent = new Intent(this, whatsnew.class);
//			        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//			        startActivity(intent);
//			        finish();
			        return true;
		      	}
	    	} catch (PackageManager.NameNotFoundException e) {
	    		e.printStackTrace();
	    	}
	    return false;
	}
}
