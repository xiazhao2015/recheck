package de.retest.recheck.configuration;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ProjectRootFinderUtil {

	private static final Logger logger = LoggerFactory.getLogger( ProjectRootFinder.class );

	private static final Set<ProjectRootFinder> projectRootFinder = Sets.newHashSet( new MavenProjectRootFinder() );

	private ProjectRootFinderUtil() {

	}

	public static Path getProjectRoot() {
		return projectRootFinder.stream() //
				.map( ProjectRootFinder::findProjectRoot ) //
				.filter( Optional::isPresent ) //
				.map( Optional::get ) //
				.findAny() //
				.orElse( noProjectRootFoundWarning() );
	}

	public static Path getProjectRoot( final Path basePath ) {
		return projectRootFinder.stream() //
				.map( finder -> finder.findProjectRoot( basePath ) ) //
				.filter( Optional::isPresent ) //
				.map( Optional::get ) //
				.findAny() //
				.orElse( noProjectRootFoundWarning( basePath ) );
	}

	private static Path noProjectRootFoundWarning( final Path... basePath ) {
		logger.warn( "Project root not found in {} or any parent folder.",
				basePath.length < 1 ? "current workdir" : basePath );
		return null;
	}
}
