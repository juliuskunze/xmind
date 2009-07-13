/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package org.xmind.ui.internal.spelling;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.ui.texteditor.IControlContentAdapter2;

import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

/**
 * 
 * @author Karelun huang
 */
public class SpellingHelper1 implements Listener, ITextListener,
        SpellCheckListener {

    private class WordData {
        int start;
        int length;

        public WordData(int start, int length) {
            super();
            this.start = start;
            this.length = length;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof WordData))
                return false;
            WordData sd = (WordData) obj;
            return this.start == sd.start && sd.length == this.length;
        }
    }

    private static final int DEFAULT_CHECK_DELAY = 500;

//    private static final String regex = "http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";//$NON-NLS-1$

    private IControlContentAdapter2 contentAdapter;

    private SpellChecker spellChecker;

    private ITextViewer textViewer;

    private Control control;

    private Runnable checkJob;

    private IDocument document;

//    private Hyperlink[] hyperlinks;
//
//    private List<AutoHyperlink> autoHypers = null;
//
//    private ImagePlaceHolder[] images;

    private List<WordData> errorWords = new ArrayList<WordData>();

    public SpellingHelper1(ITextViewer textViewer,
            IControlContentAdapter2 contentAdapter) {
        this.textViewer = textViewer;
        this.document = textViewer.getDocument();
        this.contentAdapter = contentAdapter;
        init();
    }

    private void init() {
        this.control = textViewer.getTextWidget();
        this.control.addListener(SWT.Paint, this);
        this.control.addListener(SWT.Dispose, this);
        textViewer.addTextListener(this);
        SpellCheckerAgent.visitSpellChecker(new ISpellCheckerVisitor() {
            public void handleWith(SpellChecker spellChecker) {
                if (control.isDisposed())
                    return;
                SpellingHelper1.this.spellChecker = spellChecker;
                spellChecker.addSpellCheckListener(SpellingHelper1.this);
                check();
            }
        });
    }

    protected void check() {
        checkJob = new Runnable() {
            public void run() {
                if (checkJob != this)
                    return;
                if (!control.isDisposed()) {
                    doCheck();
                    control.redraw();
                }
                checkJob = null;
            }
        };
        Display.getCurrent().timerExec(getCheckDelay(), checkJob);
    }

    private void doCheck() {
        if (control.isDisposed())
            return;
        if (spellChecker != null) {
            String content = contentAdapter.getControlContents(control);
            if (content == null || "".equals(content.trim())) { //$NON-NLS-1$
                return;
            }
            errorWords.clear();
            if (document != null) {
//                if (document instanceof IRichDocument) {
//                    IRichDocument doc = (IRichDocument) document;
//                    hyperlinks = doc.getHyperlinks();
//                    images = doc.getImages();
//                    pushAutoHypers(doc);
//                }
                spellChecker.checkSpelling(new StringWordTokenizer(content));
            }
        }
    }

    public void textChanged(TextEvent event) {
        DocumentEvent docEvent = event.getDocumentEvent();
        if (docEvent != null) {
            this.document = docEvent.getDocument();
        }
        check();
    }

    public void handleEvent(Event event) {
        if (control.isDisposed())
            return;
        int type = event.type;
        switch (type) {
        case SWT.Paint:
            paintSpellError(event.gc);
            break;
        case SWT.Dispose:
            handleDispose();
            break;
        }
    }

    public void spellingError(SpellCheckEvent event) {
        if (control.isDisposed())
            return;
        int start = event.getWordContextPosition();
        int length = event.getInvalidWord().length();
        WordData word = new WordData(start, length);
        errorWords.add(word);
    }

    private void paintSpellError(GC gc) {
        int lineStyle = gc.getLineStyle();
        int lineWidth = gc.getLineWidth();
        Color lineColor = gc.getForeground();

        gc.setLineWidth(2);
        gc.setLineStyle(SWT.LINE_DOT);
        gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        int charCount = contentAdapter.getControlContents(control).length();

        if (errorWords.isEmpty())
            return;

        for (WordData spellData : errorWords) {
            int start = spellData.start;
            if (start >= 0 && start < charCount) {
                int length = Math.min(spellData.length, charCount - start);
                for (int i = 0; i < length; i++) {
                    Point p2 = contentAdapter.getLocationAtOffset(control,
                            start + i + 1);
                    int h2 = contentAdapter.getLineHeightAtOffset(control,
                            start + i + 1);
                    p2.y += h2 - 1;

                    Point p1 = contentAdapter.getLocationAtOffset(control,
                            start + i);
                    int h1 = contentAdapter.getLineHeightAtOffset(control,
                            start + i);
                    p1.y += h1 - 1;
                    if (p1.y != p2.y) {
                        Point size = gc.stringExtent(contentAdapter
                                .getControlContents(control, start + i, 1));
                        p1.x = p2.x - size.x;
                        p1.y = p2.y;
                    }
                    gc.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
        gc.setLineWidth(lineWidth);
        gc.setLineStyle(lineStyle);
        gc.setForeground(lineColor);
    }

    private void handleDispose() {
        errorWords.clear();
        checkJob = null;
        if (spellChecker != null) {
            spellChecker.removeSpellCheckListener(this);
            spellChecker = null;
        }
    }

    protected int getCheckDelay() {
        return DEFAULT_CHECK_DELAY;
    }

//    private void pushAutoHypers(IRichDocument doc) {
//        String content = doc.get();
//        if (content == null)
//            return;
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(content);
//        while (matcher.find()) {
//            int start = matcher.start();
//            int length = matcher.end() - start;
//            AutoHyperlink autoHyper = new AutoHyperlink(start, length);
//            if (autoHypers == null)
//                autoHypers = new ArrayList<AutoHyperlink>();
//            autoHypers.add(autoHyper);
//        }
//    }
}
