package org.gautelis.gedcom2latex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.gautelis.gedcom2latex.model.Structure;
import org.gautelis.gedcom2latex.model.gedcom.FAM;
import org.gautelis.gedcom2latex.model.gedcom.GEDC;
import org.gautelis.gedcom2latex.model.gedcom.HEAD;
import org.gautelis.gedcom2latex.model.gedcom.INDI;
import org.stringtemplate.v4.STGroup;
//import org.stringtemplate.v4.ST;
//import org.stringtemplate.v4.STGroup;

import java.io.*;
import java.nio.file.Path;
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
            final PrintStream out
    ) {
        /*
        out.println("--- HEADER ---");
        Optional<HEAD> head = Structure.getHEAD(structures);
        if (head.isPresent()) {
            Optional<GEDC> gedc = head.get().GEDC();
            gedc.ifPresent(value -> out.println("GEDCOM version: " + value.getVersionNumber()));
        }
        */

        out.println("--- INDIVIDUALS ---");
        Collection<INDI> individuals = Structure.getINDIs(structures);
        for (INDI individual : individuals) {
            out.println(individual);
        }

        out.println("--- FAMILIES ---");
        Collection<FAM> families = Structure.getFAMs(structures);
        for (FAM family : families) {
            out.println(family);
        }

        STGroup group =  new STGroup();
        //group.verbose = true;

        for (Path template : templates) {
            String resource = "file:" + template.toAbsolutePath().toString();
            group.loadGroupFile(/* absolute path is "relative" to root :) */ "/", resource);
        }
    }



    private static void process(
            final Path gedcomFile,
            final Collection<Path> templates,
            final File directory,
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

            produceOutput(index, structures, templates, out);
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
            process(gedcomFile, templates, directory, System.out);
        }
        catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}
