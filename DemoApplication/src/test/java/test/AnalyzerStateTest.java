package test;

import static org.junit.Assert.fail;

import org.apache.http.util.Asserts;
import org.junit.Test;

import model.AnalyzerState;

public class AnalyzerStateTest {

	AnalyzerState analyzerState;
	
	@Test
	public void testConstruction() {
		analyzerState = new AnalyzerState();
		Asserts.notNull(analyzerState, "AnalyzerState is null after construction.");
	}
	
	

}
