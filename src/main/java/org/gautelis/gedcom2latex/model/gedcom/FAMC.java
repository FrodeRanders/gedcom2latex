package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.Optional;

/**
 * A GEDCOM FAMC (Child to family link) record.
 * <p/>
 * <pre>
 * ...
 *   1 FAMC @<XREF:FAM>@
 *     2 PEDI <PEDIGREE_LINKAGE_TYPE>
 *     2 STAT <CHILD_LINKAGE_STATUS>
 *     2 NOTE @<XREF:NOTE>@
 * </pre>
 */
public class FAMC implements Record {

    private final String familyId;

    public FAMC(Structure structure) {
        Optional<String> _familyId = structure.getData();
        familyId = _familyId.orElse("<unknown>");
    }

    public String getFamilyId() {
        return familyId;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[FAMC (child-to-family)");
        buf.append(" id=").append(null != familyId ? familyId : "");
          buf.append("]");
        return buf.toString();
    }
}
