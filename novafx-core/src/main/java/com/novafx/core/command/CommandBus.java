package com.novafx.core.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Dispatches {@link Command commands} and maintains undo / redo stacks.
 * <p>
 * Every command goes through {@link #dispatch(Command)} which:
 * <ol>
 *   <li>Captures the current state (before snapshot)</li>
 *   <li>Calls {@link Command#execute()}</li>
 *   <li>Captures the resulting state (after snapshot)</li>
 *   <li>Pushes onto the undo stack</li>
 *   <li>Clears the redo stack</li>
 * </ol>
 * <p>
 * Undo restores the before-state; redo re-applies the after-state.
 * The undo stack is bounded at {@link #MAX_UNDO} entries.
 */
public final class CommandBus {

    /** Maximum undo levels. */
    public static final int MAX_UNDO = 50;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    // ---------------------------------------------------------------
    //  Dispatch
    // ---------------------------------------------------------------

    /**
     * Executes a command and pushes it onto the undo stack.
     *
     * @param cmd the command to execute
     */
    public void dispatch(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        if (undoStack.size() > MAX_UNDO) {
            undoStack.removeLast();
        }
    }

    // ---------------------------------------------------------------
    //  Undo / Redo
    // ---------------------------------------------------------------

    /** Undo the most recent command.  No-op if nothing to undo. */
    public void undo() {
        if (undoStack.isEmpty()) return;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
    }

    /** Redo the most recently undone command.  No-op if nothing to redo. */
    public void redo() {
        if (redoStack.isEmpty()) return;
        Command cmd = redoStack.pop();
        cmd.redo();
        undoStack.push(cmd);
    }

    // ---------------------------------------------------------------
    //  Query
    // ---------------------------------------------------------------

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /** Returns the name of the command at the top of the undo stack,
     *  or null when the stack is empty. */
    public String undoName() {
        return undoStack.isEmpty() ? null : undoStack.peek().name();
    }

    /** Returns the name of the command at the top of the redo stack,
     *  or null when the stack is empty. */
    public String redoName() {
        return redoStack.isEmpty() ? null : redoStack.peek().name();
    }

    /** Clears both stacks. */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
