package org.xmind.ui.internal.protocols;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.xmind.ui.mindmap.IProtocol;
import org.xmind.ui.mindmap.IProtocolDescriptor;

public class ProtocolDescriptor implements IProtocolDescriptor {

    private IConfigurationElement element;

    private String id;

    public ProtocolDescriptor(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

        if (element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS) == null) {
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(), 0,
                    "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                    null));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.protocols.IProtocolDescriptor#getId()
     */
    public String getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.protocols.IProtocolDescriptor#getName()
     */
    public String getName() {
        return element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
    }

    public IProtocol createProtocol() throws CoreException {
        return (IProtocol) element
                .createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.protocols.IProtocolDescriptor#getProtocolNames()
     */
    public String getProtocolNames() {
        return element.getAttribute("protocolNames"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.protocols.IProtocolDescriptor#hasProtocolName(java
     * .lang.String)
     */
    public boolean hasProtocolName(String name) {
        String names = getProtocolNames();
        return names != null && names.contains(name);
    }

}
