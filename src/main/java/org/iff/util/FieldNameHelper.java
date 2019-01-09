/*******************************************************************************
 * Copyright (c) 2018-10-26 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.util;

import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.PreRequiredHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * FieldNameHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-10-26
 * auto generate by qdp.
 */
public class FieldNameHelper {
    private static final Map<String, String> columnToField = new HashMap<String, String>();
    private static final Map<String, String> fieldToColumn = new HashMap<String, String>();
    private static final Map<String, String> tableToClass = new HashMap<String, String>();

    public static boolean isJavaFieldLike(String value) {
        return !StringUtils.contains(value, '_') && StringUtils.isAllLowerCase(value.substring(0, 1));
    }

    public static boolean isJavaClassLike(String value) {
        return !StringUtils.contains(value, '_') && StringUtils.isAllUpperCase(value.substring(0, 1)) && !StringUtils.isAllUpperCase(value.substring(1));
    }

    public static boolean isColumnLike(String value) {
        return StringUtils.contains(value, '_') || StringUtils.isAllUpperCase(value);
    }

    public static String columnToField(String column) {
        {//get from cache
            String columnToField = getColumnToField(column);
            if (columnToField != null) {
                return columnToField;
            }
        }
        column = PreRequiredHelper.requireNotBlank(column);
        String field = column;
        if (isColumnLike(column)) {
            String[] split = StringUtils.split(column, '_');
            for (int i = 0; i < split.length; i++) {
                split[i] = i == 0 ? StringUtils.lowerCase(split[i]) : StringUtils.capitalize(StringUtils.lowerCase(split[i]));
            }
            field = StringUtils.join(split);
        }
        {// set to cache
            setColumnToField(column, field);
        }
        return field;
    }

    public static String fieldToColumn(String fieldName) {
        {//get from cache
            String fieldToColumn = getFieldToColumn(fieldName);
            if (fieldToColumn != null) {
                return fieldToColumn;
            }
        }
        fieldName = PreRequiredHelper.requireNotBlank(fieldName);
        String column = fieldName;
        if (isJavaFieldLike(fieldName)) {
            String[] split = StringUtils.splitByCharacterTypeCamelCase(fieldName);
            for (int i = 0; i < split.length; i++) {
                split[i] = StringUtils.upperCase(split[i]);
            }
            column = StringUtils.join(split, '_');
        }
        {//set to cache
            setFieldToColumn(fieldName, column);
        }
        return column;
    }


    public static String tableToClass(String table) {
        {//get from cache
            String tableToClass = getTableToClass(table);
            if (tableToClass != null) {
                return tableToClass;
            }
        }
        table = PreRequiredHelper.requireNotBlank(table);
        String className = table;
        if (isColumnLike(table)) {
            String[] split = StringUtils.split(table, '_');
            for (int i = 0; i < split.length; i++) {
                split[i] = StringUtils.capitalize(StringUtils.lowerCase(split[i]));
            }
            className = StringUtils.join(split);
        }
        {// set to cache
            setTableToClass(table, className);
        }
        return className;
    }

    public static String classToTable(String className) {
        {// get from cache
            String classToTable = getClassToTable(className);
            if (classToTable != null) {
                return classToTable;
            }
        }
        className = PreRequiredHelper.requireNotBlank(className);
        String table = className;
        if (isJavaClassLike(className)) {
            String[] split = StringUtils.splitByCharacterTypeCamelCase(className);
            for (int i = 0; i < split.length; i++) {
                split[i] = StringUtils.upperCase(split[i]);
            }
            table = StringUtils.join(split, '_');
        }
        {// set to cache
            setClassToTable(className, table);
        }
        return table;
    }

    public static String packageToPath(String... packageNames) {
        if (packageNames == null || packageNames.length < 1) {
            return "";
        }
        return StringUtils.replaceEach(StringUtils.join(packageNames, "/"), new String[]{".", "//"}, new String[]{"/", "/"});
    }

    private static String getColumnToField(String column) {
        return columnToField.get(column);
    }

    private static void setColumnToField(String column, String field) {
        columnToField.put(column, field);
    }

    private static String getFieldToColumn(String field) {
        return fieldToColumn.get(field);
    }

    private static void setFieldToColumn(String field, String column) {
        fieldToColumn.put(field, column);
    }

    private static String getTableToClass(String table) {
        return tableToClass.get(table);
    }

    private static void setTableToClass(String table, String className) {
        tableToClass.put(table, className);
    }

    private static String getClassToTable(String className) {
        return fieldToColumn.get(className);
    }

    private static void setClassToTable(String className, String table) {
        fieldToColumn.put(className, table);
    }
}
