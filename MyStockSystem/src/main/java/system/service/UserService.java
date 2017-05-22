package system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import system.DAO.LoginDao;

@Service
public class UserService {

    @Autowired
    private LoginDao loginDao;
}
