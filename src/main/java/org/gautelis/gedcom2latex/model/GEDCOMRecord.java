package org.gautelis.gedcom2latex.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class GEDCOMRecord {
    private static final Logger log = LoggerFactory.getLogger(GEDCOMRecord.class);

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
        log.debug("At record " + this + ", adding record " + record);
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
        addDetails(buf);
        buf.append("]");
        return buf.toString();
    }

    public void deepToString(StringBuffer buf) {
        buf.append("[");
        addDetails(buf);
        if (!orderedSubRecords.isEmpty()) {
            buf.append(" {").append(orderedSubRecords.size() + ":");
            for (GEDCOMRecord record : orderedSubRecords) {
                record.deepToString(buf);
            }
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
