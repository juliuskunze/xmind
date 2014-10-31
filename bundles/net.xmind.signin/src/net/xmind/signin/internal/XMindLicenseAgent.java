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
package net.xmind.signin.internal;

import java.util.ArrayList;
import java.util.List;

import net.xmind.signin.ILicenseInfo;
import net.xmind.signin.ILicenseListener;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

public class XMindLicenseAgent {

    private ILicenseInfo info = new LicenseInfo();

    private List<ILicenseListener> listeners = new ArrayList<ILicenseListener>(
            3);

    protected XMindLicenseAgent() {
    }

    public ILicenseInfo getLicenseInfo() {
        return info;
    }

    public void addLicenseListener(ILicenseListener listener) {
        listeners.add(listener);
    }

    public void removeLicenseListener(ILicenseListener listener) {
        listeners.remove(listener);
    }

    public void licenseVerified(ILicenseInfo info) {
        if (info == null)
            return;
        this.info = info;
        fireLicenseVerified(info);

        int type = info.getType();
        String licenseType;
        if ((type & ILicenseInfo.VALID_PRO_LICENSE_KEY) != 0) {
            licenseType = "Pro"; //$NON-NLS-1$
        } else if ((type & ILicenseInfo.VALID_PLUS_LICENSE_KEY) != 0) {
            licenseType = "Plus"; //$NON-NLS-1$
        } else if ((type & ILicenseInfo.VALID_PRO_SUBSCRIPTION) != 0) {
            licenseType = "Sub"; //$NON-NLS-1$
        } else {
            licenseType = "Free"; //$NON-NLS-1$
        }
        IEvaluationService evaluationService = (IEvaluationService) PlatformUI
                .getWorkbench().getService(IEvaluationService.class);
        if (evaluationService == null)
            return;

        System.setProperty("org.xmind.product.license_type", licenseType); //$NON-NLS-1$
        evaluationService.requestEvaluation("org.xmind.product.license_type"); //$NON-NLS-1$

    }

    private void fireLicenseVerified(final ILicenseInfo info) {
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((ILicenseListener) listener).licenseVerified(info);
                }
            });
        }
    }
}
