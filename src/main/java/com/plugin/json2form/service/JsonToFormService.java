package com.plugin.json2form.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.components.Service;
import com.plugin.json2form.model.FormField;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public final class JsonToFormService {
    private static final JsonToFormService instance = new JsonToFormService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Configuration freemarkerConfig;

    private JsonToFormService() {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        freemarkerConfig.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "/templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
    }

    public static JsonToFormService getInstance() {
        return instance;
    }

    public File convertJsonToHtml(String jsonContent) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> json = mapper.readValue(jsonContent,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        // 设置表格属性
        Map<String, String> tableAttributes = new HashMap<>();
        tableAttributes.put("class", "json-table");
        // 转换为HTML表格
        String html = Json2Table.convert(json, BuildDirection.LEFT_TO_RIGHT, tableAttributes);
// 创建输出目录
        File tmpDir = new File("tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }

        // 加载CSS样式
        String cssStyles = loadCssStyles();

        // 写入HTML文件
        File outputFile = new File("tmp/output.html");
        writeHtmlFile(html, cssStyles, outputFile);
        return outputFile;

//        JsonNode rootNode = objectMapper.readTree(jsonContent);
//        List<FormField> fields = parseJsonToFormFields(rootNode);
//        return generateHtml(fields);
    }

    private static String loadCssStyles() throws IOException {
        try (InputStream inputStream = JsonToFormService.class.getResourceAsStream("/styles/table-styles.css")) {
            if (inputStream == null) {
                throw new IOException("can not find style file");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void writeHtmlFile(String html, String cssStyles, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.write("<title>JSON数据表格展示</title>\n");
            writer.write("<style>\n");
            writer.write(cssStyles);
            writer.write("\n</style>\n");
            writer.write(JAVASCRIPT_CODE);
            writer.write("</head>\n<body>\n");
            writer.write("<h1 style=\"text-align: center; color: #333;\">JSON数据表格展示</h1>\n");
            writer.write(GLOBAL_BUTTONS);
            writer.write(html);
            writer.write("\n</body></html>");
        }
    }

    private static final String JAVASCRIPT_CODE = """
        <script>
        function toggleNode(btn) {
            const cell = btn.closest('td');
            const contents = cell.querySelectorAll('.content-wrapper > table, .content-wrapper > .list-wrapper');
            if (contents.length > 0) {
                contents.forEach(content => {
                    content.classList.toggle('collapsed');
                });
                btn.classList.toggle('expanded');
                btn.classList.toggle('collapsed');
            }
        }
        
        function toggleAll(expand) {
            const allContents = document.querySelectorAll('.json-table .content-wrapper table, .json-table .content-wrapper .list-wrapper');
            const allButtons = document.querySelectorAll('.json-table .toggle-btn');
            
            allButtons.forEach(btn => {
                if (expand) {
                    btn.classList.remove('collapsed');
                    btn.classList.add('expanded');
                } else {
                    btn.classList.remove('expanded');
                    btn.classList.add('collapsed');
                }
            });
            
            allContents.forEach(content => {
                if (expand) {
                    content.classList.remove('collapsed');
                } else {
                    content.classList.add('collapsed');
                }
            });
        }
        </script>
        """;

    private static final String GLOBAL_BUTTONS = """
        <div class="global-buttons">
            <button class="global-btn" onclick="toggleAll(true)">展开全部</button>
            <button class="global-btn" onclick="toggleAll(false)">折叠全部</button>
        </div>
        """;

    private List<FormField> parseJsonToFormFields(JsonNode node) {
        List<FormField> fields = new ArrayList<>();
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                fields.add(createFormField(key, value));
            });
        }
        return fields;
    }

    private FormField createFormField(String key, JsonNode value) {
        String type = determineInputType(value);
        String label = toTitleCase(key);
        String defaultValue = value.asText();
        
        return new FormField(key, type, defaultValue, label, true);
    }

    private String determineInputType(JsonNode value) {
        if (value.isBoolean()) return "checkbox";
        if (value.isNumber()) return "number";
        if (value.isTextual()) {
            String text = value.asText().toLowerCase();
            if (text.contains("@")) return "email";
            if (text.matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) return "file";
            if (text.length() > 100) return "textarea";
        }
        return "text";
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        
        String[] words = input.split("(?=\\p{Upper})|_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }

    private String generateHtml(List<FormField> fields) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("form-template.ftl");
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("fields", fields);
        
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        return writer.toString();
    }
} 