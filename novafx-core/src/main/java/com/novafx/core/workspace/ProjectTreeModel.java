package com.novafx.core.workspace;

import com.novafx.core.domain.Project;
import com.novafx.math.FunctionDefinition;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bridges a single domain {@link Project} to its navigable tree
 * representation.
 * <p>
 * The tree structure is <em>derived automatically</em> from the
 * project's data — there is no {@code [structure]} section in the
 * .nfx file. For instance, the parametric function always produces
 * a {@code Function} container with {@code x(t)}, {@code y(t)},
 * {@code z(t)} leaves, and the parameters section lists every
 * variable extracted from the expressions.
 * <p>
 * This is a pure data object. Use {@link #from(Project, Path)}
 * or {@link #from(Project)} to build one.
 *
 * @param project     the source domain aggregate (never null)
 * @param projectPath optional file path ({@code null} for unsaved projects)
 * @param root        the root tree node (never null)
 */
public record ProjectTreeModel(
        Project project,
        Path projectPath,
        ProjectNode root
) {

    public ProjectTreeModel {
        Objects.requireNonNull(project, "project must not be null");
        Objects.requireNonNull(root, "root must not be null");
        // projectPath may be null
    }

    // ---------------------------------------------------------------
    //  Factory — the "automatic structure derivation" entry points
    // ---------------------------------------------------------------

    /**
     * Builds a tree model from a domain {@link Project} with an
     * optional file path.
     *
     * @param project the source project
     * @param path    file path, or {@code null} if unsaved
     * @return a new tree model whose root and children mirror the
     *         project's content
     */
    public static ProjectTreeModel from(Project project, Path path) {
        Objects.requireNonNull(project, "project must not be null");

        String baseId = project.id().toString();
        FunctionDefinition def = project.functionDefinition();

        List<ProjectNode> children = buildChildren(baseId, def);

        ProjectNode root = new ProjectNode(
                baseId,
                project.name(),
                ProjectNodeType.PROJECT,
                children
        );

        return new ProjectTreeModel(project, path, root);
    }

    /**
     * Builds a tree model without a file path (new / unsaved project).
     *
     * @param project the source project
     * @return a new tree model
     */
    public static ProjectTreeModel from(Project project) {
        return from(project, null);
    }

    // ---------------------------------------------------------------
    //  Internal: derive the full child list from a FunctionDefinition
    // ---------------------------------------------------------------

    private static List<ProjectNode> buildChildren(String baseId,
                                                   FunctionDefinition def) {
        List<ProjectNode> children = new ArrayList<>(6);

        // ── Function category ──────────────────────────────────
        children.add(buildFunctionCategory(baseId, def));

        // ── Parameters category ────────────────────────────────
        children.add(buildParametersCategory(baseId, def));

        // ── Camera (always present) ────────────────────────────
        children.add(new ProjectNode(
                baseId + "/camera",
                "Camera",
                ProjectNodeType.CAMERA,
                List.of()
        ));

        // ── Render settings (always present) ───────────────────
        children.add(new ProjectNode(
                baseId + "/render",
                "Render",
                ProjectNodeType.RENDER,
                List.of()
        ));

        // ── Presets (always present) ──────────────────────────
        children.add(new ProjectNode(
                baseId + "/presets",
                "Presets",
                ProjectNodeType.PRESETS,
                List.of()
        ));

        return List.copyOf(children);
    }

    private static ProjectNode buildFunctionCategory(String baseId,
                                                     FunctionDefinition def) {
        return new ProjectNode(
                baseId + "/function",
                "Function",
                ProjectNodeType.FUNCTION,
                List.of(
                        exprNode(baseId + "/function/x", "x(t)", def.xExpression()),
                        exprNode(baseId + "/function/y", "y(t)", def.yExpression()),
                        exprNode(baseId + "/function/z", "z(t)", def.zExpression())
                )
        );
    }

    private static ProjectNode buildParametersCategory(String baseId,
                                                       FunctionDefinition def) {
        var paramChildren = new ArrayList<ProjectNode>();

        for (String name : def.parameterNames()) {
            paramChildren.add(new ProjectNode(
                    baseId + "/parameters/" + name,
                    name,
                    ProjectNodeType.PARAMETER,
                    List.of(),
                    name   // data = parameter name
            ));
        }

        return new ProjectNode(
                baseId + "/parameters",
                "Parameters",
                ProjectNodeType.PARAMETER_LIST,
                paramChildren
        );
    }

    private static ProjectNode exprNode(String id, String label,
                                        String expression) {
        return new ProjectNode(
                id,
                label,
                switch (label.charAt(0)) {
                    case 'x' -> ProjectNodeType.X_EXPR;
                    case 'y' -> ProjectNodeType.Y_EXPR;
                    case 'z' -> ProjectNodeType.Z_EXPR;
                    default -> throw new AssertionError("unexpected expr label: " + label);
                },
                List.of(),
                expression    // data = the expression string
        );
    }
}
