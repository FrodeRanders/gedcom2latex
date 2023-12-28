package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Structure;

import java.util.Optional;

/**
 * A GEDCOM NAME record.
 * <p/>
 * <pre>
 * ...
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
 * </pre>
 */
public class NAME implements Record {

    private final String name;

    private final String givenName;
    private final String surname;

    public NAME(Structure structure) {
        Optional<String> _name = structure.getData();
        name = _name.orElse("<unknown>");

        Optional<Structure> _givn = structure.getNestedStructure("GIVN");
        givenName = _givn.map(value -> value.getData("")).orElse(null);

        Optional<Structure> _surn = structure.getNestedStructure("SURN");
        surname = _surn.map(value -> value.getData("")).orElse(null);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getGivenName() {
        return Optional.ofNullable(givenName);
    }

    public Optional<String> getSurname() {
        return Optional.ofNullable(surname);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[NAME");
        buf.append(" name=").append(null != name ? name : "");
        buf.append(" given-name=").append(null != givenName ? givenName : "");
        buf.append(" surname=").append(null != surname ? surname : "");
        //buf.append(" ");
        //GEDC().ifPresent(buf::append);
         buf.append("]");
        return buf.toString();
    }
}
