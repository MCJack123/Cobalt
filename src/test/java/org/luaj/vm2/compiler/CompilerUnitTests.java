package org.luaj.vm2.compiler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CompilerUnitTests {
	public String fileName;

	public CompilerUnitTests(String file) {
		fileName = file;
	}

	@Before
	public void setup() throws Exception {
		JsePlatform.standardGlobals();
	}

	@Test
	public void compareBytecode() throws Exception {
		CompileTestHelper.compareResults("/lua5.1-tests/", fileName);
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> getTests() {
		Object[][] tests = {
			{"all"},
			{"api"},
			{"attrib"},
			{"big"},
			{"calls"},
			{"checktable"},
			{"closure"},
			{"code"},
			{"constructs"},
			{"db"},
			{"errors"},
			{"events"},
			{"files"},
			{"gc"},
			{"literals"},
			{"locals"},
			{"main"},
			{"math"},
			{"nextvar"},
			{"pm"},
			{"sort"},
			{"strings"},
			{"vararg"},
			{"verybig"},
		};

		return Arrays.asList(tests);
	}
}
