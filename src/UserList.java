import java.util.ArrayList;
import java.util.List;

public class UserList {
    public static List<User> users = new ArrayList<>();

    public static void createAndAddUser(String firstName, String lastName, String email) {
        int ID = users.size()+1;
        User user = new User(firstName, lastName, ID, email,true,0);
        users.add(user);
    }

    public static boolean emailExists(String email) {
        for (User user : users) {
            if (email.equals(user.email)) {
                return true;
            }
        }
        return false;
    }
}
