package config;

public class Constant {
	/**
	 * 所有的action的监听的必须要以"ACTION_"开头
	 * 
	 */

	/**
	 * 花名册有删除的ACTION和KEY
	 */
	public static final String ROSTER_DELETED = "roster.deleted";
	public static final String ROSTER_DELETED_KEY = "roster.deleted.key";

	/**
	 * 花名册有更新的ACTION和KEY
	 */
	public static final String ROSTER_UPDATED = "roster.updated";
	public static final String ROSTER_UPDATED_KEY = "roster.updated.key";

	/**
	 * 花名册有增加的ACTION和KEY
	 */
	public static final String ROSTER_ADDED = "roster.added";
	public static final String ROSTER_ADDED_KEY = "roster.added.key";

	/**
	 * 花名册中成员状态有改变的ACTION和KEY
	 */
	public static final String ROSTER_PRESENCE_CHANGED = "roster.presence.changed";
	public static final String ROSTER_PRESENCE_CHANGED_KEY = "roster.presence.changed.key";

	/**
	 * 收到好友邀请请求
	 */
	public static final String ROSTER_SUBSCRIPTION = "roster.subscribe";
	public static final String ROSTER_SUB_FROM = "roster.subscribe.from";
	public static final String NOTICE_ID = "notice.id";

	public static final String NEW_MESSAGE_ACTION = "roster.newmessage";

	/**
	 * 我的消息
	 */
	public static final String MY_NEWS = "my.news";
	public static final String MY_NEWS_DATE = "my.news.date";

	/**
	 * USERINFO
	 */
	public static final String LOGIN_SET = "login_set";// 登录设置
	public static final String USERID = "userId";
	public static final String APIKEY = "apiKey";
	/**
	 * 登录提示
	 */
	public static final int LOGIN_SECCESS = 0;// 成功
	public static final int HAS_NEW_VERSION = 1;// 发现新版本
	public static final int IS_NEW_VERSION = 2;// 当前版本为最新
	public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// 账号或者密码错误
	public static final int SERVER_UNAVAILABLE = 4;// 无法连接到服务器
	public static final int LOGIN_ERROR = 5;// 连接失败

	public static final String XMPP_CONNECTION_CLOSED = "xmpp_connection_closed";// 连接中断

	public static final String LOGIN = "login"; // 登录
	public static final String RELOGIN = "relogin"; // 重新登录

	/**
	 * 好友列表 组名
	 */
	public static final String ALL_FRIEND = "所有好友";// 所有好友
	public static final String NO_GROUP_FRIEND = "未分组好友";// 所有好友
	/**
	 * 系统消息
	 */
	public static final String ACTION_SYS_MSG = "action_sys_msg";// 消息类型关键字
	public static final String MSG_TYPE = "broadcast";// 消息类型关键字
	public static final String SYS_MSG = "sysMsg";// 系统消息关键字
	public static final String SYS_MSG_DIS = "系统消息";// 系统消息
	public static final String ADD_FRIEND_QEQUEST = "好友请求";// 系统消息关键字
	/**
	 * 请求某个操作返回的状态值
	 */
	public static final int SUCCESS = 0;// 存在
	public static final int FAIL = 1;// 不存在
	public static final int UNKNOWERROR = 2;// 出现莫名的错误.
	public static final int NETWORKERROR = 3;// 网络错误
	/***
	 * 企业通讯录根据用户ｉｄ和用户名去查找人员中的请求ｘｍｌ是否包含自组织
	 */
	public static final int containsZz = 0;
	/***
	 * 创建请求分组联系人列表xml分页参数
	 */
	public static final String currentpage = "1";// 当前第几页
	public static final String pagesize = "1000";// 当前页的条数

	/***
	 * 创建请求xml操作类型
	 */
	public static final String add = "00";// 增加
	public static final String rename = "01";// 增加
	public static final String remove = "02";// 增加

	/**
	 * 重连接
	 */
	/**
	 * 重连接状态acttion
	 * 
	 */
	public static final String ACTION_RECONNECT_STATE = "action_reconnect_state";
	/**
	 * 描述冲连接状态的关机子，寄放的intent的关键字
	 */
	public static final String RECONNECT_STATE = "reconnect_state";
	/**
	 * 描述冲连接，
	 */
	public static final boolean RECONNECT_STATE_SUCCESS = true;
	public static final boolean RECONNECT_STATE_FAIL = false;
	/**
	 * 是否在线的SharedPreferences名称
	 */
	public static final String PREFENCE_USER_STATE = "prefence_user_state";
	public static final String IS_ONLINE = "is_online";
	/**
	 * 精确到毫秒
	 */
	public static final String MS_FORMART = "yyyy-MM-dd HH:mm:ss SSS";

}
