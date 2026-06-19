package com.novafx.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作区管理器 — UI Kit v2 核心组件。
 * <p>
 * 管理工作区的创建、保存、加载和切换功能。
 * 提供工作区的持久化存储和恢复机制。
 */
public class WorkspaceManager {

    /** 工作区存储目录 */
    private static final String WORKSPACE_DIR = ".novafx/workspaces";

    /** 工作区文件扩展名 */
    private static final String WORKSPACE_EXT = ".nfxws";

    /** 所有工作区 */
    private final ObservableList<Workspace> workspaces = FXCollections.observableArrayList();

    /** 工作区名称 → 工作区映射 */
    private final ObservableMap<String, Workspace> workspaceMap = FXCollections.observableHashMap();

    /** 当前活动工作区 */
    private Workspace activeWorkspace;

    /** 工作区切换回调 */
    private java.util.function.Consumer<Workspace> onWorkspaceSwitched;

    /**
     * 创建工作区管理器实例。
     */
    public WorkspaceManager() {
        // 确保工作区目录存在
        ensureWorkspaceDirectory();
    }

    /**
     * 确保工作区目录存在。
     */
    private void ensureWorkspaceDirectory() {
        try {
            Path workspaceDir = Paths.get(WORKSPACE_DIR);
            if (!Files.exists(workspaceDir)) {
                Files.createDirectories(workspaceDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建新工作区。
     *
     * @param name 工作区名称
     * @return 创建的工作区
     */
    public Workspace createWorkspace(String name) {
        Workspace workspace = new Workspace(name);
        workspaces.add(workspace);
        workspaceMap.put(name, workspace);
        return workspace;
    }

    /**
     * 打开工作区。
     *
     * @param name 工作区名称
     * @return 工作区，如果不存在则返回 null
     */
    public Workspace openWorkspace(String name) {
        Workspace workspace = workspaceMap.get(name);
        if (workspace != null) {
            switchWorkspace(workspace);
        }
        return workspace;
    }

    /**
     * 切换工作区。
     *
     * @param workspace 目标工作区
     */
    public void switchWorkspace(Workspace workspace) {
        if (workspaces.contains(workspace)) {
            // 保存当前工作区
            if (activeWorkspace != null) {
                saveWorkspace(activeWorkspace);
            }

            // 切换到新工作区
            activeWorkspace = workspace;

            // 加载新工作区
            loadWorkspace(workspace);

            // 触发回调
            if (onWorkspaceSwitched != null) {
                onWorkspaceSwitched.accept(workspace);
            }
        }
    }

    /**
     * 保存工作区。
     *
     * @param workspace 要保存的工作区
     */
    public void saveWorkspace(Workspace workspace) {
        try {
            Path workspacePath = getWorkspacePath(workspace.getName());
            Files.writeString(workspacePath, serializeWorkspace(workspace));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载工作区。
     *
     * @param workspace 要加载的工作区
     */
    public void loadWorkspace(Workspace workspace) {
        try {
            Path workspacePath = getWorkspacePath(workspace.getName());
            if (Files.exists(workspacePath)) {
                String content = Files.readString(workspacePath);
                deserializeWorkspace(content, workspace);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除工作区。
     *
     * @param workspace 要删除的工作区
     */
    public void deleteWorkspace(Workspace workspace) {
        // 从列表中移除
        workspaces.remove(workspace);
        workspaceMap.remove(workspace.getName());

        // 删除文件
        try {
            Path workspacePath = getWorkspacePath(workspace.getName());
            if (Files.exists(workspacePath)) {
                Files.delete(workspacePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 如果删除的是当前工作区，切换到第一个可用的工作区
        if (activeWorkspace == workspace) {
            activeWorkspace = workspaces.isEmpty() ? null : workspaces.get(0);
        }
    }

    /**
     * 重命名工作区。
     *
     * @param workspace 工作区
     * @param newName   新名称
     */
    public void renameWorkspace(Workspace workspace, String newName) {
        String oldName = workspace.getName();

        // 删除旧文件
        try {
            Path oldPath = getWorkspacePath(oldName);
            if (Files.exists(oldPath)) {
                Files.delete(oldPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 更新名称
        workspace.setName(newName);
        workspaceMap.remove(oldName);
        workspaceMap.put(newName, workspace);

        // 保存新文件
        saveWorkspace(workspace);
    }

    /**
     * 获取所有工作区。
     *
     * @return 工作区列表
     */
    public ObservableList<Workspace> getWorkspaces() {
        return workspaces;
    }

    /**
     * 获取当前活动工作区。
     *
     * @return 活动工作区，如果没有则返回 null
     */
    public Workspace getActiveWorkspace() {
        return activeWorkspace;
    }

    /**
     * 根据名称获取工作区。
     *
     * @param name 工作区名称
     * @return 工作区，如果不存在则返回 null
     */
    public Workspace getWorkspace(String name) {
        return workspaceMap.get(name);
    }

    /**
     * 检查工作区是否存在。
     *
     * @param name 工作区名称
     * @return true 表示存在
     */
    public boolean hasWorkspace(String name) {
        return workspaceMap.containsKey(name);
    }

    /**
     * 获取工作区文件路径。
     *
     * @param name 工作区名称
     * @return 文件路径
     */
    private Path getWorkspacePath(String name) {
        return Paths.get(WORKSPACE_DIR, name + WORKSPACE_EXT);
    }

    /**
     * 序列化工作区。
     *
     * @param workspace 工作区
     * @return 序列化后的字符串
     */
    private String serializeWorkspace(Workspace workspace) {
        // 简化实现：使用 JSON 格式
        // 实际项目中应使用 Jackson 或 Gson
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(workspace.getName()).append("\",\n");
        sb.append("  \"description\": \"").append(workspace.getDescription()).append("\",\n");
        sb.append("  \"createdAt\": \"").append(workspace.getCreatedAt()).append("\",\n");
        sb.append("  \"lastModifiedAt\": \"").append(workspace.getLastModifiedAt()).append("\",\n");

        // 面板布局
        sb.append("  \"panelLayouts\": {\n");
        boolean first = true;
        for (Map.Entry<String, Workspace.PanelLayout> entry : workspace.getPanelLayouts().entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append("    \"").append(entry.getKey()).append("\": {");
            sb.append("\"x\": ").append(entry.getValue().getX());
            sb.append(", \"y\": ").append(entry.getValue().getY());
            sb.append(", \"width\": ").append(entry.getValue().getWidth());
            sb.append(", \"height\": ").append(entry.getValue().getHeight());
            sb.append(", \"visible\": ").append(entry.getValue().isVisible());
            sb.append(", \"floating\": ").append(entry.getValue().isFloating());
            sb.append("}");
        }
        sb.append("\n  },\n");

        // 项目引用
        sb.append("  \"projects\": [\n");
        first = true;
        for (Workspace.ProjectReference project : workspace.getProjects()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append("    {");
            sb.append("\"projectId\": \"").append(project.getProjectId()).append("\"");
            sb.append(", \"projectName\": \"").append(project.getProjectName()).append("\"");
            sb.append(", \"filePath\": \"").append(project.getFilePath()).append("\"");
            sb.append(", \"lastOpenedAt\": \"").append(project.getLastOpenedAt()).append("\"");
            sb.append("}");
        }
        sb.append("\n  ]\n");

        sb.append("}");
        return sb.toString();
    }

    /**
     * 反序列化工作区。
     *
     * @param content   序列化内容
     * @param workspace 工作区对象
     */
    private void deserializeWorkspace(String content, Workspace workspace) {
        // 简化实现：实际项目中应使用 JSON 解析库
        // 这里只是示例，不实现完整的 JSON 解析
    }

    /**
     * 设置工作区切换回调。
     *
     * @param callback 回调函数
     */
    public void setOnWorkspaceSwitched(java.util.function.Consumer<Workspace> callback) {
        this.onWorkspaceSwitched = callback;
    }

    /**
     * 加载所有工作区。
     */
    public void loadAllWorkspaces() {
        try {
            Path workspaceDir = Paths.get(WORKSPACE_DIR);
            if (Files.exists(workspaceDir)) {
                Files.list(workspaceDir)
                        .filter(path -> path.toString().endsWith(WORKSPACE_EXT))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            String name = fileName.substring(0, fileName.length() - WORKSPACE_EXT.length());
                            if (!workspaceMap.containsKey(name)) {
                                Workspace workspace = createWorkspace(name);
                                loadWorkspace(workspace);
                            }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存所有工作区。
     */
    public void saveAllWorkspaces() {
        for (Workspace workspace : workspaces) {
            saveWorkspace(workspace);
        }
    }
}
