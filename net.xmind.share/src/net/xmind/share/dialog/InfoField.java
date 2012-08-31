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
package net.xmind.share.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class InfoField {

    private Label label;

    private Text text;

    private boolean grabVerticalSpace;

    private boolean readOnly;

    private boolean singleLine;

    public InfoField(boolean grabVerticalSpace, boolean readOnly,
            boolean singleLine) {
        this.grabVerticalSpace = grabVerticalSpace;
        this.readOnly = readOnly;
        this.singleLine = singleLine;
    }

    public void fill(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
                true, grabVerticalSpace));
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 3;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));

        text = new Text(composite, getTextStyle());
        GridData textData = new GridData(GridData.FILL, GridData.FILL, true,
                grabVerticalSpace);
        if (grabVerticalSpace) {
            textData.heightHint = 100;
        }
        text.setLayoutData(textData);
    }

    private int getTextStyle() {
        int style = SWT.BORDER | (singleLine ? SWT.SINGLE : SWT.MULTI);
        if (readOnly)
            style |= SWT.READ_ONLY;
        return style;
    }

    public void setName(String name) {
        label.setText(name == null ? "" : name); //$NON-NLS-1$
    }

    public void setText(String text) {
        this.text.setText(text == null ? "" : text); //$NON-NLS-1$
    }

    public String getName() {
        if (label == null || label.isDisposed())
            return ""; //$NON-NLS-1$
        return label.getText();
    }

    public String getText() {
        if (text == null || text.isDisposed())
            return ""; //$NON-NLS-1$
        return text.getText();
    }

    public Label getNameWidget() {
        return label;
    }

    public Text getTextWidget() {
        return text;
    }

    public boolean isDisposed() {
        return text.isDisposed() || label.isDisposed();
    }

    public void setFocus() {
        text.setFocus();
    }

}