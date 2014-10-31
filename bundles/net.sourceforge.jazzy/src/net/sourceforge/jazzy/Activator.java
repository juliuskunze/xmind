package net.sourceforge.jazzy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.Plugin;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;

/**
 * @author Brian Sun
 */
public class Activator extends Plugin {

    /**
     * 
     */
    public Activator() {
    }
    
    /**
     * @return
     */
    public static SpellChecker createSpellChecker() {
        return new SpellChecker();
    }
    
    public static void addDefaultDictionaries(SpellChecker spellChecker) {
//      addDictionary( spellChecker, "english.0" );
        addDictionary( spellChecker, "eng_com.dic" );
        addDictionary( spellChecker, "center.dic" );
        addDictionary( spellChecker, "centre.dic" );
        addDictionary( spellChecker, "color.dic" );
        addDictionary( spellChecker, "colour.dic" );
        addDictionary( spellChecker, "ise.dic" );
        addDictionary( spellChecker, "ize.dic" );
        addDictionary( spellChecker, "labeled.dic" );
        addDictionary( spellChecker, "labelled.dic" );
        addDictionary( spellChecker, "yse.dic" );
        addDictionary( spellChecker, "yze.dic" );
    }
    
    private static void addDictionary( SpellChecker spellChecker, String fileName ) {
        InputStream is = Activator.class.getResourceAsStream( "/dict/"+fileName );
        SpellDictionary dict = null;
        try {
            dict = new SpellDictionaryHashMap( new InputStreamReader(is) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        spellChecker.addDictionary( dict );
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
