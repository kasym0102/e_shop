package com.webApp.e_shop.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "comment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment")
    private String comment;

    @Column(name = "addedDate")
    private Date addedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private ShopItem shopItem;

    @ManyToOne(fetch = FetchType.LAZY)
    private ShopUser author;

    public String getAddedDateFormat() {
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("dd.MM.yyyy - HH :mm" );
        return simpleDateFormat.format(addedDate);
    }
}
