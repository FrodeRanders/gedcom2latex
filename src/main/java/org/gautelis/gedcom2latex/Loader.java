package org.gautelis.gedcom2latex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Loader {

    private static final Logger log = LoggerFactory.getLogger(Loader.class);

    // Matches a GEDCOM record on a line: <level> (<pointer>)? <tag> (<data>)?
    private final String RE = "^(?<level>0|[1-9]+[0-9]*) (@(?<pointer>[^@]+)@ |)(?<tag>[A-Za-z0-9_]+)(?<data> [^\n\r]*|)$";
    private final Pattern pattern = Pattern.compile(RE);

    private final LineHandler handler;

    public Loader(final LineHandler handler) {
        this.handler = handler;
    }

    /**
     * Removes (possible) BOM, since it interferes with the RE matching
     * @param value a string that may contain a UTF-8 BOM (or not)
     * @return a trimmed string without the BOM
     */
    public static String trimBOM(String value) {
        // UTF-8 BOM is EF BB BF, see https://en.wikipedia.org/wiki/Byte_order_mark
        String bom = String.format("%x", new BigInteger(1, value.substring(0, 3).getBytes()));

        //BOM: 0xefbbbf3020
        if ("efbbbf".equals(bom.substring(0, 6)))
            // UTF-8
            return value.substring(1);
        else if ("feff".equals(bom.substring(0, 4)) || "ffe".equals(bom.substring(0, 3)))
            // UTF-16BE or UTF16-LE
            return value.substring(2);
        else
            return value;
    }

    public void load(File file) throws IOException {
        // As of version 7.0, a GEDCOM file is defined as UTF-8 encoded plain text.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            // Read all lines from the file, possibly removing BOM from first line
            AtomicBoolean isFirstLine = new AtomicBoolean(true);

            try (Stream<String> lines = reader.lines()) {
                lines.forEach(line -> {
                    if (isFirstLine.getAndSet(false)) {
                        // First line may contain UTF-8 BOM or ZWNBSP (Zero Width No-Break Space)
                        line = trimBOM(line.trim());
                    }
                    accept(line);
                });
            }
        }
    }

    public void load(final Stream<String> data) {
        data.forEach(this::accept);
    }

    public void accept(String line) {
        java.util.regex.Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            // A line matching the GEDCOM record format
            long level = -1L;
            String _level = matcher.group("level");
            if (null != _level && !_level.isEmpty()) {
                level = Long.parseLong(_level);
            }

            String pointer = matcher.group("pointer"); // may not exist (optional)
            String tag = matcher.group("tag");
            String data = matcher.group("data"); // may not exist (optional)
            handler.accept(level, pointer, tag, data);

        } else {
            // This line does not match a GEDCOM record format, so we assume this line is part of previous
            // data and that this data contains newlines
            handler.accept(line);
        }
    }
}
