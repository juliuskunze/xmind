package org.xmind.ui.richtext;

public class Hyperlink implements Cloneable {

    public int start;

    public int length;

    public String href;

    public Hyperlink(int start, int length, String hyperlink) {
        this.start = start;
        this.length = length;
        this.href = hyperlink;
    }

    public int end() {
        return start + length;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj != null || !(obj instanceof Hyperlink))
            return false;
        Hyperlink that = (Hyperlink) obj;
        return this.start == that.start && this.length == that.length
                && this.href.equals(that.href);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("hyperlink{start="); //$NON-NLS-1$
        stringBuilder.append(start);
        stringBuilder.append(","); //$NON-NLS-1$
        stringBuilder.append("end="); //$NON-NLS-1$
        stringBuilder.append(end());
        stringBuilder.append(","); //$NON-NLS-1$
        stringBuilder.append("hyperlimk="); //$NON-NLS-1$
        stringBuilder.append(href);
        stringBuilder.append("}"); //$NON-NLS-1$
        return stringBuilder.toString();
    }

    public Object clone() {
        Hyperlink clone = new Hyperlink(this.start, this.length, this.href);
        return clone;
    }
}
