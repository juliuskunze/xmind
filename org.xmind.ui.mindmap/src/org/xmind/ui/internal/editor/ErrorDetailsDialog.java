package org.xmind.ui.internal.editor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.resources.FontUtils;

public class ErrorDetailsDialog extends Dialog {

    private Throwable error;

    private String title;

    private long time;

    private String text;

    public ErrorDetailsDialog(Throwable error, String title, long time) {
        super((Shell) null);
        this.error = error;
        this.title = title;
        this.time = time;
        setShellStyle(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE
                | getDefaultOrientation());
        setBlockOnOpen(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
//        newShell.setText("Details of Error");
        newShell.setText(MindMapMessages.ErrorDetailDialog_title);
        newShell.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    setReturnCode(CANCEL);
                    close();
                }
            }
        });
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        createErrorLogArea(composite);
        return composite;
    }

    private void createErrorLogArea(Composite parent) {
        StyledText logControl = new StyledText(parent, SWT.READ_ONLY
                | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        logControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        logControl.setText(getLogText());
        if (Util.isMac()) {
            logControl.setFont(FontUtils.getRelativeHeight(
                    JFaceResources.DIALOG_FONT, 1));
        }
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.FINISH_ID,
                MindMapMessages.Finish_button_text, false);
        createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, true);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.FINISH_ID) {
            copyToClipboard();
        } else if (buttonId == IDialogConstants.CLOSE_ID) {
            okPressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    private void copyToClipboard() {
        Clipboard clipboard = new Clipboard(Display.getCurrent());
        try {
            clipboard.setContents(new Object[] { getLogText() },
                    new Transfer[] { TextTransfer.getInstance() });
        } finally {
            clipboard.dispose();
        }
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 420);
    }

    private String getLogText() {
        if (text != null)
            return text;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        ps.println(title);
        ps.println("-----------------------------"); //$NON-NLS-1$

        ps.println("Time: " + String.format("%1$tF %1$tT", time)); //$NON-NLS-1$ //$NON-NLS-2$

        ps.print("XMind: 3.3.0"); //$NON-NLS-1$
        if (isPro()) {
            ps.println(" Pro"); //$NON-NLS-1$
        } else {
            ps.println();
        }

        ps.println("Java Version: " + System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$ 
        ps.println("Java Vendor: " + System.getProperty("java.vendor")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("Java Runtime: " + System.getProperty("java.runtime.name")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("    Version: " + System.getProperty("java.runtime.version")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("Java VM: " + System.getProperty("java.vm.name")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("    Version: " + System.getProperty("java.vm.version")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("    Vendor: " + System.getProperty("java.vm.vendor")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("    Info: " + System.getProperty("java.vm.info")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("Operating System: " + System.getProperty("os.name") //$NON-NLS-1$ //$NON-NLS-2$ 
                + " " + System.getProperty("os.version") //$NON-NLS-1$ //$NON-NLS-2$ 
                + " (" + System.getProperty("os.arch") //$NON-NLS-1$ //$NON-NLS-2$ 
                + ")"); //$NON-NLS-1$
        ps.println("Language: " + System.getProperty("user.language")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("Country: " + System.getProperty("user.country")); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println("XMind Distribution Pack: " + System.getProperty("org.xmind.product.distribution.id")); //$NON-NLS-1$ //$NON-NLS-2$

        ps.println("-----------------------------"); //$NON-NLS-1$
        error.printStackTrace(ps);

        ps.close();

        text = out.toString();//.replaceAll("\\r\\n|\\r", "\\n");
        return text;
    }

    private static boolean isPro() {
        return Platform.getBundle("org.xmind.meggy") != null; //$NON-NLS-1$
    }

}
