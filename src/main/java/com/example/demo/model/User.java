package com.example.demo.model;


import jakarta.persistence.*; // for database storage and retrieval,
import lombok.*; // for constructing classes


import java.io.Serializable; // for serializing objects into a byte stream
import java.util.List;

@Entity // annotates this class as a table
@Data // bundled constructor from lombok
@NoArgsConstructor // lombok constructor for no fields
@AllArgsConstructor // lombok constructor for all fields
@Table(name = "Users") // set table name
public class User implements Serializable {

    // create an ID as a primary key
    // auto generate the id field with an identity strategy
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true) // make the email field unique
    private String userEmail;

    private String firstName;
    private String lastName;
    private String password;

    /* OneToMany relationship provides a unidirectional relationship between parent and child , one user (parent) can have many accounts (child) */
    /* On delete we will cascade all the related data to this user and remove any orphan data  */

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true )
    private List<Account> userAccounts;



}

/* By implementing the java.io.Serializable interface, you're informing the JVM that objects of this class are eligible to be serialized and deserialized.*/
/* Read at https://www.baeldung.com/java-serialization for quick guide regarding benefits of this.*/
