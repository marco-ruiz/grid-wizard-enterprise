package org.gwe.utils;

import java.util.List;

import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * 
 * 
 * @author Marco Ruiz
 * @since Aug 25, 2007
 */
@RunWith(value=Parameterized.class)
public abstract class AbstractRandomTest<PT> {

	private PT testScenario;
	private List<PT> testScenarios;
	
	public AbstractRandomTest(PT testScenario) {
		this.testScenario = testScenario;
	}

	public AbstractRandomTest(List<PT> testScenarios) {
		this.testScenarios = testScenarios;
	}

	public PT getTestScenario() {
		return testScenario;
	}

	public List<PT> getTestScenarios() {
		return testScenarios;
	}

	@After
	public void tearDown() {
		testScenario = null;
	}

//	public PT generateRandomTestScenario(Object... generatorParams) { return null; }
}
