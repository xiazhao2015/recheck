package de.retest.recheck;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.retest.recheck.report.ActionReplayResult;
import de.retest.recheck.ui.DefaultValueFinder;
import de.retest.recheck.ui.descriptors.RootElement;

/**
 * Interface to help recheck transform an arbitrary object into its internal format to allow persistence, state diffing
 * and ignoring of attributes and elements.
 */
public interface RecheckAdapter {

	/**
	 * Returns an instance of the RecheckAdapter, that has been initialized with the given {@link RecheckOptions}. In
	 * case more specific recheck options are used during initialization of the {@link RecheckImpl}, then these are
	 * passed on (and need to be cast).
	 */
	default RecheckAdapter initialize( final RecheckOptions opts ) {
		return this;
	}

	/**
	 * Returns {@code true} if the given object can be converted by the adapter.
	 *
	 * @param toCheck
	 *            the object to check
	 * @return true if the given object can be converted by the adapter
	 */
	boolean canCheck( Object toCheck );

	/**
	 * Convert the given object into a {@code RootElement} (respectively into a set of {@code RootElement}s if this is
	 * sensible for this type of object).
	 *
	 * @param toCheck
	 *            the object to check
	 * @return The RootElement(s) for the given object
	 */
	Set<RootElement> convert( Object toCheck );

	/**
	 * Return some metadata with respect to the checked object. For e.g. a selenium driver, this could be things like
	 * browser name and version.
	 *
	 * @param toCheck
	 *            the object to check
	 *
	 * @return The meta data for the given object
	 */
	default Map<String, String> retrieveMetadata( final Object toCheck ) {
		return Collections.emptyMap();
	}

	/**
	 * Returns a {@code DefaultValueFinder} for the converted element attributes. Default values of attributes are
	 * omitted in the Golden Master to not bloat it.
	 *
	 * @return The DefaultValueFinder for the converted element attributes
	 */
	DefaultValueFinder getDefaultValueFinder();

	/**
	 * Notifies about differences in the given {@code ActionReplayResult}.
	 *
	 * @param actionReplayResult
	 *            The {@code ActionReplayResult} containing differences to be notified about.
	 */
	default void notifyAboutDifferences( final ActionReplayResult actionReplayResult ) {}

}
