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
package org.xmind.ui.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xmind.ui.forms.WidgetFactory;

public abstract class CategorizedViewer extends StructuredViewer {

    private List<Object> categories = new ArrayList<Object>();

    private Map<Object, List<Object>> categorizedElements = new HashMap<Object, List<Object>>();

    private List<Composite> sections = new ArrayList<Composite>();

    private ScrolledForm container = null;

    private WidgetFactory factory;

    private int sectionStyle = Section.TITLE_BAR | Section.TWISTIE
            | Section.EXPANDED;

    private Listener sectionContentListener = new Listener() {

        public void handleEvent(Event event) {
            if (event.type == SWT.MouseWheel) {
                scroll(event);
            }
        }

    };

    public int getSectionStyle() {
        return sectionStyle;
    }

    public void setSectionStyle(int sectionStyle) {
        this.sectionStyle = sectionStyle;
    }

    protected Composite getContainer() {
        return container;
    }

    public void createControl(Composite parent, int style) {
        container = createContainer(parent, style);
        hookControl(container);
    }

    private ScrolledForm createContainer(Composite parent, int style) {
        if (factory == null)
            factory = new WidgetFactory(parent.getDisplay());
        ScrolledForm form = factory.createScrolledForm(parent);
        configureForm(form);
        hookForm(form);
        return form;
    }

    private void configureForm(ScrolledForm form) {
        form.getBody().setLayout(createFormLayout());
        form.setMinWidth(1);
    }

    private void hookForm(ScrolledForm form) {
        Listener eventHandler = new Listener() {
            public void handleEvent(Event event) {
                relayout();
            }
        };
        form.addListener(SWT.Resize, eventHandler);
    }

    private Layout createFormLayout() {
        RowLayout layout = new RowLayout(SWT.VERTICAL);
        layout.wrap = false;
        layout.fill = true;
        return layout;
    }

    private void relayout() {
        int width = getContainer().getClientArea().width;
        for (Composite section : sections) {
            resetWidth(width, section);
        }
        container.reflow(true);
    }

    private void resetWidth(int width, Control control) {
        Object ld = control.getLayoutData();
        if (ld instanceof GridData) {
            GridData gd = (GridData) ld;
            GridLayout gl = (GridLayout) control.getParent().getLayout();
            gd.widthHint = width - gl.marginWidth - gl.marginLeft
                    - gl.marginRight;
        } else if (ld instanceof RowData) {
            RowData rd = (RowData) ld;
            RowLayout rl = (RowLayout) control.getParent().getLayout();
            rd.width = width - rl.marginWidth - rl.marginLeft - rl.marginRight;
        }
    }

    protected List<Object> getCategories() {
        return categories;
    }

    protected List<Object> getElements(Object category) {
        return categorizedElements.get(category);
    }

    protected Composite getSection(Object category) {
        return sections.get(categories.indexOf(category));
    }

    protected Widget doFindInputItem(Object element) {
        return getControl();
    }

    protected Widget doFindItem(Object element) {
        if (getCategories().contains(element))
            return getSection(element);
        return getControl();
    }

    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        Map<Object, Boolean> expansionStates = new HashMap<Object, Boolean>();
        for (Object category : getCategories()) {
            Composite section = getSection(category);
            if (section instanceof Section) {
                expansionStates.put(category, ((Section) section).isExpanded());
            }
        }

        rebuildMap(getSortedChildren(input));
        if (getContainer() != null && !getContainer().isDisposed()) {
            refreshControls();
            for (Object category : getCategories()) {
                Boolean exp = expansionStates.get(category);
                if (exp != null) {
                    Composite section = getSection(category);
                    if (section instanceof Section) {
                        ((Section) section).setExpanded(exp);
                    }
                }
            }
            relayout();
        }
    }

    private void rebuildMap(Object[] elements) {
        clearMap();
        for (Object element : elements) {
            addElement(element);
        }
        Object[] array = null;
        ViewerFilter[] filters = getFilters();
        if (filters != null && filters.length > 0) {
            array = categories.toArray();
            for (ViewerFilter f : filters) {
                array = f.filter(this, getInput(), array);
            }
        }
        if (getSorter() != null) {
            if (array == null)
                array = categories.toArray();
            getSorter().sort(this, array);
        }
        if (array != null) {
            categories = new ArrayList<Object>(Arrays.asList(array));
        }
    }

    private void clearMap() {
        categories.clear();
        categorizedElements.clear();
    }

    private void addElement(Object element) {
        Object category = getCategory(element);
        if (category == null)
            return;

        List<Object> list = categorizedElements.get(category);
        if (list == null) {
            list = new ArrayList<Object>();
            categorizedElements.put(category, list);
            categories.add(category);
        }
        list.add(element);
    }

    private Object getCategory(Object element) {
        Object category = null;
        if (getContentProvider() instanceof ICategorizedContentProvider) {
            category = ((ICategorizedContentProvider) getContentProvider())
                    .getCategory(element);
        }
        return category;
    }

    protected void refreshControls() {
        getContainer().setRedraw(false);

        for (Composite section : sections) {
            section.dispose();
        }
        sections.clear();

        Composite parent = container.getBody();
        for (Object category : categories) {
            Composite section = createSection(parent, category);
            hookSection(section);
            sections.add(section);
        }

        getContainer().setRedraw(true);
    }

    protected void hookSection(Composite section) {
    }

    private void scroll(Event event) {
        if (container != null && !container.isDisposed()) {
            Point origin = container.getOrigin();
            int count = event.count;
            ScrollBar vBar = container.getVerticalBar();
            if (vBar != null && !vBar.isDisposed() && vBar.isVisible()
                    && vBar.isEnabled()) {
                int increment = vBar.getIncrement();
                int delta = count * increment;
                origin.y -= delta;
            } else {
                ScrollBar hBar = container.getHorizontalBar();
                if (hBar != null && !hBar.isDisposed() && hBar.isVisible()
                        && hBar.isEnabled()) {
                    int increment = hBar.getIncrement();
                    int delta = count * increment;
                    origin.x -= delta;
                }
            }
            container.setOrigin(origin);
        }
    }

    private Composite createSection(Composite parent, Object category) {
        Section section = factory.createSection(parent, getSectionStyle());
        section.setLayoutData(getSectionLayoutData());

        updateSection(category, section);

        Composite client = factory.createComposite(section, SWT.WRAP);
        client.setBackground(parent.getBackground());
        section.setClient(client);

        StackLayout layout = new StackLayout();
        client.setLayout(layout);

        Control content = createSectionContent(client, category,
                getElements(category));
        layout.topControl = content;
        hookSectionContent(content);

        return section;
    }

    protected void hookSectionContent(Control content) {
        content.addListener(SWT.MouseWheel, sectionContentListener);
    }

    private Object getSectionLayoutData() {
        return new RowData();
    }

    protected String getText(Object element) {
        if (getLabelProvider() instanceof ILabelProvider) {
            return ((ILabelProvider) getLabelProvider()).getText(element);
        }
        return element == null ? null : element.toString();
    }

    protected Image getImage(Object element) {
        if (getLabelProvider() instanceof ILabelProvider) {
            return ((ILabelProvider) getLabelProvider()).getImage(element);
        }
        return null;
    }

    protected Color getForeground(Object element) {
        if (getLabelProvider() instanceof IColorProvider) {
            return ((IColorProvider) getLabelProvider()).getForeground(element);
        }
        return null;
    }

    protected Color getBackground(Object element) {
        if (getLabelProvider() instanceof IColorProvider) {
            return ((IColorProvider) getLabelProvider()).getBackground(element);
        }
        return null;
    }

    protected Control createSectionContent(Composite parent, Object category,
            List<Object> elements) {
        Composite content = factory.createComposite(parent, SWT.WRAP);
        content.setBackground(parent.getBackground());
        content.setLayout(new GridLayout(1, true));
        return content;
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        if (getCategories().contains(element)) {
            updateSection(element, getSection(element));
        }
    }

    protected void updateSection(Object category, Composite section) {
        section.setData(category);
        if (section instanceof Section) {
            Section s = (Section) section;
            String text = getText(category);
            if (text != null)
                s.setText(text);

            Color foreground = getForeground(category);
            if (foreground != null)
                s.setTitleBarForeground(foreground);

            Color background = getBackground(category);
            if (background != null)
                s.setTitleBarBackground(background);
        }
    }

    protected void internalRefresh(Object element) {
        if (getCategories().contains(element))
            updateSection(element, getSection(element));
    }

    public void reveal(Object element) {
        Object category = getCategory(element);
        reveal(category, element);
    }

    protected void reveal(Object category, Object element) {
        Composite section = getSection(category);
        if (section != null && section instanceof Section) {
            ((Section) section).setExpanded(true);
            Point loc = section.toDisplay(0, 0);
            reveal(loc.x, loc.y);
        }
    }

    protected void reveal(int x, int y) {
        Point loc = container.toControl(x, y);
        Point origin = container.getOrigin();
        origin.x += loc.x;
        origin.y += loc.y;
        container.setOrigin(origin);
    }

    public Control getControl() {
        return container;
    }

    protected List getSelectionFromWidget() {
        List list = new ArrayList();
        for (Object category : getCategories()) {
            fillSelection(category, list);
        }
        return list;
    }

    protected abstract void fillSelection(Object category, List selection);

    protected void setSelectionToWidget(List l, boolean reveal) {
    }

    protected void setSelectionToWidget(ISelection selection, boolean reveal) {
        for (Object category : getCategories()) {
            setSelectionToCategory(category, selection, reveal);
        }
    }

    protected abstract void setSelectionToCategory(Object category,
            ISelection selection, boolean reveal);

    protected void handleDispose(DisposeEvent event) {
        sections.clear();
        clearMap();
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        super.handleDispose(event);
    }

}