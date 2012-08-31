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

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
