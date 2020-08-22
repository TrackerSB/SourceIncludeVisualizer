package bayern.steinbrecher.sourceIncludeVisualizer.cpp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class CPPIncludeGraphGenerator extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = Logger.getLogger(CPPIncludeGraphGenerator.class.getName());
    private static final PathMatcher FILE_FORMAT_MATCHER
            = FileSystems.getDefault().getPathMatcher("glob:**/*.{h,hpp,hxx,cpp,cxx}");

    public CPPIncludeGraphGenerator() {
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (FILE_FORMAT_MATCHER.matches(file)) {
            LOGGER.log(Level.FINE, String.format("Matches: '%s'", file.toString()));
        } else {
            LOGGER.log(Level.FINE, String.format("Matches not: '%s'", file.toString()));
        }
        return super.visitFile(file, attrs);
    }
}
