package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.*;

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
 * where INDIVIDUAL_EVENT_STRUCTURE is one of
 * <pre>
 *   1 [ BIRT | CHR ] [Y|<NULL>]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 *     2 FAMC @<XREF:FAM>@
 * |
 *   1 DEAT [Y|<NULL>]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 *   1 [BURI|CREM]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 *   1 ADOP
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 *     2 FAMC @<XREF:FAM>@
 *       3 ADOP <ADOPTED_BY_WHICH_PARENT>
 * |
 *   1 [BAPM|BARM|BASM|BLES]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 *   1 [CHRA|CONF|FCOM|ORDN]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 *   1 [NATU|EMIG|IMMI]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 *   1 [ CENS | PROB | WILL]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 *   1 [ GRAD | RETI ]
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 *   1 EVEN
 *     2 <<INDIVIDUAL_EVENT_DETAIL>>
 * </pre>
 * where INDIVIDUAL_EVENT_DETAIL is
 * <pre>
 *     2 <<EVENT_DETAIL>>
 *     2 AGE <AGE_AT_EVENT>
 * </pre>
 * where EVENT_DETAIL is
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
public class INDI implements Record {


    private final String id;
    private final Collection<NAME> names = new ArrayList<>();
    private final SEX sex;
    private final Collection<BIRT> births = new ArrayList<>();
    private final Collection<CHR> baptisms = new ArrayList<>();
    private final Collection<DEAT> deaths = new ArrayList<>();
    private final Collection<BURI> burials = new ArrayList<>();
    private final Collection<FAMC> childToFamilyLinks = new ArrayList<>(); // Child to family links
    private final Collection<FAMS> spouseToFamilyLinks = new ArrayList<>(); // Spouse to family links



    public INDI(Structure structure) {
        Optional<String> _id = structure.getID();
        id = _id.orElse("<unknown>");

        sex = SEX.from(structure.getNestedData("SEX", "U"));

        Collection<Structure> _births = structure.getNestedStructures("BIRT");
        _births.stream().map(BIRT::new).forEach(births::add);

        Collection<Structure> _baptisms = structure.getNestedStructures("CHR");
        _baptisms.stream().map(CHR::new).forEach(baptisms::add);

        Collection<Structure> _deaths = structure.getNestedStructures("DEAT");
        _deaths.stream().map(DEAT::new).forEach(deaths::add);

        Collection<Structure> _burials = structure.getNestedStructures("BURI");
        _burials.stream().map(BURI::new).forEach(burials::add);

        Collection<Structure> _names = structure.getNestedStructures("NAME");
        _names.stream().map(NAME::new).forEach(names::add);

        Collection<Structure> _childToFamilyLinks = structure.getNestedStructures("FAMC");
        _childToFamilyLinks.stream().map(FAMC::new).forEach(childToFamilyLinks::add);

        Collection<Structure> _spouseToFamilyLinks = structure.getNestedStructures("FAMS");
        _spouseToFamilyLinks.stream().map(FAMS::new).forEach(spouseToFamilyLinks::add);
    }

    public String getId() {
        return id;
    }

    public SEX getSex() {
        return sex;
    }

    public Collection<NAME> NAME() {
        return names;
    }

    public Collection<BIRT> BIRT() {
        return births;
    }

    public Collection<CHR> CHR() {
        return baptisms;
    }

    public Collection<DEAT> DEAT() {
        return deaths;
    }

    public Collection<BURI> BURI() {
        return burials;
    }

    public Collection<FAMC> FAMC() {
        return childToFamilyLinks;
    }

    public Collection<FAMS> FAMS() {
        return spouseToFamilyLinks;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[INDI (individual)");
        buf.append(" id=").append(null != id ? id : "");
        buf.append(" sex=").append(sex.name()).append(" ");
        NAME().forEach(buf::append);
        BIRT().forEach(buf::append);
        CHR().forEach(buf::append);
        DEAT().forEach(buf::append);
        BURI().forEach(buf::append);
        FAMC().forEach(buf::append);
        FAMS().forEach(buf::append);
        buf.append("]");
        return buf.toString();
    }
}
