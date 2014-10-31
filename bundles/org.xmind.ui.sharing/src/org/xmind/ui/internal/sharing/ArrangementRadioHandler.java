package org.xmind.ui.internal.sharing;

import static org.xmind.core.sharing.SharingConstants.ARRANGE_MODE_NAME;
import static org.xmind.core.sharing.SharingConstants.ARRANGE_MODE_PEOPLE;
import static org.xmind.core.sharing.SharingConstants.ARRANGE_MODE_TIME;
import static org.xmind.core.sharing.SharingConstants.PREF_ARRANGE_MODE;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;

/**
 * @author Jason Wong
 */
public class ArrangementRadioHandler extends AbstractHandler {

    private ExecutionEvent event;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        this.event = event;
        String newState = event.getParameter(RadioState.PARAMETER_ID);

        if (newState == null) {
            switchedNext();
            return null;
        }

        if (!HandlerUtil.matchesRadioState(event))
            switched(newState);

        return null;
    }

    private void switchedNext() {
        String oldState = LocalNetworkSharingUI.getDefault()
                .getPreferenceStore().getString(PREF_ARRANGE_MODE);

        if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(oldState)
                || ARRANGE_MODE_PEOPLE.equals(oldState)) {
            switched(ARRANGE_MODE_NAME);
        } else if (ARRANGE_MODE_NAME.equals(oldState)) {
            switched(ARRANGE_MODE_TIME);
        } else {
            switched(ARRANGE_MODE_PEOPLE);
        }
    }

    private void switched(String newState) {
        if (!isArrangeMode(newState))
            switchedNext();

        updateState(newState);
        refresh();
    }

    private void updateState(String newState) {
        try {
            HandlerUtil.updateRadioState(event.getCommand(), newState);
        } catch (ExecutionException e) {
        }

        LocalNetworkSharingUI.getDefault().getPreferenceStore()
                .putValue(PREF_ARRANGE_MODE, newState);
    }

    private void refresh() {
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (part == null)
            return;

        SharedLibrariesViewer viewer = (SharedLibrariesViewer) part
                .getAdapter(SharedLibrariesViewer.class);
        if (viewer == null)
            return;

        viewer.refresh();
    }

    private boolean isArrangeMode(String arrangeMode) {
        if (ARRANGE_MODE_PEOPLE.equals(arrangeMode)
                || ARRANGE_MODE_NAME.equals(arrangeMode)
                || ARRANGE_MODE_TIME.equals(arrangeMode))
            return true;
        return false;
    }

}
