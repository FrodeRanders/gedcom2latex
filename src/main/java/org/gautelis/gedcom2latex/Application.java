package org.gautelis.gedcom2latex;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.gautelis.gedcom2latex.model.DatePlace;
import org.gautelis.gedcom2latex.model.Individual;
import org.gautelis.gedcom2latex.model.Name;
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
import java.util.stream.Collectors;

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

    public static Set<Individual> depthFirstTraversal(Individual root) {
        Set<Individual> visited = new LinkedHashSet<>();
        Stack<Individual> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Individual vertex = stack.pop();
            if (!visited.contains(vertex)) {
                visited.add(vertex);

                Optional<Individual> father = vertex.getFather();
                father.ifPresent(stack::push);
                Optional<Individual> mother = vertex.getMother();
                mother.ifPresent(stack::push);

                // we don't follow children links here
            }
        }
        return visited;
    }

    public static Set<Individual> breadthFirstTraversal(Individual individual) {
        Set<Individual> visited = new LinkedHashSet<>();
        Queue<Individual> queue = new LinkedList<>();
        queue.add(individual);
        visited.add(individual);
        while (!queue.isEmpty()) {
            Individual vertex = queue.poll();

            Optional<Individual> father = vertex.getFather();
            if (father.isPresent()) {
                Individual _father = father.get();
                if (!visited.contains(_father)) {
                    visited.add(_father);
                    queue.add(_father);
                }
            }

            Optional<Individual> mother = vertex.getMother();
            if (mother.isPresent()) {
                Individual _mother = mother.get();
                if (!visited.contains(_mother)) {
                    visited.add(_mother);
                    queue.add(_mother);
                }
            }
        }
        return visited;
    }


    private static Map</* id */ String, Individual> analyze(
            final Map</* id */ String, Structure> index,
            final Map</* tag */ String, Collection<Structure>> structures,
            final PrintStream out
    ) {
        Map</* id */ String, Individual> individuals = Structure.getINDIs(structures)
                .stream()
                .collect(Collectors.toMap(INDI::getId, Individual::new, (a, b) -> b));

        /*
        out.println("--- HEADER ---");
        Optional<HEAD> head = Structure.getHEAD(structures);
        if (head.isPresent()) {
            Optional<GEDC> gedc = head.get().GEDC();
            gedc.ifPresent(value -> out.println("GEDCOM version: " + value.getVersionNumber()));
        }
        */

        out.println("--- FAMILIES ---");
        for (FAM family : Structure.getFAMs(structures)) {
            out.println(family);
            String familyId = family.getId();

            Individual father = null;
            Optional<String> husbandId = family.getHusbandId();
            if (husbandId.isPresent()) {
                father = individuals.get(husbandId.get());
                assert null != father;
            }

            Individual mother = null;
            Optional<String> wifeId = family.getWifeId();
            if (wifeId.isPresent()) {
                mother = individuals.get(wifeId.get());
                assert null != mother;
            }

            for (String childId : family.getChildrenId()) {
                out.println("Looking up child with id=" + childId);
                Individual child = individuals.get(childId);
                if (null != child) {
                    if (null != father) {
                        child.setFather(familyId, father);
                    }
                    if (null != mother) {
                        child.setMother(familyId, mother);
                    }
                }
            }
        }

        out.println("--- INDIVIDUALS ---");
        for (Individual individual : individuals.values()) {
            out.println(individual);
        }

        out.println("-------------------");
        Individual me = individuals.get("@I500003@"); // for now :)

        Set<Individual> myFamily = breadthFirstTraversal(me);
        for (Individual individual : myFamily) {
            Collection<Name> names = individual.getNames();
            for (Name name : names) {
                out.print(name.annotatedName());

                Collection<Individual> spouses = individual.getSpouses();
                if (!spouses.isEmpty()) {
                    out.println();

                    for (Individual spouse : spouses) {
                        out.print("   +> ");
                        Collection<Name> spouseNames = spouse.getNames();
                        for (Name spouseName : spouseNames) {
                            out.print("\"" + spouseName.annotatedName() + "\" ");
                        }
                        out.println();
                    }
                }
                out.println();
            }
        }
        out.println("-------------------");


        return individuals;
   }

    private static void produceOutput(
            final Map</* id */ String, Structure> index,
            final Map</* tag */ String, Collection<Structure>> structures,
            final Map</* id */ String, Individual> individuals,
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

        Path latexFile = directory.resolve("output.tex");
        try (FileWriterWithEncoding s = FileWriterWithEncoding.builder()
                .setPath(latexFile)
                .setAppend(false)
                .setCharsetEncoder(StandardCharsets.UTF_8.newEncoder())
                .get()) {

            // preamble(date)
            {
                ST template = group.getInstanceOf("preamble");
                LocalDate date = LocalDate.now();
                template.add("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                s.append(template.render());
            }

            // chapter(title)
            {
                ST template = group.getInstanceOf("chapter");
                template.add("title", "Individer");
                s.append(template.render());
            }

            {
                /*
                // genealogygraph_horiz(graph)
                {
                    ST template = group.getInstanceOf("genealogygraph_horizontal");
                    template.add("graph", Individual.produceLatexOutput());
                    s.append(template.render());
                }
                */

                for (Individual individual : individuals.values()) {
                    Collection<Name> names = individual.getNames();
                    Iterator<Name> niter = names.iterator();

                    // individual(id,name)
                    {
                        ST template = group.getInstanceOf("individual");
                        template.add("id", individual.getId());
                        if (niter.hasNext()) {
                            Name name = niter.next();
                            template.add("name", name.annotatedName().replace("/", ""));
                        } else {
                            template.add("name", individual.getId());
                        }
                        template.add("sex", individual.getSex().name());
                        s.append(template.render());
                    }

                    // genealogygraph_horiz(graph)
                    {
                        ST template = group.getInstanceOf("genealogygraph_horizontal");
                        template.add("graph", individual.asCoreRelationship());
                        s.append(template.render());
                    }

                    while (niter.hasNext()) {
                        Name name = niter.next();

                        // additionalName(name)
                        ST template = group.getInstanceOf("additionalName");
                        template.add("name", name);
                        s.append(template.render());
                    }

                    // born(date, place)
                    for (DatePlace birth : individual.getBirths()) {
                        ST template = group.getInstanceOf("born");
                        template.add("date", birth.date());
                        template.add("place", birth.place());
                        s.append(template.render());
                    }

                    // baptism(date, place)
                    for (DatePlace baptism : individual.getBaptisms()) {
                        ST template = group.getInstanceOf("baptism");
                        template.add("date", baptism.date());
                        template.add("place", baptism.place());
                        s.append(template.render());
                    }

                    // death(date, place)
                    for (DatePlace death : individual.getDeaths()) {
                        ST template = group.getInstanceOf("death");
                        template.add("date", death.date());
                        template.add("place", death.place());
                        s.append(template.render());
                    }

                    // burial(date, place)
                    for (DatePlace burial : individual.getBurials()) {
                        ST template = group.getInstanceOf("burial");
                        template.add("date", burial.date());
                        template.add("place", burial.place());
                        s.append(template.render());
                    }
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

            Map</* id */ String, Individual> individuals = analyze(index, structures, out);
            produceOutput(index, structures, individuals, templates, directory, out);
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
