package bayern.steinbrecher.sourceIncludeVisualizer;

import bayern.steinbrecher.jcommander.Parameter;
import bayern.steinbrecher.jcommander.Parameters;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Stefan Huber
 * @since 0.1
 */
@Parameters(resourceBundle = "bayern.steinbrecher.sourceIncludeVisualizer.CommandLine",
        commandDescriptionKey = "commandLineDescription")
public final class CommandLine {
    @Parameter(names = {"--help", "-h"}, descriptionKey = "optionHelp", help = true)
    private boolean showHelp = false;
    @Parameter(names = {"--include", "-i"}, descriptionKey = "optionInclude",
            required = true, variableArity = true)
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<String> includesToAnalyze;

    public CommandLine() {
    }

    @Contract(value = " -> new", pure = true)
    public Collection<String> getIncludesToAnalyze() {
        return Collections.unmodifiableCollection(includesToAnalyze);
    }

    public boolean isShowHelpOptionSet() {
        return showHelp;
    }
}
