package org.xmind.ui.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.IPage;

public interface IHyperlinkPage extends IPage, IMessageProvider {

    void init(IEditorPart editor, IStructuredSelection selection);

    void setContainer(IHyperlinkPageContainer container);

    IHyperlinkPageContainer getContainer();

    void setValue(String value);

    String getValue();

    boolean canFinish();

    String getErrorMessage();

    boolean tryFinish();

}
