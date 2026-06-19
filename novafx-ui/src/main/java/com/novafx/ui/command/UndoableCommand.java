package com.novafx.ui.command;

import com.novafx.core.command.Command;
import com.novafx.core.state.ProjectState;
import com.novafx.ui.controller.MainController;

/**
 * Base for commands that snapshot the entire {@link ProjectState}
 * before and after execution, enabling full undo/redo via
 * {@link MainController#restoreState(ProjectState)}.
 */
public abstract class UndoableCommand implements Command {

    protected final MainController controller;
    protected ProjectState beforeState;
    protected ProjectState afterState;

    protected UndoableCommand(MainController controller) {
        this.controller = controller;
    }

    /**
     * Subclasses implement this — it runs between the before/after
     * snapshots which {@link #execute()} handles automatically.
     */
    protected abstract void doExecute();

    @Override
    public void execute() {
        beforeState = controller.getState();
        doExecute();
        afterState = controller.getState();
    }

    @Override
    public void undo() {
        controller.restoreState(beforeState);
    }

    @Override
    public void redo() {
        controller.restoreState(afterState);
    }
}
