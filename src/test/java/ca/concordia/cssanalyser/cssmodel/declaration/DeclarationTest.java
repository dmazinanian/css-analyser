package ca.concordia.cssanalyser.cssmodel.declaration;

import static ca.concordia.cssanalyser.cssmodel.declaration.DeclarationTestSuite.vendorGeneral;
import static ca.concordia.cssanalyser.cssmodel.declaration.DeclarationTestSuite.vendorMoz;
import static ca.concordia.cssanalyser.cssmodel.declaration.DeclarationTestSuite.vendorMs;
import static ca.concordia.cssanalyser.cssmodel.declaration.DeclarationTestSuite.vendorO;
import static ca.concordia.cssanalyser.cssmodel.declaration.DeclarationTestSuite.vendorWebkit;
import static org.junit.Assert.*;

import org.junit.Test;

public class DeclarationTest {

	@Test
	public void testGetNonVendorProperty() {
		assertEquals("transition", Declaration.getNonVendorProperty(vendorGeneral.property));
		assertEquals("transition", Declaration.getNonVendorProperty(vendorWebkit.property));
		assertEquals("transition", Declaration.getNonVendorProperty(vendorO.property));
		assertEquals("transition", Declaration.getNonVendorProperty(vendorMs.property));
		assertEquals("transition", Declaration.getNonVendorProperty(vendorMoz.property));
		
	}
	
	@Test
	public void testGetVendorPrefixForProperty() {
		assertEquals("", Declaration.getVendorPrefixForProperty(vendorGeneral.property));
		assertEquals("-webkit-", Declaration.getVendorPrefixForProperty(vendorWebkit.property));
		assertEquals("-o-", Declaration.getVendorPrefixForProperty(vendorO.property));
		assertEquals("-ms-", Declaration.getVendorPrefixForProperty(vendorMs.property));
		assertEquals("-moz-", Declaration.getVendorPrefixForProperty(vendorMoz.property));
	}
	
	@Test
	public void testCanHaveVendorPrefixedProperty() {
		assertTrue((Declaration.canHaveVendorPrefixedProperty(vendorGeneral.property)));
		assertTrue((Declaration.canHaveVendorPrefixedProperty(vendorWebkit.property)));
	}
	
}
