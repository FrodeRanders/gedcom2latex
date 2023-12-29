package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Structure;

import java.util.Optional;

/**
 * A GEDCOM FAMS (Spouse to family link) record.
 * <p/>
 * <pre>
 * ...
 *   1 FAMS @<XREF:FAM>@
 *     2 <<NOTE_STRUCTURE>>
 * </pre>
 */
public class FAMS implements Record {

    private final String familyId;

    public FAMS(Structure structure) {
        Optional<String> _familyId = structure.getData();
        familyId = _familyId.orElse("<unknown>");
    }

    public String getFamilyId() {
        return familyId;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[FAMS (spouse-to-family)");
        buf.append(" id=").append(null != familyId ? familyId : "");
          buf.append("]");
        return buf.toString();
    }
}
