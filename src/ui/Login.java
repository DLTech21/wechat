/**
 * wechatdonal
 */
package ui;

import java.util.ArrayList;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import tools.AppManager;
import tools.ImageUtils;
import tools.UIHelper;
import ui.adapter.TextAdapter;
import widget.XListView;
import xmpp.XmppTool;

import bean.UserEntity;

import com.donal.wechat.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
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
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	  @Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initUI();
	}
	
	private void initUI() {
		final XListView xlistView = (XListView) findViewById(R.id.xlistview);
		xlistView.setPullRefreshEnable(false);
		xlistView.setPullLoadEnable(false);
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
		Intent intent;
		switch (v.getId()) {
		case R.id.registerButton:
			break;

		case R.id.loginButton:
			imm.hideSoftInputFromWindow(passwordET.getWindowToken(), 0);
			login();
			break;
		}
	}
	
	private void login() {
		String account = accountET.getText().toString();
		final String password = passwordET.getText().toString();
		if (account.length() == 0 ||  password.length() ==0) {
			UIHelper.ToastMessage(this, "请输入账号和密码", Toast.LENGTH_SHORT);
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
}
