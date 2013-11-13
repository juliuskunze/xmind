package org.xmind.core.io;

import java.io.IOException;

public interface ICloseableOutputTarget extends IOutputTarget {

    /**
     * Close this output target and flush all contents to the target.
     */
    void close() throws IOException;

}
