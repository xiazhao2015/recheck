package de.retest.recheck.report;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

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

	Filter filter;
	AttributeDifference notFilterMe;
	AttributesDifference attributesDifference;
	IdentifyingAttributesDifference identAttributesDifference;
	Element element;
	IdentifyingAttributes identAttributes;
	Collection<ElementDifference> childDifferences;
	ElementDifference elementDifference;
	RootElementDifference rootElementDifference;
	List<RootElementDifference> rootElementDifferences;
	StateDifference stateDifference;
	ActionReplayResult actionResult;
	TestReplayResult testResult;
	SuiteReplayResult suiteResult;
	TestReport testReport;

	@BeforeEach
	void setUp() {
		final String keyToFilter = "filterMe";
		final String keyNotToFilter = "notFilterMe";

		filter = new AttributeFilter( keyToFilter );

		notFilterMe = new AttributeDifference( keyNotToFilter, null, null );

		element = mock( Element.class );
		identAttributes = mock( IdentifyingAttributes.class );
		final List<AttributeDifference> attributeDifferences =
				Arrays.asList( new AttributeDifference( keyToFilter, null, null ), notFilterMe );
		attributesDifference = new AttributesDifference( attributeDifferences );
		identAttributesDifference =
				new IdentifyingAttributesDifference( mock( IdentifyingAttributes.class ), attributeDifferences );
		final ElementDifference firstChildDifference =
				new ElementDifference( element, attributesDifference, identAttributesDifference,
						mock( Screenshot.class ), mock( Screenshot.class ), Collections.emptyList() );
		final ElementDifference secondChildDifference =
				new ElementDifference( element, attributesDifference, identAttributesDifference,
						mock( Screenshot.class ), mock( Screenshot.class ), Collections.emptyList() );
		childDifferences = Arrays.asList( firstChildDifference, secondChildDifference );
		elementDifference = new ElementDifference( element, attributesDifference, identAttributesDifference,
				mock( Screenshot.class ), mock( Screenshot.class ), childDifferences );
		when( elementDifference.getIdentifyingAttributes() ).thenReturn( identAttributes );
		when( elementDifference.getIdentifyingAttributes().identifier() ).thenReturn( "identifier" );
		rootElementDifference =
				new RootElementDifference( elementDifference, mock( RootElement.class ), mock( RootElement.class ) );
		final RootElementDifference secondRootElementDifference =
				new RootElementDifference( elementDifference, mock( RootElement.class ), mock( RootElement.class ) );
		rootElementDifferences = Arrays.asList( rootElementDifference, secondRootElementDifference );
		stateDifference = new StateDifference( rootElementDifferences, mock( DurationDifference.class ) );

		actionResult = mock( ActionReplayResult.class );
		when( actionResult.getStateDifference() ).thenReturn( stateDifference );
		testResult = new TestReplayResult( "test", 1 );
		testResult.addAction( actionResult );
		suiteResult = new SuiteReplayResult( "", 0, mock( GroundState.class ), "", mock( GroundState.class ) );
		suiteResult.addTest( testResult );
		testReport = new TestReport();
		testReport.addSuite( suiteResult );
	}

	//	@Test
	//	void Attributes_differences_should_be_filtered_properly() throws Exception {
	//		final AttributesDifference filtered =
	//				TestReportFilter.filter( mock( Element.class ), attributesDifference, filter );
	//		assertThat( filtered.getDifferences() ).containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void identifying_attributes_differences_should_be_filtered_properly() throws Exception {
	//		when( element.getIdentifyingAttributes() ).thenReturn( mock( IdentifyingAttributes.class ) );
	//		final IdentifyingAttributesDifference filtered =
	//				TestReportFilter.filter( element, identAttributesDifference, filter );
	//		assertThat( filtered.getAttributeDifferences() ).containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void collection_of_element_differences_should_be_filtered_properly() throws Exception {
	//		when( element.getIdentifyingAttributes() ).thenReturn( identAttributes );
	//		final Collection<ElementDifference> filteredChildDifferences =
	//				TestReportFilter.filter( childDifferences, filter );
	//		final List<ElementDifference> elementDifferences = new ArrayList<>( filteredChildDifferences );
	//		assertThat( elementDifferences.get( 0 ).getAttributesDifference().getDifferences() )
	//				.containsExactly( notFilterMe );
	//		assertThat( elementDifferences.get( 1 ).getAttributesDifference().getDifferences() )
	//				.containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void element_difference_and_child_differences_should_be_filtered_properly() throws Exception {
	//		when( element.getIdentifyingAttributes() ).thenReturn( identAttributes );
	//		final ElementDifference filteredElementDifference = TestReportFilter.filter( elementDifference, filter );
	//		final List<ElementDifference> childElementDiffferences =
	//				filteredElementDifference.getChildDifferences().stream()//
	//						.collect( Collectors.toList() );
	//		assertThat( filteredElementDifference.getAttributesDifference().getDifferences() )
	//				.containsExactly( notFilterMe );
	//		assertThat( childElementDiffferences.get( 0 ).getAttributesDifference().getDifferences() )
	//				.containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void filter_element_difference_should_have_no_differences_if_filtered() {
	//		final AttributeDifference attributeDifference = mock( AttributeDifference.class );
	//		final AttributesDifference attributes = mock( AttributesDifference.class );
	//		when( attributes.getDifferences() ).thenReturn( Collections.singletonList( attributeDifference ) );
	//
	//		final ElementDifference difference = mock( ElementDifference.class );
	//		when( difference.getAttributesDifference() ).thenReturn( attributes );
	//
	//		final Filter filterAll = mock( Filter.class );
	//		when( filterAll.matches( any() ) ).thenReturn( true );
	//		when( filterAll.matches( any(), any() ) ).thenReturn( true );
	//
	//		final ElementDifference filteredDifference = TestReportFilter.filter( difference, filterAll );
	//
	//		assertThat( filteredDifference.hasAttributesDifferences() ).isFalse();
	//		assertThat( filteredDifference.hasIdentAttributesDifferences() ).isFalse();
	//		assertThat( filteredDifference.isInsertionOrDeletion() ).isFalse();
	//		assertThat( filteredDifference.hasAnyDifference() ).isFalse();
	//	}
	//
	//	@Test
	//	void root_element_difference_should_be_filtered_properly() throws Exception {
	//		when( elementDifference.getIdentifyingAttributes() ).thenReturn( identAttributes );
	//		when( elementDifference.getIdentifyingAttributes().identifier() ).thenReturn( "identifier" );
	//		final RootElementDifference filteredRootElementDifference =
	//				TestReportFilter.filter( rootElementDifference, filter );
	//		final List<AttributeDifference> differences =
	//				filteredRootElementDifference.getElementDifference().getAttributesDifference().getDifferences();
	//		assertThat( differences ).containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void list_of_root_element_differences_should_be_filtered_properly() throws Exception {
	//		final List<RootElementDifference> filteredRootElementDifferences =
	//				TestReportFilter.filterRootElementDifference( rootElementDifferences, filter );
	//		final List<AttributeDifference> differences = filteredRootElementDifferences.get( 0 ).getElementDifference()
	//				.getAttributesDifference().getDifferences();
	//		assertThat( differences ).contains( notFilterMe );
	//	}
	//
	//	@Test
	//	void state_difference_should_be_filtered_properly() throws Exception {
	//		final StateDifference filteredStateDifference =
	//				TestReportFilter.filterStateDifference( stateDifference, filter );
	//		final List<AttributeDifference> differences = filteredStateDifference.getRootElementDifferences().get( 0 )
	//				.getElementDifference().getAttributesDifference().getDifferences();
	//		assertThat( differences ).containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void state_difference_should_not_throw_if_null() {
	//		final StateDifference empty = null; // This is the cause
	//		assertThatCode( () -> TestReportFilter.filterStateDifference( empty, mock( Filter.class ) ) )
	//				.doesNotThrowAnyException();
	//	}
	//
	//	@Test
	//	void state_difference_should_not_destroy_if_no_difference() {
	//		final StateDifference empty = null; // This is the cause
	//		final StateDifference difference = mock( StateDifference.class );
	//
	//		assertThat( TestReportFilter.filterStateDifference( empty, mock( Filter.class ) ) ).isEqualTo( empty );
	//		assertThat( TestReportFilter.filterStateDifference( difference, mock( Filter.class ) ) )
	//				.isEqualTo( difference );
	//	}
	//
	//	@Test
	//	void action_replay_result_should_be_filtered_properly() throws Exception {
	//		final ActionReplayResult filteredActionReplayResult =
	//				TestReportFilter.filterActionReplayResult( actionResult, filter );
	//		final List<AttributeDifference> differences = filteredActionReplayResult.getStateDifference()
	//				.getRootElementDifferences().get( 0 ).getElementDifference().getAttributesDifference().getDifferences();
	//		assertThat( differences ).containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void action_replay_result_should_not_throw_if_null() {
	//		final ActionReplayResult result = mock( ActionReplayResult.class );
	//		when( result.getStateDifference() ).thenReturn( null ); // Just to make sure that this is the cause
	//
	//		assertThatCode( () -> TestReportFilter.filterActionReplayResult( result, mock( Filter.class ) ) )
	//				.doesNotThrowAnyException();
	//	}
	//
	//	@Test
	//	void action_replay_result_should_keep_golden_master_exceptions_even_if_filtered() {
	//		final ActionReplayResult noGoldenMasterActionResult = mock( NoGoldenMasterActionReplayResult.class );
	//
	//		final Filter noFilter = mock( Filter.class );
	//		final Filter doFilter = mock( Filter.class );
	//		when( doFilter.matches( any(), any() ) ).thenReturn( true );
	//		when( doFilter.matches( any() ) ).thenReturn( true );
	//
	//		assertThat( TestReportFilter.filterActionReplayResult( noGoldenMasterActionResult, noFilter ) )
	//				.isEqualTo( noGoldenMasterActionResult );
	//		assertThat( TestReportFilter.filterActionReplayResult( noGoldenMasterActionResult, doFilter ) )
	//				.isEqualTo( noGoldenMasterActionResult );
	//	}
	//
	//	@Test
	//	void test_replay_result_should_be_filtered_properly() throws Exception {
	//		final TestReplayResult filteredTestReplayResult = TestReportFilter.filterTestReplayResult( testResult, filter );
	//		final List<AttributeDifference> differences = filteredTestReplayResult.getActionReplayResults().get( 0 )
	//				.getStateDifference().getRootElementDifferences().get( 0 ).getElementDifference()
	//				.getAttributesDifference().getDifferences();
	//		assertThat( differences ).containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void suite_replay_result_should_be_filtered_properly() throws Exception {
	//		final SuiteReplayResult filteredSuiteReplayResult = TestReportFilter.filterSuite( suiteResult, filter );
	//		final StateDifference stateDifference = filteredSuiteReplayResult.getTestReplayResults().get( 0 )
	//				.getActionReplayResults().get( 0 ).getStateDifference();
	//		final List<AttributeDifference> differences = stateDifference.getRootElementDifferences().get( 0 )
	//				.getElementDifference().getAttributesDifference().getDifferences();
	//		assertThat( differences ).containsExactly( notFilterMe );
	//	}
	//
	//	@Test
	//	void test_report_should_be_filtered_properly() throws Exception {
	//		final TestReport filteredTestReport = TestReportFilter.filter( testReport, filter );
	//		final SuiteReplayResult suiteReplayResult = filteredTestReport.getSuiteReplayResults().get( 0 );
	//		final ActionReplayResult actionReplayResult =
	//				suiteReplayResult.getTestReplayResults().get( 0 ).getActionReplayResults().get( 0 );
	//		final List<AttributeDifference> differences = actionReplayResult.getStateDifference()
	//				.getRootElementDifferences().get( 0 ).getElementDifference().getAttributesDifference().getDifferences();
	//		assertThat( differences ).containsExactly( notFilterMe );
	//	}
}
