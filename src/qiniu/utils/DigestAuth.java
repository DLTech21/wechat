package qiniu.utils;

public class DigestAuth {

	public static String sign(Mac mac, byte[] data) throws AuthException {
		if (mac == null) {
			mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		}
		return mac.sign(data);
	}
	
	
	public static String signWithData(Mac mac, byte[] data) throws AuthException {
		if (mac == null) {
			mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		}
		return mac.signWithData(data);
	}
}