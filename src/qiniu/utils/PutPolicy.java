package qiniu.utils;

import org.json.JSONException;
import org.json.JSONStringer;

public class PutPolicy {
	/** ������������������ bucketName ������ bucketName:key */
	public String scope;
	/** ������ */
	public String callbackUrl;
	/** ������ */
	public String callbackBody;
	/** ������ */
	public String returnUrl;
	/** ������ */
	public String returnBody;
	/** ������ */
	public String asyncOps;
	/** ������ */
	public String endUser;
	/** ������ */
	public long expires;
	/** ������ */
	public String saveKey;
	/** ��������� ������0, ������Scope��� Bucket:Key ���������������insert only*/
	public int insertOnly;
	/** ���������������0, ������������������������������������ MimeType */
	public int detectMime;
	/** ������ */
	public long fsizeLimit;
	/** ������ */
	public String persistentNotifyUrl;
	/** ������ */
	public String persistentOps;
	
	public long deadline;

	public PutPolicy(String scope) {
		this.scope = scope;
	}

	public String marshal() throws JSONException {
		JSONStringer stringer = new JSONStringer();
		stringer.object();
		stringer.key("scope").value(this.scope);
		if (this.callbackUrl != null && this.callbackUrl.length() > 0) {
			stringer.key("callbackUrl").value(this.callbackUrl);
		}
		if (this.callbackBody != null && this.callbackBody.length() > 0) {
			stringer.key("callbackBody").value(this.callbackBody);
		}
		if (this.returnUrl != null && this.returnUrl.length() > 0) {
			stringer.key("returnUrl").value(this.returnUrl);
		}
		if (this.returnBody != null && this.returnBody.length() > 0) {
			stringer.key("returnBody").value(this.returnBody);
		}
		if (this.asyncOps != null && this.asyncOps.length() > 0) {
			stringer.key("asyncOps").value(this.asyncOps);
		}
		if (this.saveKey != null && this.saveKey.length() > 0) {
			stringer.key("saveKey").value(this.saveKey);
		}
		if(this.insertOnly>0){
			stringer.key("insertOnly").value(this.insertOnly);
		}
		if(this.detectMime>0){
			stringer.key("detectMime").value(this.detectMime);
		}
		if(this.fsizeLimit>0){
			stringer.key("fsizeLimit").value(this.fsizeLimit);
		}
		if (this.endUser != null && this.endUser.length() > 0) {
			stringer.key("endUser").value(this.endUser);
		}
		if (this.persistentNotifyUrl != null && this.persistentNotifyUrl.length() > 0) {
			stringer.key("persistentNotifyUrl").value(this.persistentNotifyUrl);
		}
		if (this.persistentOps != null && this.persistentOps.length() > 0) {
			stringer.key("persistentOps").value(this.persistentOps);
		}
		stringer.key("deadline").value(this.deadline);
		stringer.endObject();

		return stringer.toString();
	}

	
	/**
	 * makes an upload token.
	 * @param mac
	 * @return
	 * @throws AuthException
	 * @throws JSONException
	 */
	
	public String token(Mac mac) throws AuthException, JSONException {
		if (this.expires == 0) {
			this.expires = 3600; // 3600s, default.
		}
		this.deadline = System.currentTimeMillis() / 1000 + this.expires;
		byte[] data = this.marshal().getBytes();
		return DigestAuth.signWithData(mac, data);
	}

}
