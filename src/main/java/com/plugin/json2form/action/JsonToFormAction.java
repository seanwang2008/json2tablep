package com.plugin.json2form.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.plugin.json2form.service.JsonToFormService;
import com.plugin.json2form.ui.FormPreviewDialog;
import org.jetbrains.annotations.NotNull;

public class JsonToFormAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) return;

        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (!"json".equals(virtualFile.getExtension())) return;

        try {
            String jsonContent = new String(virtualFile.contentsToByteArray());
            String htmlContent = JsonToFormService.getInstance().convertJsonToHtml(jsonContent);
            
            // 显示预览对话框
            new FormPreviewDialog(project, htmlContent).show();
        } catch (Exception ex) {
            // 处理异常
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有当选中的是json文件时才启用该action
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabledAndVisible(
            psiFile != null && "json".equals(psiFile.getVirtualFile().getExtension())
        );
    }
} 