package com.example.demo.model;


import jakarta.persistence.*; // for database storage and retrieval,
import lombok.*; // for constructing classes


import java.io.Serializable; // for serializing objects into a byte stream

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;


}
