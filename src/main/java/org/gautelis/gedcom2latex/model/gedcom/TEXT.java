package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A GEDCOM DATA record.
 * <p/>
 * <pre>
 * ...
 *        4 TEXT <TEXT_FROM_SOURCE>
 *          5 [CONC|CONT] <TEXT_FROM_SOURCE>
 * </pre>
 *
 * <pre>
  *    3 TEXT abcd
 *      4 CONC efgh
 *      4 CONC ijkl.
 * </pre>
 */
public class TEXT implements Record {
    private final Collection<String> text = new ArrayList<>();

    public TEXT(Structure structure) {
        Optional<String> _text = structure.getData();
        _text.ifPresent(text::add);

        Collection<Structure> _nested = structure.getNestedStructures("CONC");
        _nested.stream().map(Structure::getData).forEach(_d -> _d.ifPresent(text::add));

        _nested = structure.getNestedStructures("CONT");
        _nested.stream().map(Structure::getData).forEach(_d -> _d.ifPresent(text::add));
    }

    public Collection<String> get() {
        return text;
    }

    public String getString() {
        StringBuilder buf = new StringBuilder();
        for (String s : text) {
            buf.append(s);
        }
        return Jsoup.parse(buf.toString()).text();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[TEXT (text) ");
        //get().forEach(buf::append);
        buf.append(getString());
        buf.append("]");
        return buf.toString();
    }
}
