package bayern.steinbrecher.sourceIncludeVisualizer.cpp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class CPPIncludeGraphGenerator extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = Logger.getLogger(CPPIncludeGraphGenerator.class.getName());
    private static final PathMatcher FILE_FORMAT_MATCHER
            = FileSystems.getDefault().getPathMatcher("glob:**/*.{h,hpp,hxx,cc,cpp,cxx}");
    private static final Pattern INCLUDE_PATTERN
            = Pattern.compile("^\\s*#include\\s*[<\"](?<includeName>[\\w.]+(/[\\w.]+)*)[>\"]");
    private final Map<String, Collection<String>> includeDependencies = new HashMap<>();

    public CPPIncludeGraphGenerator() {
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (FILE_FORMAT_MATCHER.matches(file)) {
            // FIXME It is assumed that every file included from anywhere has an unique name
            Collection<String> dependencies = Files.lines(file)
                    .parallel()
                    .map(INCLUDE_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .map(m -> m.group("includeName"))
                    .map(first -> {
                        try {
                            return Paths.get(first);
                        } catch (InvalidPathException ex) {
                            LOGGER.log(
                                    Level.WARNING, String.format("'%s' in '%s' is not a valid path", first, file), ex);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .map(Path::getFileName)
                    .map(Objects::toString)
                    .collect(Collectors.toList());
            includeDependencies.put(file.getFileName().toString(), dependencies);
        }
        return super.visitFile(file, attrs);
    }

    public Map<String, Collection<String>> getIncludeDependencies(){
        return Collections.unmodifiableMap(includeDependencies);
    }
}
