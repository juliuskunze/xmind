package org.xmind.ui.commands;

import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.gef.GEF;
import org.xmind.gef.command.SourceCommand;

public class DeleteRevisionCommand extends SourceCommand {

    private IRevisionManager manager;

    private Object removal = null;

    public DeleteRevisionCommand(IRevision revision) {
        super(revision);
        this.manager = revision.getOwnedManager();
    }

    public int getType() {
        return GEF.CMD_DELETE;
    }

    public void redo() {
        removal = manager.removeRevision((IRevision) getSource());
        super.redo();
    }

    public void undo() {
        manager.restoreRevision((IRevision) getSource(), removal);
        super.undo();
    }

}
