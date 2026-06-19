package com.novafx.core.workspace;

import java.util.List;
import java.util.Objects;

/**
 * An immutable node in a NovaFX project tree.
 * <p>
 * Every node carries enough information for the UI layer to render,
 * navigate, and interact with it without needing to reach back into
 * the domain model:
 * <ul>
 *   <li>{@code id} — stable, unique path within the tree
 *       (e.g. {@code "a1b2/function/x"})</li>
 *   <li>{@code displayName} — human-readable label
 *       (e.g. {@code "x(t)"}, {@code "Parameters"})</li>
 *   <li>{@code nodeType} — discriminator for icons, context menus,
 *       and editor routing</li>
 *   <li>{@code children} — sub-nodes (empty for leaves)</li>
 *   <li>{@code data} — optional payload (expression text for
 *       coordinate nodes, parameter name for parameter nodes, etc.)</li>
 * </ul>
 * <p>
 * Nodes form a tree via the {@link #children()} list. Container
 * types ({@link ProjectNodeType#PROJECT PROJECT},
 * {@link ProjectNodeType#FUNCTION FUNCTION},
 * {@link ProjectNodeType#PARAMETER_LIST PARAMETER_LIST}) always
 * have children; all others are leaves.
 *
 * @param id          stable, unique path within the owning workspace
 * @param displayName human-readable label for the tree cell
 * @param nodeType    semantic type for icons, menus, and routing
 * @param children    sub-nodes (empty list for leaf nodes)
 * @param data        optional payload ({@code null} when absent)
 */
public record ProjectNode(
        String id,
        String displayName,
        ProjectNodeType nodeType,
        List<ProjectNode> children,
        String data
) {

    /**
     * Compact constructor — defensively copies the children list
     * and rejects null arguments.
     */
    public ProjectNode {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(nodeType, "nodeType must not be null");
        children = List.copyOf(children);
        // data may be null
    }

    /**
     * Convenience constructor for nodes without a payload.
     *
     * @param id          stable path within the workspace
     * @param displayName human-readable label
     * @param nodeType    semantic type
     * @param children    sub-nodes (empty for leaves)
     */
    public ProjectNode(String id, String displayName,
                       ProjectNodeType nodeType, List<ProjectNode> children) {
        this(id, displayName, nodeType, children, null);
    }

    /**
     * Returns {@code true} when this node has no children (a leaf).
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public String toString() {
        return displayName + " (" + nodeType + ")";
    }
}
