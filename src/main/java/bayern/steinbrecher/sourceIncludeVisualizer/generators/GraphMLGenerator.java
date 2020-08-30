package bayern.steinbrecher.sourceIncludeVisualizer.generators;

import bayern.steinbrecher.javaUtility.SupplyingMap;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @since 0.1
 */
public final class GraphMLGenerator {
    private static final Collection<Color> GROUP_COLOR_NAMES = List.of(
            Color.GREEN,
            Color.BLUE,
            Color.RED,
            Color.YELLOW,
            Color.CYAN
    );
    // FIXME Assure it's not a regular group color already
    private static final Color DEFAULT_GROUP_COLOR = Color.WHITE;
    // FIXME Assure it's not a regular group color already
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
        // FIXME Colors are associated by the include directory path and not by including library
        Queue<Color> remainingGroupColors = new ArrayDeque<>(GROUP_COLOR_NAMES);
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        SupplyingMap<String, Color> associatedGroupColors = new SupplyingMap<>(groupName -> {
            // NOTE Do not separate polling and checking for emptiness to prevent race conditions
            Color groupColorName = remainingGroupColors.poll();
            if (groupColorName == null) {
                groupColorName = DEFAULT_GROUP_COLOR;
            }
            return groupColorName;
        });
        StringBuilder graphMLBuilder = new StringBuilder(HEADER);
        includeDependencies.forEach((file, includes) -> {
            String groupName = file.substring(0, Math.max(0, file.lastIndexOf('/')));

            // Insert node for internal file
            graphMLBuilder.append(generateNodeElement(file, associatedGroupColors.get(groupName)));

            // Insert include dependency edges
            includes.forEach(include -> {
                if (!includeDependencies.containsKey(include)) {
                    graphMLBuilder.append(generateNodeElement(include, DEFAULT_GROUP_COLOR));
                }

                graphMLBuilder.append(generateEdgeElement(file, include, DEFAULT_EDGE_COLOR));
            });
        });
        graphMLBuilder.append(FOOTER);
        return graphMLBuilder.toString();
    }
}
