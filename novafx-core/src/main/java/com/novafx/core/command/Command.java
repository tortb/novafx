package com.novafx.core.command;

/**
 * A single undoable user action.
 * <p>
 * Every mutation in NovaFX should go through the {@link CommandBus}
 * as a {@code Command}, making every operation automatically
 * undoable and redoable.
 */
public interface Command {

    /** Apply the command (captures the "after" state internally). */
    void execute();

    /** Revert the command to the "before" state. */
    void undo();

    /** Re-apply after an undo (defaults to {@link #execute()}). */
    default void redo() {
        execute();
    }

    /** Human-readable label shown in Edit > Undo / Redo menus. */
    String name();
}
