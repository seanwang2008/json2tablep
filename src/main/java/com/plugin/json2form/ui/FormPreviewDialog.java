package com.plugin.json2form.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

@Deprecated
public class FormPreviewDialog extends DialogWrapper {
    private final String htmlContent;

    public FormPreviewDialog(Project project, String htmlContent) {
        super(project);
        this.htmlContent = htmlContent;
        setTitle("HTML Form Preview");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        
        // 使用JCEF浏览器来显示HTML内容
        JBCefBrowser browser = new JBCefBrowser();
        browser.loadHTML(htmlContent);
        
        dialogPanel.add(browser.getComponent(), BorderLayout.CENTER);
        dialogPanel.setPreferredSize(new Dimension(800, 600));
        
        return dialogPanel;
    }
} 