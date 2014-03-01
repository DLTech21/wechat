package im.model;


import android.os.Parcel;
import android.os.Parcelable;

public class IMMessage implements Parcelable, Comparable<IMMessage> {
	public static final String IMMESSAGE_KEY = "immessage.key";
	public static final String KEY_TIME = "immessage.time";
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	private int type;
	private String content;
	private String time;
	/**
	 * 存在本地，表示与谁聊天
	 */
	private String fromSubJid;
	/**
	 * 0:接受 1：发送
	 */
	private int msgType = 0;

	public IMMessage() {
		this.type = SUCCESS;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getFromSubJid() {
		return fromSubJid;
	}

	public void setFromSubJid(String fromSubJid) {
		this.fromSubJid = fromSubJid;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(type);
		dest.writeString(content);
		dest.writeString(time);
		dest.writeString(fromSubJid);
		dest.writeInt(msgType);
	}

	public static final Parcelable.Creator<IMMessage> CREATOR = new Parcelable.Creator<IMMessage>() {

		@Override
		public IMMessage createFromParcel(Parcel source) {
			IMMessage message = new IMMessage();
			message.setType(source.readInt());
			message.setContent(source.readString());
			message.setTime(source.readString());
			message.setFromSubJid(source.readString());
			message.setMsgType(source.readInt());
			return message;
		}

		@Override
		public IMMessage[] newArray(int size) {
			return new IMMessage[size];
		}

	};

	/**
	 * 新消息的构造方法.
	 * 
	 * @param content
	 * @param time
	 */
	public IMMessage(String content, String time, String withSb, int msgType) {
		super();
		this.content = content;
		this.time = time;
		this.msgType = msgType;
		this.fromSubJid = withSb;

	}

	/**
	 * 按时间降序排列
	 */
	@Override
	public int compareTo(IMMessage oth) {
		if (null == this.getTime() || null == oth.getTime()) {
			return 0;
		}
		String time1 = "";
		String time2 = "";
		time1 = this.getTime();
		time2 = oth.getTime();
		return time1.compareTo(time2);
	}
}
