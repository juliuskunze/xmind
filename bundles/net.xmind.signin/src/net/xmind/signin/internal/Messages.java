/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.signin.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author Frank Shaka
 * 
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "net.xmind.signin.internal.messages"; //$NON-NLS-1$

    public static String SignOut_jobName;
    public static String SignInDialog_title;
    public static String SignInDialog_message;
    public static String SignInDialog_NameField_text;
    public static String SignInDialog_PasswordField_text;
    public static String SignInDialog_NotMember_text;
    public static String SignInDialog_ForgotPassword_text;
    public static String SignInDialog_Remember_text;
    public static String SignInDialog_SignIn_text;
    public static String SignInDialog_SigningIn_text;
    public static String SignInDialog_RequestError_message;
    public static String SignInDialog_ServerError_message;
    public static String SignInDialog_NetworkError_message;
    public static String SignInDialog_ApplicationError_message;

    public static String SignIn_text;
    public static String SignOut_text;
    public static String ShowAccount_text;
    public static String ShowAccount_toolTip;
    public static String ShowAccount_pattern;
    public static String ShowAccount_toolTip_pattern;

    public static String Renew_text;
    public static String Renew_toolTip;

    public static String ReportErrorJob_jobName;

    public static String ReportErrorJob_SendingErrorReport_taskTitle;

    public static String XMindNetErrorReporter_ReporterEmailInputDialog_message;

    public static String XMindNetErrorReporter_ReporterEmailInputDialog_windowTitle;

    public static String XMindUpdater_Task_CheckForUpdates;
    public static String XMindUpdater_Task_ConfirmDownloading;
    public static String XMindUpdater_Task_ChooseSaveLocation;
    public static String XMindUpdater_Task_Download;
    public static String XMindUpdater_Task_DownloadProgress_with_percentage;
    public static String XMindUpdater_Task_ConfirmInstalling;
    public static String XMindUpdater_Task_LaunchInstaller;

    public static String XMindUpdater_Error_NoXMindProductFound;
    public static String XMindUpdater_Error_NoXMindProductVersionFound;
    public static String XMindUpdater_Error_FailedToCheck_with_responseText;
    public static String XMindUpdater_Error_FailedToCheck_with_responseCode;
    public static String XMindUpdater_Error_FailedToDownload_with_errorDescription;
    public static String XMindUpdater_Error_FailedToDownloadUnknownError;
    public static String XMindUpdater_Error_FailedToDownloadUnknownError_with_responseCode;
    public static String XMindUpdater_Error_InstallerExecutableNotFound_with_executablePath;
    public static String XMindUpdater_Error_FailedToExecuteInstaller_with_errorDescription;
    public static String XMindUpdater_Error_FailedToExecuteCommand_with_commandLine;

    public static String XMindUpdater_DialogTitle;
    public static String XMindUpdater_SaveDialogTitle;
    public static String XMindUpdater_Dialog_NoUpdatesFound;
    public static String XMindUpdater_Dialog_ConfirmInstalling;
    public static String XMindUpdater_Dialog_ConfirmInstallingOnStartupCheck;
    public static String XMindUpdater_Dialog_ConfirmClearDownload;
    public static String XMindUpdater_Dialog_NewVersionAvailable;
    public static String XMindUpdater_Dialog_NewVersionDetails;

    public static String XMindUpdater_Action_Download_text;
    public static String XMindUpdater_Action_ViewDetails_text;
    public static String XMindUpdater_Action_QuitAndInstall_text;
    public static String XMindUpdater_Action_Later_text;
    public static String XMindUpdater_Action_Install_text;
    public static String XMindUpdater_Action_Clear_text;

    public static String XMindUpdater_NewDialog_NewVersionLabel;

    public static String XMindUpdater_NewDialog_RemindLaterLabel;

    public static String XMindUpdater_NewDialog_SkipLabel;

    public static String XMindUpdater_NewDialog_Title;

    public static String XMindUpdater_NewDialog_UpdateLabel;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
