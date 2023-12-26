package org.gautelis.gedcom2latex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.gautelis.gedcom2latex.model.GEDCOMRecord;
//import org.stringtemplate.v4.ST;
//import org.stringtemplate.v4.STGroup;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Application {
    private final static Logger log = LogManager.getLogger(Application.class);


    private static int loadgedcomFiles(
            final Collection<Path> gedcomFiles
    ) {
        RecordHandler handler = new RecordHandler();
        try {
            Loader loader = new Loader(handler);
            int count = 0;
            for (Path gedcomFile : gedcomFiles) {
                loader.load(gedcomFile.toFile());
                ++count;
            }

            //
            Map<String, GEDCOMRecord> records = handler.getRecords();
            for (String key : records.keySet()) {
                GEDCOMRecord record = records.get(key);
                StringBuffer buf = new StringBuffer(key + " -> ");
                record.deepToString(buf);
                System.out.println(buf);
            }

            return count;
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace(System.err);
        }
        return -1;
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


    private static void process(final Collection<Path> gedcomFiles, final Collection<Path> templates, final PrintStream out) {
        if (loadgedcomFiles(gedcomFiles) != gedcomFiles.size()) {
            System.err.println("Could not load all GEDCOM files");
            System.exit(1);
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

            final Collection<Path> gedcomFiles = new ArrayList<>();
            for (String gedcomFile : commandLine.getArgs()) {
                Path path = Path.of(gedcomFile);
                File file = path.toFile();
                if (!file.exists()) {
                    System.err.println("GEDCOM file does not exist: " + gedcomFile);
                    System.exit(1);
                }
                if (!file.canRead()) {
                    System.err.println("Can't read GEDCOM file: " + gedcomFile);
                    System.exit(1);
                }
                gedcomFiles.add(path);
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

            System.out.println("GEDCOM files: " +
                    gedcomFiles.stream()
                    .map(path -> path.toFile().getName())
                    .collect(Collectors.joining(", ")));

            System.out.println("Templates: " +
                    templates.stream()
                    .map(path -> path.toFile().getName())
                    .collect(Collectors.joining(", ")));

            process(gedcomFiles, templates, System.out);
        }
        catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}
