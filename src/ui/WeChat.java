package ui;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import tools.Logger;
import xmpp.XmppTool;

import com.donal.wechat.R;

import android.os.Bundle;
import android.util.Log;
import config.AppActivity;

/**
 * wechat
 *
 * @author donal
 *
 */
public class WeChat extends AppActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wechat);
		
		try {
			connect2xmpp();
		} catch (XMPPException e) {
			Logger.i(e);
		}
	}
	
	private void connect2xmpp() throws XMPPException {
		String password = appContext.getLoginPassword();
		String userId = appContext.getLoginUid();
		XmppTool.getConnection().login(userId, password);
		Logger.i("XMPPClient Logged in as " + XmppTool.getConnection().getUser());
		// status
		Presence presence = new Presence(Presence.Type.available);
		XmppTool.getConnection().sendPacket(presence);
	}
}
