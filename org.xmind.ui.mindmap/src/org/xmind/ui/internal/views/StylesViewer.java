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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.CategorizedGalleryViewer;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.viewers.ICategorizedContentProvider;

public class StylesViewer extends CategorizedGalleryViewer implements
        ICoreEventListener {

    private class StyleContentProvider implements ICategorizedContentProvider {

        public Object getCategory(Object element) {
            if (element instanceof IStyle) {
                IStyle style = (IStyle) element;
                return style.getType();
            }
            return null;
        }

        public Object[] getElements(Object inputElement) {
            List<Object> list = new ArrayList<Object>();
            if (inputElement instanceof IStyleSheet[]) {
                for (IStyleSheet sheet : (IStyleSheet[]) inputElement) {
                    addStyles(sheet, list);
                }
            } else if (inputElement instanceof IStyleSheet) {
                addStyles((IStyleSheet) inputElement, list);
            }
            return list.toArray();
        }

        private void addStyles(IStyleSheet sheet, List<Object> list) {
            if (sheet == MindMapUI.getResourceManager().getSystemStyleSheet()) {
                list.addAll(sheet.getStyles(IStyleSheet.AUTOMATIC_STYLES));
            } else {
                list.addAll(sheet.getAllStyles());
            }
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class StyleLabelProvider extends LabelProvider {

        public String getText(Object element) {
            if (element instanceof IStyle) {
                return ((IStyle) element).getName();
            } else if (element instanceof String) {
                String type = (String) element;
                if (IStyle.TOPIC.equalsIgnoreCase(type))
                    return MindMapMessages.StylesViewer_Topic_label;
                if (IStyle.BOUNDARY.equalsIgnoreCase(type))
                    return MindMapMessages.StylesViewer_Boundary_label;
                if (IStyle.RELATIONSHIP.equalsIgnoreCase(type))
                    return MindMapMessages.StylesViewer_Relationship_label;
                if (IStyle.SUMMARY.equalsIgnoreCase(type))
                    return MindMapMessages.StylesViewer_Summary_label;
                if (IStyle.MAP.equalsIgnoreCase(type))
                    return MindMapMessages.StylesViewer_Map_label;
            }
            return super.getText(element);
        }

    }

    private static class StylePart extends GraphicalEditPart {

        public StylePart(Object model) {
            setModel(model);
        }

        public IStyle getStyle() {
            return (IStyle) super.getModel();
        }

        protected IFigure createFigure() {
            return new StyleFigure();
        }

        protected void updateView() {
            super.updateView();
            ((StyleFigure) getFigure()).setStyle(getStyle());

            Properties properties = ((GalleryViewer) getSite().getViewer())
                    .getProperties();
            Dimension size = (Dimension) properties
                    .get(GalleryViewer.FrameContentSize);
            if (size != null) {
                getFigure().setPreferredSize(size);
            }
        }
    }

    private static class StylePartFactory implements IPartFactory {
        private IPartFactory factory;

        public StylePartFactory(IPartFactory factory) {
            this.factory = factory;
        }

        public IPart createPart(IPart context, Object model) {
            if (context instanceof FramePart && model instanceof IStyle) {
                return new StylePart(model);
            }
            return factory.createPart(context, model);
        }
    }

    private static class StyleSelectTool extends GallerySelectTool {

        protected boolean isTitleEditable(IPart p) {
            return super.isTitleEditable(p)
                    && ((IStyle) p.getModel()).getOwnedStyleSheet() != MindMapUI
                            .getResourceManager().getSystemStyleSheet();
        }

    }

    private static class StyleNameEditTool extends GalleryEditTool {

        protected IDocument getTextContents(IPart source) {
            return new Document(((IStyle) source.getModel()).getName());
        }

        protected void handleTextModified(IPart source, IDocument document) {
            ((IStyle) source.getModel()).setName(document.get());
            MindMapUI.getResourceManager().saveUserStyleSheet();
        }

        protected void hookEditor(FloatingTextEditor editor) {
            super.hookEditor(editor);
            getHelper().setPrefWidth(70);
        }

    }

    private ICoreEventRegister register;

    public StylesViewer(Composite parent) {
        setContentProvider(new StyleContentProvider());
        setLabelProvider(new StyleLabelProvider());
        createControl(parent, SWT.NO_REDRAW_RESIZE);
    }

    protected void configureNestedViewer(GalleryViewer viewer) {
        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout(
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_FILL, 1, 1,
                new Insets(5)));
        properties.set(GalleryViewer.FrameContentSize, new Dimension(64, 64));
        properties.set(GalleryViewer.TitlePlacement, GalleryViewer.TITLE_TOP);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);

        super.configureNestedViewer(viewer);
        viewer.setPartFactory(new StylePartFactory(viewer.getPartFactory()));
        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new StyleSelectTool());
        editDomain.installTool(GEF.TOOL_EDIT, new StyleNameEditTool());
        viewer.setEditDomain(editDomain);
    }

    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        if (register != null) {
            register.unregisterAll();
        } else {
            register = new CoreEventRegister(this);
        }
        if (input instanceof IStyleSheet) {
            hookSheet((IStyleSheet) input);
        } else if (input instanceof IStyleSheet[]) {
            for (IStyleSheet sheet : (IStyleSheet[]) input) {
                hookSheet(sheet);
            }
        }
    }

    @Override
    protected void handleDispose(DisposeEvent event) {
        if (register != null) {
            register.unregisterAll();
        }
        super.handleDispose(event);
    }

    private void hookSheet(IStyleSheet sheet) {
        ICoreEventSupport support = (ICoreEventSupport) sheet
                .getAdapter(ICoreEventSupport.class);
        if (support != null) {
            register.setNextSupport(support);
            register.register(Core.StyleAdd);
            register.register(Core.StyleRemove);
            register.register(Core.Name);
        }
    }

    public void handleCoreEvent(CoreEvent event) {
        if (Core.Name.equals(event.getType())) {
            if (event.getSource() instanceof IStyle) {
                update(event.getSource(), null);
            }
        } else {
            setInput(getInput());
        }
    }

    public void setSelection(ISelection selection) {
        super.setSelection(selection, true);
    }

}