package com.dubture.doctrine.test;

import org.eclipse.php.core.tests.runner.PDTTList;
import org.eclipse.php.core.tests.runner.PDTTList.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robert Gruendler <r.gruendler@gmail.com>
 */
@RunWith(PDTTList.class)
public class DoctrineAnnotationCodeAssistTest extends AbstractCodeAssistTest {

	@Parameters
	public static final String[] TEST_DIRS = { "/workspace/annotations" };
	
	public DoctrineAnnotationCodeAssistTest(String[] fileNames) {
		super(fileNames, "DoctrineAnnotations", "/workspace/codeassist_stubs"); 
	}

	@Test
	public void test(String fileName) throws Exception {
		runPdttTest(fileName);
	}

}
