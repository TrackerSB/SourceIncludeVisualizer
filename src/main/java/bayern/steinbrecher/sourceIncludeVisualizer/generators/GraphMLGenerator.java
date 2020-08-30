package bayern.steinbrecher.sourceIncludeVisualizer.generators;

import java.util.Collection;
import java.util.Map;

public final class GraphMLGenerator {
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
            + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "        xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n"
            + "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n"
            + "    <graph edgedefault=\"directed\">\n";
    private static final String FOOTER = "    </graph>\n"
            + "</graphml>\n";

    private GraphMLGenerator() {
        throw new UnsupportedOperationException("Construction of objects prohibited");
    }

    public static String convert(Map<String, Collection<String>> includeDependencies) {
        StringBuilder graphMLBuilder = new StringBuilder(HEADER);
        includeDependencies.forEach((file, includes) -> {
            graphMLBuilder.append(String.format("<node id=\"%s\"/>\n", file));
            includes.forEach(include -> {
                graphMLBuilder.append(String.format("<edge source=\"%s\" target=\"%s\"/>\n", file, include));
            });
        });
        graphMLBuilder.append(FOOTER);
        return graphMLBuilder.toString();
    }
}
