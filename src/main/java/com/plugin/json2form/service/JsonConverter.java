package com.plugin.json2form.service;

import java.util.*;

public class JsonConverter {
    private final boolean buildTopToBottom;
    private final String tableOpeningTag;
    
    public JsonConverter(BuildDirection buildDirection, Map<String, String> tableAttributes) {
        this.buildTopToBottom = buildDirection == BuildDirection.TOP_TO_BOTTOM;
        this.tableOpeningTag = "<table" + dictToHtmlAttributes(tableAttributes) + ">";
    }
    
    public String convert(Map<String, Object> jsonInput) {
        StringBuilder html = new StringBuilder(tableOpeningTag);
        
        if (buildTopToBottom) {
            html.append(markupHeaderRow(jsonInput.keySet()));
            html.append("<tr>");
            for (Object value : jsonInput.values()) {
                if (value instanceof List) {
                    html.append(maybeClub((List<?>) value));
                } else {
                    html.append(markupTableCell(value));
                }
            }
            html.append("</tr>");
        } else {
            for (Map.Entry<String, Object> entry : jsonInput.entrySet()) {
                html.append("<tr><th>").append(markup(entry.getKey())).append("</th>");
                if (entry.getValue() instanceof List) {
                    html.append(maybeClub((List<?>) entry.getValue()));
                } else {
                    html.append(markupTableCell(entry.getValue()));
                }
                html.append("</tr>");
            }
        }
        
        html.append("</table>");
        return html.toString();
    }
    
    private String markupTableCell(Object value) {
        boolean hasChildren = checkHasChildren(value);
        String cellContent = markup(value);
        String toggleContent = wrapWithToggle(cellContent, hasChildren);
        String cellClass = hasChildren ? " class=\"has-children\"" : "";
        return String.format("<td%s>%s</td>", cellClass, toggleContent);
    }
    
    private boolean checkHasChildren(Object value) {
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return !list.isEmpty();
        }
        return false;
    }
    
    private String markupHeaderRow(Collection<String> headers) {
        return "<tr><th>" + String.join("</th><th>", headers) + "</th></tr>";
    }
    
    private static String dictToHtmlAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            result.append(String.format(" %s=\"%s\"", entry.getKey(), entry.getValue()));
        }
        return result.toString();
    }
    
    private String markup(Object entry) {
        if (entry == null) {
            return "";
        }
        if (entry instanceof List) {
            List<?> list = (List<?>) entry;
            StringBuilder listMarkup = new StringBuilder("<div class=\"list-wrapper\">");
            listMarkup.append("<ul>");
            for (Object item : list) {
                listMarkup.append("<li>").append(markup(item)).append("</li>");
            }
            listMarkup.append("</ul></div>");
            return listMarkup.toString();
        }
        if (entry instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) entry;
            return convert(map);
        }
        return String.valueOf(entry);
    }
    
    private String maybeClub(List<?> listOfDicts) {
        Set<String> columnHeaders = getCommonHeaders(listOfDicts);
        if (columnHeaders == null) {
            return markupTableCell(markup(listOfDicts));
        }
        
        StringBuilder html = new StringBuilder(tableOpeningTag);
        html.append(markupHeaderRow(columnHeaders));
        
        for (Object item : listOfDicts) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dict = (Map<String, Object>) item;
            html.append("<tr><td>");
            html.append(String.join("</td><td>", 
                columnHeaders.stream()
                    .map(header -> markup(dict.get(header)))
                    .toArray(String[]::new)));
            html.append("</td></tr>");
        }
        
        html.append("</table>");
        return markupTableCell(html.toString());
    }
    
    private Set<String> getCommonHeaders(List<?> listOfDicts) {
        if (listOfDicts.size() < 2 || !(listOfDicts.get(0) instanceof Map)) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Set<String> headers = new HashSet<>(((Map<String, Object>) listOfDicts.get(0)).keySet());
        
        for (int i = 1; i < listOfDicts.size(); i++) {
            Object item = listOfDicts.get(i);
            if (!(item instanceof Map)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> dict = (Map<String, Object>) item;
            if (!headers.equals(dict.keySet())) {
                return null;
            }
        }
        
        return headers;
    }
    
    private String wrapWithToggle(String content, boolean hasChildren) {
        if (!hasChildren) {
            return content;
        }
        return String.format(
            "<div class=\"cell-content\">" +
            "<span class=\"toggle-btn expanded\" onclick=\"toggleNode(this)\"></span>" +
            "<div class=\"content-wrapper\">%s</div>" +
            "</div>", 
            content
        );
    }
} 