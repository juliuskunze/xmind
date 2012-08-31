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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.core.Core;
import org.xmind.core.ILegend;
import org.xmind.core.ISheet;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.Request;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.MindMapPropertySectionPartBase;

public class LegendPropertySectionPart extends MindMapPropertySectionPartBase {

    private Button visibilityCheck;

    protected void createContent(Composite parent) {
        visibilityCheck = new Button(parent, SWT.CHECK);
        visibilityCheck.setLayoutData(new GridData(GridData.FILL,
                GridData.CENTER, true, false));
        visibilityCheck.setText(PropertyMessages.ShowLegend_text);
        visibilityCheck.setToolTipText(PropertyMessages.ShowLegend_toolTip);
        visibilityCheck.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                changeLegendVisibility(visibilityCheck.getSelection());
            }
        });
    }

    protected void registerEventListener(ICoreEventSource source,
            ICoreEventRegister register) {
        if (source instanceof ISheet) {
            ILegend legend = ((ISheet) source).getLegend();
            if (legend instanceof ICoreEventSource) {
                source = (ICoreEventSource) legend;
                register.setNextSource(source);
                register.register(Core.Visibility);
            }
        }
    }

    protected void doRefresh() {
        if (visibilityCheck != null && !visibilityCheck.isDisposed()) {
            visibilityCheck.setSelection(isLegendVisible());
        }
    }

    private boolean isLegendVisible() {
        for (Object o : getSelectedElements()) {
            if (o instanceof ISheet) {
                ILegend legend = ((ISheet) o).getLegend();
                if (!legend.isVisible())
                    return false;
            }
        }
        return true;
    }

    public void setFocus() {
        if (visibilityCheck != null && !visibilityCheck.isDisposed()) {
            visibilityCheck.setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        visibilityCheck = null;
    }

    protected void changeLegendVisibility(boolean visible) {
        Request request = new Request(visible ? MindMapUI.REQ_SHOW_LEGEND
                : MindMapUI.REQ_HIDE_LEGEND);
        sendRequest(fillTargets(request));
    }

}