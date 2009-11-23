package org.xmind.ui.dialogs;

import org.eclipse.ui.IActionBars;

public abstract class HyperlinkPage implements IHyperlinkPage {

    private IHyperlinkPageContainer container;

    private String value;

    private boolean canFinish;

    private String message;

    private String errorMessage;

    private int messageType = NONE;

    public void setContainer(IHyperlinkPageContainer container) {
        this.container = container;
    }

    public IHyperlinkPageContainer getContainer() {
        return container;
    }

    public boolean tryFinish() {
        return true;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean canFinish() {
        return canFinish;
    }

    protected void setCanFinish(boolean canFinish) {
        this.canFinish = canFinish;
        updateButtons();
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getMessageType() {
        return messageType;
    }

    protected void setMessage(String message) {
        this.message = message;
        updateMessage();
    }

    protected void setMessage(String message, int messageType) {
        this.message = message;
        this.messageType = messageType;
        updateMessage();
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        updateMessage();
    }

    protected void updateMessage() {
        if (getContainer() != null) {
            getContainer().updateMessage();
        }
    }

    protected void updateButtons() {
        if (getContainer() != null) {
            getContainer().updateButtons();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
     */
    public void setActionBars(IActionBars actionBars) {
    }

}
