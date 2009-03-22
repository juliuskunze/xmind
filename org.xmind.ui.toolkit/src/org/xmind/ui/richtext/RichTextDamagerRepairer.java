package org.xmind.ui.richtext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.custom.StyleRange;

public class RichTextDamagerRepairer implements IPresentationDamager,
        IPresentationRepairer {

    /** The document this object works on */
    protected IDocument fDocument;
    /** The scanner it uses */
    protected ITokenScanner fScanner;

    protected StyleRange defaultStyle;

    /**
     * Creates a damager/repairer that uses the given scanner. The scanner may
     * not be <code>null</code> and is assumed to return only token that carry
     * text attributes.
     * 
     * @param scanner
     *            the token scanner to be used, may not be <code>null</code>
     */
    public RichTextDamagerRepairer(ITokenScanner scanner) {

        Assert.isNotNull(scanner);

        fScanner = scanner;
        defaultStyle = (StyleRange) RichTextUtils.DEFAULT_STYLE.clone();
    }

    /*
     * @see IPresentationDamager#setDocument(IDocument)
     * 
     * @see IPresentationRepairer#setDocument(IDocument)
     */
    public void setDocument(IDocument document) {
        fDocument = document;
    }

    //---- IPresentationDamager

    /**
     * Returns the end offset of the line that contains the specified offset or
     * if the offset is inside a line delimiter, the end offset of the next
     * line.
     * 
     * @param offset
     *            the offset whose line end offset must be computed
     * @return the line end offset for the given offset
     * @exception BadLocationException
     *                if offset is invalid in the current document
     */
    protected int endOfLineOf(int offset) throws BadLocationException {

        IRegion info = fDocument.getLineInformationOfOffset(offset);
        if (offset <= info.getOffset() + info.getLength())
            return info.getOffset() + info.getLength();

        int line = fDocument.getLineOfOffset(offset);
        try {
            info = fDocument.getLineInformation(line + 1);
            return info.getOffset() + info.getLength();
        } catch (BadLocationException x) {
            return fDocument.getLength();
        }
    }

    /*
     * @see IPresentationDamager#getDamageRegion(ITypedRegion, DocumentEvent,
     * boolean)
     */
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e,
            boolean documentPartitioningChanged) {

        if (!documentPartitioningChanged) {
            try {

                IRegion info = fDocument.getLineInformationOfOffset(e
                        .getOffset());
                int start = Math.max(partition.getOffset(), info.getOffset());

                int end = e.getOffset()
                        + (e.getText() == null ? e.getLength() : e.getText()
                                .length());

                if (info.getOffset() <= end
                        && end <= info.getOffset() + info.getLength()) {
                    // optimize the case of the same line
                    end = info.getOffset() + info.getLength();
                } else
                    end = endOfLineOf(end);

                end = Math.min(partition.getOffset() + partition.getLength(),
                        end);
                return new Region(start, end - start);

            } catch (BadLocationException x) {
            }
        }

        return partition;
    }

    //---- IPresentationRepairer

    /*
     * @see IPresentationRepairer#createPresentation(TextPresentation,
     * ITypedRegion)
     */
    public void createPresentation(TextPresentation presentation,
            ITypedRegion region) {

        if (fScanner == null) {
            // will be removed if deprecated constructor will be removed
            addRange(presentation, region.getOffset(), region.getLength(),
                    defaultStyle);
            return;
        }

        int lastStart = region.getOffset();
        int length = 0;
        boolean firstToken = true;
        IToken lastToken = Token.UNDEFINED;
        StyleRange lastAttribute = getTokenTextAttribute(lastToken);

        fScanner.setRange(fDocument, lastStart, region.getLength());

        while (true) {
            IToken token = fScanner.nextToken();
            if (token.isEOF())
                break;

            StyleRange attribute = getTokenTextAttribute(token);
            if (lastAttribute != null
                    && RichTextUtils.isSimilar(lastAttribute, attribute)) {
                length += fScanner.getTokenLength();
                firstToken = false;
            } else {
                if (!firstToken)
                    addRange(presentation, lastStart, length, lastAttribute);
                firstToken = false;
                lastToken = token;
                lastAttribute = attribute;
                lastStart = fScanner.getTokenOffset();
                length = fScanner.getTokenLength();
            }
        }

        addRange(presentation, lastStart, length, lastAttribute);
    }

    /**
     * Returns a text attribute encoded in the given token. If the token's data
     * is not <code>null</code> and a text attribute it is assumed that it is
     * the encoded text attribute. It returns the default text attribute if
     * there is no encoded text attribute found.
     * 
     * @param token
     *            the token whose text attribute is to be determined
     * @return the token's text attribute
     */
    protected StyleRange getTokenTextAttribute(IToken token) {
        Object data = token.getData();
        if (data instanceof StyleRange)
            return (StyleRange) data;
        return defaultStyle;
    }

    /**
     * Adds style information to the given text presentation.
     * 
     * @param presentation
     *            the text presentation to be extended
     * @param offset
     *            the offset of the range to be styled
     * @param length
     *            the length of the range to be styled
     * @param attr
     *            the attribute describing the style of the range to be styled
     */
    protected void addRange(TextPresentation presentation, int offset,
            int length, StyleRange attr) {
        if (attr != null) {
//            int fontStyle = attr.fontStyle;
//            StyleRange styleRange = new StyleRange(offset, length, attr
//                    .getForeground(), attr.getBackground(), fontStyle);
//            styleRange.strikeout = (fontStyle & TextAttribute.STRIKETHROUGH) != 0;
//            styleRange.underline = (fontStyle & TextAttribute.UNDERLINE) != 0;
//            styleRange.font = attr.getFont();
            StyleRange styleRange = (StyleRange) attr.clone();
            styleRange.start = offset;
            styleRange.length = length;
            presentation.addStyleRange(styleRange);
        }
    }
}
