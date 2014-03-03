/**
 * wechatdonal
 */
package im.model;

import java.io.Serializable;
import java.util.Date;

import tools.DateUtil;


/**
 * 
 * 消息实体.
 * 
 */
public class Notice implements Serializable, Comparable<Notice> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int ADD_FRIEND = 1;
	public static final int SYS_MSG = 2; 
	public static final int CHAT_MSG = 3;
	public static final int READ = 0;
	public static final int UNREAD = 1;
	public static final int All = 2;

	private String id; 
	private String title; 
	private String content; 
	private Integer status; // 状态 0已读 1未读
	private String from; 
	private String to; 
	private String noticeTime; 
	private Integer noticeType; 

	public Integer getNoticeType() {
		return noticeType;
	}

	public void setNoticeType(Integer noticeType) {
		this.noticeType = noticeType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getNoticeTime() {
		return noticeTime;
	}

	public void setNoticeTime(String noticeTime) {
		this.noticeTime = noticeTime;
	}

	@Override
	public int compareTo(Notice oth) {
		if (null == this.getNoticeTime() || null == oth.getNoticeTime()) {
			return 0;
		}
		String time1 = "";
		String time2 = "";
		time1 = this.getNoticeTime();
		time2 = oth.getNoticeTime();
		return time1.compareTo(time2);
	}

}

