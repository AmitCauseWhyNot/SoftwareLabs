package lab2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class WindowCloseTest {
    @TempDir Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"login", "welcome"})
    void closeButtonTerminatesApplicationFromEitherScreen(String screen) throws Exception {
        Path users = directory.resolve("users.txt");
        Path output = directory.resolve("process.txt");
        Files.writeString(users, "student@haifa.ac.il abcd123!\n");
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"),
                "lab2.WindowCloseProbe", screen, users.toString())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(15, TimeUnit.SECONDS),
                    "Application did not exit after closing the " + screen + " screen");
            assertEquals(0, process.exitValue(), Files.readString(output));
            assertTrue(Files.readString(output).contains("Closed " + screen));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly().waitFor();
            }
        }
    }
}
