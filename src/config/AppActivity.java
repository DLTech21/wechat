package config;

import android.os.Bundle;
import tools.AppContext;
import tools.BaseActivity;
import tools.Logger;

/**
 * wechat
 *
 * @author donal
 *
 */
public class AppActivity extends BaseActivity {
	protected WCApplication appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext =  (WCApplication)getApplication();
	}
}
