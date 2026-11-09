package lab3;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class WindowCloseTest {
    @ParameterizedTest @ValueSource(strings = {"login", "welcome", "blocked"})
    void closingAnyScreenStopsTheProcess(String mode) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Process process = new ProcessBuilder(java, "-cp", classpath, "lab3.WindowCloseProbe", mode)
                .redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Process survived window close: " + mode);
            assertEquals(0, process.exitValue(), new String(process.getInputStream().readAllBytes()));
        } finally { if (process.isAlive()) process.destroyForcibly(); }
    }
}
