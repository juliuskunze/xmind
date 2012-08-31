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
package org.xmind.ui.internal.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.xmind.gef.Request;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;

public class SheetAdvancedStylePropertySectionPart extends
        StyledPropertySectionPart {

    private Button multiLineColorsCheck;

    private Button taperedLinesCheck;

    protected void createContent(Composite parent) {
        multiLineColorsCheck = new Button(parent, SWT.CHECK);
        multiLineColorsCheck.setText(PropertyMessages.MultiLineColors_text);
        multiLineColorsCheck.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                changeRainbowColor();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        taperedLinesCheck = new Button(parent, SWT.CHECK);
        taperedLinesCheck.setText(PropertyMessages.TaperedLines_text);
        taperedLinesCheck.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                changeTaperedLines();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void changeRainbowColor() {
        Request request = createStyleRequest(CommandMessages.Command_ToggleMultiLineColors);
        if (multiLineColorsCheck.getSelection()) {
            addStyle(request, Styles.MultiLineColors,
                    Styles.DEFAULT_MULTI_LINE_COLORS);
        } else {
            addStyle(request, Styles.MultiLineColors, null);
        }
        sendRequest(request);
    }

    private void changeTaperedLines() {
        Request request = createStyleRequest(CommandMessages.Command_ToggleTaperedLines);
        if (taperedLinesCheck.getSelection()) {
            addStyle(request, Styles.LineTapered, Styles.TAPERED);
        } else {
            addStyle(request, Styles.LineTapered, null);
        }
        sendRequest(request);
    }

    protected void doRefresh() {
        if (multiLineColorsCheck != null && !multiLineColorsCheck.isDisposed()) {
            String value = getStyleValue(Styles.MultiLineColors, null);
            multiLineColorsCheck.setSelection(value != null);
        }
        if (taperedLinesCheck != null && !taperedLinesCheck.isDisposed()) {
            String value = getStyleValue(Styles.LineTapered, null);
            taperedLinesCheck.setSelection(value != null);
        }
    }

    public void dispose() {
        super.dispose();
        multiLineColorsCheck = null;
        taperedLinesCheck = null;
    }

    public void setFocus() {
        multiLineColorsCheck.setFocus();
    }

}