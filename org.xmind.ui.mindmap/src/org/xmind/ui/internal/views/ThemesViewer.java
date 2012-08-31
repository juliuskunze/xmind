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
package org.xmind.ui.internal.views;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.mindmap.IMindMapImages;
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
            ((ThemeFigure) getFigure()).setDefaultImage(getDefaultImage());

            Properties properties = ((GalleryViewer) getSite().getViewer())
                    .getProperties();
            Dimension size = (Dimension) properties
                    .get(GalleryViewer.FrameContentSize);
            if (size != null) {
                getFigure().setPreferredSize(size);
            }
        }

        protected void register() {
            registerModel(getStyle().getId());
            super.register();
        }

        @Override
        protected void unregister() {
            super.unregister();
            unregisterModel(getStyle().getId());
        }

        private Image getDefaultImage() {
            return ((ThemesViewer) getSite().getViewer())
                    .getDefaultImage(getStyle());
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
        protected boolean isTitleEditable(IPart p) {
            if (!super.isTitleEditable(p))
                return false;
            IStyle theme = (IStyle) p.getModel();
            return theme != MindMapUI.getResourceManager().getBlankTheme()
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

    private IStyle defaultTheme = null;

    private Image defaultImage = null;

    public ThemesViewer(Composite parent) {
        super();
        init();
        createControl(parent);

        final Display display = parent.getDisplay();
        getControl().setBackground(
                display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        getControl().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (defaultImage != null) {
                    defaultImage.dispose();
                    defaultImage = null;
                }
            }
        });
    }

    protected void init() {
        setPartFactory(new ThemePartFactory(getPartFactory()));
        setLabelProvider(new ThemeLabelProvider());

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new ThemeSelectTool());
        editDomain.installTool(GEF.TOOL_EDIT, new ThemeNameEditTool());
        setEditDomain(editDomain);

        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout(
                GalleryLayout.ALIGN_CENTER, GalleryLayout.ALIGN_FILL, 1, 1,
                new Insets(5)));
        properties.set(GalleryViewer.FrameContentSize, new Dimension(128, 64));
        properties.set(GalleryViewer.TitlePlacement, GalleryViewer.TITLE_TOP);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
    }

    public void setSelection(ISelection selection) {
        super.setSelection(selection, true);
    }

    public IStyle getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(IStyle defaultTheme) {
        IStyle oldTheme = this.defaultTheme;
        this.defaultTheme = defaultTheme;

        updateThemePart(oldTheme);
        updateThemePart(defaultTheme);
    }

    private Image getDefaultImage(IStyle theme) {
        return (theme == this.defaultTheme) ? getDefaultImage() : null;
    }

    private Image getDefaultImage() {
        if (defaultImage == null) {
            ImageDescriptor desc = MindMapUI.getImages().get(
                    IMindMapImages.STAR, true);
            if (desc != null) {
                try {
                    defaultImage = desc.createImage(false, getControl()
                            .getDisplay());
                } catch (Throwable e) {
                    //e.printStackTrace();
                }
            }
        }
        return defaultImage;
    }

    private void updateThemePart(IStyle theme) {
        ThemePart part = findThemePart(theme);
        if (part != null)
            part.update();
    }

    private ThemePart findThemePart(IStyle style) {
        if (style == null)
            return null;
        return (ThemePart) getPartRegistry().getPartByModel(style.getId());
    }

    public void startEditing(IStyle theme) {
        EditDomain domain = getEditDomain();
        ITool tool = domain.getDefaultTool();
        ((GallerySelectTool) tool).getStatus().setStatus(GEF.ST_ACTIVE, true);
        domain.handleRequest(GEF.REQ_EDIT, this);
    }

}