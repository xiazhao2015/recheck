package de.retest.recheck;

import java.nio.file.Path;
import java.util.ArrayList;

import de.retest.recheck.report.SuiteReplayResult;
import de.retest.recheck.report.TestReport;
import de.retest.recheck.suite.ExecutableSuite;
import de.retest.recheck.ui.descriptors.GroundState;

public class SuiteAggregator {

	private static SuiteAggregator instance;

	public static SuiteAggregator getInstance() {
		if ( instance == null ) {
			instance = new SuiteAggregator();
		}
		return instance;
	}

	/**
	 * Must only be called from a test
	 */
	public static void reset() {
		instance = null;
	}

	static SuiteAggregator getTestInstance() {
		return new SuiteAggregator();
	}

	private SuiteAggregator() {}

	private final TestReport aggregatedTestReport = TestReport.fromApi();

	private SuiteReplayResult currentSuite;

	public TestReport getAggregatedTestReport() {
		return aggregatedTestReport;
	}

	public SuiteReplayResult getSuite( final String suiteName ) {
		return getSuite( suiteName, null );
	}

	public SuiteReplayResult getSuite( final String suiteName, final Path testSourceRoot ) {
		if ( currentSuite == null ) {
			currentSuite = createSuiteReplayResult( suiteName, testSourceRoot );
		}
		if ( !suiteName.equals( currentSuite.getName() ) ) {
			currentSuite = createSuiteReplayResult( suiteName, testSourceRoot );
		}
		return currentSuite;
	}

	private SuiteReplayResult createSuiteReplayResult( final String suiteName, final Path testSourceRoot ) {
		final GroundState groundState = new GroundState();
		final ExecutableSuite execSuite = new ExecutableSuite( groundState, 0, new ArrayList<>() );
		execSuite.setName( suiteName );
		final SuiteReplayResult suiteReplayResult =
				new SuiteReplayResult( suiteName, testSourceRoot, 0, groundState, execSuite.getUuid(), groundState );
		aggregatedTestReport.addSuite( suiteReplayResult );
		return suiteReplayResult;
	}
}
