package de.retest.recheck.review.ignore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import de.retest.recheck.ignore.Filter;
import de.retest.recheck.ui.descriptors.Element;
import de.retest.recheck.ui.diff.AttributeDifference;

class PixelDiffFilterTest {

	double pixelDiff = 5.0;
	Filter cut = new PixelDiffFilter( pixelDiff );

	@Test
	void should_filter_diff_when_pixel_diff_is_not_exceeded() throws Exception {
		final Rectangle expected = new Rectangle( 0, 0, 10, 10 );
		final Rectangle actual = new Rectangle( 1, -1, 15, 5 );
		final AttributeDifference diff = new AttributeDifference( "outline", expected, actual );

		assertThat( cut.matches( mock( Element.class ), diff ) ).isTrue();
	}

	@Test
	void should_not_filter_diff_when_pixel_diff_is_exceeded() throws Exception {
		final Rectangle expected = new Rectangle( 0, 0, 10, 10 );
		final Rectangle actual = new Rectangle( 1, -1, 15, 5 );
		final AttributeDifference diff = new AttributeDifference( "outline", expected, actual );

		final Filter cut = new PixelDiffFilter( 0.0 );

		assertThat( cut.matches( mock( Element.class ), diff ) ).isFalse();
	}

	@Test
	void should_handle_pixel_strings_with_integers_and_floats() throws Exception {
		final String expected = "50px";
		final String actual = "45.3px";
		final AttributeDifference diff = new AttributeDifference( "foo", expected, actual );

		assertThat( cut.matches( mock( Element.class ), diff ) ).isTrue();
	}

	@Test
	void should_handle_pixel_strings_with_negative_integers_and_floats() throws Exception {
		final String expected = "-50px";
		final String actual = "-45.3px";
		final AttributeDifference diff = new AttributeDifference( "foo", expected, actual );

		assertThat( cut.matches( mock( Element.class ), diff ) ).isTrue();
	}

	@Test
	void should_handle_pixel_strings_with_different_decimal_separators() throws Exception {
		final String expected = "50.0px";
		final String actual = "45,3px";
		final AttributeDifference diff = new AttributeDifference( "foo", expected, actual );

		assertThat( cut.matches( mock( Element.class ), diff ) ).isTrue();
	}

	@Test
	void should_skip_nulls() throws Exception {
		final String expected = null;
		final String actual = null;
		final AttributeDifference diff = new AttributeDifference( "foo", expected, actual );

		assertThat( cut.matches( mock( Element.class ), diff ) ).isFalse();
	}

	@Test
	void should_skip_non_pixel_strings() throws Exception {
		final String expected = "bar";
		final String actual = "baz";
		final AttributeDifference diff = new AttributeDifference( "foo", expected, actual );

		assertThat( cut.matches( mock( Element.class ), diff ) ).isFalse();
	}

}