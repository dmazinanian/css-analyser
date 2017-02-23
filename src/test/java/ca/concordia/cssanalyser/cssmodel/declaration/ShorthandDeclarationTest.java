package ca.concordia.cssanalyser.cssmodel.declaration;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static ca.concordia.cssanalyser.cssmodel.declaration.DeclarationTestSuite.*;

public class ShorthandDeclarationTest {

	@Test
	public void testGetIndividualPropertiesForAShorthand() {		
		Set<String> individualPropertiesForAShorthandGeneral = ShorthandDeclaration.getIndividualPropertiesForAShorthand(vendorGeneral.getProperty());
		Set<String> individualPropertiesForAShorthandGeneralExpected = 
				new HashSet<>(Arrays.asList("transition-duration", "transition-delay", "transition-property", "transition-timing-function"));
		assertEquals(individualPropertiesForAShorthandGeneralExpected, individualPropertiesForAShorthandGeneral);
		
		Set<String> individualPropertiesForAShorthandWebkit = ShorthandDeclaration.getIndividualPropertiesForAShorthand(vendorWebkit.getProperty());
		Set<String> individualPropertiesForAShorthandWebkitlExpected = 
				new HashSet<>(Arrays.asList("-webkit-transition-duration", "-webkit-transition-delay", "-webkit-transition-property", "-webkit-transition-timing-function"));
		assertEquals(individualPropertiesForAShorthandWebkitlExpected, individualPropertiesForAShorthandWebkit);
		
	}

}
