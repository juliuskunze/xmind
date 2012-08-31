/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.ui.color;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

public class PaletteViewer extends Viewer implements IPaletteViewer {

    protected final class PaletteItemAction extends ColorAction {

        private int type;

        public PaletteItemAction(int type, String text) {
            super(null, AS_CHECK_BOX);
            this.type = type;
            setText(text);
        }

        public PaletteItemAction(PaletteItem item) {
            super(item.color, AS_CHECK_BOX);
            this.type = IColorSelection.CUSTOM;
            setToolTipText(item.description);
        }

        public int getType() {
            return type;
        }

        public void run() {
            super.run();
            actionSelected(this);
        }

    }

    private Composite control = null;

    private PaletteContents contents = null;

    private PaletteItemAction autoAction = null;

    private PaletteItemAction noneAction = null;

    private PaletteItemAction customAction = null;

    private boolean showAuto = false;

    private boolean showNone = false;

    private boolean showCustom = false;

    private RGB autoColor = null;

    private ToolBarManager autoToolBar = null;

    private Control sep1 = null;

    private ToolBarManager paletteToolBar = null;

    private Control sep2 = null;

    private ToolBarManager noneToolBar = null;

    private Control sep3 = null;

    private ToolBarManager customToolBar = null;

    private List<PaletteItemAction> paletteActions = null;

    private PaletteItemAction selection = null;

    private List<IOpenListener> openListeners = null;

    public PaletteViewer() {
    }

    public Control createControl(Composite parent) {
        if (!controlExists() && parent != null) {
            control = new Composite(parent, SWT.NO_FOCUS);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 3;
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            control.setLayout(layout);
            control.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    handleDispose(e);
                }
            });
        }
        return control;
    }

    protected void handleDispose(DisposeEvent e) {
        if (autoToolBar != null)
            autoToolBar.dispose();
        if (noneToolBar != null)
            noneToolBar.dispose();
        if (customToolBar != null)
            customToolBar.dispose();
        if (paletteToolBar != null)
            paletteToolBar.dispose();
        if (sep1 != null) {
            sep1.dispose();
            sep1 = null;
        }
        if (sep2 != null) {
            sep2.dispose();
            sep2 = null;
        }
        if (sep3 != null) {
            sep3.dispose();
            sep3 = null;
        }
    }

    private boolean controlExists() {
        return control != null && !control.isDisposed();
    }

    public Control getControl() {
        return control;
    }

    public void setPaletteItems(PaletteItem[] items) {
        setContents(items == null ? null : new PaletteContents(items));
    }

    public PaletteItem[] getPaletteItems() {
        return contents == null ? null : contents.toArray();
    }

    public void setContents(PaletteContents contents) {
        PaletteContents oldContents = this.contents;
        this.contents = contents;
        inputChanged(contents, oldContents);
    }

    public PaletteContents getContents() {
        return contents;
    }

    public boolean getShowAutoItem() {
        return showAuto;
    }

    public boolean getShowCustomItem() {
        return showCustom;
    }

    public boolean getShowNoneItem() {
        return showNone;
    }

    public void setShowAutoItem(boolean show) {
        if (show == getShowAutoItem())
            return;
        this.showAuto = show;
        if (show) {
            if (autoToolBar == null)
                autoToolBar = createItemBar();
            if (autoAction == null)
                autoAction = createItemAction(IColorSelection.AUTO,
                        PaletteMessages.PaletteViewer_Automatic,
                        getAutoColor(), autoToolBar);
        } else {
            autoAction = null;
            if (autoToolBar != null) {
                autoToolBar.dispose();
                autoToolBar = null;
            }
            if (sep1 != null) {
                sep1.dispose();
                sep1 = null;
            }
        }
        refresh();
    }

    public void setShowNoneItem(boolean show) {
        if (show == getShowNoneItem())
            return;
        this.showNone = show;
        if (show) {
            if (noneToolBar == null)
                noneToolBar = createItemBar();
            if (noneAction == null)
                noneAction = createItemAction(IColorSelection.NONE,
                        PaletteMessages.PaletteViewer_None, null, noneToolBar);
        } else {
            noneAction = null;
            if (noneToolBar != null) {
                noneToolBar.dispose();
                noneToolBar = null;
            }
            if (sep2 != null) {
                sep2.dispose();
                sep2 = null;
            }
        }
        refresh();
    }

    public void setShowCustomItem(boolean show) {
        if (show == getShowCustomItem())
            return;
        this.showCustom = show;
        if (show) {
            if (customToolBar == null)
                customToolBar = createItemBar();
            if (customAction == null)
                customAction = createItemAction(IColorSelection.CUSTOM,
                        PaletteMessages.PaletteViewer_Custom, null,
                        customToolBar);
        } else {
            customAction = null;
            if (customToolBar != null) {
                customToolBar.dispose();
                customToolBar = null;
            }
            if (sep3 != null) {
                sep3.dispose();
                sep3 = null;
            }
        }
        refresh();
    }

    public RGB getAutoColor() {
        return autoColor;
    }

    public void setAutoColor(RGB color) {
        if (color == this.autoColor
                || (color != null && color.equals(this.autoColor)))
            return;
        this.autoColor = color;
        if (autoAction != null) {
            autoAction.setColor(color);
        }
    }

    public ISelection getSelection() {
        return selection == null ? ColorSelection.EMPTY : new ColorSelection(
                selection.getType(), selection.getColor());
    }

    public void refresh() {
        setContents(getContents());
    }

    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        if (control != null && !control.isDisposed()) {
            update(control, input, oldInput);
        }
    }

    private void update(Composite parent, Object input, Object oldInput) {
        boolean showAutoItem = getShowAutoItem();
        boolean showNoneItem = getShowNoneItem();
        boolean showCustomItem = getShowCustomItem();

        PaletteItem[] oldItems = oldInput == null ? null
                : ((PaletteContents) oldInput).toArray();
        PaletteItem[] newItems = input == null ? null
                : ((PaletteContents) input).toArray();
        boolean paletteChanged = !equals(oldItems, newItems);
        boolean showPaletteItems = newItems != null && newItems.length > 0;

        int selType = selection == null ? -1 : selection.getType();
        RGB selColor = selection == null ? null : selection.getColor();

        Control last = null;

        if (showAutoItem) {
            last = showItemBar(autoToolBar, parent, last);
            if (showPaletteItems || showNoneItem || showCustomItem) {
                if (sep1 == null || sep1.isDisposed()) {
                    sep1 = createSeparator(parent);
                }
                last = moveControl(sep1, last);
            }
        }

        if (showPaletteItems) {
            last = showPalette(input, newItems, paletteChanged, parent, last);
        } else {
            if (paletteActions != null) {
                paletteActions.clear();
                paletteActions = null;
            }
            if (paletteToolBar != null) {
                paletteToolBar.dispose();
                paletteToolBar = null;
            }
        }

        if (showNoneItem) {
            if (showPaletteItems || showAutoItem) {
                if (sep2 == null || sep2.isDisposed()) {
                    sep2 = createSeparator(parent);
                }
                last = moveControl(sep2, last);
            }
            last = showItemBar(noneToolBar, parent, last);
        }

        if (showCustomItem) {
            if (showAutoItem || showPaletteItems || showNoneItem) {
                if (sep3 == null || sep3.isDisposed()) {
                    sep3 = createSeparator(parent);
                }
                last = moveControl(sep3, last);
            }
            last = showItemBar(customToolBar, parent, last);
        }

        selection = findActionToSelect(selType, selColor);

        parent.layout();
    }

    private Control showPalette(Object newInput, PaletteItem[] newItems,
            boolean paletteChanged, Composite parent, Control currentControl) {
        if (paletteToolBar == null) {
            paletteToolBar = new ToolBarManager(SWT.RIGHT | SWT.FLAT | SWT.WRAP);
            paletteChanged = true;
        }
        if (paletteActions == null) {
            paletteActions = new ArrayList<PaletteItemAction>();
            paletteChanged = true;
        }
        if (paletteChanged) {
            paletteToolBar.removeAll();
            paletteActions.clear();
            for (PaletteItem item : newItems) {
                PaletteItemAction action = new PaletteItemAction(item);
                paletteToolBar.add(action);
                paletteActions.add(action);
            }

        }
        ToolBar tb = paletteToolBar.getControl();
        if (tb == null) {
            tb = paletteToolBar.createControl(parent);
            tb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            moveControl(tb, currentControl);
        } else {
            paletteToolBar.update(true);
        }
        // Calculate a preferred width to wrap the palette tool bar
        int w = tb.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x
                / ((PaletteContents) newInput).getPreferredRows();
        ((GridData) tb.getLayoutData()).widthHint = w;
        return tb;
    }

    private Control showItemBar(ToolBarManager tbm, Composite parent,
            Control last) {
        if (tbm != null) {
            ToolBar tb = tbm.getControl();
            if (tb == null || tb.isDisposed()) {
                tb = tbm.createControl(parent);
                tb.setLayoutData(new GridData(GridData.CENTER, GridData.FILL,
                        true, false));
                moveControl(tb, last);
            } else {
                tbm.update(true);
            }
            return tb;
        }
        return last;
    }

    protected Control createSeparator(Composite parent) {
        Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return label;
    }

    private ToolBarManager createItemBar() {
        return new ToolBarManager(SWT.RIGHT | SWT.FLAT);
    }

    private PaletteItemAction createItemAction(int type, String text,
            RGB initColor, ToolBarManager parent) {
        PaletteItemAction action = new PaletteItemAction(type, text);
        action.setColor(initColor);
        ActionContributionItem ci = new ActionContributionItem(action);
        ci.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        parent.add(ci);
        return action;
    }

    private Control moveControl(Control c, Control last) {
        if (c != null) {
            if (last != null) {
                c.moveBelow(last);
            } else {
                c.moveAbove(null);
            }
            return c;
        }
        return last;
    }

    protected PaletteItemAction findActionToSelect(int type, RGB color) {
        if (autoAction != null && type == autoAction.getType())
            return autoAction;
        if (noneAction != null && type == noneAction.getType())
            return noneAction;
        if (type == IColorSelection.CUSTOM) {
            if (paletteActions != null) {
                for (PaletteItemAction a : paletteActions) {
                    if (equals(color, a.getColor()))
                        return a;
                }
            }
            if (customAction != null && equals(color, customAction.getColor()))
                return customAction;
        }
        return null;
    }

    protected static boolean equals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    public void setSelection(ISelection selection, boolean reveal) {
        PaletteItemAction oldSelection = this.selection;
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IColorSelection)) {
            selectAction(null);
            if (oldSelection != null)
                fireColorSelectionChanged();
            return;
        }
        IColorSelection colorSelection = (IColorSelection) selection;
        setSelection(colorSelection.getType(), colorSelection.getColor());
    }

    protected void setSelection(int type, RGB color) {
        PaletteItemAction oldSelection = this.selection;
        PaletteItemAction newSelection = findActionToSelect(type, color);
        if (newSelection == null) {
            if (type == IColorSelection.CUSTOM && customAction != null) {
                RGB oldCustomColor = customAction.getColor();
                if (!equals(color, oldCustomColor)) {
                    customAction.setColor(color);
                    selectAction(customAction);
                    fireColorSelectionChanged();
                    return;
                }
            }
        }
        selectAction(newSelection);
        if (newSelection != oldSelection)
            fireColorSelectionChanged();
    }

    protected void fireColorSelectionChanged() {
        fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
    }

    protected void selectAction(PaletteItemAction action) {
        if (action != null)
            action.setChecked(true);
        if (action == selection)
            return;
        this.selection = action;
        if (autoAction != null && autoAction != action)
            autoAction.setChecked(false);
        if (noneAction != null && noneAction != action)
            noneAction.setChecked(false);
        if (customAction != null && customAction != action)
            customAction.setChecked(false);
        if (paletteActions != null) {
            for (PaletteItemAction a : paletteActions) {
                if (a != action)
                    a.setChecked(false);
            }
        }
    }

    public Object getInput() {
        return getContents();
    }

    public void setInput(Object input) {
        if (!(input instanceof PaletteContents))
            input = null;
        setContents((PaletteContents) input);
    }

    public void addOpenListener(IOpenListener listener) {
        if (openListeners == null)
            openListeners = new ArrayList<IOpenListener>();
        openListeners.add(listener);
    }

    public void removeOpenListener(IOpenListener listener) {
        if (openListeners == null)
            return;
        openListeners.remove(listener);
    }

    protected void fireOpenEvent(final OpenEvent event) {
        if (openListeners == null)
            return;
        for (final Object l : openListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IOpenListener) l).open(event);
                }
            });
        }
    }

    protected void actionSelected(PaletteItemAction action) {
        if (action == customAction) {
            RGB oldColor = action.getColor();
            Shell shell = getControl().getShell();
            if (shell != null && !shell.isDisposed()) {
                RGB newColor = openNativeColorDialog(shell, oldColor);
                if (newColor != null) {
                    setSelection(action.getType(), newColor);
                    action.setColor(newColor);
                }
            }
        } else {
            setSelection(action.getType(), action.getColor());
        }
        fireOpenEvent(new OpenEvent(this, getSelection()));
    }

    protected RGB openNativeColorDialog(Shell shell, RGB oldColor) {
        ColorDialog dialog = new ColorDialog(shell, SWT.NONE);
        dialog.setRGB(oldColor);
        return dialog.open();
    }

}