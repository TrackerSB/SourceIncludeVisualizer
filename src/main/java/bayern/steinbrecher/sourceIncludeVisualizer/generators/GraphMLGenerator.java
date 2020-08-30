package bayern.steinbrecher.sourceIncludeVisualizer.generators;

import bayern.steinbrecher.javaUtility.SupplyingMap;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @since 0.1
 */
public final class GraphMLGenerator {
    private static final Collection<String> GROUP_COLOR_NAMES = List.of(
            "green",
            "blue",
            "red",
            "yellow",
            "turquoise"
    );
    // FIXME Assure it's not a regular group color already
    private static final String DEFAULT_GROUP_COLOR_NAME = "white";
    // FIXME Assure it's not a regular group color already
    private static final String DEFAULT_EDGE_COLOR_NAME = "black";
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
            + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "        xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n"
            + "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n"
            + "    <key id=\"groupColor\" for=\"node\" attr.name=\"color\" attr.type=\"string\">\n"
            + "        <default>" + DEFAULT_GROUP_COLOR_NAME + "</default>\n"
            + "    </key>\n"
            + "    <key id=\"edgeColor\" for=\"edge\" attr.name=\"color\" attr.type=\"string\">\n"
            + "        <default>" + DEFAULT_EDGE_COLOR_NAME + "</default>\n"
            + "    </key>\n"
            + "    <graph edgedefault=\"directed\">\n";
    private static final String FOOTER = "    </graph>\n"
            + "</graphml>\n";

    private GraphMLGenerator() {
        throw new UnsupportedOperationException("Construction of objects prohibited");
    }

    private static String generateNodeElement(String id, String color) {
        return String.format("<node id=\"%s\">\n    <data key=\"groupColor\">%s</data>\n</node>\n", id, color);
    }

    private static String generateEdgeElement(String source, String target, String color){
        return String.format(
                "<edge source=\"%s\" target=\"%s\">\n    <data key=\"edgeColor\">%s</data>\n</edge>\n",
                source, target, color);
    }

    public static String convert(Map<String, Collection<String>> includeDependencies) {
        // FIXME Colors are associated by the include directory path and not by including library
        Queue<String> remainingGroupColors = new ArrayDeque<>(GROUP_COLOR_NAMES);
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        SupplyingMap<String, String> associatedGroupColors = new SupplyingMap<>(groupName -> {
            // NOTE Do not separate polling and checking for emptiness to prevent race conditions
            String groupColorName = remainingGroupColors.poll();
            if (groupColorName == null) {
                groupColorName = DEFAULT_GROUP_COLOR_NAME;
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
                    graphMLBuilder.append(generateNodeElement(include, DEFAULT_GROUP_COLOR_NAME));
                }

                graphMLBuilder.append(generateEdgeElement(file, include, DEFAULT_EDGE_COLOR_NAME));
            });
        });
        graphMLBuilder.append(FOOTER);
        return graphMLBuilder.toString();
    }
}
