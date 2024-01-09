package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.Optional;

/**
 * A GEDCOM HEAD record.
 * <p/>
 * <pre>
 * 0 HEAD
 *   1 SOUR <APPROVED_SYSTEM_ID>
 *     2 VERS <VERSION_NUMBER>
 *     2 NAME <NAME_OF_PRODUCT>
 *     2 CORP <NAME_OF_BUSINESS>
 *       3 <<ADDRESS_STRUCTURE>>
 *     2 DATA <NAME_OF_SOURCE_DATA>
 *       3 DATE <PUBLICATION_DATE>
 *       3 COPR <COPYRIGHT_SOURCE_DATA>
 *         4 [CONT|CONC]<COPYRIGHT_SOURCE_DATA>
 *   1 DEST <RECEIVING_SYSTEM_NAME>
 *   1 DATE <TRANSMISSION_DATE>
 *     2 TIME <TIME_VALUE>
 *   1 SUBM @<XREF:SUBM>@
 *   1 SUBN @<XREF:SUBN>@
 *   1 FILE <FILE_NAME>
 *   1 COPR <COPYRIGHT_GEDCOM_FILE>
 *   1 GEDC
 *     2 VERS <VERSION_NUMBER>
 *     2 FORM <GEDCOM_FORM>
 *   1 CHAR <CHARACTER_SET>
 *     2 VERS <VERSION_NUMBER>
 *   1 LANG <LANGUAGE_OF_TEXT>
 *   1 PLAC
 *     2 FORM <PLACE_HIERARCHY>
 *   1 NOTE <GEDCOM_CONTENT_DESCRIPTION>
 *     2 [CONC|CONT] <GEDCOM_CONTENT_DESCRIPTION>
 * </pre>
 */
public class HEAD implements Record {

    private final GEDC gedc;
    private final CHAR characterSet;

    public HEAD(Structure structure) {
        Optional<Structure> _gedc = structure.getNestedStructure("GEDC");
        gedc = _gedc.map(GEDC::new).orElse(null);

        Optional<Structure> _char = structure.getNestedStructure("CHAR");
        characterSet = _char.map(CHAR::new).orElse(null);


    }

    public Optional<GEDC> GEDC() {
        return Optional.ofNullable(gedc);
    }

    public Optional<CHAR> CHAR() {
        return Optional.ofNullable(characterSet);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[HEAD ");
        GEDC().ifPresent(buf::append);
        buf.append(" ");
        CHAR().ifPresent(buf::append);
        buf.append("]");
        return buf.toString();
    }
}
