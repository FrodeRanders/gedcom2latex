package org.gautelis.gedcom2latex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.gautelis.gedcom2latex.model.Structure;
import org.gautelis.gedcom2latex.model.gedcom.GEDC;
import org.gautelis.gedcom2latex.model.gedcom.HEAD;
import org.gautelis.gedcom2latex.model.gedcom.INDI;
//import org.stringtemplate.v4.ST;
//import org.stringtemplate.v4.STGroup;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class Application {
    private final static Logger log = LogManager.getLogger(Application.class);


    private static LineHandler loadFile(
            final Path gedcomFile
    ) {
        LineHandler handler = new LineHandler();
        try {
            Loader loader = new Loader(handler);
            loader.load(gedcomFile.toFile());
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace(System.err);
        }
        return handler;
    }

    /*
    private static void produceOutput(
            final Stack<Requirements> requirements,
            final Map<String, Requirements> labeledRequirements,
            final Collection<Path> templates,
            final PrintStream out
    ) {
        STGroup group =  new STGroup();
        //group.verbose = true;

        for (Path template : templates) {
            String resource = "file:" + template.toAbsolutePath().toString();
            group.loadGroupFile(/* absolute path is "relative" to root :) * / "/", resource);
        }

    }
    */


    private static void process(final Path gedcomFile, final Collection<Path> templates, final PrintStream out) {
        LineHandler lineHandler = loadFile(gedcomFile);

        Optional<HEAD> head = lineHandler.getHEAD();
        if (head.isPresent()) {
            Optional<GEDC> gedc = head.get().GEDC();
            gedc.ifPresent(value -> System.out.println("GEDCOM version: " + value.getVersionNumber()));
        }

        Collection<INDI> individuals = lineHandler.getINDI();
        for (INDI individual : individuals) {
            System.out.println(individual);
        }

        Map<String, Structure> structures = lineHandler.getIndex();
        for (Structure structure : structures.values()) {
            String tag = structure.getTag();
            switch (tag) {
                case "HEAD", "TRLR" -> {}
                case "FAM" -> {}
                case "INDI" -> {}
                case "OBJE" -> {}
                case "NOTE" -> {}
                case "REPO" -> {}
                case "SOUR" -> {}
                case "SUBN" -> {}
                case "SUBM" -> {}

                default -> {
                    log.info("Unknown tag: {}", tag);
                }
            }

            if (log.isTraceEnabled()) {
                StringBuffer buf = new StringBuffer();
                structure.deepToString(buf);
                log.trace(buf);
            }
        }



        //produceOutput(requirements, labeledRequirements, templates, out);
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
                .required(false)  // TODO
                .hasArgs()
                .desc("Template used when generating output")
                .longOpt("template")
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

            final Collection<Path> templates = new ArrayList<>();
            /*
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
            */


            //

            process(gedcomFile, templates, System.out);
        }
        catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}
