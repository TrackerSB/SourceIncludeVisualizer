package bayern.steinbrecher.sourceIncludeVisualizer;

import bayern.steinbrecher.jcommander.JCommander;
import bayern.steinbrecher.jcommander.ParameterException;

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
        CommandLine commandLine = new CommandLine();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(commandLine)
                .build();
        jCommander.setProgramName("SourceIncludeVisualizer");
        boolean programCallInvalid = false;
        try {
            jCommander.parse(args);
        } catch (ParameterException ex) {
            LOGGER.log(Level.SEVERE, "Could not start program", ex);
            programCallInvalid = true;
        }

        if (commandLine.isShowHelpOptionSet() || programCallInvalid) {
            jCommander.usage();
        } else {
            Queue<String> includeElementsToProcess = new ArrayDeque<>(commandLine.getIncludesToAnalyze());
            while (!includeElementsToProcess.isEmpty()) {
                Path includeElementPath = Paths.get(includeElementsToProcess.poll())
                        .toAbsolutePath();
                try {
                    Files.walkFileTree(includeElementPath, new AnalyzeElementsFinder());
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
        }
    }
}
