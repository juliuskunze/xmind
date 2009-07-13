/*
 * Copyright (c) 2006-2008 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.spelling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.widgets.Menu;
import org.xmind.ui.texteditor.IControlContentAdapter2;

import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

public class SpellingHelper implements Listener, SpellCheckListener {
    private class SpellData {
        int start;
        int length;
        String content;

        public SpellData(int start, String content) {
            super();
            this.start = start;
            this.content = content;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof SpellData))
                return false;
            SpellData sd = (SpellData) obj;
            return this.start == sd.start && sd.content.equals(this.content);
        }
    }

    private class TextListener implements ITextListener {

        public void textChanged(TextEvent event) {
//            String text = event.getText();//the string later input
//            String repText = event.getReplacedText();//to be replaced string 
//            int length = event.getLength();//the length to be replaced string
//            int offset1 = event.getOffset();//the position of input string
//            System.out.println(text + "," + repText + "," + length + ","
//                    + offset1);

            offset = event.getOffset();
            input = event.getText();
            replaced = event.getReplacedText();
//            System.out.println(offset1);
            check();
        }
    }

    private static final int DEFAULT_CHECK_DELAY = 500;

    private Control control;

    private IControlContentAdapter2 contentAdapter;

    private Map<Integer, SpellCheckEvent> ranges = new HashMap<Integer, SpellCheckEvent>();
//
//    private List<Integer> list = new ArrayList<Integer>();
//    private List<SpellCheckEvent> errorWords = new ArrayList<SpellCheckEvent>();
    private List<SpellData> cache = new ArrayList<SpellData>();
//    private List<SpellCheckEvent> words = new ArrayList<SpellCheckEvent>();

    private Menu menu = null;

    private Runnable checkJob;

    private SpellChecker spellChecker;

    private int start = 0;

    private int offset = 0;

    private String input = null;

    private String replaced = null;

    private ITextViewer textViewer;

//    public SpellingHelper(Control control,
//            IControlContentAdapter2 contentAdapter) {
//        this.control = control;
//        this.contentAdapter = contentAdapter;
//        init();
//    }

    public SpellingHelper(ITextViewer textViewer,
            IControlContentAdapter2 contentAdapter) {
        this.textViewer = textViewer;
        this.control = textViewer.getTextWidget();
        this.contentAdapter = contentAdapter;
        init();
    }

    /**
     * 
     */
    private void init() {
//        control.addListener(SWT.Modify, this);
        control.addListener(SWT.Paint, this);
//        control.addListener(SWT.MenuDetect, this);
        control.addListener(SWT.Dispose, this);
        SpellCheckerAgent.visitSpellChecker(new ISpellCheckerVisitor() {
            public void handleWith(SpellChecker spellChecker) {
                if (control.isDisposed())
                    return;
                SpellingHelper.this.spellChecker = spellChecker;
                spellChecker.addSpellCheckListener(SpellingHelper.this);
                check();
            }
        });
        ITextListener textListener = new TextListener();
        textViewer.addTextListener(textListener);
    }

//    protected void checkMenu(int x, int y) {
//        SpellCheckEvent range = findRange(x, y);
//        if (range != null) {
//            showMenu(range);
//        }
//    }

//    private SpellCheckEvent findRange(int x, int y) {
//        Point p = control.toControl(x, y);
//        int pos = contentAdapter.getOffsetAtLocation(control, p);
//        for (SpellCheckEvent range : ranges.values()) {
//            int start = range.getWordContextPosition();
//            int length = range.getInvalidWord().length();
//            if (start < pos && pos < start + length) {
//                return range;
//            }
//        }
//
//        return null;
//    }

    /**
     * @param pos
     */
    @SuppressWarnings("unchecked")
//    private void showMenu(final SpellCheckEvent range) {
//        if (menu != null) {
//            menu.dispose();
//        }
//        menu = new Menu(control);
//        control.setMenu(menu);
//        List list = range.getSuggestions();
//        if (list.isEmpty()) {
//            MenuItem mi = new MenuItem(menu, SWT.NONE);
//            mi.setText(Messages.noSpellSuggestion);
//            mi.setEnabled(false);
//        } else {
//            for (Object o : list) {
//                final String suggestion = o.toString();
//                final MenuItem mi = new MenuItem(menu, SWT.NONE);
//                mi.setText("  " + suggestion); //$NON-NLS-1$
//                mi.addListener(SWT.Selection, new Listener() {
//                    public void handleEvent(Event event) {
//                        modifySuggestion(range, suggestion);
//                    }
//                });
//            }
//        }
//
//        new MenuItem(menu, SWT.SEPARATOR);
//
//        MenuItem mi = new MenuItem(menu, SWT.NONE);
//        mi.setText(Messages.addToDictionary);
//        mi.addListener(SWT.Selection, new Listener() {
//            public void handleEvent(Event event) {
//                addToDict(range);
//            }
//        });
//    }
    /*
     * *
     * 
     * @param range
     */
//    private void addToDict(final SpellCheckEvent range) {
//        SpellCheckerAgent.visitSpellChecker(new ISpellCheckerVisitor() {
//            public void handleWith(SpellChecker spellChecker) {
//                if (control.isDisposed())
//                    return;
//                spellChecker.addToDictionary(range.getInvalidWord());
//                check();
//                control.redraw();
//            }
//        });
//    }
    /*
     * *
     * 
     * @param range
     * 
     * @param suggestion
     */
//    private void modifySuggestion(SpellCheckEvent range, String suggestion) {
//        if (control.isDisposed())
//            return;
//
//        String old = contentAdapter.getControlContents(control);
//        int oldLength = old.length();
//        int start = range.getWordContextPosition();
//        String invalidWord = range.getInvalidWord();
//        int invalidLength = invalidWord.length();
//        if (start < oldLength && start + invalidLength <= oldLength) {
//            String newText = old.substring(0, start) + suggestion
//                    + old.substring(start + invalidWord.length(), old.length());
//            contentAdapter.setControlContents(control, newText, start
//                    + suggestion.length());
//            check();
//        }
//    }
    private void paintSpellError(GC gc) {
        int lineStyle = gc.getLineStyle();
        int lineWidth = gc.getLineWidth();
        Color lineColor = gc.getForeground();
        gc.setLineWidth(2);
        gc.setLineStyle(SWT.LINE_DOT);
        gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

        String contents = contentAdapter.getControlContents(control);
        int charCount = contents.length();
//        for (SpellCheckEvent spellEvent : ranges.values()) {
//            int start = spellEvent.getWordContextPosition();
//            if (start >= 0 && start < charCount) {
//                int length = Math.min(spellEvent.getInvalidWord().length(),
//                        charCount - start);
        for (SpellData spell : cache) {
            int start = spell.start;
            if (start >= 0 && start < charCount) {
                int length = Math
                        .min(spell.content.length(), charCount - start);

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

    /**
     * 
     */
    private void check() {
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

    protected int getCheckDelay() {
        return DEFAULT_CHECK_DELAY;
    }

    private void doCheck() {
//        ranges.clear();
//        errorWords.clear();
        if (spellChecker != null) {
            String content = contentAdapter.getControlContents(control);
//            showList("Before");
//            for (SpellData d : cache) {
//                System.out.println("Before " + d.start + " , " + d.content);
//            }
//            System.out.println();
//            System.out.println("offset: " + offset);
//            System.out.println("sd");
            reSort();
            start = getPrevWordStart(offset);
//            int end = getNextWordEnd(offset);
//            System.out.println("start: " + start);
            refreshCache(offset);
            content = content.substring(start);

            spellChecker.checkSpelling(new StringWordTokenizer(content));
        }
    }

    private void refreshCache(int offset) {
        int changed = input.length();
        if (replaced != null)
            changed -= replaced.length();
        if (cache.size() < 1)
            return;
        for (int i = cache.size() - 1; i >= 0; i--) {
            SpellData sd = cache.get(i);
            if (sd.start >= offset) {
                sd.start += changed;
            } else
                break;
        }
    }

    private int getPrevWordStart(int offset) {
        int ret = 0;
//        SpellData data=null;
        if (cache.isEmpty())
            return ret;
        for (SpellData sd : cache) {
            if (sd.start <= offset) {
                ret = sd.start;
//                data=sd;
            } else
                break;
        }
//        if(data!=null) {
//        }
        return ret;
    }

//    private int getNextWordEnd(int offset) {
//        int ret = 0;
//        if (cache.isEmpty())
//            return ret;
//
//        for (SpellData sd : cache) {
//            int start = sd.start;
//            int length = sd.content.length();
//            if (offset <= start + length) {
//
//            }
//        }
//        return 0;
//    }

    /**
     * @see cn.brainy.framework.Disposable#dispose()
     */
    protected void handleDispose() {
        ranges.clear();
        cache.clear();
//        list.clear();
//        errorWords.clear();
        checkJob = null;
        if (menu != null) {
            menu.dispose();
            menu = null;
        }
        if (spellChecker != null) {
            spellChecker.removeSpellCheckListener(this);
            spellChecker = null;
        }
    }

    public void handleEvent(Event event) {
        if (control.isDisposed())
            return;

        int type = event.type;
        switch (type) {
//        case SWT.Modify:
//            check();
//            break;
        case SWT.Paint:
            paintSpellError(event.gc);
            break;
//        case SWT.MenuDetect:
//            checkMenu(event.x, event.y);
//            break;
        case SWT.Dispose:
            handleDispose();
            break;
        }
    }

    public void spellingError(SpellCheckEvent event) {
        if (control.isDisposed())
            return;
        int start = event.getWordContextPosition();
        start += this.start;
        String word = event.getInvalidWord();

        comparatorTo(start);//right
        cache.add(new SpellData(start, word));//right

        ranges.put(start, event);
//        showList("After");
    }

    private void comparatorTo(int start) {
        reSort();
        int num = -1;
        for (int i = 0; i < cache.size(); i++) {
            SpellData data = cache.get(i);
            if (start == data.start) {
                num = i;
//                break;
            }
        }
        if (num > -1)
            cache.remove(num);
    }

//    private void showList(String str) {
//        for (SpellCheckEvent s : ranges.values()) {
//            int start = s.getWordContextPosition();
//            int length = s.getInvalidWord().length();
//            int caretOffset = contentAdapter.getCursorPosition(control);
//            System.out.println(str + ": " + start + " " + length + " "
//                    + caretOffset);
//        }
//        System.out.println();
//    }

    private void reSort() {
        Collections.sort(cache, new Comparator<SpellData>() {
            public int compare(SpellData o1, SpellData o2) {
                int start1 = o1.start;
                int start2 = o2.start;
                int ret = start1 - start2;
                if (ret == 0) {
                    int length1 = o1.content.length();
                    int length2 = o2.content.length();
                    return length1 - length2;
                }
                return ret;
            }
        });
    }
}