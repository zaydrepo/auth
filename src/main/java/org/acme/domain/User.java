package org.acme.domain;


import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@UserDefinition
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long checkDigits;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Username
    private String email;

    @Password
    private String encryptedPassword;
    @Roles
    private String role;



    public User(Long checkDigits, String firstName, String lastName, String plainPassword) {
        this.checkDigits = checkDigits;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role="user";
        this.setEncryptedPassword(BcryptUtil.bcryptHash(plainPassword));

    }




}
