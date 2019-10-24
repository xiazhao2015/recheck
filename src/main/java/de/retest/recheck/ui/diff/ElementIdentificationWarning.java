package de.retest.recheck.ui.diff;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Structurally stores the information about a test breakage caused by the
 * <a href="https://retest.github.io/docs/recheck-web/element-identification-problem/">GUI element identification
 * problem</a>. This information can be re-used by review or recheck.cli when the user applies a change.
 */
@Data
@RequiredArgsConstructor
public class ElementIdentificationWarning implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private final String testFileName;

	@XmlAttribute
	private final Integer testLineNumber;

	@XmlAttribute
	private final String findByMethodName;

	@XmlAttribute
	private final String qualifiedTestName;

	@SuppressWarnings( "unused" )
	private ElementIdentificationWarning() {
		testFileName = null;
		testLineNumber = null;
		findByMethodName = null;
		qualifiedTestName = null;
	}
}
