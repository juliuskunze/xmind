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

import org.eclipse.osgi.util.NLS;

public class SharingMessages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.ui.internal.sharing.messages"; //$NON-NLS-1$

    public static String CommonDialogTitle_LocalNetworkSharing;
    public static String ConfirmDeleteMultipleSharedMaps_dialogMessage;
    public static String ConfirmDeleteSingleSharedMap_dialogMessage;
    public static String InstallBonjourJob_BonjourInstalledSuccessfully_dialogMessage;
    public static String InstallBonjourJob_ConfirmInstallBonjour_dialogMessage;
    public static String InstallBonjourJob_ErrorOccurredWhileExecutingBonjourInstaller_errorMessage;
    public static String InstallBonjourJob_jobName;
    public static String MindMapSharingStatusItem_Shared_buttonText_withLocalUserName;
    public static String MindMapSharingStatusItem_Shared_buttonText_withRemoteUserName;
    public static String MindMapSharingStatusItem_ToShare_buttonText;
    public static String OpenedEditorHasNoXMindFileToShare_dialogMessage;
    public static String OpenRemoteSharedMapJob_ErrorOccurredWhileOpeningSharedMap_errorMessage_withSharedMapName_and_ErrorDetails;
    public static String OpenRemoteSharedMapJob_jobName_withSharedMapName;
    public static String OpenRemoteSharedMapJob_LoadRemoteMapContentTask_name;
    public static String OpenRemoteSharedMapJob_OpenWithMindMapEditorTask_name;
    public static String OpenRemoteSharedMapJob_SharedMapNotAvailable_errorMessage_withSharedMapName;
    public static String PreferencePage_EmptyName_errorMessage;
    public static String PreferencePage_FeatureDescription;
    public static String PreferencePage_Form_Name_label;
    public static String PreferencePage_Form_Status_ControlArea_Disable_buttonText;
    public static String PreferencePage_Form_Status_ControlArea_Enable_buttonText;
    public static String PreferencePage_Form_Status_ControlArea_InstallBonjourAndEnable_buttonText;
    public static String PreferencePage_Form_Status_DisplayArea_Disabled_text;
    public static String PreferencePage_Form_Status_DisplayArea_Enabled_text;
    public static String PreferencePage_Form_Status_DisplayArea_NoBonjour_warningText;
    public static String PreferencePage_Form_Status_label;
    public static String SelectLocalFilesToShare_dialogTitle;
    public static String SendMessageToRemoteUserJob_jobName;
    public static String SendSharingMessageDialog_dialogTitle_withCommonDialogTitle;
    public static String SendSharingMessageDialog_Message_label;
    public static String SendSharingMessageDialog_UserSelection_AllAvailableUsers_text;
    public static String SendSharingMessageDialog_UserSelection_SendTo_label;
    public static String SharedLibrariesViewer_LibrarySection_NoSharedMaps_warningText;
    public static String SharedLibrary_title_withLibraryName_and_MoreThanOneMaps;
    public static String SharedLibrary_title_withLibraryName_and_OneMap;
    public static String SharedLibrary_title_withLibraryName_and_ZeroMaps;
    public static String SharedMap_tooltip_AddedTime_text_withTime;
    public static String SharedMap_tooltip_MapIsMissing_warningText;
    public static String SharedMap_tooltip_ModifiedTime_text_withTime;
    public static String SharedMapsDropSupport_DropToShare_toolTip;
    public static String ShareLocalFilesJob_jobName;
    public static String ShareLocalFilesJob_DetectedMultipleNonXMindFiles_dialogMessage;
    public static String ShareLocalFilesJob_DetectedSingleNonXMindFile_dialogMessage;
    public static String SharingMessageNotification_DefaultSharedMapNamesConjunction;
    public static String SharingMessageNotification_label_withRemoteUserName_and_NumberOfSharedMaps_and_ConjunctSharedMapNames;
    public static String SharingMessageNotification_label_withRemoteUserName_and_OneSharedMap;
    public static String SharingMessageNotification_label_withRemoteUserName_and_ZeroSharedMaps;
    public static String SharingMessageNotification_LastSharedMapNameConjunction;
    public static String SharingMessageNotification_ViewAction_text;
    public static String SharingServiceStatusItem_GoOfflineAction_text;
    public static String SharingServiceStatusItem_GoOnlineAction_text;
    public static String SharingServiceStatusItem_ShowLocalNetworkSharingViewAction_text;
    public static String SharingServiceStatusItem_ShowPreferencesAction_text;
    public static String SharingServiceStatusItem_tooltip_withLocalUserName;
    public static String ToggleLocalNetworkSharingServiceStatusJob_NoSupportForYourOperatingSystem_dialogMessage;
    public static String TurnLocalNetworkSharingServiceOfflineJob_jobName;
    public static String TurnLocalNetworkSharingServiceOnlineJob_jobName;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, SharingMessages.class);
    }

    private SharingMessages() {
    }
}
