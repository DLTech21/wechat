package im.model;

/**
 * 
 * 最近联系人显示的与某个的聊天记录
 * 
 */
public class HistoryChatBean {
	public static final int ADD_FRIEND = 1;// 好友请求
	public static final int SYS_MSG = 2; // 系统消息
	public static final int CHAT_MSG = 3;// 聊天消息

	public static final int READ = 0;
	public static final int UNREAD = 1;
	private String id; // 主键
	private String title; // 标题
	private String content; // 最后内容
	private Integer status; // 最后状态 0已读 1未读
	private String from; // 最后通知来源
	private String to; // 最后通知去想
	private String noticeTime; // 最后通知时间
	private Integer noticeSum;// 收到未读消息总数、
	private Integer noticeType; // 消息类型 1.好友请求 2.系统消息

	public Integer getNoticeSum() {
		return noticeSum;
	}

	public void setNoticeSum(Integer noticeSum) {
		this.noticeSum = noticeSum;
	}

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

}
