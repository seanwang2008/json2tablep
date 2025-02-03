package com.plugin.json2form.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.components.Service;
import com.plugin.json2form.model.FormField;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

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

    public String convertJsonToHtml(String jsonContent) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        List<FormField> fields = parseJsonToFormFields(rootNode);
        return generateHtml(fields);
    }

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