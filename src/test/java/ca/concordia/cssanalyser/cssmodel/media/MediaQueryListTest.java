package ca.concordia.cssanalyser.cssmodel.media;

import static org.junit.Assert.*;
import static ca.concordia.cssanalyser.cssmodel.media.MediaTestSuite.*;

import org.junit.Test;

public class MediaQueryListTest {

	@Test
	public void mediaQueryListEquals() {
		assertTrue(mql_2mq.mediaQueryListEquals(mql_2mq_same_query_diff_order));
		assertFalse(mql_1mq_type_feature.mediaQueryListEquals(mql_1mq_same_type_diff_feature));
		assertFalse(mql_1mq_type_feature.mediaQueryListEquals(mql_1mq_different_type_same_feature));
		assertFalse(mql_1mq_same_type_diff_feature.mediaQueryListEquals(mql_1mq_different_type_same_feature));
	}

}
