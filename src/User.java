public class User {
    String firstName;
    String lastName;
    int ID;
    String email;
    boolean active;
    int points;

    public User(int ID, String firstName, String lastName,  String email, boolean active, int points) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ID = ID;
        this.email = email;
        this.active = active;
        this.points = points;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getID() {
        return ID;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public int getPoints() {
        return points;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPoints(int points) {
        this.points = points;
    }


}
