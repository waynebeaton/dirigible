/*
 * Copyright (c) 2010-2021 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2010-2021 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.database.sql.builders.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.dirigible.database.sql.DataType;
import org.eclipse.dirigible.database.sql.ISqlDialect;
import org.eclipse.dirigible.database.sql.builders.AbstractCreateSqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Create Table Builder.
 */
public abstract class AbstractTableBuilder<TABLE_BUILDER extends AbstractTableBuilder> extends AbstractCreateSqlBuilder {

	private static final Logger logger = LoggerFactory.getLogger(AbstractTableBuilder.class);

	private String table = null;

	private List<String[]> columns = new ArrayList<String[]>();

	/**
	 * Instantiates a new creates the table builder.
	 *
	 * @param dialect
	 *            the dialect
	 * @param table
	 *            the table
	 */
	public AbstractTableBuilder(ISqlDialect dialect, String table) {
		super(dialect);
		this.table = table;
	}

	/**
	 * Gets the table.
	 *
	 * @return the table
	 */
	protected String getTable() {
		return table;
	}

	/**
	 * Gets the columns.
	 *
	 * @return the columns
	 */
	protected List<String[]> getColumns() {
		return columns;
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, DataType type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String... args) {
		logger.trace("column: " + name + ", type: " + (type != null ? type.name() : null) + ", isPrimaryKey: " + isPrimaryKey + ", isNullable: "
				+ isNullable + ", isUnique: " + isUnique + ", isIdentity: " + isIdentity + ", args: " + Arrays.toString(args));
		String[] definition = new String[] { name, getDialect().getDataTypeName(type) };
		String[] column = null;
		if (isIdentity) {
			column = Stream.of(definition, args, new String[] { getDialect().getIdentityArgument() }).flatMap(Stream::of).toArray(String[]::new);
		} else {
			column = Stream.of(definition, args).flatMap(Stream::of).toArray(String[]::new);
		}
		if (!isNullable) {
			column = Stream.of(column, new String[] { getDialect().getNotNullArgument() }).flatMap(Stream::of).toArray(String[]::new);
		}
		if (isPrimaryKey) {
			column = Stream.of(column, new String[] { getDialect().getPrimaryKeyArgument() }).flatMap(Stream::of).toArray(String[]::new);
		}
		if (isUnique && !isPrimaryKey) {
			column = Stream.of(column, new String[] { getDialect().getUniqueArgument() }).flatMap(Stream::of).toArray(String[]::new);
		}

		this.columns.add(column);
		return (TABLE_BUILDER) this;
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, DataType type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return column(name, type, isPrimaryKey, isNullable, isUnique, false, args);
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, DataType type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return column(name, type, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, DataType type, Boolean isPrimaryKey, Boolean isNullable) {
		return column(name, type, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, DataType type, Boolean isPrimaryKey) {
		return column(name, type, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, DataType type) {
		return column(name, type, false, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, int type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return column(name, DataType.values()[type], isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, int type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return column(name, DataType.values()[type], isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, int type, Boolean isPrimaryKey, Boolean isNullable) {
		return column(name, DataType.values()[type], isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, int type, Boolean isPrimaryKey) {
		return column(name, DataType.values()[type], isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, int type) {
		return column(name, DataType.values()[type], false, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, int type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return column(name, DataType.values()[type], isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, Double type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return column(name, DataType.values()[type.intValue()], isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, Double type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return column(name, DataType.values()[type.intValue()], isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, Double type, Boolean isPrimaryKey, Boolean isNullable) {
		return column(name, DataType.values()[type.intValue()], isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, Double type, Boolean isPrimaryKey) {
		return column(name, DataType.values()[type.intValue()], isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, Double type) {
		return column(name, DataType.values()[type.intValue()], false, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, Double type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return column(name, DataType.values()[type.intValue()], isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, String type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return column(name, DataType.valueOf(type), isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, String type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return column(name, DataType.valueOf(type), isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, String type, Boolean isPrimaryKey, Boolean isNullable) {
		return column(name, DataType.valueOf(type), isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, String type, Boolean isPrimaryKey) {
		return column(name, DataType.valueOf(type), isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, String type) {
		return column(name, DataType.valueOf(type), false, true, false, new String[] {});
	}

	/**
	 * Column.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER column(String name, String type, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return column(name, DataType.valueOf(type), isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column varchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnVarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String... args) {
		String[] definition = new String[] { OPEN + length + CLOSE };
		String[] coulmn = Stream.of(definition, args).flatMap(Stream::of).toArray(String[]::new);
		return this.column(name, DataType.VARCHAR, isPrimaryKey, isNullable, isUnique, isIdentity, coulmn);
	}

	/**
	 * Column varchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnVarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String args) {
		return columnVarchar(name, length, isPrimaryKey, isNullable, isUnique, isIdentity, splitValues(args));
	}

	/**
	 * Column varchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnVarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity) {
		return columnVarchar(name, length, isPrimaryKey, isNullable, isUnique, isIdentity, new String[] {});
	}
	
	/**
	 * Column varchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnVarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnVarchar(name, length, isPrimaryKey, isNullable, isUnique, false);
	}

	/**
	 * Column varchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnVarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable) {
		return columnVarchar(name, length, isPrimaryKey, isNullable, false);
	}

	/**
	 * Column varchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnVarchar(String name, int length, Boolean isPrimaryKey) {
		return columnVarchar(name, length, isPrimaryKey, true);
	}

	/**
	 * Column varchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnVarchar(String name, int length) {
		return columnVarchar(name, length, false);
	}
	
	/**
	 * Column nvarchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnNvarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String... args) {
		String[] definition = new String[] { OPEN + length + CLOSE };
		String[] coulmn = Stream.of(definition, args).flatMap(Stream::of).toArray(String[]::new);
		return this.column(name, DataType.NVARCHAR, isPrimaryKey, isNullable, isUnique, isIdentity, coulmn);
	}

	/**
	 * Column nvarchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnNvarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String args) {
		return columnNvarchar(name, length, isPrimaryKey, isNullable, isUnique, isIdentity, splitValues(args));
	}

	/**
	 * Column nvarchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnNvarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity) {
		return columnNvarchar(name, length, isPrimaryKey, isNullable, isUnique, isIdentity, new String[] {});
	}
	
	/**
	 * Column nvarchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnNvarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnNvarchar(name, length, isPrimaryKey, isNullable, isUnique, false);
	}

	/**
	 * Column nvarchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnNvarchar(String name, int length, Boolean isPrimaryKey, Boolean isNullable) {
		return columnNvarchar(name, length, isPrimaryKey, isNullable, false);
	}

	/**
	 * Column nvarchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnNvarchar(String name, int length, Boolean isPrimaryKey) {
		return columnNvarchar(name, length, isPrimaryKey, true);
	}

	/**
	 * Column nvarchar.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnNvarchar(String name, int length) {
		return columnNvarchar(name, length, false);
	}
	

	/**
	 * Column char.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnChar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String... args) {
		String[] definition = new String[] { OPEN + length + CLOSE };
		String[] coulmn = Stream.of(definition, args).flatMap(Stream::of).toArray(String[]::new);
		return this.column(name, DataType.CHAR, isPrimaryKey, isNullable, isUnique, isIdentity, coulmn);
	}

	/**
	 * Column char.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnChar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnChar(name, length, isPrimaryKey, isNullable, isUnique, false, splitValues(args));
	}

	/**
	 * Column char.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnChar(String name, int length, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnChar(name, length, isPrimaryKey, isNullable, isUnique, false, new String[] {});
	}

	/**
	 * Column char.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnChar(String name, int length, Boolean isPrimaryKey, Boolean isNullable) {
		return columnChar(name, length, isPrimaryKey, isNullable, false);
	}

	/**
	 * Column char.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnChar(String name, int length, Boolean isPrimaryKey) {
		return columnChar(name, length, isPrimaryKey, true);
	}

	/**
	 * Column char.
	 *
	 * @param name
	 *            the name
	 * @param length
	 *            the length
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnChar(String name, int length) {
		return columnChar(name, length, false);
	}

	/**
	 * Column date.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDate(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.DATE, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column date.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDate(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnDate(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column date.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDate(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnDate(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column date.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDate(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnDate(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column date.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDate(String name, Boolean isPrimaryKey) {
		return columnDate(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column date.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDate(String name) {
		return columnDate(name, false, true, false, new String[] {});
	}

	/**
	 * Column time.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTime(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.TIME, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column time.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTime(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnTime(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column time.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTime(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnTime(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column time.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTime(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnTime(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column time.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTime(String name, Boolean isPrimaryKey) {
		return columnTime(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column time.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTime(String name) {
		return columnTime(name, false, true, false, new String[] {});
	}

	/**
	 * Column timestamp.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTimestamp(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.TIMESTAMP, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column timestamp.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTimestamp(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnTimestamp(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column timestamp.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTimestamp(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnTimestamp(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column timestamp.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTimestamp(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnTimestamp(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column timestamp.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTimestamp(String name, Boolean isPrimaryKey) {
		return columnTimestamp(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column timestamp.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTimestamp(String name) {
		return columnTimestamp(name, false, true, false, new String[] {});
	}

	/**
	 * Column integer.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnInteger(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String... args) {
		return this.column(name, DataType.INTEGER, isPrimaryKey, isNullable, isUnique, isIdentity, args);
	}

	/**
	 * Column integer.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnInteger(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String args) {
		return columnInteger(name, isPrimaryKey, isNullable, isUnique, isIdentity, splitValues(args));
	}

	/**
	 * Column integer.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnInteger(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnInteger(name, isPrimaryKey, isNullable, isUnique, false, new String[] {});
	}

	/**
	 * Column integer.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnInteger(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnInteger(name, isPrimaryKey, isNullable, false);
	}

	/**
	 * Column integer.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnInteger(String name, Boolean isPrimaryKey) {
		return columnInteger(name, isPrimaryKey, true);
	}

	/**
	 * Column integer.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnInteger(String name) {
		return columnInteger(name, false);
	}

	/**
	 * Column tinyint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTinyint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.TINYINT, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column tinyint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTinyint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnTinyint(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column tinyint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTinyint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnTinyint(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column tinyint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTinyint(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnTinyint(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column tinyint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTinyint(String name, Boolean isPrimaryKey) {
		return columnTinyint(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column tinyint.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnTinyint(String name) {
		return columnTinyint(name, false, true, false, new String[] {});
	}

	/**
	 * Column bigint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBigint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity,
			String... args) {
		return this.column(name, DataType.BIGINT, isPrimaryKey, isNullable, isUnique, isIdentity, args);
	}

	/**
	 * Column bigint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBigint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, Boolean isIdentity, String args) {
		return columnBigint(name, isPrimaryKey, isNullable, isUnique, isIdentity, splitValues(args));
	}

	/**
	 * Column bigint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBigint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnBigint(name, isPrimaryKey, isNullable, isUnique, false, new String[] {});
	}

	/**
	 * Column bigint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBigint(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnBigint(name, isPrimaryKey, isNullable, false);
	}

	/**
	 * Column bigint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBigint(String name, Boolean isPrimaryKey) {
		return columnBigint(name, isPrimaryKey, true);
	}

	/**
	 * Column bigint.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBigint(String name) {
		return columnBigint(name, false);
	}

	/**
	 * Column smallint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnSmallint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.SMALLINT, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column smallint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnSmallint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnSmallint(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column smallint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnSmallint(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnSmallint(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column smallint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnSmallint(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnSmallint(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column smallint.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnSmallint(String name, Boolean isPrimaryKey) {
		return columnSmallint(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column smallint.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnSmallint(String name) {
		return columnSmallint(name, false, true, false, new String[] {});
	}

	/**
	 * Column real.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnReal(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.REAL, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column real.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnReal(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnReal(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column real.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnReal(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnReal(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column real.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnReal(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnReal(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column real.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnReal(String name, Boolean isPrimaryKey) {
		return columnReal(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column real.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnReal(String name) {
		return columnSmallint(name, false, true, false, new String[] {});
	}
	
	/**
	 * Column float.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnFloat(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.FLOAT, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column float.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnFloat(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnReal(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column float.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnFloat(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnReal(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column float.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnFloat(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnReal(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column float.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnFloat(String name, Boolean isPrimaryKey) {
		return columnReal(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column float.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnFloat(String name) {
		return columnSmallint(name, false, true, false, new String[] {});
	}

	/**
	 * Column double.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDouble(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.DOUBLE, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column double.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDouble(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnDouble(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column double.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDouble(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnDouble(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column double.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDouble(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnDouble(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column double.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDouble(String name, Boolean isPrimaryKey) {
		return columnDouble(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column double.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDouble(String name) {
		return columnSmallint(name, false, true, false, new String[] {});
	}

	/**
	 * Column boolean.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBoolean(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String... args) {
		return this.column(name, DataType.BOOLEAN, isPrimaryKey, isNullable, isUnique, args);
	}

	/**
	 * Column boolean.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBoolean(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique, String args) {
		return columnBoolean(name, isPrimaryKey, isNullable, isUnique, splitValues(args));
	}

	/**
	 * Column boolean.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBoolean(String name, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnBoolean(name, isPrimaryKey, isNullable, isUnique, new String[] {});
	}

	/**
	 * Column boolean.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBoolean(String name, Boolean isPrimaryKey, Boolean isNullable) {
		return columnBoolean(name, isPrimaryKey, isNullable, false, new String[] {});
	}

	/**
	 * Column boolean.
	 *
	 * @param name
	 *            the name
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBoolean(String name, Boolean isPrimaryKey) {
		return columnBoolean(name, isPrimaryKey, true, false, new String[] {});
	}

	/**
	 * Column boolean.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBoolean(String name) {
		return columnBoolean(name, false, true, false, new String[] {});
	}

	/**
	 * Column blob.
	 *
	 * @param name
	 *            the name
	 * @param isNullable
	 *            the is nullable
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBlob(String name, Boolean isNullable, String... args) {
		return this.column(name, DataType.BLOB, false, isNullable, false, args);
	}

	/**
	 * Column blob.
	 *
	 * @param name
	 *            the name
	 * @param isNullable
	 *            the is nullable
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBlob(String name, Boolean isNullable, String args) {
		return columnBlob(name, isNullable, splitValues(args));
	}

	/**
	 * Column blob.
	 *
	 * @param name
	 *            the name
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBlob(String name, Boolean isNullable) {
		return columnBlob(name, isNullable, new String[] {});
	}

	/**
	 * Column blob.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBlob(String name) {
		return columnBlob(name, false, new String[] {});
	}

	/**
	 * Column decimal.
	 *
	 * @param name
	 *            the name
	 * @param precision
	 *            the precision
	 * @param scale
	 *            the scale
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDecimal(String name, int precision, int scale, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique,
			Boolean isIdentity, String... args) {
		String[] definition = new String[] { OPEN + precision + "," + scale + CLOSE };
		String[] coulmn = Stream.of(definition, args).flatMap(Stream::of).toArray(String[]::new);
		return this.column(name, DataType.DECIMAL, isPrimaryKey, isNullable, isUnique, isIdentity, coulmn);
	}

	/**
	 * Column decimal.
	 *
	 * @param name
	 *            the name
	 * @param precision
	 *            the precision
	 * @param scale
	 *            the scale
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @param isIdentity
	 *            the is identity
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDecimal(String name, int precision, int scale, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique,
			Boolean isIdentity, String args) {
		return columnDecimal(name, precision, scale, isPrimaryKey, isNullable, isUnique, isIdentity, splitValues(args));
	}

	/**
	 * Column decimal.
	 *
	 * @param name
	 *            the name
	 * @param precision
	 *            the precision
	 * @param scale
	 *            the scale
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @param isUnique
	 *            the is unique
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDecimal(String name, int precision, int scale, Boolean isPrimaryKey, Boolean isNullable, Boolean isUnique) {
		return columnDecimal(name, precision, scale, isPrimaryKey, isNullable, isUnique, false, new String[] {});
	}

	/**
	 * Column decimal.
	 *
	 * @param name
	 *            the name
	 * @param precision
	 *            the precision
	 * @param scale
	 *            the scale
	 * @param isPrimaryKey
	 *            the is primary key
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDecimal(String name, int precision, int scale, Boolean isPrimaryKey, Boolean isNullable) {
		return columnDecimal(name, precision, scale, isPrimaryKey, isNullable, false);
	}

	/**
	 * Column decimal.
	 *
	 * @param name
	 *            the name
	 * @param precision
	 *            the precision
	 * @param scale
	 *            the scale
	 * @param isPrimaryKey
	 *            the is primary key
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDecimal(String name, int precision, int scale, Boolean isPrimaryKey) {
		return columnDecimal(name, precision, scale, isPrimaryKey, true);
	}

	/**
	 * Column decimal.
	 *
	 * @param name
	 *            the name
	 * @param precision
	 *            the precision
	 * @param scale
	 *            the scale
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnDecimal(String name, int precision, int scale) {
		return columnDecimal(name, precision, scale, false);
	}
	
	/**
	 * Column bit.
	 *
	 * @param name
	 *            the name
	 * @param isNullable
	 *            the is nullable
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBit(String name, Boolean isNullable, String... args) {
		return this.column(name, DataType.BIT, false, isNullable, false, args);
	}

	/**
	 * Column bit.
	 *
	 * @param name
	 *            the name
	 * @param isNullable
	 *            the is nullable
	 * @param args
	 *            the args
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBit(String name, Boolean isNullable, String args) {
		return columnBit(name, isNullable, splitValues(args));
	}

	/**
	 * Column bit.
	 *
	 * @param name
	 *            the name
	 * @param isNullable
	 *            the is nullable
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBit(String name, Boolean isNullable) {
		return columnBit(name, isNullable, new String[] {});
	}

	/**
	 * Column bit.
	 *
	 * @param name
	 *            the name
	 * @return the creates the table builder
	 */
	public TABLE_BUILDER columnBit(String name) {
		return columnBit(name, false, new String[] {});
	}
	
	/**
	 * Generate table.
	 *
	 * @param sql
	 *            the sql
	 */
	protected void generateTable(StringBuilder sql) {
		String tableName = (isCaseSensitive()) ? encapsulate(this.getTable()) : this.getTable();
		sql.append(SPACE).append(KEYWORD_TABLE).append(SPACE).append(tableName);
	}

	/**
	 * Generate columns.
	 *
	 * @param sql
	 *            the sql
	 */
	protected void generateColumns(StringBuilder sql) {
		if (!this.getColumns().isEmpty()) {
			sql.append(traverseColumns());
		}
	}
	
	/**
	 * Generate column names.
	 *
	 * @param sql
	 *            the sql
	 */
	protected void generateColumnNamesForDrop(StringBuilder sql) {
		if (!this.getColumns().isEmpty()) {
			sql.append(traverseColumnNamesForDrop());
		}
	}
	
	/**
	 * Traverse columns.
	 *
	 * @return the string
	 */
	protected String traverseColumns() {
		StringBuilder snippet = new StringBuilder();
		snippet.append(SPACE);
		for (String[] column : this.columns) {
			boolean first = true;
			for (String arg : column) {
				if (first) {
					String columnName = (isCaseSensitive()) ? encapsulate(arg) : arg;
					snippet.append(columnName).append(SPACE);
					first = false;
					continue;
				}
				snippet.append(arg).append(SPACE);
			}
			snippet.append(COMMA).append(SPACE);
		}
		return snippet.toString().substring(0, snippet.length() - 2);
	}
	
	/**
	 * Traverse columns.
	 *
	 * @return the string
	 */
	protected String traverseColumnNamesForDrop() {
		StringBuilder snippet = new StringBuilder();
		for (String[] column : this.columns) {
			String columnName = (isCaseSensitive()) ? encapsulate(column[0]) : column[0];
			snippet.append(KEYWORD_DROP).append(SPACE).append(KEYWORD_COLUMN).append(SPACE);
			snippet.append(columnName).append(SPACE);
			snippet.append(COMMA).append(SPACE);
		}
		return snippet.toString().substring(0, snippet.length() - 2);
	}

	/**
	 * Traverse column names.
	 *
	 * @param names
	 *            the columns
	 * @return the string
	 */
	protected String traverseNames(Set<String> names) {
		StringBuilder snippet = new StringBuilder();
		snippet.append(SPACE);
		for (String column : names) {
			String columnName = (isCaseSensitive()) ? encapsulate(column) : column;
			snippet.append(columnName).append(SPACE).append(COMMA).append(SPACE);
		}
		return snippet.toString().substring(0, snippet.length() - 2);
	}

	/**
	 * Split values.
	 *
	 * @param columns
	 *            the columns
	 * @return the string[]
	 */
	protected String[] splitValues(String columns) {
		String[] array = new String[] {};
		if (columns != null) {
			array = columns.split(",");
		}
		for (int i = 0; i < array.length; i++) {
			array[i] = array[i].trim();
		}
		return array;
	}

}
