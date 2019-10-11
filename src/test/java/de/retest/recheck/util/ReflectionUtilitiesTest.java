package de.retest.recheck.util;

import static de.retest.recheck.util.ReflectionUtilities.getSimpleName;
import static de.retest.recheck.util.ReflectionUtilities.setChildInParentToNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Field;

import javax.swing.JMenu;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import de.retest.recheck.ui.image.Screenshot;
import de.retest.recheck.util.ReflectionUtilities.IncompatibleTypesException;

public class ReflectionUtilitiesTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void getField_should_find_field_in_superclass() {
		final Field field = ReflectionUtilities.getField( JMenu.class, "mouseListener" );
		assertThat( field ).isNotNull();
	}

	@Test
	@Ignore
	public void getField_should_return_null_if_no_field() {
		// instead of throwing an exception
		// this is important if we check for fields that get added by Javaagent
		final Field field = ReflectionUtilities.getField( JMenu.class, "feblededdu" );
		assertThat( field ).isNull();
	}

	@Test
	public void isThreadDeathWhileClosingSuT_returns_true_on_direct() throws Exception {
		final Throwable e = getThreadDeathWhileClosingSuTEx();
		final boolean result = ReflectionUtilities.isThreadDeathWhileClosingSuT( e );
		assertThat( result ).isTrue();
	}

	@Test
	public void isThreadDeathWhileClosingSuT_returns_true_on_child() throws Exception {
		final Throwable e = new Exception( getThreadDeathWhileClosingSuTEx() );
		final boolean result = ReflectionUtilities.isThreadDeathWhileClosingSuT( e );
		assertThat( result ).isTrue();
	}

	@Test
	public void isThreadDeathWhileClosingSuT_returns_true_on_grandchild() throws Exception {
		final Throwable e = new Throwable( new Exception( getThreadDeathWhileClosingSuTEx() ) );
		final boolean result = ReflectionUtilities.isThreadDeathWhileClosingSuT( e );
		assertThat( result ).isTrue();
	}

	@Test
	public void isThreadDeathWhileClosingSuT_returns_false() throws Exception {
		// wrong method
		final ThreadDeath e1 = Mockito.mock( ThreadDeath.class );
		@SuppressWarnings( "restriction" )
		final StackTraceElement[] value1 =
				{ new StackTraceElement( sun.awt.AppContext.class.getCanonicalName(), "disposeNOT", "", 0 ) };
		Mockito.when( e1.getStackTrace() ).thenReturn( value1 );

		// wrong class
		final ThreadDeath e2 = Mockito.mock( ThreadDeath.class );
		final StackTraceElement[] value2 =
				{ new StackTraceElement( Object.class.getCanonicalName(), "dispose", "", 0 ) };
		Mockito.when( e2.getStackTrace() ).thenReturn( value2 );
		Mockito.when( e2.getCause() ).thenReturn( e1 );

		// wrong Exception
		final Exception e3 = Mockito.mock( Exception.class );
		@SuppressWarnings( "restriction" )
		final StackTraceElement[] value3 =
				{ new StackTraceElement( sun.awt.AppContext.class.getCanonicalName(), "dispose", "", 0 ) };
		Mockito.when( e3.getStackTrace() ).thenReturn( value3 );
		Mockito.when( e3.getCause() ).thenReturn( e2 );

		assertThat( ReflectionUtilities.isThreadDeathWhileClosingSuT( e3 ) ).isFalse();
	}

	private static ThreadDeath getThreadDeathWhileClosingSuTEx() {
		final ThreadDeath e = Mockito.mock( ThreadDeath.class );
		@SuppressWarnings( "restriction" )
		final StackTraceElement[] value =
				{ new StackTraceElement( sun.awt.AppContext.class.getCanonicalName(), "dispose", "", 0 ) };
		Mockito.when( e.getStackTrace() ).thenReturn( value );
		return e;
	}

	@Test
	public void instanceOf_Class_String_should_handle_equal_types() throws Exception {
		final Class<?> instanceClass = A.class;
		final String className = instanceClass.getName();

		final boolean actual = ReflectionUtilities.instanceOf( instanceClass, className );

		assertThat( actual ).isTrue();
	}

	@Test
	public void instanceOf_Class_String_should_handle_nested_types() throws Exception {
		final Class<?> instanceClass = B.class;
		final String className = instanceClass.getName();

		final boolean actual = ReflectionUtilities.instanceOf( instanceClass, className );

		assertThat( actual ).isTrue();
	}

	@Test
	public void instanceOf_Class_String_should_handle_multiple_nested_types() throws Exception {
		final Class<?> instanceClass = C.class;
		final String className = instanceClass.getName();

		final boolean actual = ReflectionUtilities.instanceOf( instanceClass, className );

		assertThat( actual ).isTrue();
	}

	@Test
	public void instanceOf_Class_String_should_handle_unrelated_types() throws Exception {
		final Class<?> instanceClass = A.class;
		final String className = B.class.getName();

		final boolean actual = ReflectionUtilities.instanceOf( instanceClass, className );

		assertThat( actual ).isFalse();
	}

	@Test
	public void instanceOf_Class_String_should_handle_usage_of_toString() throws Exception {
		final Class<?> instanceClass = A.class;
		final String className = instanceClass.toString();

		expectedException.expect( IllegalArgumentException.class );
		expectedException.expectMessage( "Class name starts with 'class '. This is probably due to the use of "
				+ "Object#toString(), whereas Class#getName() should be used." );
		ReflectionUtilities.instanceOf( instanceClass, className );
	}

	@Test
	public void instanceOf_String_Class_should_handle_existent_types() throws Exception {
		final Class<?> clazz = A.class;
		final String instanceClassName = clazz.getName();

		final boolean actual = ReflectionUtilities.instanceOf( instanceClassName, clazz );

		assertThat( actual ).isTrue();
	}

	@Test
	public void instanceOf_String_Class_should_handle_inexistent_types() throws Exception {
		final Class<?> clazz = A.class;
		final String instanceClassName = "SomeUnkownType";

		final boolean actual = ReflectionUtilities.instanceOf( instanceClassName, clazz );

		assertThat( actual ).isFalse();
	}

	@Test
	public void setChildInParentToNull_should_set_multiple_reference_to_null() throws Exception {
		final Object a = new Object();
		final Pair<Object, Object> pair = Pair.of( a, a );

		setChildInParentToNull( pair, a );

		assertThat( pair.getLeft() ).isNull();
		assertThat( pair.getRight() ).isNull();
	}

	@Test
	public void setChildInParentToNull_should_set_only_correct_reference_to_null() throws Exception {
		final Object a = new Object();
		final Pair<Object, Object> pair = Pair.of( a, new Object() );

		setChildInParentToNull( pair, a );

		assertThat( pair.getLeft() ).isNull();
		assertThat( pair.getRight() ).isNotNull();
	}

	@Test
	public void setField_should_throw_meaningful_IncompatibleTypesException_on_wrong_types() {
		try {
			ReflectionUtilities.setField( new IntField(), "field", "asd" );
			fail( "Should throw exception!" );
		} catch ( final IncompatibleTypesException e ) {
			// expected
		}

		try {
			ReflectionUtilities.setField( new NotYetImplementedField(), "field", "asd" );
			fail( "Should throw exception!" );
		} catch ( final IncompatibleTypesException e ) {
			// expected
		}
	}

	@Test
	public void hasMethod_should_return_true_if_method_exist() throws Exception {
		assertThat( ReflectionUtilities.hasMethod( Object.class, "toString" ) ).isTrue();
		assertThat( ReflectionUtilities.hasMethod( Object.class, "equals", Object.class ) ).isTrue();
	}

	@Test
	public void hasMethod_should_return_false_if_method_NOT_exist() throws Exception {
		assertThat( ReflectionUtilities.hasMethod( Object.class, "notExisting" ) ).isFalse();
		assertThat( ReflectionUtilities.hasMethod( Object.class, "equals" ) ).isFalse();
		assertThat( ReflectionUtilities.hasMethod( Object.class, "equals", Object.class, Object.class ) ).isFalse();
	}

	@Test
	public void getSimpleName_should_be_robust() {
		assertThat( getSimpleName( "" ) ).isEqualTo( "" );
		assertThat( getSimpleName( "Main" ) ).isEqualTo( "Main" );
		assertThat( getSimpleName( null ) ).isEqualTo( null );
	}

	@Test
	public void getSimpleName_should_return_classname() throws Exception {
		assertThat( getSimpleName( "de.retest.Main" ) ).isEqualTo( "Main" );
		assertThat( getSimpleName( "de.retest.Main$1" ) ).isEqualTo( "Main$1" );
	}

	private static class IntField {
		@SuppressWarnings( "unused" )
		private final int field = 0;
	}

	private static class NotYetImplementedField {
		@SuppressWarnings( "unused" )
		private final Screenshot field = null;
	}

	private class A {};

	private class B extends A {};

	private class C extends B {};
}
