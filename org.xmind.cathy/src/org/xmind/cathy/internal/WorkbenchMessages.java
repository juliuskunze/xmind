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
package org.xmind.cathy.internal;

import org.eclipse.osgi.util.NLS;

public class WorkbenchMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.cathy.internal.messages"; //$NON-NLS-1$

    public static String AppWindowTitle;

    public static String File_menu_text;
    public static String Edit_menu_text;
    public static String Help_menu_text;

    public static String GeneralPrefPage_title;
    public static String RecentFiles_label;
    public static String RestoreLastSession_label;
    public static String AutoSave_label;
    public static String AutoSave_Minutes;

    public static String AutoSaveJob_name;
    public static String AutoSaveJob_finished;
    public static String AutoSaveJob_errorOccurred;

    public static String KeyAssist_text;

    public static String SignIn_text;
    public static String SignOut_text;
    public static String ShowAccount_text;
    public static String ShowAccount_toolTip;
    public static String ShowAccount_pattern;
    public static String ShowAccount_toolTip_pattern;

    public static String Welcome_text;
    public static String Welcome_toolTip;
    public static String Help_text;
    public static String Help_toolTip;
    public static String Feedback_text;
    public static String Feedback_toolTip;
    public static String Invite_text;
    public static String Invite_toolTip;

    public static String ProChecker_CheckPro_jobName;

    public static String Update_jobName;

    public static String Update_text;

    public static String Update_toolTip;

    static {
        NLS.initializeMessages(BUNDLE_NAME, WorkbenchMessages.class);
    }

}