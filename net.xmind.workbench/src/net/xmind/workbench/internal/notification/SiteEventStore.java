/**
 * 
 */
package net.xmind.workbench.internal.notification;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xmind.workbench.internal.XMindNetWorkbench;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class SiteEventStore {

    private static final String TAG_ROOT = "site-events"; //$NON-NLS-1$

    private static final String TAG_EVENTS = "events"; //$NON-NLS-1$

    private static final String TAG_EVENT = "event"; //$NON-NLS-1$

    private XMLMemento writeRoot;

    private IMemento events;

    private List<ISiteEvent> eventList = new ArrayList<ISiteEvent>();

    private Map<String, ISiteEvent> eventMap = new HashMap<String, ISiteEvent>();

    public SiteEventStore(Reader reader) {
        try {
            this.writeRoot = XMLMemento.createReadRoot(reader);
        } catch (WorkbenchException e) {
            XMindNetWorkbench.log(e, "Failed to read site-events.xml."); //$NON-NLS-1$
            this.writeRoot = XMLMemento.createWriteRoot(TAG_ROOT);
        }
        this.events = writeRoot.getChild(TAG_EVENTS);
        if (this.events == null) {
            this.events = writeRoot.createChild(TAG_EVENTS);
        }
        for (IMemento m : this.events.getChildren(TAG_EVENT)) {
            ISiteEvent event = new XMLSiteEvent(m);
            eventList.add(event);
            eventMap.put(event.getId(), event);
        }
    }

    public SiteEventStore(List<ISiteEvent> events) {
        this.writeRoot = XMLMemento.createWriteRoot(TAG_ROOT);
        this.events = writeRoot.createChild(TAG_EVENTS);
        for (ISiteEvent event : events) {
            copyEvent(event);
        }
    }

    @SuppressWarnings("unchecked")
    public SiteEventStore() {
        this(Collections.EMPTY_LIST);
    }

    public List<ISiteEvent> getEvents() {
        return eventList;
    }

    public ISiteEvent findEvent(String id) {
        return eventMap.get(id);
    }

    public List<ISiteEvent> calcNewEvents(List<ISiteEvent> updates,
            boolean startup) {
        List<ISiteEvent> newEvents = new ArrayList<ISiteEvent>();
        for (ISiteEvent event : updates) {
            if (shouldPrompt(event, startup)) {
                newEvents.add(event);
            }
        }
        return newEvents;
    }

    private boolean shouldPrompt(ISiteEvent event, boolean startup) {
        String prompt = event.getPrompt();
        if ("always".equals(prompt)) //$NON-NLS-1$
            return true;
        if ("startup".equals(prompt)) //$NON-NLS-1$
            return startup;
        if (findEvent(event.getId()) == null)
            return !"never".equals(prompt); //$NON-NLS-1$
        return false;
    }

    public XMLSiteEvent copyEvent(ISiteEvent event) {
        XMLSiteEvent e = createEvent(event.getId());
        e.setTitle(event.getTitle());
        e.setPrompt(event.getPrompt());
        e.setEventUrl(event.getEventUrl());
        e.setMoreUrl(event.getMoreUrl());
        e.setOpenExternal(event.isOpenExternal());
        e.setActionText(event.getActionText());
        return e;
    }

    public XMLSiteEvent createEvent(String id) {
        IMemento child = events.createChild(TAG_EVENT);
        XMLSiteEvent event = new XMLSiteEvent(child);
        event.setId(id);
        eventList.add(event);
        eventMap.put(id, event);
        return event;
    }

    public void save(Writer writer) throws IOException {
        writeRoot.save(writer);
    }

}