package com.novafx.ui.command;

import com.novafx.ui.controller.MainController;

/**
 * Command for editing the parametric function expressions and range.
 */
public final class UpdateFunctionCommand extends UndoableCommand {

    private final String xExpr, yExpr, zExpr;
    private final double start, end, step;

    public UpdateFunctionCommand(MainController controller,
                                  String xExpr, String yExpr, String zExpr,
                                  double start, double end, double step) {
        super(controller);
        this.xExpr = xExpr;
        this.yExpr = yExpr;
        this.zExpr = zExpr;
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @Override
    protected void doExecute() {
        controller.updateFunction(xExpr, yExpr, zExpr, start, end, step);
    }

    @Override
    public String name() {
        return "Update Function";
    }
}
