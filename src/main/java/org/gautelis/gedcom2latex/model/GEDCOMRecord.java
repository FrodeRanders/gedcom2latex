package org.gautelis.gedcom2latex.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class GEDCOMRecord {

    final long level;

    final String pointer;
    final String tag;
    final Collection<GEDCOMRecord> orderedSubRecords = new ArrayList<>();

    final StringBuffer data = new StringBuffer();

    public GEDCOMRecord(long level, String pointer, String tag) {
        this.level = level;
        this.pointer = pointer;
        this.tag = tag;
    }

    public GEDCOMRecord(long level, String pointer, String tag, String data) {
        this.level = level;
        this.pointer = pointer;
        this.tag = tag;
        this.data.append(data);
    }

    public void addRecord(GEDCOMRecord record) {
        orderedSubRecords.add(record);
    }

    public Collection<GEDCOMRecord> getOrderedSubRecords() {
        return orderedSubRecords;
    }

    public void appendData(String data) {
        this.data.append("\n").append(data);
    }

    public Optional<String> getData() {
        if (data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(data.toString());
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
        return toString(buf);
    }

    public String toString(StringBuffer buf) {
        buf.append(level);

        // Optional pointer
        if (null != pointer && !pointer.isEmpty()) {
            buf.append(" @").append(pointer).append("@");
        }

        buf.append(" ").append(tag);

        if (!data.isEmpty()) {
            buf.append(" ").append(data.toString().trim());
        }
        buf.append(" : {");
        buf.append(orderedSubRecords.size() + " records");
        //for (GEDCOMRecord record : orderedSubRecords) {
        //    buf.append(record.toString(buf));
        //}
        buf.append("}]");

        return buf.toString();
    }

}
