package xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import config.CommonValue;

/**
 * @author Sam.Io
 * @time 2011/11/18
 * @project AdXmpp
 */
public class XmppTool {

	private static XMPPConnection con = null;
	
	private static void openConnection() {
		try {
			ConnectionConfiguration connConfig = new ConnectionConfiguration(CommonValue.XMPP_HOST, 5222);
			con = new XMPPConnection(connConfig);
			con.connect();
		} catch (XMPPException xe) {
			xe.printStackTrace();
		}
	}

	public static XMPPConnection getConnection() {
		if (con == null) {
			openConnection();
		}
		return con;
	}

	public static void closeConnection() {
		con.disconnect();
		con = null;
	}
}
