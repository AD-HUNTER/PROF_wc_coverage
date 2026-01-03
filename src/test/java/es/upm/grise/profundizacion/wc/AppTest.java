package es.upm.grise.profundizacion.wc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AppTest {

    private static Path testFile = Paths.get("ejemplo.txt");
    private final PrintStream originalOut = System.out;

    @BeforeAll
    public static void setup() throws IOException {
        Files.writeString(testFile, "kjdbvws wonvwofjw\n sdnfwijf ooj    kjndfohwouer 21374 vehf\n jgfosj\n\nskfjwoief ewjf\n\n\ndkfgwoihgpw vs wepfjwfin");
    }

    @AfterAll
    public static void teardown() {
        try {
            Files.deleteIfExists(testFile);
        } catch (IOException e) {
            System.err.println("Error deleting test file: " + e.getMessage());
            try {
                Thread.sleep(100);
                Files.deleteIfExists(testFile);
            } catch (IOException | InterruptedException ex) {
                System.err.println("Failed to delete test file on retry: " + ex.getMessage());
            }
        }
    }

    @Test
    public void testUsageMessageWhenNoArgs() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        App.main(new String[] {});

        assertEquals("Usage: wc [-clw file]\n".trim(), output.toString().trim());
    }

    private String runAppCapture(String[] args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            App.main(args);
        } finally {
            System.setOut(originalOut);
        }
        return out.toString().trim();
    }

    @Test
    public void testWrongArguments() {
        String out = runAppCapture(new String[] { "-c" });
        assertEquals("Wrong arguments!", out);
    }

    @Test
    public void testCannotFindFile() {
        String fname = "no_such_file_12345.txt";
        String out = runAppCapture(new String[] { "-c", fname });
        assertEquals("Cannot find file: " + fname, out);
    }

    @Test
    public void testCommandsDoNotStartWithDash() {
        String out = runAppCapture(new String[] { "c", testFile.toString() });
        assertEquals("The commands do not start with -", out);
    }

    @Test
    public void testUnrecognizedCommand() {
        String out = runAppCapture(new String[] { "-x", testFile.toString() });
        assertEquals("Unrecognized command: x", out);
    }

    @Test
    public void testCountSingleCommandC() throws IOException {
        Counter counter = new Counter(Files.newBufferedReader(testFile));
        int expectedC = counter.getNumberCharacters();
        String out = runAppCapture(new String[] { "-c", testFile.toString() });
        assertEquals(expectedC + "\t" + testFile.toString(), out);
    }

    @Test
    public void testCountMultipleCommandsOrder() throws IOException {
        Counter counter = new Counter(Files.newBufferedReader(testFile));
        int expectedC = counter.getNumberCharacters();
        int expectedL = counter.getNumberLines();
        int expectedW = counter.getNumberWords();

        String out1 = runAppCapture(new String[] { "-clw", testFile.toString() });
        assertEquals(expectedC + "\t" + expectedL + "\t" + expectedW + "\t" + testFile.toString(), out1);

        String out2 = runAppCapture(new String[] { "-wlc", testFile.toString() });
        assertEquals(expectedW + "\t" + expectedL + "\t" + expectedC + "\t" + testFile.toString(), out2);
    }

    @Test
    public void testNoCommandsAfterDash() {
        String out = runAppCapture(new String[] { "-", testFile.toString() });
        // App imprime "\t" + fileName, pero runAppCapture hace trim(), por eso comparamos con el nombre solo
        assertEquals(testFile.toString(), out);
    }

    @Test
    public void testAppHandlesIOExceptionFromCounter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try (MockedConstruction<FileReader> mockFileReader = mockConstruction(FileReader.class);
             MockedConstruction<BufferedReader> mocked = mockConstruction(BufferedReader.class, (mock, ctx) -> {
                 when(mock.read()).thenThrow(new IOException("forced"));
             })) {
            App.main(new String[] { "-c", "ignored.txt" });
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("Error reading file: ignored.txt", out.toString().trim());
    }
}
