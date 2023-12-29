package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Structure;

import java.util.Optional;

/**
 * A GEDCOM BURI (Buried) record.
 * <p/>
 * <pre>
 *   1 BURI [Y|<NULL>]
 *     2 TYPE <EVENT_OR_FACT_CLASSIFICATION>
 *     2 DATE <DATE_VALUE>
 *     2 PLAC <PLACE_NAME>
 *       3 FORM <PLACE_HIERARCHY>
 *       3 FONE <PLACE_PHONETIC_VARIATION>
 *         4 TYPE <PHONETIC_TYPE>
 *       3 ROMN <PLACE_ROMANIZED_VARIATION>
 *         4 TYPE <ROMANIZED_TYPE>
 *       3 MAP
 *         4 LATI <PLACE_LATITUDE>
 *         4 LONG <PLACE_LONGITUDE>
 *       3 <<NOTE_STRUCTURE>>
 *     2 <<ADDRESS_STRUCTURE>>
 *     2 AGNC <RESPONSIBLE_AGENCY>
 *     2 RELI <RELIGIOUS_AFFILIATION>
 *     2 CAUS <CAUSE_OF_EVENT>
 *     2 RESN <RESTRICTION_NOTICE>
 *     2 <<NOTE_STRUCTURE>>
 *     2 <<SOURCE_CITATION>>
 *     2 <<MULTIMEDIA_LINK>>
 *     2 AGE <AGE_AT_EVENT>
 *     2 FAMC @<XREF:FAM>@
  * </pre>
 */
public class BURI implements Record {

    private final String date;

    private final String place;


    public BURI(Structure structure) {
        date = structure.getNestedData("DATE", "<unknown>");
        place = structure.getNestedData("PLAC", "<unknown>");
     }

    public Optional<String> getDate() {
        return Optional.ofNullable(date);
    }

    public Optional<String> getPlace() {
        return Optional.ofNullable(place);
    }


    public String toString() {
        StringBuffer buf = new StringBuffer("[BURI (buried)");
        buf.append(" date=").append(null != date ? date : "");
        buf.append(" place=").append(null != place ? place : "");
         buf.append("]");
        return buf.toString();
    }
}
