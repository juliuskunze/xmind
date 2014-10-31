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
package org.xmind.core.sharing;

import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 * @author Frank Shaka
 * 
 */
public interface ISharingService extends IAdaptable, ISchedulingRule {

    /**
     * State for 'service is inactive' (value is <code>0</code>).
     */
    int INACTIVE = 0;

    /**
     * State for 'service is being activated' (value is <code>1</code>).
     */
    int ACTIVATING = 1;

    /**
     * State for 'service is active' (value is <code>2</code>).
     */
    int ACTIVE = 2;

    /**
     * State for 'service is being deactivated' (value is <code>3</code>).
     */
    int DEACTIVATING = 3;

    /**
     * Activate this service.
     * 
     * @param monitor
     * @return {@link org.eclipse.core.runtime.Status#CANCEL_STATUS} if this
     *         service is not in {@link #INACTIVE} state.
     */
    IStatus activate(IProgressMonitor monitor);

    /**
     * Deactivate this service.
     * 
     * @param monitor
     * @return {@link org.eclipse.core.runtime.Status#CANCEL_STATUS} if this
     *         service is not in {@link #ACTIVE} state
     */
    IStatus deactivate(IProgressMonitor monitor);

    /**
     * Returns the current state of this service.
     * 
     * @return one of {@link #INACTIVE}, {@link #ACTIVATING}, {@link #ACTIVE},
     *         {@link #DEACTIVATING}
     */
    int getStatus();

    /**
     * Refreshes this service.
     * 
     * @param monitor
     * @return {@link org.eclipse.core.runtime.Status#CANCEL_STATUS} if this
     *         service is not in {@link #ACTIVE} state
     */
    IStatus refresh(IProgressMonitor monitor);

    /**
     * Register a background job with this service. Registered jobs will be
     * canceled when this service is being deactivated. This method should be
     * called before the job is scheduled.
     * 
     * @param job
     *            the job to register
     */
    void registerJob(Job job);

    ILocalSharedLibrary getLocalLibrary();

    IContactManager getContactManager();

    Collection<IRemoteSharedLibrary> getRemoteLibraries();

    IRemoteSharedLibrary findRemoteLibrary(String symbolicName);

    IRemoteSharedLibrary findRemoteLibraryByID(String contactID);

    void addSharingListener(ISharingListener listener);

    void removeSharingListener(ISharingListener listener);

}
