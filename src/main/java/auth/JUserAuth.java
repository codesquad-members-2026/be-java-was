package auth;

import org.mindrot.jbcrypt.BCrypt;

public class JUserAuth {

    public static String hashPassword(String userInput){
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(userInput, salt);
    }

    public static boolean checkPassword(String userPasswordFromDB, String userInput){
        return BCrypt.checkpw(userInput, userPasswordFromDB);
    }

}
