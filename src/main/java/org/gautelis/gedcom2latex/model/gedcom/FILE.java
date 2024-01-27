package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A GEDCOM FILE record.
 * <p/>
 * <pre>
 * ...
 *    2 FILE <MULTIMEDIA_FILE_REFN>
 *      3 FORM <MULTIMEDIA_FORMAT>
 *        4 TYPE <SOURCE_MEDIA_TYPE>
 *      3 TITL <DESCRIPTIVE_TITLE>
 * </pre>
 */
public class FILE implements Record {

    private final String reference;
    private final String title;
    private final Collection<FORM> formats = new ArrayList<>();

    public FILE(Structure structure) {
        Optional<String> _name = structure.getData();
        reference = _name.orElse("<unknown>");

        Optional<Structure> _title = structure.getNestedStructure("TITL");
        title = _title.map(value -> value.getData("")).orElse(null);

        Collection<Structure> _formats = structure.getNestedStructures("FORM");
        _formats.stream().map(FORM::new).forEach(formats::add);
    }

    public String getReference() {
        return reference;
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Collection<FORM> FORM() {
        return formats;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[FILE");
        buf.append(" reference=").append(null != reference ? reference : "");
        buf.append(" title=").append(null != title ? title : "").append(" ");
        FORM().forEach(buf::append);
        buf.append("]");
        return buf.toString();
    }
}
