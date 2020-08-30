package bayern.steinbrecher.sourceIncludeVisualizer.generators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
class LibraryDetector {
    private static final Logger LOGGER = Logger.getLogger(LibraryDetector.class.getName());
    private final Collection<String> projectFiles;
    private static final String NOT_FOUND_LIBRARY_NAME = "LibraryNotFound";
    private static final String PROJECT_INTERNAL_LIBRARY_PREFIX = "project_";
    private final Set<String> notFoundLibraries = new HashSet<>();

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
            int lastDirectorySeparatorIndex = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));
            String projectInternalLibraryName = file.substring(0, Math.max(0, lastDirectorySeparatorIndex));
            containingLibraries.add(PROJECT_INTERNAL_LIBRARY_PREFIX + projectInternalLibraryName);
        }

        return switch (containingLibraries.size()) {
            case 0 -> {
                LOGGER.log(Level.INFO, String.format("Library which contains '%s' could not be found", file));
                notFoundLibraries.add(file);
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

    public Set<String> getNotFoundLibraries() {
        return Collections.unmodifiableSet(notFoundLibraries);
    }

    private enum ExternalLibrary {
        // Based on https://en.cppreference.com/w/cpp/header (State: 2020-08-30)
        CPP_STANDARD("C++StandardLibrary", Set.of(
                "algorithm", "any", "array", "assert.h", "atomic", "barrier", "bit", "bitset", "cassert", "cctype",
                "cerrno", "cfenv", "cfloat", "charconv", "chrono", "cinttypes", "climits", "clocale", "cmath",
                "codecvt", "compare", "complex", "concepts", "condition_variable", "coroutine", "csetjmp", "csignal",
                "cstdarg", "cstddef", "cstdint", "cstdinttypes", "cstdio", "cstdlib", "cstring", "ctime", "ctype.h",
                "cuchar", "cwchar", "cwtype", "deque", "errno.h", "exception", "execution", "fenv.h", "filesystem",
                "float.h", "format", "forward_list", "fstream", "functional", "future", "initializer_list",
                "inttypes.h", "iomanip", "ios", "iosfwd", "iostream", "istream", "iterator", "memory", "latch",
                "limits", "limits.h", "locale", "locale.h", "map", "math.h", "memory_resource", "mutex", "new",
                "numbers", "numeric", "optional", "ostream", "queue", "random", "ranges", "ratio", "regex",
                "scoped_allocator", "semaphore", "set", "setjmp.h", "shared_mutex", "signal.h", "source_location",
                "span", "sstream", "stack", "stdarg.h", "stdexcept", "stddef.h", "stdint.h", "stdio.h", "stdlib.h",
                "streambuf", "string", "string.h", "string_view", "stop_token", "strstream", "syncstream",
                "system_error", "thread", "time.h", "tuple", "type_traits", "typeindex", "typeinfo", "uchar.h",
                "unordered_map", "unordered_set", "utility", "valarray", "variant", "vector", "version", "wchar.h",
                "wctype.h"
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
