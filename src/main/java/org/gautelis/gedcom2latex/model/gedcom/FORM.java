package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.Optional;

/**
 * A GEDCOM FORM record.
 * <p/>
 * <pre>
 * ...
 *      3 FORM <MULTIMEDIA_FORMAT>
 *        4 TYPE <SOURCE_MEDIA_TYPE>
 * </pre>
 */
public class FORM implements Record {

    private final String format;
    private final String type;

    public FORM(Structure structure) {
        Optional<String> _name = structure.getData();
        format = _name.orElse("<unknown>");

        Optional<Structure> _type = structure.getNestedStructure("TYPE");
        type = _type.map(value -> value.getData("")).orElse(null);
    }

    public String getFormat() {
        return format;
    }

    public Optional<String> getType() {
        return Optional.ofNullable(type);
    }


    public String toString() {
        StringBuffer buf = new StringBuffer("[FORM (format)");
        buf.append(" format=").append(null != format ? format : "");
        buf.append(" type=").append(null != type ? type : "");
        buf.append("]");
        return buf.toString();
    }
}
