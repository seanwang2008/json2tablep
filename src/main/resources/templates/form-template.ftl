<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Generated Form</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 20px auto;
            padding: 20px;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        input[type="text"],
        input[type="number"],
        input[type="email"],
        textarea,
        select {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        input[type="checkbox"] {
            margin-right: 5px;
        }
        .form-actions {
            margin-top: 20px;
            text-align: right;
        }
        button {
            padding: 8px 15px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <form>
        <#list fields as field>
        <div class="form-group">
            <label for="${field.name}">${field.label}</label>
            <#if field.type == "textarea">
                <textarea id="${field.name}" name="${field.name}" rows="4">${field.value}</textarea>
            <#elseif field.type == "checkbox">
                <input type="${field.type}" id="${field.name}" name="${field.name}" 
                       <#if field.value == "true">checked</#if>>
            <#else>
                <input type="${field.type}" id="${field.name}" name="${field.name}" 
                       value="${field.value}" <#if field.required>required</#if>>
            </#if>
        </div>
        </#list>
        <div class="form-actions">
            <button type="submit">Submit</button>
        </div>
    </form>
</body>
</html> 