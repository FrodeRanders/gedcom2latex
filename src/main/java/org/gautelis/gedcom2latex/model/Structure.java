package org.gautelis.gedcom2latex.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A structure that captures hierarchical GEDCOM records.
 * <p/>
 * Example:
 * <pre>
 * 0 HEAD
 *  1 SOUR <APPROVED_SYSTEM_ID>
 *    2 VERS <VERSION_NUMBER>
 *    2 NAME <NAME_OF_PRODUCT>
 *    2 CORP <NAME_OF_BUSINESS>
 *      3 <<ADDRESS_STRUCTURE>>
 *    2 DATA <NAME_OF_SOURCE_DATA>
 *      3 DATE <PUBLICATION_DATE>
 *      3 COPR <COPYRIGHT_SOURCE_DATA>
 *        4 [CONT|CONC]<COPYRIGHT_SOURCE_DATA>
 *  1 DEST <RECEIVING_SYSTEM_NAME>
 *  1 DATE <TRANSMISSION_DATE>
 *    2 TIME <TIME_VALUE>
 *  1 SUBM @<XREF:SUBM>@
 *  1 SUBN @<XREF:SUBN>@
 *  1 FILE <FILE_NAME>
 *  1 COPR <COPYRIGHT_GEDCOM_FILE>
 *  1 GEDC
 *    2 VERS <VERSION_NUMBER>
 *    2 FORM <GEDCOM_FORM>
 *  1 CHAR <CHARACTER_SET>
 *    2 VERS <VERSION_NUMBER>
 *  1 LANG <LANGUAGE_OF_TEXT>
 *  1 PLAC
 *    2 FORM <PLACE_HIERARCHY>
 *  1 NOTE <GEDCOM_CONTENT_DESCRIPTION>
 *    2 [CONC|CONT] <GEDCOM_CONTENT_DESCRIPTION>
 * </pre>
 * In this case we will end up with a "HEAD" structure, which contains 12 sub-structures
 * corresponding to the level 1 records.
 */
public class Structure {
    private static final Logger log = LoggerFactory.getLogger(Structure.class);

    final long level;

    final String pointer;
    final String tag;
    final Map<String, Collection<Structure>> structures = new HashMap<>();

    final StringBuffer data = new StringBuffer();

    public Structure(long level, String pointer, String tag) {
        this.level = level;
        this.pointer = pointer;
        this.tag = tag;
    }

    public Structure(long level, String pointer, String tag, String data) {
        this.level = level;
        this.pointer = pointer;
        this.tag = tag;
        if (null != data) {
            this.data.append(data.trim());
        }
    }

    public void addStructure(Structure structure) {
        log.trace("At {}, adding {}", this, structure);
        Collection<Structure> taggedStructures = structures.computeIfAbsent(structure.tag, k -> new ArrayList<>());
        taggedStructures.add(structure);
    }

    public void appendData(String data) {
        if (null != data) {
            this.data.append("\n").append(data.trim());
        }
    }

    public Map<String, Collection<Structure>> getStructures() {
        return structures;
    }

    public Collection<Structure> getStructures(String tag) {
        return structures.computeIfAbsent(tag, k -> new ArrayList<>());
    }

    public Optional<Structure> getNestedStructure(String tag) {
        Collection<Structure> tags = getStructures(tag);
        return tags.stream().findFirst();
    }

    public Collection<Structure> getNestedStructures(String tag) {
        return getStructures(tag);
    }

    public Optional<String> getNestedData(String tag) {
        Optional<Structure> structure = getNestedStructure(tag);
        return structure.flatMap(Structure::getData);
    }

    public String getNestedData(String tag, String defaultValue) {
        Optional<String> data = getNestedData(tag);
        return data.orElse(defaultValue);
    }

    public Optional<String> getData() {
        if (data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(data.toString());
    }

    public String getData(String defaultValue) {
        if (data.isEmpty()) {
            return defaultValue;
        }
        return data.toString();
    }


    public long getLevel() {
        return level;
    }

    public Optional<String> getID() {
        return Optional.ofNullable(pointer);
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        addDetails(buf);
        buf.append("]");
        return buf.toString();
    }

    public void deepToString(StringBuffer buf) {
        buf.append("[");
        addDetails(buf);
        if (!structures.isEmpty()) {
            buf.append(" {");
            structures.values().forEach(taggedRecords -> taggedRecords.forEach(record -> record.deepToString(buf)));
            buf.append("}");
        }
        buf.append("]");
    }

    private void addDetails(StringBuffer buf) {
        buf.append(level);

        if (null != pointer && !pointer.isEmpty()) {
            buf.append(" @").append(pointer).append("@");
        }

        buf.append(" ").append(tag);

        if (!data.isEmpty()) {
            buf.append(" ").append(data.toString().trim());
        }
    }
}
