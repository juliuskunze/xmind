package net.xmind.signin;

import net.xmind.signin.internal.XMindCommand;

public interface IXMindCommandHandler {

    boolean handleXMindCommand(XMindCommand command);

}
