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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.xmind.ui.texteditor.IControlContentAdapter2;

import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

public class SpellingHelper implements Listener, SpellCheckListener {

    private static final int DEFAULT_CHECK_DELAY = 500;

    private Control control;

    private IControlContentAdapter2 contentAdapter;

    private Map<Integer, SpellCheckEvent> ranges = new HashMap<Integer, SpellCheckEvent>();

    private Menu menu = null;

    private Runnable checkJob;

    private SpellChecker spellChecker;

    public SpellingHelper(Control control,
            IControlContentAdapter2 contentAdapter) {
        this.control = control;
        this.contentAdapter = contentAdapter;
        init();
    }

    /**
     * 
     */
    private void init() {
        control.addListener(SWT.Modify, this);
        control.addListener(SWT.Paint, this);
        control.addListener(SWT.MenuDetect, this);
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
    }

    protected void checkMenu(int x, int y) {
        SpellCheckEvent range = findRange(x, y);
        if (range != null) {
            showMenu(range);
        }
    }

    private SpellCheckEvent findRange(int x, int y) {
        Point p = control.toControl(x, y);
        int pos = contentAdapter.getOffsetAtLocation(control, p);
        for (SpellCheckEvent range : ranges.values()) {
            int start = range.getWordContextPosition();
            int length = range.getInvalidWord().length();
            if (start < pos && pos < start + length) {
                return range;
            }
        }
        return null;
    }

    /**
     * @param pos
     */
    @SuppressWarnings("unchecked")
    private void showMenu(final SpellCheckEvent range) {
        if (menu != null) {
            menu.dispose();
        }
        menu = new Menu(control);
        control.setMenu(menu);
        List list = range.getSuggestions();
        if (list.isEmpty()) {
            MenuItem mi = new MenuItem(menu, SWT.NONE);
            mi.setText(Messages.noSpellSuggestion);
            mi.setEnabled(false);
        } else {
            for (Object o : list) {
                final String suggestion = o.toString();
                final MenuItem mi = new MenuItem(menu, SWT.NONE);
                mi.setText("  " + suggestion); //$NON-NLS-1$
                mi.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        modifySuggestion(range, suggestion);
                    }
                });
            }
        }

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem mi = new MenuItem(menu, SWT.NONE);
        mi.setText(Messages.addToDictionary);
        mi.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                addToDict(range);
            }
        });
    }

    /**
     * @param range
     */
    private void addToDict(final SpellCheckEvent range) {
        SpellCheckerAgent.visitSpellChecker(new ISpellCheckerVisitor() {
            public void handleWith(SpellChecker spellChecker) {
                if (control.isDisposed())
                    return;
                spellChecker.addToDictionary(range.getInvalidWord());
                check();
                control.redraw();
            }
        });
    }

    /**
     * @param range
     * @param suggestion
     */
    private void modifySuggestion(SpellCheckEvent range, String suggestion) {
        if (control.isDisposed())
            return;

        String old = contentAdapter.getControlContents(control);
        int oldLength = old.length();
        int start = range.getWordContextPosition();
        String invalidWord = range.getInvalidWord();
        int invalidLength = invalidWord.length();
        if (start < oldLength && start + invalidLength <= oldLength) {
            String newText = old.substring(0, start) + suggestion
                    + old.substring(start + invalidWord.length(), old.length());
            contentAdapter.setControlContents(control, newText, start
                    + suggestion.length());
            check();
        }
    }

    /**
     * @param gc
     */
    private void paintSpellError(GC gc) {
        int lineStyle = gc.getLineStyle();
        int lineWidth = gc.getLineWidth();
        Color lineColor = gc.getForeground();
        gc.setLineWidth(2);
        gc.setLineStyle(SWT.LINE_DOT);
        gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        int charCount = contentAdapter.getControlContents(control).length();
        for (SpellCheckEvent sce : ranges.values()) {
            int start = sce.getWordContextPosition();
            if (start >= 0 && start < charCount) {
                int length = Math.min(sce.getInvalidWord().length(), charCount
                        - start);
                if (length > 0) {
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
        ranges.clear();
        if (spellChecker != null) {
            spellChecker.checkSpelling(new StringWordTokenizer(contentAdapter
                    .getControlContents(control)));
        }
    }

    /**
     * @see cn.brainy.framework.Disposable#dispose()
     */
    protected void handleDispose() {
        ranges.clear();
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
        case SWT.Modify:
            check();
            break;
        case SWT.Paint:
            paintSpellError(event.gc);
            break;
        case SWT.MenuDetect:
            checkMenu(event.x, event.y);
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
        int caretOffset = contentAdapter.getCursorPosition(control);
        if (!(caretOffset >= start && caretOffset < start + length))
            ranges.put(start, event);
    }

}