package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Structure;


/**
 * A GEDCOM GEDC record.
 * <pre>
 * ...
 *  1 GEDC
 *    2 VERS <VERSION_NUMBER>
 *    2 FORM <GEDCOM_FORM>
 * </pre>
 */
public class GEDC {

    private final String versionNumber;
    private final String gedcomForm;

    public GEDC(Structure structure) {
        versionNumber = structure.getNestedData("VERS","<unknown>");
        gedcomForm = structure.getNestedData("FORM","<unknown>");
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public String getGedcomForm() {
        return gedcomForm;
    }


    public String toString() {
        StringBuffer buf = new StringBuffer("[GEDC");
        buf.append(" version=").append(null != versionNumber ? versionNumber : "");
        buf.append(" form=").append(null != gedcomForm ? gedcomForm : "");
        buf.append("]");
        return buf.toString();
    }
}
