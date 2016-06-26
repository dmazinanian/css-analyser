package ca.concordia.cssanalyser.cssmodel.media;

import static ca.concordia.cssanalyser.cssmodel.media.MediaTestSuite.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MediaQueryTest {
	
	@Test
	public void testMediaQueryEquals() {
		assertFalse(mq_only_query.mediaQueryEquals(mq_type_feature));
		assertFalse(mq_only_query.mediaQueryEquals(mq_same_type_diff_feature));
		assertFalse(mq_same_type_diff_feature.mediaQueryEquals(mq_diff_type_same_feature));
		assertFalse(mq_only_type.mediaQueryEquals(mq_only_query));
		assertTrue(mq_type_feature.mediaQueryEquals(mq_same_media_query));
		assertFalse(mq_mutiple_features.mediaQueryEquals(mq_multiple_features_different));
		assertTrue(mq_multiple_features_different.mediaQueryEquals(mq_mutiple_features_diff_order));
		assertFalse(mq_with_prefix.mediaQueryEquals(mq_with_diff_prefix));
	}

}
