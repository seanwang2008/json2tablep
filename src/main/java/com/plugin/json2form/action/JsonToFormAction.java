package com.plugin.json2form.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.plugin.json2form.service.JsonToFormService;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;

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
            File htmlContentFile = JsonToFormService.getInstance().convertJsonToHtml(jsonContent);
            // 自动打开生成的HTML文件
            openInBrowser(htmlContentFile);
            // 显示预览对话框
//            new FormPreviewDialog(project, htmlContent).show();
        } catch (Exception ex) {
            // 处理异常
        }
    }

    // 添加新方法用于打开浏览器
    private static void openInBrowser(File htmlFile) {
        try {
            // 检查是否支持Desktop功能
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                // 检查是否支持浏览功能
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(htmlFile.toURI());
                } else {
                    System.out.println("系统不支持自动打开浏览器，请手动打开文件：" + htmlFile.getAbsolutePath());
                }
            } else {
                // 如果不支持Desktop功能，尝试使用系统命令打开
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder builder;

                if (os.contains("win")) {
                    // Windows
                    builder = new ProcessBuilder("cmd", "/c", "start", htmlFile.toURI().toString());
                } else if (os.contains("mac")) {
                    // macOS
                    builder = new ProcessBuilder("open", htmlFile.getPath());
                } else if (os.contains("nix") || os.contains("nux")) {
                    // Linux
                    builder = new ProcessBuilder("xdg-open", htmlFile.getPath());
                } else {
                    System.out.println("无法识别的操作系统，请手动打开文件：" + htmlFile.getAbsolutePath());
                    return;
                }

                builder.start();
            }
        } catch (Exception e) {
            System.err.println("打开浏览器时发生错误: " + e.getMessage());
            System.out.println("请手动打开文件：" + htmlFile.getAbsolutePath());
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