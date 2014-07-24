/**
 * wechatdonal
 */
package ui;

import java.util.ArrayList;
import java.util.HashMap;

import tools.AppManager;
import tools.ImageUtils;
import tools.UIHelper;
import ui.adapter.TextAdapter;
import bean.UserEntity;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import com.donal.wechat.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import config.ApiClent;
import config.AppActivity;
import config.CommonValue;
import config.ApiClent.ClientCallback;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Login extends AppActivity{
	
	private ProgressDialog loadingPd;
	private InputMethodManager imm;
	private EditText accountET;
	private EditText passwordET;
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);
	}

	  @Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initUI();
		initSDK();
	}
	
	private void initUI() {
		final ListView xlistView = (ListView) findViewById(R.id.xlistview);
		View mHeaderView = getLayoutInflater().inflate(R.layout.login_header, null);
		RelativeLayout layout = (RelativeLayout) mHeaderView.findViewById(R.id.layout);
		xlistView.addHeaderView(mHeaderView);
		LayoutParams p = (LayoutParams) layout.getLayoutParams();
		p.height = ImageUtils.getDisplayHeighth(this)-40;
		layout.setLayoutParams(p);
		xlistView.setAdapter(new TextAdapter(this, new ArrayList<String>()));
		xlistView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scollState) {
				if (scollState == SCROLL_STATE_TOUCH_SCROLL) {
					imm.hideSoftInputFromWindow(xlistView.getWindowToken(), 0);
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				
			}
		});
		accountET = (EditText) mHeaderView.findViewById(R.id.editTextAccount);
		passwordET = (EditText) mHeaderView.findViewById(R.id.editTextPassword);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.registerButton:
			RegisterPage registerPage = new RegisterPage();
			registerPage.setRegisterCallback(new EventHandler() {
				public void afterEvent(int event, int result, Object data) {
					// 解析注册结果
					if (result == SMSSDK.RESULT_COMPLETE) {
						@SuppressWarnings("unchecked")
						HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
						String country = (String) phoneMap.get("country");
						String phone = (String) phoneMap.get("phone");

						// 提交用户信息
						Intent intent = new Intent(Login.this, Register2.class);
						intent.putExtra("mobile", phone);
						startActivityForResult(intent, CommonValue.REQUEST_REGISTER_INFO);
					}
				}
			});
			registerPage.show(this);
			break;

		case R.id.loginButton:
			imm.hideSoftInputFromWindow(passwordET.getWindowToken(), 0);
			login();
			break;
		}
	}
	
	private void register() {
		Intent intent = new Intent(Login.this, Register1.class);
		startActivity(intent);
	}
	
	private void login() {
		String account = accountET.getText().toString();
		final String password = passwordET.getText().toString();
		if (account.length() == 0 ||  password.length() ==0) {
			showToast("请输入账号和密码");
		}
		else {
			loadingPd = UIHelper.showProgress(this, null, null, true);
			ApiClent.login(appContext, account, password, new ClientCallback() {
				@Override
				public void onSuccess(Object data) {
					UIHelper.dismissProgress(loadingPd);
					UserEntity user = (UserEntity) data;
					if (user.status == 1) {
						appContext.saveLoginInfo(user);
						appContext.saveLoginPassword(password);
						saveLoginConfig(appContext.getLoginInfo());
						Intent intent = new Intent(Login.this, Tabbar.class);
						startActivity(intent);
						AppManager.getAppManager().finishActivity(Login.this);
					}
				}
				
				@Override
				public void onFailure(String message) {
					UIHelper.dismissProgress(loadingPd);
				}
				
				@Override
				public void onError(Exception e) {
					UIHelper.dismissProgress(loadingPd);
				}
			});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
//		case CommonValue.LoginAndRegister.RegisterSuccess:
//			setResult(RESULT_OK);
//			AppManager.getAppManager().finishActivity(Login.this);
//			break;
		default:
			break;
		}
	}
	
	private static String APPKEY = "271df56b854c";
	private static String APPSECRET = "3aef03abb6906b0f13dec93616ff56b7";
	private void initSDK() {
		// 初始化短信SDK
		SMSSDK.initSDK(this, APPKEY, APPSECRET);
	}
}
