package org.apache.harmony.awt.datatransfer;

import java.awt.Point;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;

public class DragSourceEventProxy implements Runnable {
    public static final int DRAG_ACTION_CHANGED = 3;
    public static final int DRAG_DROP_END = 6;
    public static final int DRAG_ENTER = 1;
    public static final int DRAG_EXIT = 5;
    public static final int DRAG_MOUSE_MOVED = 4;
    public static final int DRAG_OVER = 2;
    private final DragSourceContext context;
    private final int modifiers;
    private final boolean success;
    private final int targetActions;
    private final int type;
    private final int userAction;

    /* renamed from: x */
    private final int f46x;

    /* renamed from: y */
    private final int f47y;

    public DragSourceEventProxy(DragSourceContext context2, int type2, int userAction2, int targetActions2, Point location, int modifiers2) {
        this.context = context2;
        this.type = type2;
        this.userAction = userAction2;
        this.targetActions = targetActions2;
        this.f46x = location.x;
        this.f47y = location.y;
        this.modifiers = modifiers2;
        this.success = false;
    }

    public DragSourceEventProxy(DragSourceContext context2, int type2, int userAction2, boolean success2, Point location, int modifiers2) {
        this.context = context2;
        this.type = type2;
        this.userAction = userAction2;
        this.targetActions = userAction2;
        this.f46x = location.x;
        this.f47y = location.y;
        this.modifiers = modifiers2;
        this.success = success2;
    }

    public void run() {
        switch (this.type) {
            case 1:
                this.context.dragEnter(newDragSourceDragEvent());
                return;
            case 2:
                this.context.dragOver(newDragSourceDragEvent());
                return;
            case 3:
                this.context.dropActionChanged(newDragSourceDragEvent());
                return;
            case 4:
                this.context.dragMouseMoved(newDragSourceDragEvent());
                return;
            case 5:
                this.context.dragExit(new DragSourceEvent(this.context, this.f46x, this.f47y));
                return;
            case 6:
                this.context.dragExit(new DragSourceDropEvent(this.context, this.userAction, this.success, this.f46x, this.f47y));
                return;
            default:
                return;
        }
    }

    private DragSourceDragEvent newDragSourceDragEvent() {
        return new DragSourceDragEvent(this.context, this.userAction, this.targetActions, this.modifiers, this.f46x, this.f47y);
    }
}
