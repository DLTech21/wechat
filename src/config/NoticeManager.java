package config;

import im.model.Notice;

import java.util.List;

import tools.StringUtils;

import db.DBManager;
import db.SQLiteTemplate;
import db.SQLiteTemplate.RowMapper;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

/**
 * 
 * 通知，消息管理.
 * 
 */
public class NoticeManager {
	private static NoticeManager noticeManager = null;
	private static DBManager manager = null;

	private NoticeManager(Context context) {
		SharedPreferences sharedPre = context.getSharedPreferences(
				CommonValue.LOGIN_SET, Context.MODE_PRIVATE);
		String databaseName = sharedPre.getString(CommonValue.USERID, null);
		manager = DBManager.getInstance(context, databaseName);
	}

	public static NoticeManager getInstance(Context context) {

		if (noticeManager == null) {
			noticeManager = new NoticeManager(context);
		}

		return noticeManager;
	}

	/**
	 * 
	 * 保存消息.
	 * 
	 * @param notice
	 */
	public long saveNotice(Notice notice) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		if (StringUtils.notEmpty(notice.getTitle())) {
			contentValues.put("title", StringUtils.doEmpty(notice.getTitle()));
		}
		if (StringUtils.notEmpty(notice.getContent())) {
			contentValues.put("content",
					StringUtils.doEmpty(notice.getContent()));
		}
		if (StringUtils.notEmpty(notice.getTo())) {
			contentValues.put("notice_to", StringUtils.doEmpty(notice.getTo()));
		}
		if (StringUtils.notEmpty(notice.getFrom())) {
			contentValues.put("notice_from",
					StringUtils.doEmpty(notice.getFrom()));
		}
		contentValues.put("type", notice.getNoticeType());
		contentValues.put("status", notice.getStatus());
		contentValues.put("notice_time", notice.getNoticeTime());
		return st.insert("im_notice", contentValues);
	}

	/**
	 * 
	 * 获取所有未读消息.
	 * 
	 * @return
	 */
	public List<Notice> getUnReadNoticeList() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<Notice> list = st.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor
						.getColumnIndex("content")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setFrom(cursor.getString(cursor
						.getColumnIndex("notice_from")));
				notice.setTo(cursor.getString(cursor
						.getColumnIndex("notice_to")));
				notice.setNoticeType(cursor.getInt(cursor
						.getColumnIndex("type")));
				notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				return notice;
			}

		}, "select * from im_notice where status=" + Notice.UNREAD + "", null);
		return list;
	}

	/**
	 * 
	 * 更新状态.
	 * 
	 * @param status
	 */
	public void updateStatus(String id, Integer status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		st.updateById("im_notice", id, contentValues);
	}

	/**
	 * 
	 * 更新添加好友状态.
	 * 
	 * @param status
	 */
	public void updateAddFriendStatus(String id, Integer status, String content) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		contentValues.put("content", content);
		st.updateById("im_notice", id, contentValues);
	}

	/**
	 * 
	 * 获取未读消息的条数.
	 * 
	 * @return
	 */
	public Integer getUnReadNoticeCount() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.getCount("select _id from im_notice where status=?",
				new String[] { "" + Notice.UNREAD });
	}

	/**
	 * 
	 * 更具主键获取消息.
	 * 
	 * @param id
	 */
	public Notice getNoticeById(String id) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.queryForObject(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor
						.getColumnIndex("content")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setFrom(cursor.getString(cursor
						.getColumnIndex("notice_from")));
				notice.setTo(cursor.getString(cursor
						.getColumnIndex("notice_to")));
				notice.setNoticeType(cursor.getInt(cursor
						.getColumnIndex("type")));
				notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				return notice;
			}

		}, "select * from im_notice where _id=?", new String[] { id });
	}

	/**
	 * 
	 * 获取所有未读聊消息.(分类)1 好友添加 2系统 消息 3 聊天
	 * 
	 * @return
	 */
	public List<Notice> getUnReadNoticeListByType(int type) {

		String sql;
		String[] str = new String[] { "" + Notice.UNREAD, "" + type };
		sql = "select * from im_notice where status=? and type=? order by notice_time desc";
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<Notice> list = st.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor
						.getColumnIndex("content")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setFrom(cursor.getString(cursor
						.getColumnIndex("notice_from")));
				notice.setTo(cursor.getString(cursor
						.getColumnIndex("notice_to")));
				notice.setNoticeType(cursor.getInt(cursor
						.getColumnIndex("type")));
				notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				notice.setNoticeTime(cursor.getString(cursor
						.getColumnIndex("notice_time")));
				return notice;
			}
		}, sql, str);
		return list;
	}

	/**
	 * 
	 * 获取未读消息的条数（分类）.1 好友添加 2系统 消息 3 聊天
	 * 
	 * @return
	 */
	public Integer getUnReadNoticeCountByType(int type) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.getCount(
				"select _id from im_notice where status=? and type=?",
				new String[] { "" + Notice.UNREAD, "" + type });
	}

	/**
	 * 
	 * 获取来自某人未读消息的条数（分类）.1 好友添加 2系统 消息 3 聊天
	 * 
	 * @return
	 */
	public Integer getUnReadNoticeCountByTypeAndFrom(int type, String from) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st
				.getCount(
						"select _id from im_notice where status=? and type=? and motice_from=?",
						new String[] { "" + Notice.UNREAD, "" + type, from });
	}

	/**
	 * 
	 * 更新某人所有通知状态.
	 * 
	 * @param status
	 */
	public void updateStatusByFrom(String xfrom, Integer status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues values = new ContentValues();
		values.put("status", status);
		st.update("im_notice", values, "notice_from=?", new String[] { ""
				+ xfrom });
	}

	/**
	 * 
	 * 分页获取所有聊消息.(分类)1 好友添加 2系统 消息 3 聊天 降序排列
	 * 
	 * @param isRead
	 *            0 已读 1 未读 2 全部
	 * @param type
	 *            2系统， 3聊天，1 好友添加
	 * @return
	 */
	public List<Notice> getNoticeListByTypeAndPage(int type, int isRead,
			int pageNum, int pageSize) {
		int fromIndex = (pageNum - 1) * pageSize;
		StringBuilder sb = new StringBuilder();
		String[] str = null;
		sb.append("select * from im_notice where type=?");
		if (Notice.UNREAD == isRead || Notice.READ == isRead) {
			str = new String[] { "" + type, "" + isRead, "" + fromIndex,
					"" + pageSize };
			sb.append(" and status=? ");
		} else {
			str = new String[] { "" + type, "" + fromIndex, "" + pageSize };
		}
		sb.append(" order by notice_time desc limit ? , ? ");
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<Notice> list = st.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor
						.getColumnIndex("content")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setFrom(cursor.getString(cursor
						.getColumnIndex("notice_from")));
				notice.setTo(cursor.getString(cursor
						.getColumnIndex("notice_to")));
				notice.setNoticeType(cursor.getInt(cursor
						.getColumnIndex("type")));
				notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				notice.setNoticeTime(cursor.getString(cursor
						.getColumnIndex("notice_time")));
				return notice;
			}
		}, sb.toString(), str);
		return list;
	}

	/**
	 * 
	 * 分页获取所有聊消息.(分类)1 好友添加 2系统 消息 3 聊天 降序排列
	 * 
	 * @param isRead
	 *            0 已读 1 未读 2 全部
	 * @return
	 */
	public List<Notice> getNoticeListByTypeAndPage(int isRead) {

		StringBuilder sb = new StringBuilder();
		String[] str = null;
		sb.append("select * from im_notice where  type in(1,2)");
		if (Notice.UNREAD == isRead || Notice.READ == isRead) {
			str = new String[] { "" + isRead };
			sb.append(" and status=? ");
		}
		sb.append(" order by notice_time desc");
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<Notice> list = st.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor
						.getColumnIndex("content")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setFrom(cursor.getString(cursor
						.getColumnIndex("notice_from")));
				notice.setTo(cursor.getString(cursor
						.getColumnIndex("notice_to")));
				notice.setNoticeType(cursor.getInt(cursor
						.getColumnIndex("type")));
				notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				notice.setNoticeTime(cursor.getString(cursor
						.getColumnIndex("notice_time")));
				return notice;
			}
		}, sb.toString(), str);
		return list;
	}

	/**
	 * 根据id删除记录
	 * 
	 */
	public void delById(String noticeId) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		st.deleteById("im_notice", noticeId);
	}

	/**
	 * 
	 * 删除全部记录
	 * 
	 */
	public void delAll() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		st.execSQL("delete from im_notice");
	}

	/**
	 * 删除与某人的通知
	 * 
	 * @param fromUser
	 */
	public int delNoticeHisWithSb(String fromUser) {
		if (StringUtils.empty(fromUser)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.deleteByCondition("im_notice", "notice_from=?",
				new String[] { "" + fromUser });
	}
}
