package config;

import java.util.List;
import java.util.Properties;

import org.apache.http.client.CookieStore;

import bean.UserEntity;
import bean.UserInfo;

import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.utils.L;

import tools.AppContext;
import tools.AppException;
import tools.ImageCacheUtil;
import tools.Logger;
import tools.StringUtils;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

/**
 * wechat
 *
 * @author donal
 *
 */
public class WCApplication extends AppContext {
	private static WCApplication mApplication;
	
	private NotificationManager mNotificationManager;
	
	private boolean login = false;	//登录状态
	private String loginUid = "0";	//登录用户的id
	private String apiKey = "0";	//登录用户的id
	
	public synchronized static WCApplication getInstance() {
		return mApplication;
	}
	
	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}
	
	public void onCreate() {
		mApplication = this;
		Logger.setDebug(true);
		Logger.getLogger().setTag("wechat");
		ImageCacheUtil.init(this);
		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		L.disableLogging();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		CookieStore cookieStore = new PersistentCookieStore(this);  
		QYRestClient.getIntance().setCookieStore(cookieStore);
		
		Intent intent = new Intent();
        intent.setAction("tools.NetworkState.Service");
        startService(intent);
	}
	
	/**
	 * 用户是否登录
	 * @return
	 */
	public boolean isLogin() {
		try {
			String loginStr = getProperty("user.login");
			if (StringUtils.isEmpty(loginStr)) {
				login = false;
			}
			else {
				login = (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return login;
	}

	/**
	 * 保存登录信息
	 * @param username
	 * @param pwd
	 */
	@SuppressWarnings("serial")
	public void saveLoginInfo(final UserEntity user) {
		this.loginUid = user.userInfo.userId;
		this.apiKey = user.apiKey;
		this.login = true;
		setProperties(new Properties(){
			{
				setProperty("user.login","1");
				setProperty("user.uid", user.userInfo.userId);
				setProperty("user.name", user.userInfo.nickName);
				setProperty("user.face", user.userInfo.userHead);
				setProperty("user.description", user.userInfo.description);
				setProperty("user.registerDate", user.userInfo.registerDate);
				setProperty("user.apikey", user.apiKey);
			}
		});		
	}

	/**
	 * 获取登录用户id
	 * @return
	 */
	public String getLoginUid() {
		return (getProperty("user.uid"));
	}
	
	public String getLoginApiKey() {
		return (getProperty("user.apikey"));
	}
	
	
	public void saveLoginPassword(final String password) {
		setProperties(new Properties(){
			{
				setProperty("user.password",password);
			}
		});		
	}
	
	public String getLoginPassword() {
		return (getProperty("user.password"));
	}
	/**
	 * 获取登录信息
	 * @return
	 */
	public UserEntity getLoginInfo() {		
		UserEntity lu = new UserEntity();		
		UserInfo userInfo = new UserInfo();
		userInfo.userId = (getProperty("user.uid"));
		userInfo.nickName = (getProperty("user.name"));
		userInfo.userHead = (getProperty("user.face"));
		userInfo.description = (getProperty("user.description"));
		userInfo.registerDate = (getProperty("user.registerDate"));
		lu.apiKey = (getProperty("user.apikey"));
		lu.userInfo = userInfo;
		return lu;
	}
	
	public String getNickname() {		
		return (getProperty("user.name"));
	}
	
	/**
	 * 退出登录
	 */
	public void setUserLogout() {
		this.login = false;
		setProperties(new Properties(){
			{
				setProperty("user.login","0");
			}
		});	
	}
	
	public boolean isNeedCheckLogin() {
		try {
			String loginStr = getProperty("user.needchecklogin");
			if (StringUtils.isEmpty(loginStr)) {
				return false;
			}
			else {
				return (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setNeedCheckLogin() {
		setProperties(new Properties(){
			{
				setProperty("user.needchecklogin","1");
			}
		});
	}
	
	public void saveNotiWhen(final String when) {
		setProperties(new Properties(){
			{
				setProperty("noti.when",when);
			}
		});
	}
	
	public String getNotiWhen() {
		try {
			String loginStr = getProperty("noti.when");
			if (StringUtils.isEmpty(loginStr)) {
				return "0";
			}
			else {
				return loginStr;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "0";
	}
	
}
