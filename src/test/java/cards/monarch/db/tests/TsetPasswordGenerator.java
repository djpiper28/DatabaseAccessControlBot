package cards.monarch.db.tests;

import cards.monarch.db.commands.PasswordGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TsetPasswordGenerator {

    @Test
    public void testPasswordGenerator() {
        String password = PasswordGenerator.getPassword();
        assertTrue(password != "");
        assertTrue(password.length() == PasswordGenerator.PASSWORD_LEN);
    }

}
