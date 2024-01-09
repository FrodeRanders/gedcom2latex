package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A GEDCOM FAM (Family) record.
 * <p/>
 * <pre>
 * 0 @<XREF:FAM>@ FAM
 *   1 RESN <RESTRICTION_NOTICE>
 *   1 <<FAMILY_EVENT_STRUCTURE>>
 *   1 HUSB @<XREF:INDI>@
 *   1 WIFE @<XREF:INDI>@
 *   1 CHIL @<XREF:INDI>@
 *   1 NCHI <COUNT_OF_CHILDREN>
 *   1 SUBM @<XREF:SUBM>@
 *   1 <<LDS_SPOUSE_SEALING>>
 *   1 REFN <USER_REFERENCE_NUMBER>
 *     2 TYPE <USER_REFERENCE_TYPE>
 *   1 RIN <AUTOMATED_RECORD_ID>
 *   1 <<CHANGE_DATE>>
 *   1 <<NOTE_STRUCTURE>>
 *   1 <<SOURCE_CITATION>>
 *   1 <<MULTIMEDIA_LINK>>
 * </pre>
 * with FAMILY_EVENT_STRUCTURE being one of
 * <pre>
 *   1 [ANUL|CENS|DIV|DIVF]
 *     2 <<FAMILY_EVENT_DETAIL>>
 * |
 *   1 [ENGA|MARB|MARC]
 *     2 <<FAMILY_EVENT_DETAIL>>
 * |
 *   1 MARR [Y|<NULL>]
 *     2 <<FAMILY_EVENT_DETAIL>>
 * |
 *   1 [MARL|MARS]
 *     2 <<FAMILY_EVENT_DETAIL>>
 * |
 *   1 RESI
 *     2 <<FAMILY_EVENT_DETAIL>>
 * |
 *   1 EVEN [<EVENT_DESCRIPTOR> | <NULL>]
 *     2 <<FAMILY_EVENT_DETAIL>>
 * </pre>
 * with FAMILY_EVENT_DETAIL being
 * <pre>
 *     2 HUSB
 *       3 AGE <AGE_AT_EVENT>
 *     2 WIFE
 *       3 AGE <AGE_AT_EVENT>
 *     2 <<EVENT_DETAIL>>
 * </pre>
 * with EVENT_DETAIL being
 * <pre>
 *     2 TYPE <EVENT_OR_FACT_CLASSIFICATION>
 *     2 DATE <DATE_VALUE>
 *     2 <<PLACE_STRUCTURE>>
 *     2 <<ADDRESS_STRUCTURE>>
 *     2 AGNC <RESPONSIBLE_AGENCY>
 *     2 RELI <RELIGIOUS_AFFILIATION>
 *     2 CAUS <CAUSE_OF_EVENT>
 *     2 RESN <RESTRICTION_NOTICE>
 *     2 <<NOTE_STRUCTURE>>
 *     2 <<SOURCE_CITATION>>
 *     2 <<MULTIMEDIA_LINK>>
 * </pre>
 */
public class FAM implements Record {

    private final String id;
    private final String husbandId;
    private final String wifeId;
    private final Collection<String> childrenId = new ArrayList<>();

    public FAM(Structure structure) {
        Optional<String> _id = structure.getID();
        id = _id.orElse("<unknown>");

        husbandId = structure.getNestedData("HUSB", null);
        wifeId = structure.getNestedData("WIFE", null);

        Collection<Structure> _children = structure.getNestedStructures("CHIL");
        for (Structure _child : _children) {
            Optional<String> childId = _child.getData();
            childId.ifPresent(childrenId::add);
        }
    }

    public String getId() {
        return id;
    }

    public Optional<String> getHusbandId() {
        return Optional.ofNullable(husbandId);
    }

    public Optional<String> getWifeId() {
        return Optional.ofNullable(wifeId);
    }

    public Collection<String> getChildrenId() {
        return childrenId;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder("[FAM (family)");
        buf.append(" id=").append(null != id ? id : "");
        buf.append(" husband=").append(husbandId);
        buf.append(" wife=").append(wifeId);
        for (String childId : childrenId) {
            buf.append(" child=").append(childId);
        }
        buf.append("]");
        return buf.toString();
    }
}
