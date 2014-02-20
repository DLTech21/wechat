/**
 * wechatdonal
 */
package bean;

import java.io.Serializable;

import tools.AppException;
import tools.Logger;

import com.google.gson.Gson;

/**
 * wechat
 *
 * @author donal
 *
 */
public class UserInfo implements Serializable {
	public String userId;
	public String nickName;
	public String description;
	public String registerDate;
	public String userHead;
	
	/**
	 * @param string
	 * @return
	 * @throws AppException 
	 */
	public static UserInfo parse(String string) throws AppException {
		UserInfo data = new UserInfo();
		try {
			Gson gson = new Gson();
			data = gson.fromJson(string, UserInfo.class);
		} catch (Exception e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
}
