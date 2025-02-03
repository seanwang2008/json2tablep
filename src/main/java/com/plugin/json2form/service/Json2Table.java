package com.plugin.json2form.service;

import java.util.Map;

public class Json2Table {
    
    /**
     * 将JSON转换为HTML表格
     * 
     * @param jsonInput JSON对象
     * @param buildDirection 构建方向
     * @param tableAttributes 表格属性
     * @return HTML表格字符串
     */
    public static String convert(Map<String, Object> jsonInput, 
                               BuildDirection buildDirection, 
                               Map<String, String> tableAttributes) {
        JsonConverter converter = new JsonConverter(buildDirection, tableAttributes);
        return converter.convert(jsonInput);
    }
    
    /**
     * 使用默认设置将JSON转换为HTML表格
     */
    public static String convert(Map<String, Object> jsonInput) {
        return convert(jsonInput, BuildDirection.LEFT_TO_RIGHT, null);
    }
} 