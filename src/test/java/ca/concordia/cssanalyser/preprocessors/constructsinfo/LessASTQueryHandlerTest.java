package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class LessASTQueryHandlerTest {

	private static final LessASTQueryHandler QUERY_HANDLER = ConstructsInfoSuite.getQueryHandler();
		
	@Test
	public void getMixinCallInfoTest() {
		List<LessMixinCall> mixinCallInfo = QUERY_HANDLER.getMixinCallInfo();
		assertEquals(1, mixinCallInfo.get(0).getNumberOfParameters());
		assertEquals(1, mixinCallInfo.get(1).getNumberOfParameters());
		assertEquals(2, mixinCallInfo.get(2).getNumberOfParameters());
	}

}
