package foi.air.szokpt.accountmng.services;

import foi.air.szokpt.accountmng.entitites.User;
import foi.air.szokpt.accountmng.entitites.UserRole;
import foi.air.szokpt.accountmng.exceptions.NotFoundException;
import foi.air.szokpt.accountmng.exceptions.ValidationException;
import foi.air.szokpt.accountmng.repositories.RoleRepository;
import foi.air.szokpt.accountmng.repositories.UserRepository;
import foi.air.szokpt.accountmng.util.hashing.Hasher;
import foi.air.szokpt.accountmng.util.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Validator<User> updateUserValidator;
    private final Validator<User> registerUserValidator;
    private final Hasher hasher;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       @Qualifier("registerUserValidator") Validator<User> registerUserValidator,
                       @Qualifier("updateUserValidator") Validator<User> updateUserValidator,
                       Hasher hasher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.updateUserValidator = updateUserValidator;
        this.registerUserValidator = registerUserValidator;
        this.hasher = hasher;
    }

    public List<User> getUsers() {
        return userRepository.findAll(Sort.by("id"));
    }

    public void registerUser(User user) {
        registerUserValidator.validateData(user);
        assignRoleToUser(user, user.getRole().getName());
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(hasher.hashText(user.getPassword()));
        userRepository.save(user);
    }


    public User getUser(int id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException());
    }

    public void updateUser(int id, User newUserData) {
        Optional<User> optionalExistingUser = userRepository.findById(id);
        if (optionalExistingUser.isPresent()) {
            newUserData.setId(id);
            updateUserValidator.validateData(newUserData);
            User existingUser = optionalExistingUser.get();
            saveUserUpdate(existingUser, newUserData);
        } else {
            throw new NotFoundException();
        }
    }

    private void saveUserUpdate(User existingUser, User newUserData) {
        existingUser.setEmail(newUserData.getEmail());
        existingUser.setFirstName(newUserData.getFirstName());
        existingUser.setLastName(newUserData.getLastName());
        existingUser.setUsername(newUserData.getUsername());
        assignRoleToUser(existingUser, newUserData.getRole().getName());
        existingUser.setPassword(hasher.hashText(newUserData.getPassword()));
        existingUser.setBlocked(newUserData.isBlocked());
        userRepository.save(existingUser);
    }

    private void assignRoleToUser(User user, String roleName) {
        Optional<UserRole> userRole = roleRepository.findByName(roleName);
        UserRole role = userRole.orElseThrow(() ->
                new ValidationException("Role with name '" + user.getRole().getName() + "' does not exist"));
        user.setRole(role);
    }


}
