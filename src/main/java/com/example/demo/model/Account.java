package com.example.demo.model;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;



@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Accounts")
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private int accountNumber;

    private int routingNumber;
    private String accountType;
    private String currencyType;
    private BigDecimal accountBalance;

    /* Many relationship provides a unidirectional relationship between child and parent , Many Accounts (child) belong to one user (child) */
    /* JoinColumn names the column that represents our foreign key in this table  */
    /* When using mapping in parent, you want to make sure to map to the name of this field ex. "user" */
    @ManyToOne
    @JoinColumn(name ="user_id")
    private User user;


}
