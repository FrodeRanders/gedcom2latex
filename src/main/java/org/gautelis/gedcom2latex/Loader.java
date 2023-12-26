package org.gautelis.gedcom2latex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Loader {

    private static final Logger log = LoggerFactory.getLogger(Matcher.class);

    private final String RE = "^(?<level>0|[1-9]+[0-9]*) (@(?<pointer>[^@]+)@ |)(?<tag>[A-Za-z0-9_]+)(?<data> [^\n\r]*|)$";
    private final Pattern pattern = Pattern.compile(RE);

    private final RecordHandler handler;

    public Loader(final RecordHandler handler) {
        this.handler = handler;
    }

    public static String cutBOM(String value) {
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
            AtomicBoolean isFirstLine = new AtomicBoolean(true);

            try (Stream<String> lines = reader.lines()) {
                lines.forEach(line -> {
                    if (isFirstLine.getAndSet(false)) {
                        // First line may contain UTF-8 BOM or ZWNBSP (Zero Width No-Break Space)
                        line = cutBOM(line.trim());
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
            long level = -1L;
            String _level = matcher.group("level");
            if (null != _level && !_level.isEmpty()) {
                level = Long.parseLong(_level);
            }

            String pointer = matcher.group("pointer");
            String tag = matcher.group("tag");
            String data = matcher.group("data");

            handler.acceptRecord(level, pointer, tag, data);

        } else {
            handler.acceptData(line);
        }
    }
}
