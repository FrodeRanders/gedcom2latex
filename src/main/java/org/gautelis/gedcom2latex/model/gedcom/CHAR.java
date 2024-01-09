package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

/**
 * A GEDCOM CHAR record.
 * <pre>
 * ...
  *  1 CHAR <CHARACTER_SET>
 *    2 VERS <VERSION_NUMBER>
 * </pre>
 */
public class CHAR implements Record {

    private final String characterSet;
    private final String versionNumber;

    public CHAR(Structure structure) {
        characterSet = structure.getData("");
        versionNumber = structure.getNestedData("VERS", "");
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[CHAR");
        buf.append(" character-set=").append(characterSet);
        buf.append(" version-number=").append(versionNumber);
        buf.append("]");
        return buf.toString();
    }
}
