package ca.concordia.cssanalyser.cssmodel.media;

import static org.junit.Assert.*;

import static ca.concordia.cssanalyser.cssmodel.media.MediaTestSuite.*;

import org.junit.Test;

public class MediaFeatureExpressionTest {

	@Test
	public void testMediFeatureExpressionEquals() {
		assertFalse(mfex.mediaFeatureEquals(mfex_diff_feature_diff_expr));
		assertFalse(mfex.mediaFeatureEquals(mfex_same_feature_diff_expr)); 						
		assertFalse(mfex.mediaFeatureEquals(mfex_diff_feature_same_expr));
		assertFalse(mfex.mediaFeatureEquals(mfex_diff_feature_diff_expr));
	}

}
