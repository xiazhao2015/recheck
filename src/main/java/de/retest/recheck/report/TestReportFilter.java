package de.retest.recheck.report;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.retest.recheck.NoGoldenMasterActionReplayResult;
import de.retest.recheck.ignore.Filter;
import de.retest.recheck.report.action.ActionReplayData;
import de.retest.recheck.ui.actions.TargetNotFoundException;
import de.retest.recheck.ui.descriptors.SutState;
import de.retest.recheck.ui.diff.AttributesDifference;
import de.retest.recheck.ui.diff.ElementDifference;
import de.retest.recheck.ui.diff.IdentifyingAttributesDifference;
import de.retest.recheck.ui.diff.InsertedDeletedElementDifference;
import de.retest.recheck.ui.diff.LeafDifference;
import de.retest.recheck.ui.diff.RootElementDifference;
import de.retest.recheck.ui.diff.StateDifference;

public class TestReportFilter {

	private TestReportFilter() {}

	public static TestReport filter( final TestReport report, final Filter filter ) {
		final TestReport filteredReport = new TestReport();

		report.getSuiteReplayResults() //
				.forEach( suite -> filteredReport.addSuite( filterSuite( suite, filter ) ) );

		return filteredReport;
	}

	private static SuiteReplayResult filterSuite( final SuiteReplayResult result, final Filter filter ) {
		final SuiteReplayResult newResult = newSuiteReplayResult( result );

		result.getTestReplayResults() //
				.forEach( test -> newResult.addTest( filterTest( test, filter ) ) );

		return newResult;
	}

	private static SuiteReplayResult newSuiteReplayResult( final SuiteReplayResult result ) {
		return new SuiteReplayResult( result.getName(), result.getSuiteNr(), result.getExecSuiteSutVersion(),
				result.getSuiteUuid(), result.getReplaySutVersion() );
	}

	public static TestReplayResult filterTest( final TestReplayResult result, final Filter filter ) {
		final TestReplayResult newResult = getNewTestReplayResult( result );

		result.getActionReplayResults() //
				.forEach( action -> newResult.addAction( filterAction( action, filter ) ) );
		return newResult;
	}

	private static TestReplayResult getNewTestReplayResult( final TestReplayResult result ) {
		return new TestReplayResult( result.getName(), result.getTestNr() );
	}

	private static ActionReplayResult filterAction( final ActionReplayResult result, final Filter filter ) {
		if ( result instanceof NoGoldenMasterActionReplayResult ) {
			return result;
		}
		return getActionReplayResult( result, filterStateDifference( result.getStateDifference(), filter ) );
	}

	private static ActionReplayResult getActionReplayResult( final ActionReplayResult result,
			final StateDifference filteredStateDifference ) {
		return ActionReplayResult.createActionReplayResult(
				ActionReplayData.withTarget( result.getDescription(), result.getTargetComponent(),
						result.getGoldenMasterPath() ),
				result.getThrowableWrapper(), (TargetNotFoundException) result.getTargetNotFoundException(),
				filteredStateDifference, result.getDuration(), new SutState( result.getWindows() ) );
	}

	private static StateDifference filterStateDifference( final StateDifference stateDifference, final Filter filter ) {
		if ( stateDifference == null || stateDifference.getRootElementDifferences().isEmpty() ) {
			return stateDifference;
		}
		return new StateDifference( filterRootElementDifferences( stateDifference.getRootElementDifferences(), filter ),
				stateDifference.getDurationDifference() );
	}

	private static List<RootElementDifference> filterRootElementDifferences( final List<RootElementDifference> diffs,
			final Filter filter ) {
		return diffs.stream().map( diff -> filterRootElementDifference( diff, filter ) ).collect( Collectors.toList() );
	}

	private static RootElementDifference filterRootElementDifference( final RootElementDifference diff,
			final Filter filter ) {
		return new RootElementDifference( filterElementDifference( diff.getElementDifference(), filter ),
				diff.getExpectedDescriptor(), diff.getActualDescriptor() );
	}

	private static ElementDifference filterElementDifference( final ElementDifference diff, final Filter filter ) {

		final AttributesDifference attributesDifference = getFilteredAttributesDifference( diff, filter );

		final LeafDifference identDifference = getIdentAttributeDifference( diff, filter );

		final Collection<ElementDifference> childDifferences = getChildDifferences( diff, filter );

		return new ElementDifference( diff.getElement(), attributesDifference, identDifference,
				diff.getExpectedScreenshot(), diff.getActualScreenshot(), childDifferences );
	}

	private static AttributesDifference getFilteredAttributesDifference( final ElementDifference diff,
			final Filter filter ) {
		final AttributesDifference attributesDifference = diff.getAttributesDifference();
		if ( diff.hasAttributesDifferences() ) {
			return attributesDifference.getDifferences().stream() //
					.filter( d -> !filter.matches( diff.getElement(), d ) ) //
					.collect( Collectors.collectingAndThen( Collectors.toList(), AttributesDifference::new ) );
		}
		return attributesDifference;
	}

	private static LeafDifference getIdentAttributeDifference( final ElementDifference diff, final Filter filter ) {
		if ( !diff.hasIdentAttributesDifferences()
				|| diff.getIdentifyingAttributesDifference() instanceof InsertedDeletedElementDifference ) {
			return diff.getIdentifyingAttributesDifference();
		}

		return ((IdentifyingAttributesDifference) diff.getIdentifyingAttributesDifference()).getAttributeDifferences()
				.stream() //
				.filter( d -> !filter.matches( diff.getElement(), d ) ) //
				.collect( Collectors.collectingAndThen( Collectors.toList(),
						diffs -> new IdentifyingAttributesDifference( diff.getIdentifyingAttributes(), diffs ) ) );

	}

	private static Collection<ElementDifference> getChildDifferences( final ElementDifference diff,
			final Filter filter ) {
		final Collection<ElementDifference> childDifferences = diff.getChildDifferences();

		if ( !childDifferences.isEmpty() ) {
			return childDifferences.stream().map( d -> filterElementDifference( d, filter ) ) //
					.filter( d -> !filter.matches( d.getElement() ) ) //
					.collect( Collectors.toList() );
		}

		return childDifferences;
	}

}
