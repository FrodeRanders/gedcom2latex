package org.gautelis.gedcom2latex;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.gautelis.gedcom2latex.model.Structure;
import org.gautelis.gedcom2latex.model.gedcom.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
//import org.stringtemplate.v4.ST;
//import org.stringtemplate.v4.STGroup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Application {
    private final static Logger log = LogManager.getLogger(Application.class);


    private static boolean loadFile(
            final Path gedcomFile, Map<String, Structure> index, Map</* tag/type */ String, Collection<Structure>> structures
    ) {
        LineHandler handler = new LineHandler(index, structures);
        try {
            Loader loader = new Loader(handler);
            loader.load(gedcomFile.toFile());
            return true;
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace(System.err);
        }
        return false;
    }

    private static void produceOutput(
            final Map</* id */ String, Structure> index,
            final Map</* tag */ String, Collection<Structure>> structures,
            final Collection<Path> templates,
            final Path directory,
            final PrintStream out
    ) {
        STGroup group =  new STGroup();
        //group.verbose = true;

        for (Path template : templates) {
            String resource = "file:" + template.toAbsolutePath().toString();
            group.loadGroupFile(/* absolute path is "relative" to root :) */ "/", resource);
        }

        /*
        out.println("--- HEADER ---");
        Optional<HEAD> head = Structure.getHEAD(structures);
        if (head.isPresent()) {
            Optional<GEDC> gedc = head.get().GEDC();
            gedc.ifPresent(value -> out.println("GEDCOM version: " + value.getVersionNumber()));
        }
        */

        out.println("--- INDIVIDUALS ---");
        for (INDI individual : Structure.getINDIs(structures)) {
            out.println(individual);
        }

        out.println("--- FAMILIES ---");
        for (FAM family : Structure.getFAMs(structures)) {
            out.println(family);
        }

        Path latexFile = directory.resolve("output.tex");
        try (FileWriterWithEncoding s = FileWriterWithEncoding.builder()
                .setPath(latexFile)
                .setAppend(false)
                .setCharsetEncoder(StandardCharsets.UTF_8.newEncoder())
                .get()) {

            // preamble(date)
            {
                ST preamble = group.getInstanceOf("preamble");
                LocalDate date = LocalDate.now();
                preamble.add("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                s.append(preamble.render());
            }

            // chapter(title)
            {
                ST preamble = group.getInstanceOf("chapter");
                preamble.add("title", "Individer");
                s.append(preamble.render());
            }

            // INDIvidual(id,name)
            {
                Collection<INDI> individuals = Structure.getINDIs(structures);
                for (INDI individual : individuals) {
                    Optional<NAME> name = individual.NAME().stream().findFirst();

                    ST preamble = group.getInstanceOf("individual");
                    preamble.add("id", individual.getId());
                    if (name.isPresent()) {
                        preamble.add("name", name.get().getName());
                    } else {
                        preamble.add("name", individual.getId());
                    }
                    s.append(preamble.render());
                }
            }

            // chapter(title)
            {
                ST preamble = group.getInstanceOf("chapter");
                preamble.add("title", "Familier");
                s.append(preamble.render());
            }

            // postamble(date)
            {
                ST postamble = group.getInstanceOf("postamble");
                s.append(postamble.render());
            }
        } catch (IOException ioe) {
            String info = "Failed to produce output: " + ioe.getMessage();
            log.error(info, ioe);
            out.println(info);
            out.flush();
        }
    }



    private static void process(
            final Path gedcomFile,
            final Collection<Path> templates,
            final Path directory,
            final PrintStream out
    ) {
        final Map</* id */ String, Structure> index = new HashMap<>();
        final Map</* tag */ String, Collection<Structure>> structures = new HashMap<>();

        if (loadFile(gedcomFile, index, structures)) {
            Optional<HEAD> head = Structure.getHEAD(structures);
            if (head.isPresent()) {
                Optional<GEDC> gedc = head.get().GEDC();
                if (gedc.isPresent()) {
                    String version = gedc.get().getVersionNumber();
                    out.println("Loaded GEDCOM file, version: " + version);

                    switch (version) {
                        case "5.5.1" -> {
                            out.println("Processing...");
                        }
                        default -> {
                            out.println("Cannot process GEDCOM version " + version);
                            out.flush();
                            System.exit(1);
                        }
                    }
                }
            }

            //
            if (log.isTraceEnabled()) {
                StringBuffer buf = new StringBuffer();
                for (Structure structure : index.values()) {
                    structure.deepToString(buf);
                }
                log.trace(buf);
            }

            produceOutput(index, structures, templates, directory, out);
        }
    }



    public static void main(String[] args) {
        if (!System.getProperty("file.encoding").equals("UTF-8")) {
            System.out.println(
                    "Changing system encoding from '" + System.getProperty("file.encoding") + "' to 'UTF-8'"
            );
            System.setProperty("file.encoding", "UTF-8");
        }

        Options options = new Options();
        options.addOption(Option.builder("t")
                .required(true)
                .hasArgs()
                .desc("Template used when generating output")
                .longOpt("template")
                .build());

        options.addOption(Option.builder("d")
                .required(false)
                .hasArgs()
                .desc("Directoy where output is produced")
                .longOpt("directory")
                .build());

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);

            Path gedcomFile = null;
            for (String _gedcomFile : commandLine.getArgs()) {
                Path path = Path.of(_gedcomFile);
                File file = path.toFile();
                if (!file.exists()) {
                    System.err.println("GEDCOM file does not exist: " + _gedcomFile);
                    System.exit(1);
                }
                if (!file.canRead()) {
                    System.err.println("Can't read GEDCOM file: " + _gedcomFile);
                    System.exit(1);
                }
                gedcomFile = path;;
            }

            //
            final Collection<Path> templates = new ArrayList<>();
            for (String template : commandLine.getOptionValues("t")) {
                Path path = Path.of(template);
                File file = path.toFile();
                if (!file.exists()) {
                    System.err.println("Template does not exist: " + template);
                    System.exit(1);
                }
                if (!file.canRead()) {
                    System.err.println("Can't read template: " + template);
                    System.exit(1);
                }
                templates.add(path);
            }

            //
            File directory = new File("latex");
            String _directory = commandLine.getOptionValue("d");
            if (null != _directory && !_directory.isEmpty()) {
                directory = new File(_directory);
            }

            if (directory.exists()) {
                if (directory.isFile()) {
                    System.err.println("There exists a file where output was supposed to go: " + directory.getAbsolutePath());
                    System.err.flush();
                    System.exit(2);
                }
                System.out.println("WARNING: Output directory already exists: will replace output in " + directory.getAbsolutePath());

            } else if (!directory.mkdir()) {
                System.err.println("Could not create output directory: " + directory.getAbsolutePath());
                System.err.flush();
                System.exit(3);
            }

            if (!directory.canWrite()) {
                System.err.println("Not allowed to write to output directory: " + directory.getAbsolutePath());
                System.err.flush();
                System.exit(4);
            }

            //
            process(gedcomFile, templates, directory.toPath(), System.out);
        }
        catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}
