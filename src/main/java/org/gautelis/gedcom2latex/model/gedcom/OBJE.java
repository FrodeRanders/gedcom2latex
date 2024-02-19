package org.gautelis.gedcom2latex.model.gedcom;

import org.gautelis.gedcom2latex.model.Record;
import org.gautelis.gedcom2latex.model.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A GEDCOM OBJE record.
 * <p/>
 * <pre>
 * ...
 *  1 @XREF:OBJE@ OBJE
 *    2 FILE <MULTIMEDIA_FILE_REFN>
 *      3 FORM <MULTIMEDIA_FORMAT>
 *        4 TYPE <SOURCE_MEDIA_TYPE>
 *      3 TITL <DESCRIPTIVE_TITLE>
 *    2 REFN <USER_REFERENCE_NUMBER>
 *      3 TYPE <USER_REFERENCE_TYPE>
 *    3 RIN <AUTOMATED_RECORD_ID>
 *    2 <<NOTE_STRUCTURE>>
 *    2 <<SOURCE_CITATION>>
 *    2 CHAN
 *      3 DATE <CHANGE_DATE>
 *        4 TIME <TIME_VALUE>
 *      3 <<NOTE_STRUCTURE>>
 * </pre>
 * where NOTE_STRUCTURE is one of
 * <pre>
 *    n NOTE @<XREF:NOTE>@
 * |
 *    n NOTE [<SUBMITTER_TEXT> | <NULL>]
 *      n+1 [CONC|CONT] <SUBMITTER_TEXT>
 * </pre>
 * where SOURCE_CITATION is one of
 * <pre>
 *    2 SOUR @<XREF:SOUR>@    // pointer to source record (preferred)
 *      3 PAGE <WHERE_WITHIN_SOURCE>
 *      3 EVEN <EVENT_TYPE_CITED_FROM>
 *        4 ROLE <ROLE_IN_EVENT>
 *      3 DATA
 *        4 DATE <ENTRY_RECORDING_DATE>
 *        4 TEXT <TEXT_FROM_SOURCE>
 *          5 [CONC|CONT] <TEXT_FROM_SOURCE>
 *      3 <<MULTIMEDIA_LINK>>
 *      3 <<NOTE_STRUCTURE>>
 *      3 QUAY <CERTAINTY_ASSESSMENT>
 * |
 *    2 SOUR <SOURCE_DESCRIPTION>    // Systems not using source records
 *      3 [CONC|CONT] <SOURCE_DESCRIPTION>
 *      3 TEXT <TEXT_FROM_SOURCE>
 *        4 [CONC|CONT] <TEXT_FROM_SOURCE>
 *      3 <<MULTIMEDIA_LINK>>
 *      3 <<NOTE_STRUCTURE>>
 *      3 QUAY <CERTAINTY_ASSESSMENT>
 * </pre>
 * where MULTIMEDIA_LINK is one of
 * <pre>
 *      3 OBJE @<XREF:OBJE>@
 * |
 *      3 OBJE
 *        4 FILE <MULTIMEDIA_FILE_REFN>
 *          5 FORM <MULTIMEDIA_FORMAT>
 *            6 MEDI <SOURCE_MEDIA_TYPE>
 *        4 TITL <DESCRIPTIVE_TITLE>
 * </pre>
 * Other variants
 * <pre>
 *      3 OBJE
 *        4 FILE
 *        4 FORM <MULTIMEDIA_FORMAT>
 *          5 MEDI <SOURCE_MEDIA_TYPE>
 * </pre>
 * and (MyHeritage)
 * <pre>
 * 1 OBJE
 *   2 FORM jpg
 *   2 FILE https://sites-cf.mhcache.com/unique.jpg
 *   2 _FILESIZE 219362
 *   2 _PRIM Y
 *   2 _CUTOUT Y
 *   2 _PARENTRIN MH:P500094
 *   2 _PERSONALPHOTO Y
 *   2 _PHOTO_RIN MH:P500096
 * </pre>
 */
public class OBJE implements Record {
    private final String id;
    private final Collection<FILE> files = new ArrayList<>();

    public OBJE(Structure structure) {
        Optional<String> _id = structure.getPointer();
        id = _id.orElse(null);

        Collection<Structure> _files = structure.getNestedStructures("FILE");
        _files.stream().map(FILE::new).forEach(files::add);
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Collection<FILE> FILE() {
        return files;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[OBJE (multimedia record)");
        buf.append(" id=").append(null != id ? id : "").append(" ");
        FILE().forEach(buf::append);
        buf.append("]");
        return buf.toString();
    }
}
