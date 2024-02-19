package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A GEDCOM DATA record.
 * <p/>
 * <pre>
 * ...
 *      3 DATA
 *        4 DATE <ENTRY_RECORDING_DATE>
 *        4 TEXT <TEXT_FROM_SOURCE>
 *          5 [CONC|CONT] <TEXT_FROM_SOURCE>
 * </pre>
 *
 * <pre>
 *  2 DATA
 *    3 TEXT abcd
 *      4 CONC efgh
 *      4 CONC ijkl.
 * </pre>
 */
public class DATA implements Record {
    private final Collection<TEXT> text = new ArrayList<>();

    public DATA(Structure structure) {
        Collection<Structure> _text = structure.getNestedStructures("TEXT");
        _text.stream().map(TEXT::new).forEach(text::add);
    }

    public Collection<TEXT> TEXT() {
        return text;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[DATA (data) ");
        TEXT().forEach(buf::append);
        buf.append("]");
        return buf.toString();
    }
}
