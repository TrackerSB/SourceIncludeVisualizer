package bayern.steinbrecher.sourceIncludeVisualizer;

import bayern.steinbrecher.jcommander.JCommander;
import bayern.steinbrecher.jcommander.Parameter;
import bayern.steinbrecher.jcommander.ParameterException;
import bayern.steinbrecher.jcommander.Parameters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Parameters(resourceBundle = "bayern.steinbrecher.sourceIncludeVisualizer.CommandLine",
            commandDescriptionKey = "commandLineDescription")
    private static class CommandLine {
        @Parameter(names = {"--help", "-h"}, descriptionKey = "optionHelp", help = true)
        private boolean showHelp = false;
        @Parameter(names = {"--includeDirectories", "-i"}, descriptionKey = "optionIncludeDirectories",
                required = true, variableArity = true)
        private List<String> includeDirectories;
    }

    private Main() {
        throw new UnsupportedOperationException("Construction of objects prohibited");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(commandLine)
                .build();
        jCommander.setProgramName("SourceIncludeVisualizer");
        try {
            jCommander.parse(args);
        } catch (ParameterException ex) {
            LOGGER.log(Level.SEVERE, "Could not start program", ex);
            commandLine.showHelp = true;
        }

        if(commandLine.showHelp){
            jCommander.usage();
        } else {
            System.out.println(commandLine.includeDirectories);
        }
    }
}
