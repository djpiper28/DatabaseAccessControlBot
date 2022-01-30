package cards.monarch.db.tests;

import cards.monarch.db.database.DatabaseLogin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDatabaseLogin {

    @BeforeAll
    static void createUserTxt() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File("user.txt"));
        pw.println("user");
        pw.println("pass");
        pw.close();
    }

    @Test
    public void testRead() throws IOException {
        DatabaseLogin dbl = new DatabaseLogin("", 0, "");
        assertTrue(dbl.getUsername().equals("user"));
        assertTrue(dbl.getPassword().equals("pass"));
    }

    @AfterAll
    static void testFileNotFound() {
        File f = new File("user.txt");
        assertTrue(f.delete());
        assertThrows(IOException.class, () -> {
           DatabaseLogin dbl = new DatabaseLogin("", 0, "");
        });
    }

}
