package org.xmind.ui.internal.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class DialogPaneContainer {

    private Composite composite;

    private DialogPane currentDialog;

    public Control getControl() {
        return composite;
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);

        Display display = parent.getDisplay();
        final Color background = new Color(display, 0x38, 0x38, 0x38);
        composite.setBackground(background);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                background.dispose();
                handleDispose();
            }
        });
    }

    protected void handleDispose() {
        hideCurrentDialog();
    }

    protected void showDialog(DialogPane dialog) {
        if (composite == null || composite.isDisposed())
            return;

        doHideCurrentDialog();
        currentDialog = dialog;
        currentDialog.createControl(composite);
        currentDialog.setFocus();
        composite.layout(true);
    }

    protected DialogPane getCurrentDialog() {
        return currentDialog;
    }

    protected void hideCurrentDialog() {
        doHideCurrentDialog();
        if (composite != null && !composite.isDisposed()) {
            composite.layout(true);
        }
    }

    private void doHideCurrentDialog() {
        if (currentDialog != null) {
            Control pageControl = currentDialog.getControl();
            currentDialog.dispose();
            if (pageControl != null && !pageControl.isDisposed()) {
                pageControl.dispose();
            }
            currentDialog = null;
        }
    }

    public void dispose() {
        hideCurrentDialog();
        if (composite != null) {
            composite.dispose();
        }
    }

    public void setFocus() {
        if (currentDialog != null) {
            currentDialog.setFocus();
        } else {
            composite.setFocus();
        }
    }

}