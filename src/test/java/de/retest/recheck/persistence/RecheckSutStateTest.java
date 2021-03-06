package de.retest.recheck.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.retest.recheck.RecheckAdapter;
import de.retest.recheck.ui.descriptors.RootElement;
import de.retest.recheck.ui.descriptors.SutState;

class RecheckSutStateTest {

	@Test
	void loadExpected_with_existing_folder_but_missing_retest_xml_should_return_null( @TempDir final Path temp ) {
		final File existingFolder = temp.resolve( "folder" ).toFile();
		existingFolder.mkdirs();

		assertThat( RecheckSutState.loadExpected( existingFolder ) ).isNull();
	}

	@Test
	void loadExpected_with_not_existing_file_should_return_null( @TempDir final Path temp ) {
		final File notExistingFile = temp.resolve( "asdwgerafaasd" ).toFile();

		assertThat( notExistingFile ).doesNotExist();
		assertThat( RecheckSutState.loadExpected( notExistingFile ) ).isNull();
	}

	@Test
	void convert_should_throw_exception_if_adapter_returns_null() {
		final Object convert = mock( Object.class );
		final RecheckAdapter adapter = mock( RecheckAdapter.class );
		when( adapter.convert( any() ) ).thenReturn( null );

		assertThatThrownBy( () -> RecheckSutState.convert( convert, adapter ) )
				.isInstanceOf( IllegalStateException.class );
	}

	@Test
	void convert_should_throw_exception_if_adapter_returns_empty_set() {
		final Object convert = mock( Object.class );
		final RecheckAdapter adapter = mock( RecheckAdapter.class );
		when( adapter.convert( any() ) ).thenReturn( Collections.emptySet() );

		assertThatThrownBy( () -> RecheckSutState.convert( convert, adapter ) )
				.isInstanceOf( IllegalStateException.class );
	}

	@Test
	void convert_should_create_state_if_adapter_returns_set() {
		final Object convert = mock( Object.class );
		final RecheckAdapter adapter = mock( RecheckAdapter.class );
		when( adapter.convert( any() ) ).thenReturn( Collections.singleton( mock( RootElement.class ) ) );

		assertThat( RecheckSutState.convert( convert, adapter ) ).isNotNull();
	}

	@Test
	void convert_should_respect_metadata() {
		final Object convert = mock( Object.class );
		final RecheckAdapter adapter = mock( RecheckAdapter.class );
		when( adapter.convert( any() ) ).thenReturn( Collections.singleton( mock( RootElement.class ) ) );
		final Map<String, String> metadata = new HashMap<>();
		metadata.put( "someKey", "someValue" );
		when( adapter.retrieveMetadata( any() ) ).thenReturn( metadata );

		assertThat( RecheckSutState.convert( convert, adapter ).getMetadata( "someKey" ) ).isEqualTo( "someValue" );
	}

	@Test
	void convert_should_add_global_metadata() throws Exception {
		final Object convert = mock( Object.class );
		final RecheckAdapter adapter = mock( RecheckAdapter.class );
		when( adapter.convert( any() ) ).thenReturn( Collections.singleton( mock( RootElement.class ) ) );

		final SutState state = RecheckSutState.convert( convert, adapter );

		assertThat( state.getMetadata() ).isNotEmpty();
	}

	@Test
	void convert_should_not_check_object_is_null_and_let_adapter_handle() {
		final RecheckAdapter adapter = mock( RecheckAdapter.class );
		when( adapter.convert( any() ) ).thenReturn( Collections.singleton( mock( RootElement.class ) ) );

		assertThat( RecheckSutState.convert( null, adapter ) ).isNotNull();
		verify( adapter ).convert( null );
	}
}
