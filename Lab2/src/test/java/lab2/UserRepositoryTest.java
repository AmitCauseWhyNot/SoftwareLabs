package lab2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    @TempDir Path directory;

    @Test
    void loadsOnlyValidRowsAndContinuesAfterErrors() throws IOException {
        Path file = directory.resolve("users.txt");
        Files.writeString(file, "  first@b.co   abcd123!  \r\n"
                + "invalid abcd123!\r\nsecond@b.co\r\n"
                + "second@b.co abcd123! extra\r\n\r\n"
                + "\tsecond@b.co\tABCD123!\t", StandardCharsets.UTF_8);
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        ArrayList<User> users = UserRepository.load(file, new PrintStream(errors));
        assertEquals(2, users.size());
        assertEquals("first@b.co", users.get(0).getName());
        assertTrue(UserRepository.authenticate(users, "second@b.co", "ABCD123!"));
        assertEquals(4, errors.toString(StandardCharsets.UTF_8).lines().count());
    }

    @Test
    void preservesDuplicateNamesAndChecksTheirOwnPasswords() throws IOException {
        Path file = directory.resolve("users.txt");
        Files.writeString(file, "a@b.co abcd123!\na@b.co xyz1234!\nb@b.co ABCD123!");
        ArrayList<User> users = UserRepository.load(file, System.err);
        assertEquals(3, users.size());
        assertTrue(UserRepository.authenticate(users, "a@b.co", "abcd123!"));
        assertTrue(UserRepository.authenticate(users, "a@b.co", "xyz1234!"));
        assertFalse(UserRepository.authenticate(users, "a@b.co", "ABCD123!"));
        assertFalse(UserRepository.authenticate(users, "missing@b.co", "abcd123!"));
        assertFalse(UserRepository.authenticate(users, null, null));
    }

    @Test
    void emptyFileDoesNotAllowAnyLogin() throws IOException {
        Path file = Files.createFile(directory.resolve("empty.txt"));
        ArrayList<User> users = UserRepository.load(file, System.err);
        assertTrue(users.isEmpty());
        assertFalse(UserRepository.authenticate(users, "a@b.co", "abcd123!"));
        assertFalse(UserRepository.authenticate(List.of(), "", ""));
    }

    @Test
    void missingFileIsReportedAsIoFailure() {
        assertThrows(IOException.class,
                () -> UserRepository.load(directory.resolve("missing.txt"), System.err));
    }

    @Test
    void suppliedLab1DataHasFourValidUsers() throws IOException {
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        ArrayList<User> users = UserRepository.load(Path.of("users.txt"), new PrintStream(errors));
        assertEquals(4, users.size());
        assertEquals(5, errors.toString(StandardCharsets.UTF_8).lines().count());
        assertTrue(UserRepository.authenticate(users, "B_Kernighan@mail.com", "!a01011942"));
        assertTrue(UserRepository.authenticate(users, "+first_error@this.row", "passw0rd%"));
        assertFalse(UserRepository.authenticate(users, "password@error.now", "~helloworld"));
    }
}
