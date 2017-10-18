package com.mybatis.jpa.statement.builder;

import java.lang.reflect.Method;

import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.SqlCommandType;

import com.mybatis.jpa.meta.MybatisColumnMeta;
import com.mybatis.jpa.meta.PersistentMeta;
import com.mybatis.jpa.statement.MybatisStatementResolver;
import com.mybatis.jpa.statement.SqlAssistant;

public class InsertSelectiveBuilder extends AbstractStatementBuilder {

	@Override
	public void parseStatementInternal(MybatisStatementResolver resolver, Method method) {
		// 方法名
		resolver.setMethodName(method.getName());
		// 参数类型
		resolver.setParameterTypeClass(Object.class);
		// sqlScript
		resolver.setSqlScript(buildSql(method));
		// 返回值类型
		resolver.setResultType(int.class);
		resolver.setResultMapId(null);

		resolver.setSqlCommandType(SqlCommandType.INSERT);

		// 主键策略
		resolver.setKeyGenerator(new NoKeyGenerator());
		resolver.setKeyProperty(null);
		resolver.setKeyColumn(null);

		resolver.resolve();
	}

	@Override
	protected String buildSqlInternal(Method method, PersistentMeta persistentMeta) {
		// columns
		StringBuilder columns = new StringBuilder();
		columns.append("<trim prefix='(' suffix=')' suffixOverrides=',' > ");
		// values
		StringBuilder values = new StringBuilder();
		values.append("<trim prefix='(' suffix=')' suffixOverrides=',' > ");
		for (MybatisColumnMeta columnMeta : persistentMeta.getColumnMetaMap().values()) {
			// columns
			columns.append("<if test='" + columnMeta.getProperty() + "!= null'> ");
			columns.append(columnMeta.getColumnName() + ", ");
			columns.append("</if> ");
			// values
			values.append("<if test='" + columnMeta.getProperty() + "!= null'> ");
			values.append(SqlAssistant.resolveSqlParameter(columnMeta) + ", ");
			values.append("</if> ");
		}

		columns.append("</trim> ");
		values.append("</trim> ");

		return "<script>" + "INSERT INTO " + persistentMeta.getTableName() + columns.toString() + " VALUES "
				+ values.toString() + "</script>";
	}

}
