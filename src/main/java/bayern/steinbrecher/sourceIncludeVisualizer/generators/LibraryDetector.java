package bayern.steinbrecher.sourceIncludeVisualizer.generators;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
final class LibraryDetector {
    private static final Logger LOGGER = Logger.getLogger(LibraryDetector.class.getName());
    private final Collection<String> projectFiles;
    private static final String NOT_FOUND_LIBRARY_NAME = "LibraryNotFound";
    private static final String PROJECT_INTERNAL_LIBRARY_PREFIX = "project_";

    public LibraryDetector(Collection<String> projectFiles) {
        this.projectFiles = projectFiles;
    }

    public String detect(String file) {
        List<String> containingLibraries = Arrays.stream(ExternalLibrary.values())
                .filter(lib -> lib.contains(file))
                .map(ExternalLibrary::getName)
                .collect(Collectors.toList());
        if (projectFiles.contains(file)) {
            /* FIXME The library name considers only the containing directory path but not the library name it actually
             * belongs to
             */
            String projectInternalLibraryName = file.substring(0, Math.max(0, file.lastIndexOf('/')));
            containingLibraries.add(PROJECT_INTERNAL_LIBRARY_PREFIX + projectInternalLibraryName);
        }

        return switch (containingLibraries.size()) {
            case 0 -> {
                LOGGER.log(Level.INFO, String.format("Library which contains '%s' could not be found", file));
                yield NOT_FOUND_LIBRARY_NAME;
            }
            case 1 -> {
                System.out.println(file + " -> " + containingLibraries.get(0));
                yield containingLibraries.get(0);
            }
            default -> {
                String containingLibrariesList = String.join(", ", containingLibraries);
                LOGGER.log(
                        Level.INFO,
                        String.format("'%s' is contained by multiple libraries (%s)", file, containingLibrariesList)
                );
                yield containingLibraries.get(0);
            }
        };
    }

    private enum ExternalLibrary {
        CPP_STANDARD("C++StandardLibrary", Set.of(
                "algorithm", "deque", "functional", "iostream", "queue", "set", "string", "unordered_map", "utility", "vector"
        ));
        private static final String EXTERNAL_LIBRARY_NAME_PREFIX = "external_";
        private final String nameSuffix;
        private final Collection<String> headerPaths;

        ExternalLibrary(String nameSuffix, Collection<String> headerPaths) {
            this.nameSuffix = nameSuffix;
            this.headerPaths = headerPaths;
        }

        public String getName() {
            return EXTERNAL_LIBRARY_NAME_PREFIX + nameSuffix;
        }

        public boolean contains(String file) {
            return headerPaths.contains(file);
        }
    }
}
