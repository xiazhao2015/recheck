package de.retest.recheck.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.retest.recheck.NoGoldenMasterActionReplayResult;
import de.retest.recheck.ignore.Filter;
import de.retest.recheck.review.ignore.AttributeFilter;
import de.retest.recheck.ui.descriptors.Element;
import de.retest.recheck.ui.descriptors.GroundState;
import de.retest.recheck.ui.descriptors.IdentifyingAttributes;
import de.retest.recheck.ui.descriptors.RootElement;
import de.retest.recheck.ui.diff.AttributeDifference;
import de.retest.recheck.ui.diff.AttributesDifference;
import de.retest.recheck.ui.diff.DurationDifference;
import de.retest.recheck.ui.diff.ElementDifference;
import de.retest.recheck.ui.diff.IdentifyingAttributesDifference;
import de.retest.recheck.ui.diff.RootElementDifference;
import de.retest.recheck.ui.diff.StateDifference;
import de.retest.recheck.ui.image.Screenshot;

class TestReportFilterTest {

	TestReport testReport;
	SuiteReplayResult suiteResult;
	TestReplayResult testResult;
	ActionReplayResult actionResult = mock( ActionReplayResult.class );

	StateDifference stateDifference;

	Element element = mock( Element.class );

	ElementDifference elementDifference;

	Collection<ElementDifference> childDifferences;

	final String keyToFilter = "outline";
	final String keyNotToFilter = "text";

	final String identKeyToFilter = "class";
	final String identKeyNotToFilter = "path";

	AttributesDifference attributesDifference;

	IdentifyingAttributes identAttributes = mock( IdentifyingAttributes.class );
	IdentifyingAttributesDifference identAttributesDifference;

	Filter filter;

	@BeforeEach
	void setUp() {
		filter = new AttributeFilter( keyToFilter );

		attributesDifference = new AttributesDifference( getAttributeDifferences() );
		final IdentifyingAttributesDifference identAttributeDiffs =
				new IdentifyingAttributesDifference( identAttributes, getIdentifyingAttributeDifferences() );
		elementDifference = new ElementDifference( element, attributesDifference, identAttributeDiffs, null, null,
				getChildDifferences() );
		when( elementDifference.getIdentifyingAttributes() ).thenReturn( identAttributes );
		when( elementDifference.getIdentifyingAttributes().identifier() ).thenReturn( "identifier" );

		stateDifference = getStateDifference();

		when( actionResult.getStateDifference() ).thenReturn( stateDifference );
		when( actionResult.getAllElementDifferences() ).thenReturn( Collections.singletonList( elementDifference ) );

		testResult = new TestReplayResult( "test", 1 );
		testResult.addAction( actionResult );

		suiteResult = new SuiteReplayResult( "", 0, mock( GroundState.class ), "", mock( GroundState.class ) );
		suiteResult.addTest( testResult );

		testReport = new TestReport();
		testReport.addSuite( suiteResult );
	}

	private StateDifference getStateDifference() {
		return new StateDifference( //
				Collections.singletonList( new RootElementDifference( elementDifference, mock( RootElement.class ),
						mock( RootElement.class ) ) ),
				mock( DurationDifference.class ) );
	}

	private List<AttributeDifference> getIdentifyingAttributeDifferences() {
		return Arrays.asList( //
				new AttributeDifference( identKeyToFilter, "Class", "class" ), //
				new AttributeDifference( identKeyNotToFilter, "html[1]/div[1]", "html[1]/div[3]" ) );
	}

	private List<ElementDifference> getChildDifferences() {
		return Arrays.asList( //
				new ElementDifference( element, attributesDifference, identAttributesDifference,
						mock( Screenshot.class ), mock( Screenshot.class ), Collections.emptyList() ) );
	}

	private List<AttributeDifference> getAttributeDifferences() {
		return Arrays.asList( //
				new AttributeDifference( keyToFilter, "10px", "18px" ), //
				new AttributeDifference( keyNotToFilter, "foobar", "barfoo" ) );
	}

	@Test
	void state_difference_should_be_filtered_properly() throws Exception {
		final StateDifference filteredStateDifference =
				TestReportFilter.filterStateDifference( stateDifference, filter );
		final List<String> differences = filteredStateDifference.getRootElementDifferences().get( 0 )
				.getElementDifference().getAttributeDifferences().stream().map( AttributeDifference::getKey )
				.collect( Collectors.toList() );
		assertThat( differences ).containsExactlyInAnyOrder( keyNotToFilter, identKeyNotToFilter, identKeyToFilter )
				.doesNotContain( keyToFilter );
	}

	@Test
	void state_difference_should_not_throw_if_null() {
		final StateDifference empty = null; // This is the cause
		assertThatCode( () -> TestReportFilter.filterStateDifference( empty, mock( Filter.class ) ) )
				.doesNotThrowAnyException();
	}

	@Test
	void state_difference_should_not_destroy_if_no_difference() {
		final StateDifference empty = null; // This is the cause
		final StateDifference difference = mock( StateDifference.class );

		assertThat( TestReportFilter.filterStateDifference( empty, mock( Filter.class ) ) ).isEqualTo( empty );
		assertThat( TestReportFilter.filterStateDifference( difference, mock( Filter.class ) ) )
				.isEqualTo( difference );
	}

	@Test
	void action_replay_result_should_be_filtered_properly() throws Exception {
		final ActionReplayResult filteredActionReplayResult = TestReportFilter.filterAction( actionResult, filter );
		final List<String> differences = filteredActionReplayResult.getStateDifference().getRootElementDifferences()
				.get( 0 ).getElementDifference().getAttributeDifferences().stream().map( AttributeDifference::getKey )
				.collect( Collectors.toList() );
		assertThat( differences ).containsExactlyInAnyOrder( keyNotToFilter, identKeyNotToFilter, identKeyToFilter )
				.doesNotContain( keyToFilter );
	}

	@Test
	void action_replay_result_should_not_throw_if_null() {
		final ActionReplayResult result = mock( ActionReplayResult.class );
		when( result.getStateDifference() ).thenReturn( null ); // Just to make sure that this is the cause

		assertThatCode( () -> TestReportFilter.filterAction( result, mock( Filter.class ) ) )
				.doesNotThrowAnyException();
	}

	@Test
	void action_replay_result_should_keep_golden_master_exceptions_even_if_filtered() {
		final ActionReplayResult noGoldenMasterActionResult = mock( NoGoldenMasterActionReplayResult.class );

		final Filter noFilter = mock( Filter.class );
		final Filter doFilter = mock( Filter.class );
		when( doFilter.matches( any(), any() ) ).thenReturn( true );
		when( doFilter.matches( any() ) ).thenReturn( true );

		assertThat( TestReportFilter.filterAction( noGoldenMasterActionResult, noFilter ) )
				.isEqualTo( noGoldenMasterActionResult );
		assertThat( TestReportFilter.filterAction( noGoldenMasterActionResult, doFilter ) )
				.isEqualTo( noGoldenMasterActionResult );
	}

	@Test
	void test_replay_result_should_be_filtered_properly() throws Exception {
		final TestReplayResult filteredTestReplayResult = TestReportFilter.filterTest( testResult, filter );
		final List<String> differences = filteredTestReplayResult.getActionReplayResults().get( 0 ).getStateDifference()
				.getRootElementDifferences().get( 0 ).getElementDifference().getAttributeDifferences().stream()
				.map( AttributeDifference::getKey ).collect( Collectors.toList() );
		assertThat( differences ).containsExactlyInAnyOrder( keyNotToFilter, identKeyNotToFilter, identKeyToFilter )
				.doesNotContain( keyToFilter );
	}

	@Test
	void suite_replay_result_should_be_filtered_properly() throws Exception {
		final SuiteReplayResult filteredSuiteReplayResult = TestReportFilter.filterSuite( suiteResult, filter );
		final StateDifference stateDifference = filteredSuiteReplayResult.getTestReplayResults().get( 0 )
				.getActionReplayResults().get( 0 ).getStateDifference();
		final List<String> differences = stateDifference.getRootElementDifferences().get( 0 ).getElementDifference()
				.getAttributeDifferences().stream().map( AttributeDifference::getKey ).collect( Collectors.toList() );
		assertThat( differences ).containsExactlyInAnyOrder( keyNotToFilter, identKeyNotToFilter, identKeyToFilter )
				.doesNotContain( keyToFilter );
	}

	@Test
	void filter_attributes_of_testreport() {
		final TestReport filteredTestReport = TestReportFilter.filter( testReport, filter );
		final List<String> filteredKeys = filteredTestReport.getSuiteReplayResults().get( 0 ).getTestReplayResults()
				.get( 0 ).getActionReplayResults().get( 0 ).getAllElementDifferences().get( 0 )
				.getAttributeDifferences().stream().map( AttributeDifference::getKey ).collect( Collectors.toList() );
		assertThat( filteredKeys ).doesNotContain( keyToFilter ).containsExactlyInAnyOrder( keyNotToFilter,
				identKeyNotToFilter, identKeyToFilter );
	}
}
