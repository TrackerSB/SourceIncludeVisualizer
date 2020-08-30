package bayern.steinbrecher.sourceIncludeVisualizer;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

public enum ColorPalette {
    COLOR_BLIND_FRIENDLY(List.of(
            // Color-blind-friendly palette from https://jfly.uni-koeln.de/color/
            new Color(153, 153, 153),
            new Color(230, 159, 0),
            new Color(86, 180, 233),
            new Color(0, 158, 115),
            new Color(240, 228, 66),
            new Color(0, 114, 178),
            new Color(213, 94, 0),
            new Color(204, 121, 167)
    ));
    // NOTE Further color palettes may be found in the RColorBrewer palette chart

    private final Collection<Color> colors;

    ColorPalette(Collection<Color> colors) {
        this.colors = colors;
    }

    public Collection<Color> getColors() {
        return colors;
    }
}
