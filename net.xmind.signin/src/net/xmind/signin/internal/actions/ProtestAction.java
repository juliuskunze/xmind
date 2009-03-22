/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package net.xmind.signin.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ISiteFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.operations.UninstallOperation;

/**
 * @author Karelun Huang
 * 
 */
@SuppressWarnings("restriction")
public class ProtestAction extends XMindNetActionDelegate implements
        IWorkbenchWindowActionDelegate {
    private static final String JOB_TITLE = "Disable the Meggy feaure";

    public ProtestAction() {
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {

    }

    public void run(IAction action) {
        // TODO Auto-generated method stub
        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
//                workbench.close();
                try {
                    disableSomefeatures();
                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void disableSomefeatures() throws CoreException {
        final List<ISiteFeatureReference> features = new ArrayList<ISiteFeatureReference>();
        getDisabledFeature(features);
        for (ISiteFeatureReference feature : features) {
            System.out.println(feature.toString());
        }
        Job job = new Job(JOB_TITLE) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (features.isEmpty())
                    return Status.CANCEL_STATUS;
                for (ISiteFeatureReference feature : features) {
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    try {
                        Thread.sleep(1000);
                        UninstallOperation disableOperation = new UninstallOperation(
                                null, feature.getFeature(null));
                        disableOperation.execute(null, null);
                        monitor.worked(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (CoreException e1) {
                        e1.printStackTrace();
                    }
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        PlatformUI.getWorkbench().getProgressService().showInDialog(
                Display.getCurrent().getActiveShell(), job);
    }

    private void getDisabledFeature(List<ISiteFeatureReference> features)
            throws CoreException {

        IConfiguredSite[] sites = SiteManager.getLocalSite()
                .getCurrentConfiguration().getConfiguredSites();
        for (IConfiguredSite csite : sites) {
//            HashMap<String, ArrayList<ISiteFeatureReference>> dublicate_features = new HashMap<String, ArrayList<ISiteFeatureReference>>();
//        IConfiguredSite csite = sites[1];
            ISite site = csite.getSite();
            ISiteFeatureReference[] frs = site.getFeatureReferences();

            for (ISiteFeatureReference f : frs) {
                VersionedIdentifier ia = f.getVersionedIdentifier();
                String identifier = ia.getIdentifier();
                if ("org.xmind.meggy.feature".equals(identifier)) {
                    if (features == null)
                        features = new ArrayList<ISiteFeatureReference>();
                    features.add(f);
                }
//                System.out.println(f.toString());
//                System.out.println(identifier);
//
//                ArrayList<ISiteFeatureReference> fsl = dublicate_features
//                        .get(identifier);
//                if (fsl == null) {
//                    fsl = new ArrayList<ISiteFeatureReference>();
//                    dublicate_features.put(identifier, fsl);
//                }
//                fsl.add(f);
            }

            //////////////

//            for (String s : dublicate_features.keySet()) {
//                ArrayList<ISiteFeatureReference> pls = dublicate_features
//                        .get(s);
//                if (pls.size() > 1) {
//                    ISiteFeatureReference max = null;
//                    for (ISiteFeatureReference e : pls) {
//                        PluginVersionIdentifier pa = e.getVersionedIdentifier()
//                                .getVersion();
//                        if (max == null)
//                            max = e;
//                        else {
//                            if (pa.isGreaterThan(max.getVersionedIdentifier()
//                                    .getVersion()))
//                                max = e;
//                        }
//                    }
//                    for (ISiteFeatureReference e : pls) {
//                        if (e != max)
//                            return e;
////                            toUnistall.add(e);
//                    }
//                }
        }
    }

//        System.out.println(features.size());
//        for (ISiteFeatureReference feature : features) {
//            
//        }
//    }

    public void selectionChanged(IAction action, ISelection selection) {
    }
}
