package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractRelationshipDecoration;

public class ZigzagRelationshipDecoration extends
        AbstractRelationshipDecoration {

    public ZigzagRelationshipDecoration() {
        super();
    }

    public ZigzagRelationshipDecoration(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {

        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint scp = new PrecisionPoint(getSourceControlPoint(figure));
        PrecisionPoint tcp = new PrecisionPoint(getTargetControlPoint(figure));

        if (Math.abs(tcp.x - tp.x) <= Math.abs(tcp.y - tp.y)) { //target
            tcp.x = tp.x; // 1,3
        } else {
            tcp.y = tp.y; // 2,4
        }
        if (Math.abs(scp.x - sp.x) <= Math.abs(scp.y - sp.y)) { //source
            scp.x = sp.x; // 1,3
        } else {
            scp.y = sp.y; // 2,4
        }
        if (tcp.x == tp.x) {
            if (scp.x == sp.x) {
                tcp.y = scp.y = (scp.y + tcp.y) / 2;
            } else if (scp.y == sp.y) {
                tcp.y = scp.y = sp.y;
                scp.x = tp.x;
            }
        } else if (tcp.y == tp.y) {
            if (scp.y == sp.y) {
                tcp.x = scp.x = (tcp.x + scp.x) / 2;
//                tcp.x = scp.x = (tp.x + sp.x) / 2;
//                tcp.y = scp.y = sp.y;
            } else if (scp.x == sp.x) {
                tcp.x = scp.x = sp.x;
                scp.y = tp.y;
            }
        }
        shape.moveTo(sp);
        shape.lineTo(scp);
        shape.lineTo(tcp);
        shape.lineTo(tp);
    }

    protected double getSourceAnchorAngle(IFigure figure) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint scp = new PrecisionPoint(getSourceControlPoint(figure));
        PrecisionPoint tcp = getTargetControlPoint(figure);
        PrecisionPoint p1 = new PrecisionPoint(0.0, 0.0);
        PrecisionPoint p2 = new PrecisionPoint(0.0, 0.0);
        if (Math.abs(scp.x - sp.x) <= Math.abs(scp.y - sp.y)) {
            if (tp.y < sp.y) { //3
                p1 = new PrecisionPoint(0, 1);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.y + scp.y) / 2 > sp.y) {
                    p1 = new PrecisionPoint(0, -1);
                    p2 = new PrecisionPoint(0, 0);
                }
            } else if (tp.y > sp.y) { // 1
                p1 = new PrecisionPoint(0, -1);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.y + scp.y) / 2 < sp.y) {
                    p1 = new PrecisionPoint(0, 1);
                    p2 = new PrecisionPoint(0, 0);
                }

            }
        } else {
            if (tp.x < sp.x) { //4
                p1 = new PrecisionPoint(1, 0);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.x + scp.x) / 2 > sp.x) {
                    p1 = new PrecisionPoint(0, 0);
                    p2 = new PrecisionPoint(0, 0);
                }

            } else if (tp.x > sp.x) { //2
                p1 = new PrecisionPoint(0, 0);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.x + scp.x) / 2 < sp.x) {
                    p1 = new PrecisionPoint(1, 0);
                    p2 = new PrecisionPoint(0, 0);
                }
            }
        }
        return Geometry.getAngle(p2, p1);
    }

    protected double getTargetAnchorAngle(IFigure figure) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint tcp = getTargetControlPoint(figure);
        PrecisionPoint scp = getSourceControlPoint(figure);
        PrecisionPoint p1 = new PrecisionPoint(0.0, 0.0);
        PrecisionPoint p2 = new PrecisionPoint(0.0, 0.0);
        if (Math.abs(tcp.x - tp.x) <= Math.abs(tcp.y - tp.y)) {
            if (tp.y > sp.y) { // 3
                p1 = new PrecisionPoint(0, 1);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.y + scp.y) / 2 > tp.y) {
                    p1 = new PrecisionPoint(0, -1);
                    p2 = new PrecisionPoint(0, 0);
                }

            } else if (tp.y < sp.y) {//  1
                p1 = new PrecisionPoint(0, -1);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.y + scp.y) / 2 < tp.y) {
                    p1 = new PrecisionPoint(0, 1);
                    p2 = new PrecisionPoint(0, 0);
                }
            }
        } else {
            if (tp.x < sp.x) { // 2
                p1 = new PrecisionPoint(-1, 0);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.x + scp.x) / 2 < tp.x) {
                    p1 = new PrecisionPoint(1, 0);
                    p2 = new PrecisionPoint(0, 0);
                }
            } else if (tp.x > sp.x) { // 4
                p1 = new PrecisionPoint(1, 0);
                p2 = new PrecisionPoint(0, 0);
                if ((tcp.x + scp.x) / 2 > tp.x) {
                    p1 = new PrecisionPoint(-1, 0);
                    p2 = new PrecisionPoint(0, 0);
                }
            }
        }
        return Geometry.getAngle(p2, p1);
    }

    protected void calcTitlePosition(IFigure figure, PrecisionPoint titlePos,
            PrecisionPoint sourcePos, PrecisionPoint targetPos,
            PrecisionPoint sourceCP, PrecisionPoint targetCP) {

        double x = 0.0, y = 0.0;
        double targetXOffset = Math.abs(targetCP.x - targetPos.x);
        double targetYOffset = Math.abs(targetCP.y - targetPos.y);
        double sourceXOffset = Math.abs(sourceCP.x - sourcePos.x);
        double sourceYOffset = Math.abs(sourceCP.y - sourcePos.y);
        if (targetXOffset <= targetYOffset && sourceXOffset <= sourceYOffset) {
            x = (sourcePos.x + targetPos.x) / 2;
            y = (sourceCP.y + targetCP.y) / 2;
        } else if (targetXOffset <= targetYOffset
                && sourceXOffset > sourceYOffset) {
            x = targetPos.x;
            y = sourcePos.y;
        } else if (targetXOffset > targetYOffset
                && sourceXOffset <= sourceYOffset) {
            x = sourcePos.x;
            y = targetPos.y;
        } else if (targetXOffset > targetYOffset
                && sourceXOffset > sourceYOffset) {
            x = (sourceCP.x + targetCP.x) / 2;
            y = (sourcePos.y + targetPos.y) / 2;
        }
        titlePos.setLocation(x, y);
    }
}
