import java.util.ArrayList;
import java.util.List;

public class UserList {
    public static List<User> users = new ArrayList<>();

    public static void createAndAddUser(String firstName, String lastName, String email) {
        int ID = users.size()+1;
        User user = new User(ID, firstName, lastName,  email,true,0);
        users.add(user);
    }
    public static void createAndAddUser(int ID, String firstName, String lastName, String email, boolean active, int points) {
        User user = new User(ID, firstName, lastName, email, active, points);
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

    public static void clearUsers() {
        users.clear();
    }
}
