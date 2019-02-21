package de.retest.recheck.configuration;

import java.nio.file.Path;

public class ProjectConfigurationUtil {

	private ProjectConfigurationUtil() {

	}

	public static Path findProjectConfigurationFolder() {
		final Path projectRoot = ProjectRootFinderUtil.getProjectRoot();
		return projectRoot != null ? projectRoot.resolve( ProjectConfiguration.RETEST_PROJECT_CONFIG_FOLDER ) : null;
	}

	public static Path findProjectConfigurationFolder( final Path path ) {
		return ProjectRootFinderUtil.getProjectRoot( path ).resolve(
				ProjectConfiguration.RETEST_PROJECT_CONFIG_FOLDER );
	}

	public static Path findProjectConfiguration() {
		return findProjectConfigurationFolder().resolve( ProjectConfiguration.RETEST_PROJECT_PROPERTIES );
	}

	public static Path findProjectConfiguration( final Path path ) {
		return findProjectConfigurationFolder( path ).resolve( ProjectConfiguration.RETEST_PROJECT_PROPERTIES );
	}

}
