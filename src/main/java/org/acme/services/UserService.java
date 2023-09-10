package org.acme.services;



import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.User;
import org.acme.repositories.UserRepo;


@ApplicationScoped
public class UserService {

    @Inject
    private UserRepo userRepository;

    @Transactional
    public void registerUser(String firstName, String lastName, String email, String plainPassword) {

        // Hash the plain password
        String encryptedPassword = BcryptUtil.bcryptHash(plainPassword);

        // Create a new user entity
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEncryptedPassword(encryptedPassword);
        user.setCheckDigits(12223l);
        user.setRole("user");
        // Save the user to the database
        userRepository.persist(user);
    }



}

