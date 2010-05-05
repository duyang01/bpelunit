package org.bpelunit.framework.control.deploy.activebpel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.bpelunit.framework.exception.ConfigurationException;
import org.bpelunit.framework.exception.DeploymentException;
import org.bpelunit.framework.exception.SpecificationException;
import org.bpelunit.test.util.TestTestRunner;
import org.junit.Test;

/**
 * End-to-end test case using the ActiveBPEL engine. Requires an installed and
 * <em>running</em> instance installed in an application server set up under the
 * path specified in the CATALINA_HOME environment variable. The server should
 * use the default configuration values used in
 * {@link org.bpelunit.framework.control.deploy.activebpel.ActiveBPELDeployer}.
 * 
 * @author Antonio García-Domínguez
 */
public class ActiveBPELEndToEndTest {
	private static final String TEST_SUITE_DIR = "resources/engines";
	private static final String TEST_SUITE_FNAME = "tacService-activebpel.bpts";
	private static final String TEST_SUITE_ENDLESS_FNAME = "tacService-activebpel-endless.bpts";

	@Test
	public void allTestCasesPass() throws ConfigurationException,
			DeploymentException, SpecificationException {
		checkAssumptions();

		TestTestRunner runner = new TestTestRunner(TEST_SUITE_DIR,
				TEST_SUITE_FNAME);
		runner.testRun();
		assertEquals("All test cases should pass", 4, runner.getPassed());
	}

	@Test
	public void endlessLoopIsTerminated() throws ConfigurationException,
			DeploymentException, SpecificationException {
		checkAssumptions();

		TestTestRunner runner = new TestTestRunner(TEST_SUITE_DIR, TEST_SUITE_ENDLESS_FNAME);
		runner.testRun();
		assertEquals("Only the case with empty input passed",
				1, runner.getPassed());
		assertTrue("Some processes were terminated",
				ActiveBPELDeployer._terminatedProcessCount >= 1);
	}

	private void checkAssumptions() {
		assumeNotNull(System
				.getenv(ActiveBPELDeployer.DEFAULT_APPSERVER_DIR_ENVVAR));
		assumeTrue(new File(System
				.getenv(ActiveBPELDeployer.DEFAULT_APPSERVER_DIR_ENVVAR), "bpr")
				.canRead());
	}
}