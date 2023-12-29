package org.gautelis.gedcom2latex;

import org.gautelis.gedcom2latex.model.Structure;
import org.gautelis.gedcom2latex.model.gedcom.HEAD;
import org.gautelis.gedcom2latex.model.gedcom.INDI;
import org.gautelis.gedcom2latex.model.gedcom.FAM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class LineHandler {
    private static final Logger log = LoggerFactory.getLogger(LineHandler.class);

    private final Map</* id */ String, Structure> index;

    private final Map</* tag */ String, Collection<Structure>> structures;

    private final Stack<Structure> stack = new Stack<>();

    public LineHandler(Map<String, Structure> index, Map</* tag/type */ String, Collection<Structure>> structures) {
        this.index = index;
        this.structures = structures;
    }

    public Map<String, Structure> getIndex() {
        return index;
    }

    private String indent(long level) {
        StringBuilder buf = new StringBuilder();
        for (long i=0L; i < level; i++) {
            buf.append("  ");
        }
        return buf.toString();
    }

    public void accept(long level, String pointer, String tag, String data){

        Structure structure = new Structure(level, pointer, tag, data);
        log.debug("Read: {}{}", indent(level), structure);

        // Keep track of all top-level structures (individuals, families, ...)
        if (level == 0L) {
            // Store indexed on type/tag (includes HEAD, TRLR)
            Collection<Structure> taggedStructures = structures.computeIfAbsent(structure.getTag(), k -> new ArrayList<>());
            taggedStructures.add(structure);

            // Store indexed on ID (excludes HEAD, TRLR)
            Optional<String> id = structure.getID();
            id.ifPresent(s -> index.put(s, structure));
        }

        // Keep track of "current" structure (using a stack)
        if (stack.isEmpty()) {
            assert level == 0L;
        }
        else {
            Structure current = stack.peek();
            if (structure.getLevel() <= current.getLevel()) {
                do {
                    if (stack.isEmpty())
                        break;

                    current = stack.pop();
                    log.trace("Popping: {}", current);
                } while (current.getLevel() > structure.getLevel());
            }

            if (!stack.isEmpty()) {
                current = stack.peek();
                current.addStructure(structure);
            }
        }

        stack.push(structure);
        log.trace("Pushing: {}", structure);

    }

    public void accept(String data) {
        Structure current = stack.peek();
        if (null == current) {
            log.error("Stack is empty: No current structure when appending data: " + data);
        } else {
            log.debug("Read: {}{}", indent(current.getLevel()), data);
            current.appendData(data);
        }
    }
}
