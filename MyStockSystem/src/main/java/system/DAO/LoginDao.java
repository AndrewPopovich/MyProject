package system.DAO;

import org.springframework.stereotype.Repository;
import system.model.User;

import java.util.Arrays;
import java.util.List;

@Repository
public class LoginDao {
    private List<User> users = Arrays.asList(new User("admin", "admin"),
            new User("user1", " qwerty"));

    public List<User> getAllUsers() {
        return users;
    }
}
