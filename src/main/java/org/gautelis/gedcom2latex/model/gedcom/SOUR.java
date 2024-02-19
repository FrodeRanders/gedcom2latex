package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A GEDCOM SOUR record.
 * <p/>
 * <pre>
 * ...
 *  1 SOUR <APPROVED_SYSTEM_ID>
 *    2 VERS <VERSION_NUMBER>
 *    2 NAME <NAME_OF_PRODUCT>
 *    2 CORP <NAME_OF_BUSINESS>
 *      3 <<ADDRESS_STRUCTURE>>
 *    2 DATA <NAME_OF_SOURCE_DATA>
 *      3 DATE <PUBLICATION_DATE>
 *      3 COPR <COPYRIGHT_SOURCE_DATA>
 *        4 [CONT|CONC]<COPYRIGHT_SOURCE_DATA>
 * </pre>
 */
public class SOUR implements Record {
    private final String sourceId;
    private final Collection<DATA> data = new ArrayList<>();

    public SOUR(Structure structure) {
        Optional<String> _sourceId = structure.getData();
        sourceId = _sourceId.orElse("<unknown>");

        Collection<Structure> _data = structure.getNestedStructures("DATA");
        _data.stream().map(DATA::new).forEach(data::add);
    }

    public Collection<DATA> DATA() {
        return data;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[SOUR (source)");
        buf.append(" id=").append(null != sourceId ? sourceId : "").append(" ");
        DATA().forEach(buf::append);
        buf.append("]");
        return buf.toString();
    }
}
