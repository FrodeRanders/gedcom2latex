package org.gautelis.gedcom2latex;

import org.gautelis.gedcom2latex.model.Structure;
import org.gautelis.gedcom2latex.model.gedcom.HEAD;
import org.gautelis.gedcom2latex.model.gedcom.INDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class LineHandler {
    private static final Logger log = LoggerFactory.getLogger(LineHandler.class);

    private final Map<String, Structure> index = new HashMap<>();

    final Map</* tag/type */ String, Collection<Structure>> structures = new HashMap<>();

    private final Stack<Structure> stack = new Stack<>();

    public LineHandler() {
    }

    public Map<String, Structure> getIndex() {
        return index;
    }

    public Optional<HEAD> getHEAD() {
        Collection<Structure> heads = structures.get("HEAD");
        Optional<Structure> head = heads.stream().findFirst();
        return head.map(HEAD::new);
    }

    public Collection<INDI> getINDI() {
        Collection<Structure> individuals = structures.get("INDI");
        return individuals.stream().map(INDI::new).collect(Collectors.toList());
    }

    public void accept(long level, String pointer, String tag, String data){

        Structure structure = new Structure(level, pointer, tag, data);
        log.trace("Read: {}", structure);

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
            current.appendData(data);
        }
    }
}
