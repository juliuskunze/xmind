/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.findreplace;

import static org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CLOSE_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CLOSE_LABEL;
import static org.eclipse.jface.dialogs.IDialogConstants.HORIZONTAL_SPACING;
import static org.eclipse.jface.dialogs.IDialogConstants.VERTICAL_SPACING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.internal.dialogs.DialogMessages;

/**
 * @author Frank Shaka
 */
public class FindReplaceDialog extends Dialog implements IPartListener {

    private static final String EMPTY = ""; //$NON-NLS-1$

    private static final String STRING_NOT_FOUND = DialogMessages.FindReplaceDialog_StringNotFound;

    private static Map<Shell, FindReplaceDialog> SINGLETONS = new HashMap<Shell, FindReplaceDialog>();

    private static final int TEXT_WIDTH = 120;

    private static final int FIND_ID = CLIENT_ID + 1;
    private static final int FIND_ALL_ID = CLIENT_ID + 2;
    private static final int REPLACE_ID = CLIENT_ID + 3;
    private static final int REPLACE_ALL_ID = CLIENT_ID + 4;

    private static List<String> FindHistory = new ArrayList<String>();
    private static List<String> ReplaceHistory = new ArrayList<String>();

    private IFindReplaceOperationProvider operationProvider;

    private Combo findInput;

    private Combo replaceInput;

    private Label infoLabel;

    private Map<Integer, Button> opButtons = new HashMap<Integer, Button>();

    private Map<Integer, List<Button>> paramWidgets = new HashMap<Integer, List<Button>>();

    private String initialFindText = null;

    private IWorkbenchWindow window;

    private IWorkbenchPart currentPart;

    private FindReplaceDialog(IWorkbenchWindow window) {
        super(window.getShell());
        setShellStyle(SWT.TITLE | SWT.MODELESS | SWT.CLOSE);
        final Shell shell = window.getShell();
        SINGLETONS.put(shell, this);
        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                SINGLETONS.remove(shell);
            }
        });
        this.window = window;
    }

    /**
     * 
     */
    private void startListeningToPartChanges() {
        window.getPartService().addPartListener(this);
    }

    private void stopListeningToPartChanges() {
        window.getPartService().removePartListener(this);
    }

    public void setInitialFindText(String initialFindText) {
        this.initialFindText = initialFindText;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        Composite parent = getShell().getParent();

        Monitor monitor = getShell().getDisplay().getPrimaryMonitor();
        if (parent != null) {
            monitor = parent.getMonitor();
        }

        Rectangle monitorBounds = monitor.getClientArea();
        Point centerPoint;
        if (parent != null) {
            centerPoint = getDialogCenter(parent.getBounds());
        } else {
            centerPoint = getDialogCenter(monitorBounds);
        }

        return new Point(centerPoint.x - (initialSize.x / 2), Math
                .max(monitorBounds.y, Math.min(centerPoint.y
                        - (initialSize.y / 2), monitorBounds.y
                        + monitorBounds.height - initialSize.y)));
    }

    private static Point getDialogCenter(Rectangle rect) {
        return new Point(rect.x + rect.width * 2 / 3, rect.y + rect.height / 2);
    }

    public void create() {
        super.create();
        IWorkbenchPart activePart = window.getActivePage().getActivePart();
        partActivated(activePart);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.verticalSpacing = 10;
        layout.marginTop = layout.marginHeight;
        layout.marginHeight = 0;
        layout.marginBottom = 0;

        createInputGroup(composite);
        createParameterGroups(composite);
        createOperationButtonGroup(composite);

        return composite;
    }

    /**
     * @param inputArea
     * @param factory
     */
    protected void createInputGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NO_FOCUS);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout inputLayout = new GridLayout(2, false);
        inputLayout.marginWidth = 0;
        inputLayout.marginHeight = 0;
        composite.setLayout(inputLayout);

        findInput = createInputWidget(composite,
                DialogMessages.FindReplaceDialog_Find_label, FindHistory,
                initialFindText);
        replaceInput = createInputWidget(composite,
                DialogMessages.FindReplaceDialog_ReplaceWith_label,
                ReplaceHistory, null);
    }

    /**
     * @param parent
     * @param factory
     * @param label
     * @param history
     * @param initText
     */
    private Combo createInputWidget(Composite parent, String label,
            List<String> history, String initText) {
        Label labelWidget = new Label(parent, SWT.NONE);
        labelWidget.setText(label);
        labelWidget
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        Combo input = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        layoutData.widthHint = TEXT_WIDTH;
        input.setLayoutData(layoutData);
        for (String t : history) {
            input.add(t);
        }
        if (initText != null)
            input.setText(initText);
        else if (!history.isEmpty())
            input.setText(history.get(0));

        input.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                updateOperationButtons();
                infoLabel.setText(EMPTY);
            }
        });
        return input;
    }

    /**
     * @param optionsGroup
     * @param factory
     */
    protected void createParameterGroups(Composite parent) {
        Composite composite = new Composite(parent, SWT.NO_FOCUS);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = convertVerticalDLUsToPixels(VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(HORIZONTAL_SPACING);
        composite.setLayout(layout);

        Composite optionGroup = createParameterGroup(composite,
                DialogMessages.FindReplaceDialog_OptionGroup);
        ((GridData) optionGroup.getLayoutData()).horizontalSpan = 2;
        createParameterWidget(optionGroup, SWT.CHECK,
                DialogMessages.FindReplaceDialog_CaseSensitive,
                IFindReplaceOperationProvider.PARAM_CASE_SENSITIVE);
        createParameterWidget(optionGroup, SWT.CHECK,
                DialogMessages.FindReplaceDialog_WholeWord,
                IFindReplaceOperationProvider.PARAM_WHOLE_WORD);

        Composite directionGroup = createParameterGroup(composite,
                DialogMessages.FindReplaceDialog_DirectionGroup);
        createParameterWidget(directionGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_Forward,
                IFindReplaceOperationProvider.PARAM_FORWARD);
        createParameterWidget(directionGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_Backward,
                IFindReplaceOperationProvider.PARAM_BACKWARD);

        Composite scopeGroup = createParameterGroup(composite,
                DialogMessages.FindReplaceDialog_ScopeGroup);
        createParameterWidget(scopeGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_CurrentMap,
                IFindReplaceOperationProvider.PARAM_CURRENT_MAP);
        createParameterWidget(scopeGroup, SWT.RADIO,
                DialogMessages.FindReplaceDialog_Workbook,
                IFindReplaceOperationProvider.PARAM_WORKBOOK);
    }

    private Composite createParameterGroup(Composite composite, String text) {
        Group group = new Group(composite, SWT.NO_FOCUS);
        group.setText(text);
        GridData groupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        group.setLayoutData(groupLayoutData);
        GridLayout groupLayout = new GridLayout();
        groupLayout.horizontalSpacing = 7;
        groupLayout.verticalSpacing = 7;
        group.setLayout(groupLayout);
        return group;
    }

    protected Button createParameterWidget(Composite parent, int style,
            String text, int paramId) {
        Button button = new Button(parent, style);
        button.setText(text);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        addParameterWidget(paramId, button);
//        if (operationProvider != null)
//            button
//                    .setSelection((operationProvider.getParameter() & paramId) != 0);
        button.setData(paramId);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Button b = (Button) event.widget;
                if (operationProvider != null)
                    operationProvider.setParameter((Integer) b.getData(), b
                            .getSelection());
                updateOperationButtons();
            }
        });
        return button;
    }

    private void addParameterWidget(final int paramId, final Button widget) {
        List<Button> widgets = paramWidgets.get(paramId);
        if (widgets == null) {
            widgets = new ArrayList<Button>();
            paramWidgets.put(paramId, widgets);
        }
        widgets.add(widget);
        paramWidgets.put(paramId, widgets);
        widget.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                removeParameterWidget(paramId, widget);
            }
        });
    }

    private void removeParameterWidget(int paramId, Button widget) {
        List<Button> widgets = paramWidgets.get(paramId);
        if (widgets != null) {
            widgets.remove(widget);
            if (widgets.isEmpty()) {
                paramWidgets.remove(paramId);
            }
        }
    }

    private void updateParameterWidgets() {
        for (Integer paramId : paramWidgets.keySet()) {
            updateParameterWidget(paramId);
        }
    }

    private void updateParameterWidget(Integer paramId) {
        List<Button> widgets = paramWidgets.get(paramId);
        if (widgets != null) {
            for (Button widget : widgets) {
                widget.setSelection(hasParameter(paramId.intValue()));
            }
        }
    }

    private boolean hasParameter(int paramId) {
        if (operationProvider == null)
            return false;
        int bit = operationProvider.getParameter() & paramId;
        return bit != 0;
    }

    /**
     * @param buttons
     * @param factory
     */
    protected void createOperationButtonGroup(Composite parent) {
        Composite buttons = new Composite(parent, SWT.NO_FOCUS);
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout buttonsLayout = new GridLayout(2, true);
        buttonsLayout.marginHeight = 0;
        buttonsLayout.marginWidth = 0;
        buttons.setLayout(buttonsLayout);
        createOperationButton(buttons, FIND_ID,
                DialogMessages.FindReplaceDialog_Find_text, true);
        createOperationButton(buttons, FIND_ALL_ID,
                DialogMessages.FindReplaceDialog_FindAll_text, false);
        createOperationButton(buttons, REPLACE_ID,
                DialogMessages.FindReplaceDialog_Replace_text, false);
        createOperationButton(buttons, REPLACE_ALL_ID,
                DialogMessages.FindReplaceDialog_ReplaceAll_text, false);
    }

    protected Button createOperationButton(Composite parent, int id,
            String text, boolean defaultButton) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        button.setText(text);
        button.setData(id);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                buttonPressed((Integer) event.widget.getData());
            }
        });
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
        }
        registerOperationButton(id, button);
        return button;
    }

    protected void registerOperationButton(final int id, final Button widget) {
        opButtons.put(id, widget);
        widget.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                unregisterOperationButton(id, widget);
            }

        });
    }

    private void unregisterOperationButton(int id, Button widget) {
        if (opButtons.get(id) == widget) {
            opButtons.remove(id);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getButton(int)
     */
    @Override
    protected Button getButton(int id) {
        Button button = super.getButton(id);
        if (button == null)
            button = opButtons.get(id);
        return button;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createButtonBar(Composite parent) {
        Composite container = new Composite(parent, SWT.NO_FOCUS);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout containerLayout = new GridLayout(2, false);
        containerLayout.horizontalSpacing = 0;
        containerLayout.verticalSpacing = 0;
        containerLayout.marginHeight = 7;
        containerLayout.marginWidth = 7;
        container.setLayout(containerLayout);

        infoLabel = new Label(container, SWT.NONE);
        infoLabel.setBackground(container.getBackground());
        infoLabel.setFont(container.getFont());
        infoLabel.setForeground(container.getDisplay().getSystemColor(
                SWT.COLOR_RED));
        infoLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
                true));

        Composite composite = (Composite) super.createButtonBar(container);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, CLOSE_ID, CLOSE_LABEL, false);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (CLOSE_ID == buttonId) {
            closePressed();
        } else if (FIND_ID == buttonId) {
            findPressed();
        } else if (FIND_ALL_ID == buttonId) {
            findAllPressed();
        } else if (REPLACE_ID == buttonId) {
            replacePressed();
        } else if (REPLACE_ALL_ID == buttonId) {
            replaceAllPressed();
        } else
            super.buttonPressed(buttonId);
    }

    /**
     * 
     */
    protected void findPressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        if (operationProvider.find(getFindText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        findInput.setFocus();
    }

    /**
     * 
     */
    protected void findAllPressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        boolean all = (operationProvider.getParameter() & IFindReplaceOperationProvider.PARAM_ALL) != 0;
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                true);
        if (operationProvider.find(getFindText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                all);
        findInput.setFocus();
    }

    /**
     * 
     */
    protected void replacePressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        if (operationProvider.replace(getFindText(), getReplaceText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        findInput.setFocus();
    }

    /**
     * 
     */
    protected void replaceAllPressed() {
        saveHistories();
        if (operationProvider == null)
            return;
        boolean all = (operationProvider.getParameter() & IFindReplaceOperationProvider.PARAM_ALL) != 0;
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                true);
        if (operationProvider.replace(getFindText(), getReplaceText())) {
            setInfoText(EMPTY);
        } else {
            setInfoText(STRING_NOT_FOUND);
        }
        operationProvider.setParameter(IFindReplaceOperationProvider.PARAM_ALL,
                all);
        findInput.setFocus();
    }

    protected void closePressed() {
        close();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        saveHistories();
        return super.close();
    }

    /**
     * @return
     */
    public String getFindText() {
        return findInput == null || findInput.isDisposed() ? EMPTY : findInput
                .getText();
    }

    /**
     * @return
     */
    public String getReplaceText() {
        return replaceInput == null || replaceInput.isDisposed() ? EMPTY
                : replaceInput.getText();
    }

    protected void setInfoText(String text) {
        if (infoLabel != null && !infoLabel.isDisposed()) {
            infoLabel.setText(text);
            infoLabel.getParent().layout();
        }
    }

    protected void saveHistories() {
        saveHistory(FindHistory, findInput);
        saveHistory(ReplaceHistory, replaceInput);
    }

    protected void saveHistory(List<String> history, Combo input) {
        String text = input.getText();
        if (text == null || EMPTY.equals(text))
            return;
        if (history.remove(text) && !input.isDisposed())
            input.remove(text);
        history.add(0, text);
        if (!input.isDisposed()) {
            input.add(text, 0);
            input.setText(text);
        }
    }

    protected void updateOperationButtons() {
        if (getShell() == null || getShell().isDisposed())
            return;

        getButton(FIND_ID).setEnabled(canFind());
        getButton(FIND_ALL_ID).setEnabled(canFindAll());
        getButton(REPLACE_ID).setEnabled(canReplace());
        getButton(REPLACE_ALL_ID).setEnabled(canReplaceAll());
    }

    protected boolean canFind() {
        return getFindText() != null && !EMPTY.equals(getFindText())
                && operationProvider != null
                && operationProvider.canFind(getFindText());
    }

    protected boolean canReplace() {
        return canFind()
                && getReplaceText() != null
                && operationProvider != null
                && operationProvider
                        .canReplace(getFindText(), getReplaceText());
    }

    protected boolean canFindAll() {
        return getFindText() != null && !EMPTY.equals(getFindText())
                && operationProvider != null
                && operationProvider.canFindAll(getFindText());
    }

    protected boolean canReplaceAll() {
        return canFindAll()
                && getReplaceText() != null
                && operationProvider != null
                && operationProvider.canReplaceAll(getFindText(),
                        getReplaceText());
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.FindReplaceDialog_windowTitle);
        startListeningToPartChanges();
        newShell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                stopListeningToPartChanges();
            }
        });
    }

    /**
     * @param operationProvider
     *            the operationProvider to set
     */
    public void setOperationProvider(
            IFindReplaceOperationProvider operationProvider) {
        this.operationProvider = operationProvider;
    }

    /**
     * @return the operationProvider
     */
    public IFindReplaceOperationProvider getOperationProvider() {
        return operationProvider;
    }

    public void partActivated(IWorkbenchPart part) {
        this.currentPart = part;
        setOperationProvider(getOperationProvider(part));
        updateOperationButtons();
        updateParameterWidgets();
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (part != this.currentPart)
            return;

        setOperationProvider(null);
        updateOperationButtons();
        updateParameterWidgets();
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    private IFindReplaceOperationProvider getOperationProvider(
            IWorkbenchPart part) {
        if (part == null)
            return null;
        return (IFindReplaceOperationProvider) part
                .getAdapter(IFindReplaceOperationProvider.class);
    }

    public static FindReplaceDialog getInstance(IWorkbenchWindow window) {
        Shell shell = window.getShell();
        FindReplaceDialog instance = SINGLETONS.get(shell);
        if (instance != null)
            return instance;
        return new FindReplaceDialog(window);
    }
}