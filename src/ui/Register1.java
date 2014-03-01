package ui;

import com.donal.wechat.R;

import config.AppActivity;
import config.CommonValue;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;

public class Register1 extends AppActivity{
	private EditText mobileET;
//	private CountDown cd;
	boolean canVertify ;
	int leftSeconds;
	private ProgressDialog loadingPd;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.REQUEST_REGISTER_INFO:
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register1);
		initUI();
	}
	
	private void initUI() {
		Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
		accretionArea(rightBarButton);
		mobileET = (EditText) findViewById(R.id.editTextPhone);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.rightBarButton:
			this.getVertifyCode();
			break;
		}
	}

	private void getVertifyCode() {
		if (StringUtils.isMobileNO(mobileET.getText().toString())) {
			step2();
		}
		else {
			showToast("请输入正确的手机号码");
		}
	}
	
	private void step2() {
		Intent intent = new Intent(Register1.this, Register2.class);
		intent.putExtra("mobile", mobileET.getText().toString());
		startActivityForResult(intent, CommonValue.REQUEST_REGISTER_INFO);
	}
	
}
