package org.xmind.ui.mindmap;

public interface IProtocolDescriptor {

    String getId();

    String getName();

    String getProtocolNames();

    boolean hasProtocolName(String name);

}