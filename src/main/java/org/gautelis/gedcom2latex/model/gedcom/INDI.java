package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A GEDCOM INDI (Individual) record.
 * <p/>
 * <pre>
 * 0 @XREF:INDI@ INDI
 *   1 RESN <RESTRICTION_NOTICE>
 *   1 NAME <NAME_PERSONAL>
 *     2 TYPE <NAME_TYPE>
 *     2 NPFX <NAME_PIECE_PREFIX>
 *     2 GIVN <NAME_PIECE_GIVEN>
 *     2 NICK <NAME_PIECE_NICKNAME>
 *     2 SPFX <NAME_PIECE_SURNAME_PREFIX
 *     2 SURN <NAME_PIECE_SURNAME>
 *     2 NSFX <NAME_PIECE_SUFFIX>
 *     2 <<NOTE_STRUCTURE>>
 *     2 <<SOURCE_CITATION>>
 *     2 FONE <NAME_PHONETIC_VARIATION>
 *       3 TYPE <PHONETIC_TYPE>
 *       3 <<PERSONAL_NAME_PIECES>>
 *     2 ROMN <NAME_ROMANIZED_VARIATION>
 *       3 TYPE <ROMANIZED_TYPE>
 *       3 <<PERSONAL_NAME_PIECES>>
 *   1 SEX <SEX_VALUE>
 *   1 <<INDIVIDUAL_EVENT_STRUCTURE>>    // BIRT, DEAT, BURI, ADOP, BAPM, CENS, ...
 *   1 <<INDIVIDUAL_ATTRIBUTE_STRUCTURE>>
 *   1 <<LDS_INDIVIDUAL_ORDINANCE>>
 *   1 FAMC @<XREF:FAM>@                 // Child to family link
 *     2 PEDI <PEDIGREE_LINKAGE_TYPE>
 *     2 STAT <CHILD_LINKAGE_STATUS>
 *     2 NOTE @<XREF:NOTE>@
 *   1 FAMS @<XREF:FAM>@                 // Spouse to family link
 *     2 NOTE @<XREF:NOTE>@
 *   1 SUBM @<XREF:SUBM>@
 *   1 <<ASSOCIATION_STRUCTURE>>
 *   1 ALIA @<XREF:INDI>@
 *   1 ANCI @<XREF:SUBM>@
 *   1 DESI @<XREF:SUBM>@
 *   1 RFN <PERMANENT_RECORD_FILE_NUMBER>
 *   1 AFN <ANCESTRAL_FILE_NUMBER>
 *   1 REFN <USER_REFERENCE_NUMBER>
 *     2 TYPE <USER_REFERENCE_TYPE>
 *   1 RIN <AUTOMATED_RECORD_ID>
 *   1 <<CHANGE_DATE>>
 *   1 <<NOTE_STRUCTURE>>
 *   1 <<SOURCE_CITATION>>
 *   1 <<MULTIMEDIA_LINK>>
 * </pre>
 */
public class INDI implements Record {

    private final String id;
    private final Collection<NAME> names = new ArrayList<>();

    public INDI(Structure structure) {
        Optional<String> _id = structure.getID();
        id = _id.orElse("<unknown>");

        Collection<Structure> _names = structure.getNestedStructures("NAME");
        _names.stream().map(NAME::new).forEach(names::add);
    }

    public Collection<NAME> NAME() {
        return names;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[INDI");
        buf.append(" id=").append(null != id ? id : "");
        buf.append(" ");
        NAME().forEach(buf::append);
        buf.append("]");
        return buf.toString();
    }
}
