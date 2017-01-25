/*  Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package net.davidtanzer.babysteps;

import java.nio.file.Paths;

import org.junit.Test;

import com.github.approval.Approval;
import com.github.approval.pathmappers.ParentPathMapper;
import com.github.approval.reporters.Reporters;

public class BabystepsTimerTest {
	private static final Approval<String> APPROVER = Approval.of(String.class)
            .withReporter(Reporters.console())
            .withPathMapper(new ParentPathMapper<String>(Paths.get("src", "test", "resources", BabystepsTimerTest.class.getPackage().getName().replaceAll(".", "/"))))
            .build();
	static {
		BabystepsTimer.setQuitHandler(() -> {});
	}
	
	@Test
	public void startAndQuitApp() throws Exception {
		startApp();
		BabystepsTimer.quit();
		verifyLogOutputFor("startAndQuitApp");
	}

	private void verifyLogOutputFor(String testMethodName) {
		String fileName = testMethodName + ".txt";
		APPROVER.verify(BabystepsTimer.getLogLines().toString(), Paths.get(fileName));
	}

	private void startApp() throws InterruptedException {
		BabystepsTimer.main(null);
	}
}
