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
package org.xmind.ui.internal.workbench;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * Brainstorm Perspective
 * 
 * @author Brian Sun
 */
public class Perspective implements IPerspectiveFactory {

    public static final String CONSOLE_VIEW_ID = "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$
    public static final String OUTLINE_VIEW_ID = "org.eclipse.ui.views.ContentOutline"; //$NON-NLS-1$
    public static final String PROPERTIES_VIEW_ID = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$

    protected float getRatio1() {
        return (float) (1.0 - 250.0 / Util.getInitialWindowSize().x);
    }

    public void createInitialLayout(IPageLayout pageLayout) {
        createUpRightLayout(pageLayout);
        createBottomRightLayout(pageLayout);
        createBottomLayout(pageLayout);
        createLeftLayout(pageLayout);

        pageLayout.addShowViewShortcut(OUTLINE_VIEW_ID);
        pageLayout.addShowViewShortcut(MindMapUI.VIEW_OVERVIEW);
        pageLayout.addShowViewShortcut(MindMapUI.VIEW_MARKER);
        pageLayout.addShowViewShortcut(MindMapUI.VIEW_NOTES);
        pageLayout.addShowViewShortcut(MindMapUI.VIEW_STYLES);
        pageLayout.addShowViewShortcut(PROPERTIES_VIEW_ID);
        pageLayout.addShowViewShortcut(MindMapUI.VIEW_THEMES);
        pageLayout.addShowViewShortcut(MindMapUI.VIEW_BROSWER);
        pageLayout.addShowViewShortcut(MindMapUI.VIEW_REVISIONS);
    }

    private void createUpRightLayout(IPageLayout pageLayout) {
        IFolderLayout layout = pageLayout.createFolder("upRight", //$NON-NLS-1$
                IPageLayout.RIGHT, getRatio1(), pageLayout.getEditorArea());
        layout.addView(OUTLINE_VIEW_ID);
        layout.addView(MindMapUI.VIEW_OVERVIEW);
        layout.addPlaceholder(MindMapUI.VIEW_STYLES);
    }

    private void createBottomRightLayout(IPageLayout pageLayout) {
        IFolderLayout layout = pageLayout.createFolder("bottomRight", //$NON-NLS-1$
                IPageLayout.BOTTOM, 0.5f, "upRight"); //$NON-NLS-1$
        layout.addView(PROPERTIES_VIEW_ID);
        layout.addView(MindMapUI.VIEW_MARKER);
    }

    private void createBottomLayout(IPageLayout pageLayout) {
        IPlaceholderFolderLayout layout = pageLayout.createPlaceholderFolder(
                "bottom", IPageLayout.BOTTOM, 0.7f, pageLayout.getEditorArea()); //$NON-NLS-1$
        layout.addPlaceholder(MindMapUI.VIEW_NOTES);
        layout.addPlaceholder(MindMapUI.VIEW_THEMES);
    }

    private void createLeftLayout(IPageLayout pageLayout) {
        IPlaceholderFolderLayout layout = pageLayout.createPlaceholderFolder(
                "left", IPageLayout.LEFT, 0.37f, pageLayout.getEditorArea()); //$NON-NLS-1$
        layout.addPlaceholder("org.xmind.ui.BrowserView"); //$NON-NLS-1$
    }

}