package org.gautelis.gedcom2latex;

import org.gautelis.gedcom2latex.model.GEDCOMRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;


public class RecordHandler {
    private static final Logger log = LoggerFactory.getLogger(RecordHandler.class);

    private final Map<String, GEDCOMRecord> records = new HashMap<>();
    private final Stack<GEDCOMRecord> stack = new Stack<>();

    public RecordHandler() {
    }

    public Map<String, GEDCOMRecord> getRecords() {
        return records;
    }

    public void acceptRecord(long level, String pointer, String tag, String data){

        GEDCOMRecord record = new GEDCOMRecord(level, pointer, tag, data);
        log.trace("Read: {}", record);

        // Top-level records (individuals, families, ...)
        if (level == 0L) {

            Optional<String> id = record.getID();
            if (id.isPresent()) {
                records.put(id.get(), record);
            } else {
                if (records.containsKey(tag)) {
                    log.warn("Multiple top-level records of type " + tag + ": " + record);
                }
                records.put(tag, record);
            }
        }

        // Stack
        if (stack.isEmpty()) {
            // This should be a top-level record
            assert level == 0L;
        }
        else {
            GEDCOMRecord currentRecord = stack.peek();
            if (null != currentRecord) {
                currentRecord.addRecord(record);

                if (record.getLevel() <= currentRecord.getLevel()) {
                    do {
                        if (stack.isEmpty())
                            break;

                        currentRecord = stack.pop();
                        log.trace("Poping: {}", currentRecord);
                    } while (currentRecord.getLevel() > record.getLevel());
                }
            }
        }

        stack.push(record);
        log.trace("Pushing: {}", record);

    }

    public void acceptData(String data) {
        GEDCOMRecord currentRecord = stack.peek();
        if (null == currentRecord) {
            log.error("Stack is empty: No current record when appending data: " + data);
        } else {
            currentRecord.appendData(data);
        }
    }
}
