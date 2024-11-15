package com.infodation.userservice.services;

import com.infodation.userservice.models.User;
import com.infodation.userservice.models.dto.user.CreateUserDTO;
import com.infodation.userservice.models.dto.user.UpdateUserDTO;
import com.infodation.userservice.repositories.UserRepository;
import com.infodation.userservice.services.iservice.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;

    @Override
    public Page<User> getAll(Pageable pageable, String name) {
        if (name == null) {
            name = "";
        }
        if (!name.isEmpty()) {
            System.out.println("Searching for name: " + name);
            return userRepository.findByName(name, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }




    @Override
    public User getByUserId(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public User save(CreateUserDTO user) {
        User newUser = new User();
        newUser.setUserId(user.getUserId());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setEmail(user.getEmail());
        newUser.setSex(user.getSex());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(newUser);
    }

    @Override
    public User update(String useId, UpdateUserDTO user) {
        User userToUpdate = userRepository.findByUserId(useId).orElse(null);

        if (userToUpdate == null) {
            return null;
        }

        userToUpdate.setFirstName(user.getFirstName());
        userToUpdate.setLastName(user.getLastName());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setSex(user.getSex());
        userToUpdate.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(userToUpdate);
    }

    @Override
    public void delete(String userId) {
        userRepository.deleteByUserId(userId);
    }
}
