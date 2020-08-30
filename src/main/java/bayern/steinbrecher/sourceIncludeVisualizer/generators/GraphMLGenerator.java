package bayern.steinbrecher.sourceIncludeVisualizer.generators;

import bayern.steinbrecher.javaUtility.SupplyingMap;
import bayern.steinbrecher.sourceIncludeVisualizer.ColorPalette;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public final class GraphMLGenerator {
    private static final Logger LOGGER = Logger.getLogger(GraphMLGenerator.class.getName());
    private static final Collection<Color> GROUP_COLOR_NAMES = ColorPalette.COLOR_BLIND_FRIENDLY.getColors();
    // FIXME Assure it's not a regular group color already
    private static final Color DEFAULT_GROUP_COLOR = Color.WHITE;
    private static final Color DEFAULT_EDGE_COLOR = Color.BLACK;
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
            + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "        xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n"
            + "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n"
            + "    <key id=\"groupColorRed\" for=\"node\" attr.name=\"r\" attr.type=\"int\"/>\n"
            + "    <key id=\"groupColorGreen\" for=\"node\" attr.name=\"g\" attr.type=\"int\"/>\n"
            + "    <key id=\"groupColorBlue\" for=\"node\" attr.name=\"b\" attr.type=\"int\"/>\n"
            + "    <key id=\"edgeColorRed\" for=\"edge\" attr.name=\"r\" attr.type=\"int\"/>\n"
            + "    <key id=\"edgeColorGreen\" for=\"edge\" attr.name=\"g\" attr.type=\"int\"/>\n"
            + "    <key id=\"edgeColorBlue\" for=\"edge\" attr.name=\"b\" attr.type=\"int\"/>\n"
            + "    <graph edgedefault=\"directed\">\n";
    private static final String FOOTER = "    </graph>\n"
            + "</graphml>\n";

    private GraphMLGenerator() {
        throw new UnsupportedOperationException("Construction of objects prohibited");
    }

    private static String generateNodeElement(String id, Color color) {
        return String.format("<node id=\"%s\">\n"
                        + "    <data key=\"groupColorRed\">%d</data>\n"
                        + "    <data key=\"groupColorGreen\">%d</data>\n"
                        + "    <data key=\"groupColorBlue\">%d</data>\n"
                        + "</node>\n",
                id, color.getRed(), color.getGreen(), color.getBlue());
    }

    private static String generateEdgeElement(String source, String target, Color color) {
        return String.format("<edge source=\"%s\" target=\"%s\">\n"
                        + "    <data key=\"edgeColorRed\">%d</data>\n"
                        + "    <data key=\"edgeColorGreen\">%d</data>\n"
                        + "    <data key=\"edgeColorBlue\">%d</data>\n"
                        + "</edge>\n",
                source, target, color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String convert(Map<String, Collection<String>> includeDependencies) {
        Queue<Color> remainingGroupColors = new ArrayDeque<>(GROUP_COLOR_NAMES);
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        SupplyingMap<String, Color> associatedGroupColors = new SupplyingMap<>(groupName -> {
            // NOTE Do not separate polling and checking for emptiness to prevent race conditions
            Color groupColor = remainingGroupColors.poll();
            if (groupColor == null) {
                groupColor = DEFAULT_GROUP_COLOR;
            }
            return groupColor;
        });
        LibraryDetector libraryDetector = new LibraryDetector(includeDependencies.keySet());
        StringBuilder graphMLBuilder = new StringBuilder(HEADER);
        includeDependencies.forEach((file, includes) -> {
            String sourceGroupName = libraryDetector.detect(file);

            // Insert node for internal file
            graphMLBuilder.append(generateNodeElement(file, associatedGroupColors.get(sourceGroupName)));

            // Insert include dependency edges
            includes.forEach(include -> {
                String targetGroupName = libraryDetector.detect(include);
                graphMLBuilder.append(generateNodeElement(include, associatedGroupColors.get(targetGroupName)));
                graphMLBuilder.append(generateEdgeElement(file, include, DEFAULT_EDGE_COLOR));
            });
        });
        Set<String> notFoundLibraries = libraryDetector.getNotFoundLibraries();
        if (!notFoundLibraries.isEmpty()) {
            String notFoundLibrariesList = notFoundLibraries.stream()
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.joining(", "));
            String notFoundLibrariesStatistic = String.format(
                    "#NotFoundLibraries: %d\nNot Found: %s", notFoundLibraries.size(), notFoundLibrariesList);
            LOGGER.log(Level.INFO, notFoundLibrariesStatistic);
        }
        graphMLBuilder.append(FOOTER);
        return graphMLBuilder.toString();
    }
}
