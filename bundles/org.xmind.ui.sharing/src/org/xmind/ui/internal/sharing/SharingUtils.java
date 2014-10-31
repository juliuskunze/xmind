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
package org.xmind.ui.internal.sharing;

import static org.xmind.core.sharing.SharingConstants.COMMAND_SOURCE;
import static org.xmind.core.sharing.SharingConstants.PROP_CONTENT;
import static org.xmind.core.sharing.SharingConstants.PROP_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_MAPS;
import static org.xmind.core.sharing.SharingConstants.PROP_NAME;
import static org.xmind.core.sharing.SharingConstants.PROP_REMOTE;
import static org.xmind.core.sharing.SharingConstants.PROP_REMOTE_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_VERIFICATION_CODE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.command.Command;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.arguments.ArrayMapper;
import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.command.remote.ICommandServerAdvertiser;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceInfo;
import org.xmind.core.command.remote.IIdentifier;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.command.remote.RemoteCommandJob;
import org.xmind.core.internal.sharing.LocalSharedMap;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.sharing.ILocalSharedLibrary;
import org.xmind.core.sharing.ILocalSharedMap;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.dialogs.IDialogConstants;
import org.xmind.ui.internal.editor.FileEditorInput;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharingUtils {

    private static final String SETTINGS_SHARE_LOCAL_FILES_DIALOG = "org.xmind.ui.sharing.ShareLocalFilesDialog"; //$NON-NLS-1$

    private static final String SETTING_FILTER_PATH = "filterPath"; //$NON-NLS-1$

    private static String COMPUTER_NAME = null;

    private SharingUtils() {
        throw new AssertionError();
    }

    public static List<ISharedMap> getSharedMapsFrom(
            IStructuredSelection selection) {
        return getSharedMapsFrom(selection, null);
    }

    public static List<ISharedMap> getSharedMapsFrom(
            IStructuredSelection selection, IFilter filter) {
        List<ISharedMap> list = new ArrayList<ISharedMap>(selection.size());
        for (Object element : selection.toList()) {
            if (element instanceof ISharedMap
                    && (filter == null || filter.select(element))) {
                list.add((ISharedMap) element);
            }
        }
        return list;

    }

    public static void openSharedMaps(final IWorkbenchPage page,
            final Object... maps) {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
                for (int i = 0; i < maps.length; i++) {
                    Object map = maps[i];
                    if (map instanceof ISharedMap) {
                        openSharedMap(page, (ISharedMap) map);
                    }
                }
            }
        });
    }

    public static void openSharedMaps(final IWorkbenchPage page,
            final Collection<? extends ISharedMap> maps) {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
                for (ISharedMap map : maps) {
                    openSharedMap(page, map);
                }
            }
        });
    }

    private static void openSharedMap(final IWorkbenchPage page,
            final ISharedMap map) {
        if (map.getSharedLibrary().isLocal()) {
            openLocalMap(page, map);
        } else {
            final Display display = Display.getCurrent();
            Job openRemoteMapJob = new Job(
                    NLS.bind(
                            SharingMessages.OpenRemoteSharedMapJob_jobName_withSharedMapName,
                            map.getResourceName())) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    monitor.beginTask(null, 100);

                    monitor.subTask(SharingMessages.OpenRemoteSharedMapJob_LoadRemoteMapContentTask_name);
                    SubProgressMonitor monitor1 = new SubProgressMonitor(
                            monitor, 90,
                            SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
                    InputStream stream = map.getResourceAsStream(monitor1);
                    if (monitor1.isCanceled())
                        return Status.CANCEL_STATUS;
                    monitor1.done();

                    if (stream == null) {
                        display.asyncExec(new Runnable() {
                            public void run() {
                                MessageDialog
                                        .openInformation(
                                                page.getWorkbenchWindow()
                                                        .getShell(),
                                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                                NLS.bind(
                                                        SharingMessages.OpenRemoteSharedMapJob_SharedMapNotAvailable_errorMessage_withSharedMapName,
                                                        map.getResourceName()));
                            }
                        });
                        return new Status(IStatus.WARNING,
                                LocalNetworkSharingUI.PLUGIN_ID,
                                "Failed to load shared map content from remote client."); //$NON-NLS-1$
                    }

                    monitor.subTask(SharingMessages.OpenRemoteSharedMapJob_OpenWithMindMapEditorTask_name);
                    final IWorkbook workbook;
                    try {
                        File tempPath = Core.getWorkspace().createTempFile(
                                "remoteMaps", //$NON-NLS-1$
                                "", ".temp"); //$NON-NLS-1$ //$NON-NLS-2$
                        tempPath.mkdirs();
                        DirectoryStorage storage = new DirectoryStorage(
                                tempPath);
                        try {
                            workbook = Core.getWorkbookBuilder()
                                    .loadFromStream(stream, storage);
                        } catch (Throwable e) {
                            final String errorMessage = e.getLocalizedMessage();
                            display.asyncExec(new Runnable() {
                                public void run() {
                                    MessageDialog
                                            .openInformation(
                                                    page.getWorkbenchWindow()
                                                            .getShell(),
                                                    SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                                    NLS.bind(
                                                            SharingMessages.OpenRemoteSharedMapJob_ErrorOccurredWhileOpeningSharedMap_errorMessage_withSharedMapName_and_ErrorDetails,
                                                            map.getResourceName(),
                                                            errorMessage));
                                }
                            });
                            return new Status(IStatus.WARNING,
                                    LocalNetworkSharingUI.PLUGIN_ID, null, e);
                        }
                    } finally {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;

                    display.asyncExec(new Runnable() {
                        public void run() {
                            SafeRunner.run(new SafeRunnable() {
                                public void run() throws Exception {
                                    IEditorInput editorInput = new SharedWorkbookEditorInput(
                                            map, map.getResourceName(),
                                            workbook);
                                    page.openEditor(editorInput,
                                            MindMapUI.MINDMAP_EDITOR_ID);
                                }
                            });
                        }
                    });
                    return Status.OK_STATUS;
                }
            };
            openRemoteMapJob.setUser(true);
            ISharingService sharingService = LocalNetworkSharingUI.getDefault()
                    .getSharingService();
            sharingService.registerJob(openRemoteMapJob);
            openRemoteMapJob.setRule(sharingService);
            openRemoteMapJob.schedule();
        }
    }

    private static void openLocalMap(IWorkbenchPage page, ISharedMap map) {
        String path = ((LocalSharedMap) map).getResourcePath();

        if (map.isMissing()) {
            showDialog(DIALOG_INFO,
                    SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                    SharingMessages.ShareLibraryMapHandler_mapMissingTipText);
            return;
        }

        IEditorInput editorInput = new FileEditorInput(new File(path));
        try {
            page.openEditor(editorInput, MindMapUI.MINDMAP_EDITOR_ID);
        } catch (Throwable e) {
            LocalNetworkSharingUI.log("Failed to open local shared map: " //$NON-NLS-1$
                    + map.getResourceName(), e);
        }
    }

    public static void sendMessage(ISharedLibrary[] remoteLibraries,
            final String message, final String[] mapIDs) {
        ISharingService service = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (service == null || remoteLibraries == null)
            return;

        ICommandServiceDomain domain = (ICommandServiceDomain) service
                .getAdapter(ICommandServiceDomain.class);
        if (domain == null) {
            LocalNetworkSharingUI
                    .log("LocalNetworkSharingUI.sendMessage: No command service domain available.", null); //$NON-NLS-1$
            return;
        }

        ICommandServerAdvertiser advertiser = domain
                .getCommandServerAdvertiser();
        ICommandServiceInfo localInfo = advertiser.getRegisteredInfo();
        if (localInfo == null) {
            LocalNetworkSharingUI
                    .log("LocalNetworkSharingUI.sendMessage: Local command server not registered (no registered info).", null); //$NON-NLS-1$
            return;
        }

        IIdentifier localId = localInfo.getId();
        if (localId == null) {
            LocalNetworkSharingUI
                    .log("LocalNetworkSharingUI.sendMessage: Local command server not registered (no registered name).", null); //$NON-NLS-1$
            return;
        }

        final String localName = localId.getName();

        for (int i = 0; i < remoteLibraries.length; i++) {
            IRemoteCommandService remoteService = (IRemoteCommandService) remoteLibraries[i]
                    .getAdapter(IRemoteCommandService.class);
            if (remoteService != null) {
                RemoteCommandJob job = new RemoteCommandJob(
                        SharingMessages.SendMessageToRemoteUserJob_jobName,
                        LocalNetworkSharingUI.PLUGIN_ID, remoteService) {

                    public IStatus consumeReturnValue(IProgressMonitor monitor,
                            IStatus returnValue) {
                        return Status.OK_STATUS;
                    }

                    @Override
                    protected ICommand createCommand(IProgressMonitor monitor)
                            throws CoreException {
                        Attributes data = new Attributes();
                        data.with(PROP_REMOTE, localName);
                        data.with(PROP_CONTENT, message);
                        if (mapIDs != null && mapIDs.length > 0) {
                            ArrayMapper mapsWriter = new ArrayMapper(
                                    data.getRawMap(), PROP_MAPS);
                            for (int i = 0; i < mapIDs.length; i++) {
                                mapsWriter.next();
                                mapsWriter.set(PROP_ID, mapIDs[i]);
                            }
                            mapsWriter.setSize();
                        }
                        return new Command(COMMAND_SOURCE,
                                "sharing/message", data, null, null); //$NON-NLS-1$
                    }

                };
                job.setRule(service);
                service.registerJob(job);
                job.schedule();
            }
        }
    }

    public static boolean connectRemoteLibrary(
            final ISharedLibrary remoteLibrary) {
        ISharingService service = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (service == null || remoteLibrary == null)
            return false;

        if (!PlatformUI.isWorkbenchRunning())
            return false;

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return false;

        Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return false;

        final boolean[] answer = new boolean[] { false };
        display.syncExec(new Runnable() {
            public void run() {
                ConnectionDialog dialog = new ConnectionDialog(
                        getDialogParent(workbench),
                        NLS.bind(
                                SharingMessages.SharingUtils_connectRemoteLibraryTipText_withRemoteName,
                                remoteLibrary.getName()));
                int code = dialog.open();
                if (code == Dialog.OK) {
                    answer[0] = true;
                }
            }
        });

        if (!answer[0])
            return false;

        final String id = service.getLocalLibrary().getContactID();
        final String verificationCode = service.getContactManager()
                .getVerificationCode();
        IRemoteCommandService remoteService = (IRemoteCommandService) remoteLibrary
                .getAdapter(IRemoteCommandService.class);
        if (remoteService == null)
            return false;

        RemoteCommandJob job = new RemoteCommandJob(
                SharingMessages.ConnectRemoteLibraryJob_name,
                LocalNetworkSharingUI.PLUGIN_ID, remoteService) {

            public IStatus consumeReturnValue(IProgressMonitor monitor,
                    IStatus returnValue) {
                return Status.OK_STATUS;
            }

            @Override
            protected ICommand createCommand(IProgressMonitor monitor)
                    throws CoreException {
                Attributes data = new Attributes();
                data.with(PROP_REMOTE_ID, id);
                data.with(PROP_VERIFICATION_CODE, verificationCode);
                return new Command(COMMAND_SOURCE,
                        "sharing/connection", data, null, null); //$NON-NLS-1$
            }
        };
        job.setRule(service);
        service.registerJob(job);
        job.schedule();
        return true;
    }

    public static void returnConnectionInfo(final ISharedLibrary remoteLibrary,
            final String verificationCode) {
        final ISharingService service = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (service == null || remoteLibrary == null)
            return;

        IRemoteCommandService remoteService = (IRemoteCommandService) remoteLibrary
                .getAdapter(IRemoteCommandService.class);
        if (remoteService != null) {
            RemoteCommandJob job = new RemoteCommandJob(
                    SharingMessages.ReturnConnectionInfoJob_name,
                    LocalNetworkSharingUI.PLUGIN_ID, remoteService) {
                public IStatus consumeReturnValue(IProgressMonitor monitor,
                        IStatus returnValue) {
                    return Status.OK_STATUS;
                }

                @Override
                protected ICommand createCommand(IProgressMonitor monitor)
                        throws CoreException {
                    ILocalSharedLibrary localLibrary = service
                            .getLocalLibrary();

                    Attributes data = new Attributes();
                    data.with(PROP_REMOTE_ID, localLibrary.getContactID());
                    data.with(PROP_NAME, localLibrary.getName());
                    data.with(PROP_VERIFICATION_CODE, verificationCode);
                    return new Command(COMMAND_SOURCE,
                            "sharing/connectionResult", data, null, null); //$NON-NLS-1$
                }
            };
            job.setRule(service);
            service.registerJob(job);
            job.schedule();
        }
    }

//    public static Job toggleServiceStatus(final ISharingService sharingService,
//            boolean showInDialog) {
//        int status = sharingService.getConnectionStatus();
//        if (status == ISharingService.CONNECTED) {
//            return disconnectSharingService(sharingService, showInDialog);
//        } else if (status == ISharingService.DISCONNECTED) {
//            return connectSharingService(sharingService, showInDialog);
//        } else {
//            return null;
//        }
//    }
//
//    public static Job connectSharingService(
//            final ISharingService sharingService, boolean showInDialog) {
//        Job job = new Job("Enable Local Network Sharing") {
//            protected IStatus run(IProgressMonitor monitor) {
//                IStatus status = sharingService.connect(monitor);
//                if (status.getSeverity() == IStatus.ERROR) {
//                    LocalNetworkSharingUI
//                            .getDefault()
//                            .getPreferenceStore()
//                            .setValue(
//                                    LocalNetworkSharingUI.PREF_SKIP_LAUNCHING_ON_STARTUP,
//                                    true);
//                    return new LocalNetworkSharingUnavailabilityHandler()
//                            .handleLocalNetworkSharingUnavailability(status);
//                } else if (status.isOK()) {
//                    LocalNetworkSharingUI
//                            .getDefault()
//                            .getPreferenceStore()
//                            .setValue(
//                                    LocalNetworkSharingUI.PREF_SKIP_LAUNCHING_ON_STARTUP,
//                                    false);
//                }
//                return status;
//            };
//        };
//        sharingService.registerJob(job);
//        job.setUser(showInDialog);
//        job.schedule();
//        return job;
//    }
//
//    public static Job disconnectSharingService(
//            final ISharingService sharingService, boolean showInDialog) {
//        Job job = new Job("Disable Local Network Sharing") {
//            protected IStatus run(IProgressMonitor monitor) {
//                IStatus status = sharingService.disconnect(monitor);
//                LocalNetworkSharingUI
//                        .getDefault()
//                        .getPreferenceStore()
//                        .setValue(
//                                LocalNetworkSharingUI.PREF_SKIP_LAUNCHING_ON_STARTUP,
//                                true);
//                return status;
//            }
//        };
//        job.setUser(showInDialog);
//        job.schedule();
//        return job;
//    }

    public static void addSharedMapFromLocalFiles(
            final ISharingService sharingService) {
        final File[] files = chooseLocalFiles(Display.getCurrent()
                .getActiveShell());
        if (files != null && files.length > 0) {
//            BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
//                public void run() {
//                    addSharedMaps(sharingService, files);
//                }
//            });
            addSharedMaps(Display.getCurrent().getActiveShell(),
                    sharingService, files);
        }
    }

    public static void updateSharedMaps(Shell shell,
            ISharingService sharingService, ILocalSharedMap map) {
        File file = new File(map.getResourcePath());
        if (!file.exists())
            return;

        SendSharingMapDialog dialog = new SendSharingMapDialog(shell,
                sharingService, map);
        int code = dialog.open();
        if (code != Dialog.OK)
            return;

        sharingMap(sharingService, file, dialog.getReceivers(),
                dialog.getMessage());
    }

    public static void addSharedMaps(Shell shell,
            ISharingService sharingService, final File[] files) {
        if (shell == null || files == null || !(files.length > 0))
            return;

        SendSharingMapDialog dialog = new SendSharingMapDialog(shell,
                sharingService, files);
        int code = dialog.open();
        if (code != Dialog.OK)
            return;

        sharingMap(sharingService, files, dialog.getReceivers(),
                dialog.getMessage());
    }

    private static void sharingMap(final ISharingService sharingService,
            final File[] files, final List<String> receivers, String message) {
        final String[] mapIDs = new String[files.length];
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            public void run() {
                ILocalSharedLibrary library = sharingService.getLocalLibrary();
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    ISharedMap map = library.addSharedMap(file, receivers);
                    mapIDs[i] = map.getID();
                }
            }
        });

        SharingUtils.sendMessage(getMessageReceiver(sharingService, receivers),
                message, mapIDs);
    }

    private static void sharingMap(final ISharingService sharingService,
            final File file, final List<String> receivers, String message) {
        final String[] mapID = new String[1];
        final ILocalSharedLibrary library = sharingService.getLocalLibrary();
        List<String> alreadySharedReceivers = getAlreadySharedReceivers(
                library, file);
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            public void run() {
                ISharedMap map = library.addSharedMap(file, receivers);
                mapID[0] = map.getID();
            }
        });

        SharingUtils.sendMessage(
                getMessageReceiver(sharingService, receivers,
                        alreadySharedReceivers), message, mapID);
    }

    private static List<String> getAlreadySharedReceivers(
            ILocalSharedLibrary library, File file) {
        ISharedMap maps[] = library.getMaps();
        for (ISharedMap map : maps) {
            String filePath = file.getAbsolutePath();
            ILocalSharedMap m = (ILocalSharedMap) map;
            if (filePath.equals(m.getResourcePath())) {
                List<String> receivers = new ArrayList<String>();
                for (String receiver : m.getReceiverIDs()) {
                    receivers.add(receiver);
                }
                return receivers;
            }
        }
        return new ArrayList<String>();
    }

    private static ISharedLibrary[] getMessageReceiver(
            ISharingService sharingService, List<String> receivers) {
        Collection<IRemoteSharedLibrary> remotes = sharingService
                .getRemoteLibraries();

        ISharedLibrary[] receiverLibraries;
        if (receivers.size() > 0) { //Share to select
            receiverLibraries = new ISharedLibrary[receivers.size()];
            int i = 0;
            for (IRemoteSharedLibrary remote : remotes) {
                if (receivers.contains(remote.getContactID())) {
                    receiverLibraries[i++] = remote;
                }

            }
        } else { //Share to all
            receiverLibraries = remotes.toArray(new ISharedLibrary[remotes
                    .size()]);
        }
        return receiverLibraries;
    }

    private static ISharedLibrary[] getMessageReceiver(
            ISharingService sharingService, List<String> receivers,
            List<String> alreadySharedReceivers) {
        for (String rec : alreadySharedReceivers) {
            receivers.remove(rec);
        }

        if (receivers.size() == 0)
            return null;

        return getMessageReceiver(sharingService, receivers);
    }

    private static File[] chooseLocalFiles(Shell parentShell) {
        FileDialog fileDialog = new FileDialog(parentShell, SWT.OPEN
                | SWT.MULTI);
        String filterPath = loadFilterPath();
        initializeFileDialog(fileDialog, filterPath);
        String selected = fileDialog.open();
        if (selected == null)
            return null;
        filterPath = fileDialog.getFilterPath();
        saveFilterPath(filterPath);
        String[] fileNames = fileDialog.getFileNames();
        if (fileNames == null || fileNames.length == 0) {
            return null;
        } else {
            File[] files = new File[fileNames.length];
            for (int i = 0; i < fileNames.length; i++) {
                files[i] = new File(filterPath, fileNames[i]);
            }
            return files;
        }
    }

    private static void initializeFileDialog(FileDialog fileDialog,
            String filterPath) {
        fileDialog.setFilterExtensions(new String[] { "*" //$NON-NLS-1$
                + MindMapUI.FILE_EXT_XMIND });
        fileDialog.setFilterNames(new String[] { NLS.bind(
                "{0} (*{1})", //$NON-NLS-1$
                IDialogConstants.FILE_DIALOG_FILTER_WORKBOOK,
                MindMapUI.FILE_EXT_XMIND) });
        fileDialog.setFilterIndex(0);
        fileDialog.setFilterPath(filterPath);
        fileDialog.setText(SharingMessages.SelectLocalFilesToShare_dialogTitle);
    }

    private static String loadFilterPath() {
        IDialogSettings settings = getDialogSettings();
        String path = settings.get(SETTING_FILTER_PATH);
        if (path != null)
            return path;
        path = System.getProperty("user.home"); //$NON-NLS-1$
        if (path != null)
            return path;
        if (Platform.OS_WIN32.equals(Platform.getOS()))
            return "C:\\"; //$NON-NLS-1$
        return "/"; //$NON-NLS-1$
    }

    private static void saveFilterPath(String path) {
        IDialogSettings settings = getDialogSettings();
        settings.put(SETTING_FILTER_PATH, path);
    }

    private static IDialogSettings getDialogSettings() {
        return LocalNetworkSharingUI
                .getDialogSettingsSection(SETTINGS_SHARE_LOCAL_FILES_DIALOG);
    }

    @SuppressWarnings("unused")
    @Deprecated
    private static void addSharedMaps(ISharingService sharingService,
            File[] files) {
        ILocalSharedLibrary library = sharingService.getLocalLibrary();
        for (int i = 0; i < files.length; i++) {
            library.addSharedMap(files[i]);
        }
    }

    public static String getComputerName() {
        if (COMPUTER_NAME == null) {
            COMPUTER_NAME = calculateComputerName();
        }
        return COMPUTER_NAME;
    }

    /**
     * Retrieve the name describing this computer.
     * 
     * <p>
     * Note that this method may block the current thread for a little time. So
     * be cautious when using it in a UI thread.
     * </p>
     * 
     * @return the computer name
     */
    private static String calculateComputerName() {
        String name = null;

        //////////////////////////////////////////////////////////////////
        // Try retrieving computer name using platform specific method:
        if (Platform.OS_WIN32.equals(Platform.getOS())) {
            // On Windows, computer name can be retrieved from environment variables:
            name = System.getenv("COMPUTERNAME"); //$NON-NLS-1$
        } else if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            // On Mac OS X, computer name is stored in system preference store:
            // Tested on Mac OS X 10.6, 10.7 & 10.8.
            ProcessBuilder pb = new ProcessBuilder("scutil", //$NON-NLS-1$
                    "--get", //$NON-NLS-1$
                    "ComputerName"); //$NON-NLS-1$
            // Start the scutil process and take all its output as the computer name:
            try {
                Process proc = pb.start();
                InputStream in = proc.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
                name = out.toString("UTF-8"); //$NON-NLS-1$
                if (name.endsWith("\n")) { //$NON-NLS-1$
                    name = name.substring(0, name.length() - 1);
                }
            } catch (Throwable e) {
                // ignore
            }
        }

        if (name != null && !"".equals(name)) //$NON-NLS-1$
            return name;

        //////////////////////////////////////////////////////////////////
        // Then, try retrieving local host name defined by networking facilities:
        // http://stackoverflow.com/questions/7883542/getting-the-computer-name-in-java
        try {
            name = java.net.InetAddress.getLocalHost().getHostName();
            if (name != null) {
                if (name.matches("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*)|([0-9A-Fa-f]{0,4}\\:[0-9A-Fa-f]{0,4}\\:[0-9A-Fa-f]{0,4}\\:[0-9A-Fa-f]{0,4}\\:[0-9A-Fa-f]{0,4}\\:[0-9A-Fa-f]{0,4}.*)")) { //$NON-NLS-1$
                    // Drop names containing IP addresses:
                    name = null;
                } else if (name.endsWith(".local")) { //$NON-NLS-1$
                    // Trim Mac OS X host names that ends with '.local':
                    name = name.substring(0, name.length() - 6);
                }
            }
        } catch (Throwable e) {
            // ignore
        }

        if (name != null && !"".equals(name)) //$NON-NLS-1$
            return name;

        //////////////////////////////////////////////////////////////////
        // Finally, take the current login user name:
        return System.getProperty("user.name"); //$NON-NLS-1$
    }

    public static final int DIALOG_CONFIRM = MessageDialog.CONFIRM;

    public static final int DIALOG_INFO = MessageDialog.INFORMATION;

    public static boolean showDialog(final int dialogType, final String title,
            final String message) {
        if (!PlatformUI.isWorkbenchRunning())
            return false;

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return false;

        Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return false;

        final boolean[] answer = new boolean[] { false };
        display.syncExec(new Runnable() {
            public void run() {
                if (dialogType == DIALOG_CONFIRM) {
                    answer[0] = MessageDialog.openConfirm(
                            getDialogParent(workbench), title, message);
                }
                if (dialogType == DIALOG_INFO) {
                    MessageDialog.openInformation(getDialogParent(workbench),
                            title, message);
                }
            }
        });
        return answer[0];
    }

    private static Shell getDialogParent(IWorkbench workbench) {
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window != null)
            return window.getShell();
        return Display.getCurrent().getActiveShell();
    }

    public static void run(final IRunnableWithProgress runnable, Display display) {
        final Throwable[] exception = new Throwable[] { null };
        if (display != null && !display.isDisposed()) {
            display.syncExec(new Runnable() {
                public void run() {
                    final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                            null);
                    dialog.setOpenOnRun(false);
                    final boolean[] completed = new boolean[] { false };
                    Display.getCurrent().timerExec(240, new Runnable() {
                        public void run() {
                            if (!completed[0])
                                dialog.open();
                        }
                    });
                    try {
                        dialog.run(true, false, runnable);
                    } catch (InterruptedException e) {
                        // ignore
                    } catch (InvocationTargetException e) {
                        exception[0] = e.getCause();
                    } catch (Throwable e) {
                        exception[0] = e;
                    } finally {
                        completed[0] = true;
                        dialog.close();
                        Shell shell = dialog.getShell();
                        if (shell != null) {
                            shell.dispose();
                        }
                    }
                }
            });
        } else {
            try {
                runnable.run(new NullProgressMonitor());
            } catch (InterruptedException e) {
                // ignore
            } catch (InvocationTargetException e) {
                exception[0] = e.getCause();
            } catch (Throwable e) {
                exception[0] = e;
            }
        }
        if (exception[0] != null) {
            LocalNetworkSharingUI.log(
                    "Failed to disconnect from local network sharing service:", //$NON-NLS-1$
                    exception[0]);

        }
    }

    public static void saveRemoteMaps(IWorkbenchPage page,
            Collection<? extends ISharedMap> maps) {
        for (ISharedMap map : maps) {
            saveRemoteMap(page, map);
        }
    }

    public static void saveRemoteMap(final IWorkbenchPage page,
            final ISharedMap map) {
        if (map.getSharedLibrary().isLocal())
            return;

        FileDialog dialog = new FileDialog(
                page.getWorkbenchWindow().getShell(), SWT.SAVE);
        dialog.setFileName(map.getResourceName());
        initializeSaveFileDialog(dialog);
        final String[] filePath = new String[1];
        filePath[0] = dialog.open();
        if (filePath[0] == null)
            return;

        final Display display = Display.getCurrent();
        Job saveRemoteMapJob = new Job(NLS.bind(
                SharingMessages.SharingUtils_saveRemoteMapJob_name_withMapName,
                map.getResourceName())) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(null, 100);

                monitor.subTask(SharingMessages.SharingUtils_saveRemoteMapJob_loadingText);
                SubProgressMonitor monitor1 = new SubProgressMonitor(monitor,
                        90, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
                InputStream stream = map.getResourceAsStream(monitor1);
                if (monitor1.isCanceled())
                    return Status.CANCEL_STATUS;
                monitor1.done();

                if (stream == null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            MessageDialog
                                    .openInformation(
                                            page.getWorkbenchWindow()
                                                    .getShell(),
                                            SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                            NLS.bind(
                                                    SharingMessages.OpenRemoteSharedMapJob_SharedMapNotAvailable_errorMessage_withSharedMapName,
                                                    map.getResourceName()));
                        }
                    });
                    return new Status(IStatus.WARNING,
                            LocalNetworkSharingUI.PLUGIN_ID,
                            "Failed to load shared map content from remote client."); //$NON-NLS-1$
                }

                monitor.subTask(SharingMessages.SharingUtils_saveRemoteMapJob_savingText);
                final IWorkbook workbook;
                try {
                    File tempPath = Core.getWorkspace().createTempFile(
                            "remoteMaps", //$NON-NLS-1$
                            "", ".temp"); //$NON-NLS-1$ //$NON-NLS-2$
                    tempPath.mkdirs();
                    DirectoryStorage storage = new DirectoryStorage(tempPath);
                    try {
                        workbook = Core.getWorkbookBuilder().loadFromStream(
                                stream, storage);
                    } catch (Throwable e) {
                        final String errorMessage = e.getLocalizedMessage();
                        display.asyncExec(new Runnable() {
                            public void run() {
                                MessageDialog
                                        .openInformation(
                                                page.getWorkbenchWindow()
                                                        .getShell(),
                                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                                NLS.bind(
                                                        SharingMessages.SharingUtils_saveRemoteMapJob_errorText_withMapName_and_ErrorMessage,
                                                        map.getResourceName(),
                                                        errorMessage));
                            }
                        });
                        return new Status(IStatus.WARNING,
                                LocalNetworkSharingUI.PLUGIN_ID, null, e);
                    }
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }

                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                if (!filePath[0].endsWith(MindMapUI.FILE_EXT_XMIND))
                    filePath[0] += MindMapUI.FILE_EXT_XMIND;
                File file = new File(filePath[0]);
                FileUtils.ensureFileParent(file);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        workbook.save(fos);
                    } finally {
                        fos.close();
                    }
                } catch (Exception e) {
                }

                return Status.OK_STATUS;
            }
        };

        saveRemoteMapJob.setUser(true);
        ISharingService sharingService = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        sharingService.registerJob(saveRemoteMapJob);
        saveRemoteMapJob.setRule(sharingService);
        saveRemoteMapJob.schedule();
    }

    private static void initializeSaveFileDialog(FileDialog fileDialog) {
        fileDialog.setFilterExtensions(new String[] { "*" //$NON-NLS-1$
                + MindMapUI.FILE_EXT_XMIND });
        fileDialog.setFilterNames(new String[] { NLS.bind(
                "{0} (*{1})", //$NON-NLS-1$
                IDialogConstants.FILE_DIALOG_FILTER_WORKBOOK,
                MindMapUI.FILE_EXT_XMIND) });
        fileDialog.setFilterIndex(0);
    }

}
