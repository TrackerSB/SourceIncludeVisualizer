package bayern.steinbrecher.sourceIncludeVisualizer;

import bayern.steinbrecher.jcommander.JCommander;
import bayern.steinbrecher.jcommander.ParameterException;
import bayern.steinbrecher.sourceIncludeVisualizer.cpp.CPPIncludeGraphGenerator;
import bayern.steinbrecher.sourceIncludeVisualizer.generators.GraphMLGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private Main() {
        throw new UnsupportedOperationException("Construction of objects prohibited");
    }

    public static void main(String[] args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(commandLineArgs)
                .build();
        jCommander.setProgramName("SourceIncludeVisualizer");
        boolean programCallInvalid = false;
        try {
            jCommander.parse(args);
        } catch (ParameterException ex) {
            LOGGER.log(Level.SEVERE, "Could not start program", ex);
            programCallInvalid = true;
        }

        if (commandLineArgs.isShowHelpOptionSet() || programCallInvalid) {
            jCommander.usage();
        } else {
            Queue<String> includeElementsToProcess = new ArrayDeque<>(commandLineArgs.getIncludesToAnalyze());
            CPPIncludeGraphGenerator graphCollector = new CPPIncludeGraphGenerator();
            while (!includeElementsToProcess.isEmpty()) {
                Path includeElementPath = Paths.get(includeElementsToProcess.poll())
                        .toAbsolutePath();
                graphCollector.setIncludeRoot(includeElementPath);
                try {
                    Files.walkFileTree(includeElementPath, graphCollector);
                } catch (IOException ex) {
                    LOGGER.log(
                            Level.SEVERE,
                            String.format(
                                    "Could not search for elements to analyze in '%s'",
                                    includeElementPath.toString()
                            )
                    );
                }
            }
            Path outputFile;
            try {
                outputFile = Files.createTempFile("SourceIncludeVisualizer_", ".graphml");
            } catch (IOException ex) {
                throw new Error("Could not create output file for storing generated GraphML description", ex);
            }
            String graphMLContent = GraphMLGenerator.convert(graphCollector.getIncludeDependencies());
            try {
                Files.writeString(outputFile, graphMLContent);
            } catch (IOException ex) {
                throw new Error("Could not export generated GraphML description", ex);
            }
            LOGGER.log(Level.INFO, String.format("Exported GraphML description to '%s'", outputFile));
        }
    }
}
