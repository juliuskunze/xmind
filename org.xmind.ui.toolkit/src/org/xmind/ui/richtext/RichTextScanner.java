package org.xmind.ui.richtext;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;

public class RichTextScanner implements ITokenScanner {

    private IRichDocument document;

    private int offset;

    private int length;

    private StyleRange[] textStyles;

    private int styleIndex;

//    private StyleRange lastStyle;

    private Hyperlink[] hyperlinks;

    private int hyperlinkIndex;

    private int lastOffset;

    private int lastLength;

    public void setRange(IDocument document, int offset, int length) {
        if (!(document instanceof IRichDocument))
            return;
        this.document = (IRichDocument) document;
        this.offset = offset;
        this.length = length;
        this.textStyles = this.document.getTextStyles();
        this.styleIndex = 0;
        this.hyperlinks = this.document.getHyperlinks();
        this.hyperlinkIndex = 0;
        this.lastOffset = offset;
        this.lastLength = 0;
        getHyperlinkAt(offset);
    }

    public int getTokenLength() {
        return lastLength;
//        if (lastStyle == null)
//            return 0;
//        return Math.min(lastStyle.length, offset + length - lastStyle.start);
    }

    public int getTokenOffset() {
        return lastOffset;
//        if (lastStyle == null)
//            return 0;
//        return Math.max(lastStyle.start, offset);
    }

    public IToken nextToken() {

        int current = lastOffset + lastLength;
        if (current >= offset + length) {
            return Token.EOF;
        }

        int next = offset + length;
        Hyperlink h = getHyperlinkAt(current);
        if (h != null) {
            if (current >= h.start) {
                next = Math.min(next, h.start + h.length);
            } else {
                next = Math.min(next, h.start);
            }
        }

        StyleRange style = getTextStyleAt(current);
        if (style != null) {
            if (current >= style.start) {
                next = Math.min(next, style.start + style.length);
            } else {
                next = Math.min(next, style.start);
            }
        }

        this.lastOffset = current;
        this.lastLength = next - current;
        if (lastLength <= 0) {
            return Token.EOF;
        }

//        Color foreground = null;
//        Color background = null;
//        int fontStyle = 0;
//        Font font = null;
//        if (style != null && current >= style.start
//                && next <= style.start + style.length) {
//            foreground = style.foreground;
//            background = style.background;
//            fontStyle = RichTextUtils.getFontStyle(style);
//            font = style.font;
//            if (style.underline) {
//                fontStyle |= TextAttribute.UNDERLINE;
//            }
//            if (style.strikeout) {
//                fontStyle |= TextAttribute.STRIKETHROUGH;
//            }
//        }
//        if (h != null && current >= h.start && next <= h.start + h.length) {
//            if (foreground == null) {
//                foreground = Display.getCurrent()
//                        .getSystemColor(SWT.COLOR_BLUE);
//            }
//            fontStyle |= TextAttribute.UNDERLINE;
//        }
//
//        TextAttribute attr = new TextAttribute(foreground, background,
//                fontStyle, font);

        if (!(style != null && current >= style.start && next <= style.start
                + style.length)) {
            style = null;
        }

        if (h != null && current >= h.start && next <= h.start + h.length) {
            if (style == null)
                style = RichTextUtils.DEFAULT_STYLE;
            style = (StyleRange) style.clone();
            if (style.foreground == null) {
                style.foreground = Display.getCurrent().getSystemColor(
                        SWT.COLOR_BLUE);
            }
            style.underline = true;
        }

        return new Token(style);
//
//        while (styleIndex < textStyles.length
//                || hyperlinkIndex < hyperlinks.length) {
//            h = hyperlinkIndex < hyperlinks.length ? hyperlinks[hyperlinkIndex]
//                    : null;
//            if (h != null) {
//
//            }
//
//            style = styleIndex < textStyles.length ? textStyles[styleIndex]
//                    : null;
//
////            if (textStyles.length > 0) {
////                style = textStyles[styleIndex];
////            }
////            if (hyperlinks.length > 0) {
////                Hyperlink hyper = hyperlinks[hyperlinkIndex];
////                hyperlinkIndex++;
////                if (style == null) {
////                    style = new StyleRange();
////                    style.start = hyper.start;
////                    style.length = hyper.length;
////                }
////                style.foreground = Display.getCurrent().getSystemColor(
////                        SWT.COLOR_BLUE);
////                style.underline = true;
////            }
//            styleIndex++;
//
//            if (isStyleInRange(style)) {
//                this.lastStyle = style;
//                return createToken(style);
//            }
//        }
//        return Token.EOF;
    }

    private Hyperlink getHyperlinkAt(int position) {
        while (hyperlinkIndex < hyperlinks.length) {
            Hyperlink h = hyperlinks[hyperlinkIndex];
            if (position < h.start + h.length)
                return h;
            hyperlinkIndex++;
        }
        return null;
    }

    private StyleRange getTextStyleAt(int position) {
        while (styleIndex < textStyles.length) {
            StyleRange style = textStyles[styleIndex];
            if (position < style.start + style.length)
                return style;
            styleIndex++;
        }
        return null;
    }

//    public IToken nextToken() {
//        this.lastStyle = null;
//        StyleRange style = null;
//        while (styleIndex < textStyles.length) {
//
//            style = textStyles[styleIndex];
//            styleIndex++;
//            if (isStyleInRange(style)) {
//                this.lastStyle = style;
//                return createToken(style);
//            }
//        }
//        return Token.EOF;
//    }

//    private boolean isInHyperlink(Hyperlink hyper) {
//        return hyper.start < offset + length && hyper.end() > offset;
//    }

//    private IToken createToken(Color color) {
//        TextAttribute textAttr = new TextAttribute(color, null,
//                TextAttribute.UNDERLINE);
//        return new Token(textAttr);
//    }
//
//    private IToken createToken(StyleRange style) {
//        return new Token(createTextAttribute(style));
//    }
//
//    private TextAttribute createTextAttribute(StyleRange style) {
//        int fontStyle = RichTextUtils.getFontStyle(style);
//        if (style.underline) {
//            fontStyle |= TextAttribute.UNDERLINE;
//        }
//        if (style.strikeout) {
//            fontStyle |= TextAttribute.STRIKETHROUGH;
//        }
//        TextAttribute attr = new TextAttribute(style.foreground,
//                style.background, fontStyle, style.font);
//        return attr;
//    }
//
//    private boolean isStyleInRange(StyleRange style) {
//        return style.start < offset + length
//                && style.start + style.length > offset;
//    }

}
