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
package org.xmind.ui.internal.views;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditor;

public class ThemesViewer extends GalleryViewer {

    private static class ThemeLabelProvider extends LabelProvider {

        public String getText(Object element) {
            if (element instanceof IStyle) {
                IStyle style = (IStyle) element;
                return style.getName();
            }
            return super.getText(element);
        }

    }

    private static class ThemePart extends GraphicalEditPart {

        public ThemePart(IStyle style) {
            setModel(style);
        }

        public IStyle getStyle() {
            return (IStyle) super.getModel();
        }

        protected IFigure createFigure() {
            return new ThemeFigure();
        }

        protected void updateView() {
            super.updateView();
            ((ThemeFigure) getFigure()).setTheme(getStyle());

            Properties properties = ((GalleryViewer) getSite().getViewer())
                    .getProperties();
            Dimension size = (Dimension) properties
                    .get(GalleryViewer.FrameContentSize);
            if (size != null) {
                getFigure().setPreferredSize(size);
            }
        }

    }

    private static class ThemePartFactory implements IPartFactory {

        private IPartFactory factory;

        public ThemePartFactory(IPartFactory factory) {
            this.factory = factory;
        }

        public IPart createPart(IPart context, Object model) {
            if (context instanceof FramePart && model instanceof IStyle)
                return new ThemePart((IStyle) model);
            return factory.createPart(context, model);
        }

    }

    private static class ThemeSelectTool extends GallerySelectTool {
        public boolean isTitleEditable(IPart p) {
            if (!super.isTitleEditable(p))
                return false;
            IStyle theme = (IStyle) p.getModel();
            return theme != MindMapUI.getResourceManager().getDefaultTheme()
                    && theme.getOwnedStyleSheet() != MindMapUI
                            .getResourceManager().getSystemThemeSheet();
        }
    }

    private static class ThemeNameEditTool extends GalleryEditTool {

        protected IDocument getTextContents(IPart source) {
            return new Document(((IStyle) source.getModel()).getName());
        }

        protected void handleTextModified(IPart source, IDocument document) {
            ((IStyle) source.getModel()).setName(document.get());
            MindMapUI.getResourceManager().saveUserThemeSheet();
        }

        protected void hookEditor(FloatingTextEditor editor) {
            super.hookEditor(editor);
            getHelper().setPrefWidth(130);
        }

    }

    public ThemesViewer(Composite parent) {
        super();
        setPartFactory(new ThemePartFactory(getPartFactory()));
        setLabelProvider(new ThemeLabelProvider());
        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout(
                GalleryLayout.ALIGN_CENTER, GalleryLayout.ALIGN_FILL, 1, 1,
                new Insets(5)));
        properties.set(GalleryViewer.FrameContentSize, new Dimension(128, 64));
        properties.set(GalleryViewer.TitlePlacement, GalleryViewer.TITLE_TOP);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new ThemeSelectTool());
        editDomain.installTool(GEF.TOOL_EDIT, new ThemeNameEditTool());
        editDomain.setViewer(this);

        createControl(parent);

        final Display display = parent.getDisplay();
        getControl().setBackground(
                display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    }

    public void setSelection(ISelection selection) {
        super.setSelection(selection, true);
    }

}