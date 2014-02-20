package db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * SQLite数据库模板工具类
 * 
 * 该类提供了数据库操作常用的增删改查,以及各种复杂条件匹配,分页,排序等操作
 * 
 * @see SQLiteDatabase
 */
public class SQLiteTemplate {
	/**
	 * Default Primary key
	 */
	protected String mPrimaryKey = "_id";

	/**
	 * DBManager
	 */
	private DBManager dBManager;
	/**
	 * 是否为一个事务
	 */
	private boolean isTransaction = false;
	/**
	 * 数据库连接
	 */
	private SQLiteDatabase dataBase = null;

	private SQLiteTemplate() {
	}

	private SQLiteTemplate(DBManager dBManager, boolean isTransaction) {
		this.dBManager = dBManager;
		this.isTransaction = isTransaction;
	}

	/**
	 * isTransaction 是否属于一个事务 注:一旦isTransaction设为true
	 * 所有的SQLiteTemplate方法都不会自动关闭资源,需在事务成功后手动关闭
	 * 
	 * @return
	 */
	public static SQLiteTemplate getInstance(DBManager dBManager,
			boolean isTransaction) {
		return new SQLiteTemplate(dBManager, isTransaction);
	}

	/**
	 * 执行一条sql语句
	 * 
	 * @param name
	 * @param tel
	 */
	public void execSQL(String sql) {
		try {
			dataBase = dBManager.openDatabase();
			dataBase.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
	}

	/**
	 * 执行一条sql语句
	 * 
	 * @param name
	 * @param tel
	 */
	public void execSQL(String sql, Object[] bindArgs) {
		try {
			dataBase = dBManager.openDatabase();
			dataBase.execSQL(sql, bindArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
	}

	/**
	 * 向数据库表中插入一条数据
	 * 
	 * @param table
	 *            表名
	 * @param content
	 *            字段值
	 */
	public long insert(String table, ContentValues content) {
		try {
			dataBase = dBManager.openDatabase();
			// insert方法第一参数：数据库表名，第二个参数如果CONTENT为空时则向表中插入一个NULL,第三个参数为插入的内容
			return dataBase.insert(table, null, content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return 0;
	}

	/**
	 * 批量删除指定主键数据
	 * 
	 * @param ids
	 */
	public void deleteByIds(String table, Object... primaryKeys) {
		try {
			if (primaryKeys.length > 0) {
				StringBuilder sb = new StringBuilder();
				for (@SuppressWarnings("unused")
				Object id : primaryKeys) {
					sb.append("?").append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				dataBase = dBManager.openDatabase();
				dataBase.execSQL("delete from " + table + " where "
						+ mPrimaryKey + " in(" + sb + ")",
						(Object[]) primaryKeys);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
	}

	/**
	 * 根据某一个字段和值删除一行数据, 如 name="jack"
	 * 
	 * @param table
	 * @param field
	 * @param value
	 * @return 返回值大于0表示删除成功
	 */
	public int deleteByField(String table, String field, String value) {
		try {
			dataBase = dBManager.openDatabase();
			return dataBase.delete(table, field + "=?", new String[] { value });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return 0;
	}

	/**
	 * 根据条件删除数据
	 * 
	 * @param table
	 *            表名
	 * @param whereClause
	 *            查询语句 参数采用?
	 * @param whereArgs
	 *            参数值
	 * @return 返回值大于0表示删除成功
	 */
	public int deleteByCondition(String table, String whereClause,
			String[] whereArgs) {
		try {
			dataBase = dBManager.openDatabase();
			return dataBase.delete(table, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return 0;
	}

	/**
	 * 根据主键删除一行数据
	 * 
	 * @param table
	 * @param id
	 * @return 返回值大于0表示删除成功
	 */
	public int deleteById(String table, String id) {
		try {
			dataBase = dBManager.openDatabase();
			return deleteByField(table, mPrimaryKey, id);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return 0;
	}

	/**
	 * 根据主键更新一行数据
	 * 
	 * @param table
	 * @param id
	 * @param values
	 * @return 返回值大于0表示更新成功
	 */
	public int updateById(String table, String id, ContentValues values) {
		try {
			dataBase = dBManager.openDatabase();
			return dataBase.update(table, values, mPrimaryKey + "=?",
					new String[] { id });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return 0;
	}

	/**
	 * 更新数据
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return 返回值大于0表示更新成功
	 */
	public int update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {
		try {
			dataBase = dBManager.openDatabase();
			return dataBase.update(table, values, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return 0;
	}

	/**
	 * 根据主键查看某条数据是否存在
	 * 
	 * @param table
	 * @param id
	 * @return
	 */
	public Boolean isExistsById(String table, String id) {
		try {
			dataBase = dBManager.openDatabase();
			return isExistsByField(table, mPrimaryKey, id);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return null;
	}

	/**
	 * 根据某字段/值查看某条数据是否存在
	 * 
	 * @param status
	 * @return
	 */
	public Boolean isExistsByField(String table, String field, String value) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
				.append(field).append(" =?");
		try {
			dataBase = dBManager.openDatabase();
			return isExistsBySQL(sql.toString(), new String[] { value });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(null);
			}
		}
		return null;
	}

	/**
	 * 使用SQL语句查看某条数据是否存在
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public Boolean isExistsBySQL(String sql, String[] selectionArgs) {
		Cursor cursor = null;
		try {
			dataBase = dBManager.openDatabase();
			cursor = dataBase.rawQuery(sql, selectionArgs);
			if (cursor.moveToFirst()) {
				return (cursor.getInt(0) > 0);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(cursor);
			}
		}
		return null;
	}

	/**
	 * 查询一条数据
	 * 
	 * @param rowMapper
	 * @param sql
	 * @param args
	 * @return
	 */
	public <T> T queryForObject(RowMapper<T> rowMapper, String sql,
			String[] args) {
		Cursor cursor = null;
		T object = null;
		try {
			dataBase = dBManager.openDatabase();
			cursor = dataBase.rawQuery(sql, args);
			if (cursor.moveToFirst()) {
				object = rowMapper.mapRow(cursor, cursor.getCount());
			}
		} finally {
			if (!isTransaction) {
				closeDatabase(cursor);
			}
		}
		return object;

	}

	/**
	 * 查询
	 * 
	 * @param rowMapper
	 * @param sql
	 * @param startResult
	 *            开始索引 注:第一条记录索引为0
	 * @param maxResult
	 *            步长
	 * @return
	 */
	public <T> List<T> queryForList(RowMapper<T> rowMapper, String sql,
			String[] selectionArgs) {
		Cursor cursor = null;
		List<T> list = null;
		try {
			dataBase = dBManager.openDatabase();
			cursor = dataBase.rawQuery(sql, selectionArgs);
			list = new ArrayList<T>();
			while (cursor.moveToNext()) {
				list.add(rowMapper.mapRow(cursor, cursor.getPosition()));
			}
		} finally {
			if (!isTransaction) {
				closeDatabase(cursor);
			}
		}
		return list;
	}

	/**
	 * 分页查询
	 * 
	 * @param rowMapper
	 * @param sql
	 * @param startResult
	 *            开始索引 注:第一条记录索引为0
	 * @param maxResult
	 *            步长
	 * @return
	 */
	public <T> List<T> queryForList(RowMapper<T> rowMapper, String sql,
			int startResult, int maxResult) {
		Cursor cursor = null;
		List<T> list = null;
		try {
			dataBase = dBManager.openDatabase();
			cursor = dataBase.rawQuery(sql + " limit ?,?", new String[] {
					String.valueOf(startResult), String.valueOf(maxResult) });
			list = new ArrayList<T>();
			while (cursor.moveToNext()) {
				list.add(rowMapper.mapRow(cursor, cursor.getPosition()));
			}
		} finally {
			if (!isTransaction) {
				closeDatabase(cursor);
			}
		}
		return list;
	}

	/**
	 * 获取记录数
	 * 
	 * @return
	 */
	public Integer getCount(String sql, String[] args) {
		Cursor cursor = null;
		try {
			dataBase = dBManager.openDatabase();
			cursor = dataBase.rawQuery("select count(*) from (" + sql + ")",
					args);
			if (cursor.moveToNext()) {
				return cursor.getInt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!isTransaction) {
				closeDatabase(cursor);
			}
		}
		return 0;
	}

	/**
	 * 分页查询
	 * 
	 * @param rowMapper
	 * @param table
	 *            检索的表
	 * @param columns
	 *            由需要返回列的列名所组成的字符串数组，传入null会返回所有的列。
	 * @param selection
	 *            查询条件子句，相当于select语句where关键字后面的部分，在条件子句允许使用占位符"?"
	 * @param selectionArgs
	 *            对应于selection语句中占位符的值，值在数组中的位置与占位符在语句中的位置必须一致，否则就会有异常
	 * @param groupBy
	 *            对结果集进行分组的group by语句（不包括GROUP BY关键字）。传入null将不对结果集进行分组
	 * @param having
	 *            对查询后的结果集进行过滤,传入null则不过滤
	 * @param orderBy
	 *            对结果集进行排序的order by语句（不包括ORDER BY关键字）。传入null将对结果集使用默认的排序
	 * @param limit
	 *            指定偏移量和获取的记录数，相当于select语句limit关键字后面的部分,如果为null则返回所有行
	 * @return
	 */
	public <T> List<T> queryForList(RowMapper<T> rowMapper, String table,
			String[] columns, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, String limit) {
		List<T> list = null;
		Cursor cursor = null;
		try {
			dataBase = dBManager.openDatabase();
			cursor = dataBase.query(table, columns, selection, selectionArgs,
					groupBy, having, orderBy, limit);
			list = new ArrayList<T>();
			while (cursor.moveToNext()) {
				list.add(rowMapper.mapRow(cursor, cursor.getPosition()));
			}
		} finally {
			if (!isTransaction) {
				closeDatabase(cursor);
			}
		}
		return list;
	}

	/**
	 * Get Primary Key
	 * 
	 * @return
	 */
	public String getPrimaryKey() {
		return mPrimaryKey;
	}

	/**
	 * Set Primary Key
	 * 
	 * @param primaryKey
	 */
	public void setPrimaryKey(String primaryKey) {
		this.mPrimaryKey = primaryKey;
	}

	/**
	 * 
	 * @author shimiso
	 * 
	 * @param <T>
	 */
	public interface RowMapper<T> {
		/**
		 * 
		 * @param cursor
		 *            游标
		 * @param index
		 *            下标索引
		 * @return
		 */
		public T mapRow(Cursor cursor, int index);
	}

	/**
	 * 关闭数据库
	 */
	public void closeDatabase(Cursor cursor) {
		if (null != dataBase) {
			dataBase.close();
		}
		if (null != cursor) {
			cursor.close();
		}
	}
}
