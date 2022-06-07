package dev.jbang.source;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.jbang.dependencies.DependencyResolver;

/**
 * Code is an interface for classes representing different code bases
 * (sources/jars) that can be used as, or turned into, executable code.
 */
public interface Code {

	/**
	 * Returns the reference to resource to be executed. This contains both the
	 * original reference (which can be a URL or Maven GAV or other kinds of
	 * non-file resource references) and a path to the file that contains the actual
	 * resource (which can be an ephemeral temporary/cached file).
	 */
	ResourceRef getResourceRef();

	/**
	 * Returns the path to the main application JAR file. This can be an existing
	 * JAR file or one that was generated by JBang.
	 */
	Path getJarFile();

	/**
	 * Returns the main class of the application JAR file or `null` if this can't be
	 * determined.
	 */
	default String getMainClass() {
		return null;
	}

	/**
	 * Returns the runtime Java options that should be passed to the `java`
	 * executable when the application gets run.
	 */
	default List<String> getRuntimeOptions() {
		return Collections.emptyList();
	}

	/**
	 * Determines if CDS has been enabled for this Source
	 */
	default boolean enableCDS() {
		return false;
	}

	/**
	 * Returns the requested Java version. Returns `Optional.empty()` if no version
	 * was set.
	 */
	default String getJavaVersion() {
		return null;
	}

	/**
	 * Returns the resource's description. Returns `Optional.empty()` if no
	 * description is available (or if the description is an empty string).
	 */
	default Optional<String> getDescription() {
		return Optional.empty();
	}

	/**
	 * Returns the resource's Maven GAV. Returns `Optional.empty()` if no GAV is
	 * available.
	 */
	default Optional<String> getGav() {
		return Optional.empty();
	}

	/**
	 * Updates the given resolver with the dependencies required by this Source
	 * object
	 */
	DependencyResolver updateDependencyResolver(DependencyResolver resolver);

	default boolean isJar() {
		return Code.isJar(getResourceRef().getFile());
	}

	static boolean isJar(Path backingFile) {
		return backingFile != null && backingFile.toString().endsWith(".jar");
	}

	default boolean isJShell() {
		return Code.isJShell(getResourceRef().getFile());
	}

	static boolean isJShell(Path backingFile) {
		return backingFile != null && backingFile.toString().endsWith(".jsh");
	}

	/**
	 * Returns the Jar associated with this Input or `null` if there is none.
	 */
	Jar asJar();

	/**
	 * Returns the SourceSet associated with this Input or `null` if there is none.
	 */
	SourceSet asSourceSet();

	default boolean needsBuild(RunContext context) {
		// anything but .jar and .jsh files needs jar
		return !(isJar() || isJShell() || context.isForceJsh() || context.isInteractive());
	}

	Builder builder(RunContext ctx);

	CmdGenerator cmdGenerator(RunContext ctx);

	// https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
	static List<String> quotedStringToList(String subjectString) {
		List<String> matchList = new ArrayList<>();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(subjectString);
		while (regexMatcher.find()) {
			if (regexMatcher.group(1) != null) {
				// Add double-quoted string without the quotes
				matchList.add(regexMatcher.group(1));
			} else if (regexMatcher.group(2) != null) {
				// Add single-quoted string without the quotes
				matchList.add(regexMatcher.group(2));
			} else {
				// Add unquoted word
				matchList.add(regexMatcher.group());
			}
		}
		return matchList;
	}
}
