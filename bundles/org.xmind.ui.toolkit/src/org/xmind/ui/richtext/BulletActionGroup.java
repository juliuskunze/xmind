package org.xmind.ui.richtext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolItem;

public class BulletActionGroup extends ContributionItem {

    private List<IAction> actions = new ArrayList<IAction>();

    private IAction currentAction = null;

    private IPropertyChangeListener actionListener = null;

    private ToolItem widget = null;

    private IPropertyChangeListener currentActionListener = null;

    private LocalResourceManager imageManager;

    public BulletActionGroup() {

    }

    public void add(IAction action) {
        actions.add(action);
        action.addPropertyChangeListener(getActionListener());
        if (currentAction == null)
            setCurrentAction(action);
    }

    public void remove(IAction action) {
        action.removePropertyChangeListener(getActionListener());
        actions.remove(action);
        if (currentAction == action) {
            setCurrentAction(actions.isEmpty() ? null : actions.get(0));
        }
    }

    public IAction getCurrentAction() {
        return currentAction;
    }

    private void setCurrentAction(IAction action) {
        if (currentAction != null && currentActionListener != null) {
            currentAction.removePropertyChangeListener(currentActionListener);
        }
        this.currentAction = action;
        if (action != null)
            action.setChecked(true);
        for (IAction a : actions) {
            if (a != action)
                a.setChecked(false);
        }
        if (action != null) {
            if (widget != null && !widget.isDisposed())
                action.addPropertyChangeListener(getCurrentActionListener());
        }
        update(null);
    }

    private IPropertyChangeListener getActionListener() {
        if (actionListener == null) {
            actionListener = new IPropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    actionPropertyChange(event);
                }

            };
        }
        return actionListener;
    }

    private void actionPropertyChange(PropertyChangeEvent event) {
        IAction trigerAction = (IAction) event.getSource();
        String property = event.getProperty();
        if (IAction.CHECKED.equals(property)
                && Boolean.TRUE.equals(event.getNewValue())) {
            setCurrentAction(trigerAction);
        }
    }

    private IPropertyChangeListener getCurrentActionListener() {
        if (currentActionListener == null) {
            currentActionListener = new IPropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    currentActionPropertyChange(event);
                }

            };
        }
        return currentActionListener;
    }

    private void currentActionPropertyChange(final PropertyChangeEvent event) {
        if (isVisible() && widget != null) {
            Display display = widget.getDisplay();
            if (display.getThread() == Thread.currentThread()) {
                update(event.getProperty());
            } else {
                display.asyncExec(new Runnable() {
                    public void run() {
                        update(event.getProperty());
                    }

                });
            }
        }
    }

    public void update() {
        update(null);
    }

    public void update(String propertyName) {
        if (widget == null || currentAction == null)
            return;

        boolean textChanged = propertyName == null
                || propertyName.equals(IAction.TEXT);
        boolean imageChanged = propertyName == null
                || propertyName.equals(IAction.IMAGE);
        boolean tooltipTextChanged = propertyName == null
                || propertyName.equals(IAction.TOOL_TIP_TEXT);
        boolean enableStateChanged = propertyName == null
                || propertyName.equals(IAction.ENABLED)
                || propertyName.equals(IContributionManagerOverrides.P_ENABLED);
        boolean checkChanged = (currentAction.getStyle() == IAction.AS_CHECK_BOX || currentAction
                .getStyle() == IAction.AS_RADIO_BUTTON)
                && (propertyName == null || propertyName
                        .equals(IAction.CHECKED));
        ToolItem ti = (ToolItem) widget;
        String text = currentAction.getText();
// the set text is shown only if there is no image or if forced
// by MODE_FORCE_TEXT
        boolean showText = text != null && !hasImages(currentAction);

// only do the trimming if the text will be used
        if (showText && text != null) {
            text = Action.removeAcceleratorText(text);
            text = Action.removeMnemonics(text);
        }

        if (textChanged) {
            String textToSet = showText ? text : ""; //$NON-NLS-1$
            boolean rightStyle = (ti.getParent().getStyle() & SWT.RIGHT) != 0;
            if (rightStyle || !ti.getText().equals(textToSet)) {
                // In addition to being required to update the text if
                // it
                // gets nulled out in the action, this is also a
                // workaround
                // for bug 50151: Using SWT.RIGHT on a ToolBar leaves
                // blank space
                ti.setText(textToSet);
            }
        }

        if (imageChanged) {
            // only substitute a missing image if it has no text
            updateImages(!showText);
        }

        if (tooltipTextChanged || textChanged) {
            String toolTip = currentAction.getToolTipText();
            if ((toolTip == null) || (toolTip.length() == 0)) {
                toolTip = text;
            }

            ExternalActionManager.ICallback callback = ExternalActionManager
                    .getInstance().getCallback();
            String commandId = currentAction.getActionDefinitionId();
            if ((callback != null) && (commandId != null) && (toolTip != null)) {
                String acceleratorText = callback.getAcceleratorText(commandId);
                if (acceleratorText != null && acceleratorText.length() != 0) {
                    toolTip = JFaceResources.format(
                            "Toolbar_Tooltip_Accelerator", //$NON-NLS-1$
                            new Object[] { toolTip, acceleratorText });
                }
            }

            // if the text is showing, then only set the tooltip if
            // different
            if (!showText || toolTip != null && !toolTip.equals(text)) {
                ti.setToolTipText(toolTip);
            } else {
                ti.setToolTipText(null);
            }
        }

        if (enableStateChanged) {
            boolean shouldBeEnabled = currentAction.isEnabled()
                    && isEnabledAllowed();

            if (ti.getEnabled() != shouldBeEnabled) {
                ti.setEnabled(shouldBeEnabled);
            }
        }

        if (checkChanged) {
            boolean bv = currentAction.isChecked();

            if (ti.getSelection() != bv) {
                ti.setSelection(bv);
            }
        }

    }

    private boolean hasImages(IAction actionToCheck) {
        return actionToCheck.getImageDescriptor() != null
                || actionToCheck.getHoverImageDescriptor() != null
                || actionToCheck.getDisabledImageDescriptor() != null;
    }

    private boolean updateImages(boolean forceImage) {
        ResourceManager parentResourceManager = JFaceResources.getResources();

        ImageDescriptor image = currentAction.getHoverImageDescriptor();
        if (image == null) {
            image = currentAction.getImageDescriptor();
        }
        ImageDescriptor disabledImage = currentAction
                .getDisabledImageDescriptor();

        // Make sure there is a valid image.
        if (image == null && forceImage) {
            image = ImageDescriptor.getMissingImageDescriptor();
        }

        LocalResourceManager localManager = new LocalResourceManager(
                parentResourceManager);

        // performance: more efficient in SWT to set disabled and hot
        // image before regular image
        widget.setDisabledImage(disabledImage == null ? null : localManager
                .createImageWithDefault(disabledImage));
        widget.setImage(image == null ? null : localManager
                .createImageWithDefault(image));

        disposeOldImages();
        imageManager = localManager;

        return image != null;

    }

    private void disposeOldImages() {
        if (imageManager != null) {
            imageManager.dispose();
            imageManager = null;
        }
    }

    private boolean isEnabledAllowed() {
        if (getParent() == null) {
            return true;
        }
        Boolean value = getParent().getOverrides().getEnabled(this);
        return (value == null) ? true : value.booleanValue();
    }

}
