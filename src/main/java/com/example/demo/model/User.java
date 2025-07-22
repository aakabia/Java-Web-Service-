package com.example.demo.model;


import jakarta.persistence.*; // for database storage and retrieval,
import lombok.*; // for constructing classes
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.io.Serializable; // for serializing objects into a byte stream
import java.util.*;

@Entity // annotates this class as a table
@Data // bundled constructor from lombok
@Builder
@NoArgsConstructor // lombok constructor for no fields
@AllArgsConstructor // lombok constructor for all fields
@Table(name = "Users") // set table name
public class User implements Serializable, UserDetails {

    // create an ID as a primary key
    // auto generate the id field with an identity strategy
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true) // make the email field unique
    private String email;
    @Column(unique = true) // make the userName field unique
    private String username;
    private String firstName;
    private String lastName;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    /* OneToMany relationship provides a unidirectional relationship between parent and child , one user (parent) can have many accounts (child) */
    /* On delete we will cascade all the related data to this user and remove any orphan data  */

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY )
    private List<Account> userAccounts;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
// I implemented UserDetails within this class for flexibility with spring security.
/* By implementing the java.io.Serializable interface, you're informing the JVM that objects of this class are eligible to be serialized and deserialized.*/
/* Read at https://www.baeldung.com/java-serialization for quick guide regarding benefits of this.*/
/*  @ElementCollection specifically  tells JPA to create a separate table to store the elements of the collection ex.(Set<Role>),
    This is because enums and other basic types can’t be stored as a collection in one column any table.*/
// <? extends GrantedAuthority> generic says I’m returning a Collection of some unknown type that is either GrantedAuthority or a subclass/implementation of it
// A stream is a cleaner, more expressive way to process collections of data (for loop vs stream)