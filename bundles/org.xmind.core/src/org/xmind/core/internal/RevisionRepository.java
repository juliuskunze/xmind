package org.xmind.core.internal;

import org.xmind.core.IRevisionRepository;

public abstract class RevisionRepository implements IRevisionRepository {

    public Object getAdapter(Class adapter) {
        return null;
    }

}
