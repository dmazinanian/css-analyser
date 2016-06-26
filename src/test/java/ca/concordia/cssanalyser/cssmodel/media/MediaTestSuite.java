package ca.concordia.cssanalyser.cssmodel.media;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.fixturesutil.FixturesUtil;

@RunWith(Suite.class)
@SuiteClasses({ 
	MediaFeatureExpressionTest.class,
	MediaQueryTest.class,
	MediaQueryListTest.class
})
public class MediaTestSuite {
	
	static MediaQueryList mql_1mq_only_feature,
			mql_1mq_type_feature,
			mql_1mq_same_type_diff_feature,
			mql_1mq_different_type_same_feature,
			mql_1mq_only_type,
			mql_2mq,
			mql_2mq_same_query_diff_order,
			mql_2mq_with_prefix,
			mql_3mq,
			mql_1mq_multiple_features,
			mql_1mq_multi_feature_diff_query,
			mql_1mq_multi_feature_diff_order;
	
	static MediaQuery 
			mq_only_query,				
			mq_type_feature,
			mq_same_type_diff_feature,
			mq_diff_type_same_feature,
			mq_only_type,
			mq_same_media_query,
			mq_mutiple_features,
			mq_multiple_features_different,
			mq_mutiple_features_diff_order,
			mq_with_prefix,
			mq_with_diff_prefix;
	
	static MediaFeatureExpression
			mfex,
			mfex_same_feature_diff_expr,
			mfex_diff_feature_same_expr,
			mfex_diff_feature_diff_expr;

	
	@BeforeClass
	public static void setUpOnce() {
		mql_1mq_only_feature 				= getMediaQueryListFromString("@media (min-width: 480px)");                                       
		mql_1mq_type_feature				= getMediaQueryListFromString("@media screen and (min-width: 480px)");                            
		mql_1mq_same_type_diff_feature		= getMediaQueryListFromString("@media screen and (min-width: 1024px)");                            
		mql_1mq_different_type_same_feature	= getMediaQueryListFromString("@media print and (min-width: 480px)");                             
		mql_1mq_only_type 					= getMediaQueryListFromString("@media print");                                                    
		mql_2mq								= getMediaQueryListFromString("@media (min-width: 700px), handheld and (orientation: landscape)");
		mql_2mq_same_query_diff_order 		= getMediaQueryListFromString("@media handheld and (orientation: landscape), (min-width: 700px)");
		mql_2mq_with_prefix 				= getMediaQueryListFromString("@media not screen and (color), screen and (color)");    
		mql_3mq								= getMediaQueryListFromString("@media screen and (min-width: 480px), not screen and (color), print and (color)");
		mql_1mq_multiple_features			= getMediaQueryListFromString("@media screen and (min-width: 480px) and (color) and (orientation: landscape)");
		mql_1mq_multi_feature_diff_query	= getMediaQueryListFromString("@media screen and (max-width: 480px) and (color) and (orientation: portrait)");
		mql_1mq_multi_feature_diff_order	= getMediaQueryListFromString("@media screen and (color) and (max-width: 480px) and (orientation: portrait)");
		
		mq_only_query 					= getMediaQuery(mql_1mq_only_feature, 0);
		mq_type_feature					= getMediaQuery(mql_1mq_type_feature, 0);
		mq_same_type_diff_feature		= getMediaQuery(mql_1mq_same_type_diff_feature, 0);
		mq_diff_type_same_feature		= getMediaQuery(mql_1mq_different_type_same_feature, 0);
		mq_only_type 					= getMediaQuery(mql_1mq_only_type, 0);
		mq_same_media_query 			= getMediaQuery(mql_3mq, 0);
		mq_mutiple_features				= getMediaQuery(mql_1mq_multiple_features, 0);
		mq_multiple_features_different 	= getMediaQuery(mql_1mq_multi_feature_diff_query, 0);
		mq_mutiple_features_diff_order	= getMediaQuery(mql_1mq_multi_feature_diff_order, 0);
		mq_with_prefix 					= getMediaQuery(mql_2mq_with_prefix, 0);
		mq_with_diff_prefix				= getMediaQuery(mql_2mq_with_prefix, 1);
		
		mfex 						= getMediaFeatureExpression(mq_only_query, 0);                    
		mfex_same_feature_diff_expr = getMediaFeatureExpression(mq_same_type_diff_feature, 0);
		mfex_diff_feature_same_expr = getMediaFeatureExpression(mq_multiple_features_different, 0);
		mfex_diff_feature_diff_expr = getMediaFeatureExpression(mq_mutiple_features_diff_order, 0);
		
	}
	
	private static MediaQueryList getMediaQueryListFromString(String mediQueryString) {
		StyleSheet styleSheetFromString = FixturesUtil.getStyleSheetFromString(mediQueryString + "{ .dummy {} }");
		List<Selector> selectorsList = FixturesUtil.getSelectorsList(styleSheetFromString);
		return selectorsList.get(0).getMediaQueryLists().iterator().next();
	}
	
	public static MediaQuery getMediaQuery(MediaQueryList mediaQueryList, int index) {
		int i = 0;
		for (MediaQuery mq : mediaQueryList) {
			if (i++ == index)
				return mq;
		}
		return null;
	}
	
	public static MediaFeatureExpression getMediaFeatureExpression(MediaQuery mediaQuery, int index) {
		int i = 0;
		for (MediaFeatureExpression mfex : mediaQuery.getMediaFeatureExpressions()) {
			if (i++ == index)
				return mfex;
		}
		return null;
	}
}
