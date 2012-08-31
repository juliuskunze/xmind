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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.decorators.NumberingDecorator;
import org.xmind.ui.mindmap.INumberingPart;
import org.xmind.ui.mindmap.ITopicPart;

public class NumberingPart extends MindMapPartBase implements INumberingPart {

    public NumberingPart() {
        setDecorator(NumberingDecorator.getInstance());
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            TopicPart topic = (TopicPart) getParent();
            if (topic.getNumberingPart() == this)
                topic.setNumberingPart(null);
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            TopicPart topic = (TopicPart) getParent();
            topic.setNumberingPart(this);
        }
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public String getNumberingText() {
        ITopicPart topicPart = getTopicPart();
        if (topicPart != null)
            return topicPart.getNumberingText();
        return null;
    }

    public String getFullNumberingText() {
        ITopicPart topicPart = getTopicPart();
        if (topicPart != null)
            return topicPart.getFullNumberingText();
        return null;
    }

    protected IFigure createFigure() {
        return new RotatableWrapLabel();
    }

}