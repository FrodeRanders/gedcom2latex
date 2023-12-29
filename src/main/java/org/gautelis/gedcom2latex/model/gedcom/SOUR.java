package org.gautelis.gedcom2latex.model.gedcom;

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
    public String toString() {
        StringBuffer buf = new StringBuffer("[SOUR (source) ...");
        buf.append("]");
        return buf.toString();
    }
}
