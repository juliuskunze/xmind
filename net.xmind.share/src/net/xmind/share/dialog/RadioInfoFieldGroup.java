/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

public class RadioInfoFieldGroup implements ISelectionProvider {

    private Group group;

    private List<Button> buttons = new ArrayList<Button>();

    private boolean grabVerticalSpace;

    private ISelectionChangedListener listener;

    public RadioInfoFieldGroup(boolean grabVerticalSpace) {
        this.grabVerticalSpace = grabVerticalSpace;
    }

    public void fill(Composite parent) {
        group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                grabVerticalSpace));
        GridLayout layout = new GridLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        layout.verticalSpacing = 3;
        layout.horizontalSpacing = 0;
        group.setLayout(layout);
    }

    public void setName(String name) {
        group.setText(name == null ? "" : name); //$NON-NLS-1$
    }

    public String getName() {
        if (group == null || group.isDisposed())
            return ""; //$NON-NLS-1$
        return group.getText();
    }

    public Group getNameWidget() {
        return group;
    }

    public boolean isDisposed() {
        return group.isDisposed();
    }

    public void setFocus() {
        if (!group.setFocus()) {
            if (!buttons.isEmpty()) {
                buttons.get(0).setFocus();
            }
        }
    }

    public Button addOption(final Object value, String label) {
        final Button button = new Button(group, SWT.RADIO | SWT.WRAP);
        button.setText(label);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        button.setData(value);
        if (buttons.isEmpty())
            button.setSelection(true);
        buttons.add(button);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (button.getSelection()) {
                    fireSelectionChanged(value);
                }
            }
        });
        return button;
    }

    protected void fireSelectionChanged(Object value) {
        if (listener != null) {
            listener.selectionChanged(new SelectionChangedEvent(this,
                    getSelection()));
        }
    }

    public Object getSelectedValue() {
        for (Button button : buttons) {
            if (button.getSelection())
                return button.getData();
        }
        return null;
    }

    public void setSelectedValue(Object value) {
        for (Button button : buttons) {
            button.setSelection(equals(value, button.getData()));
        }
    }

    private static boolean equals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        this.listener = listener;
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (listener == this.listener)
            this.listener = null;
    }

    public void setSelection(ISelection selection) {
        setSelectedValue(((IStructuredSelection) selection).getFirstElement());
    }

    public ISelection getSelection() {
        return new StructuredSelection(getSelectedValue());
    }
}