package system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import system.DAO.LoginDao;
import system.model.User;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private LoginDao loginDao;

    public List<User> getAllUsers() {
        return loginDao.getAllUsers();
    }
}
