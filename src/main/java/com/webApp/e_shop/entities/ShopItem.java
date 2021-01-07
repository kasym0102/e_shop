package com.webApp.e_shop.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.attoparser.dom.Text;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private double price;

    @Column(name = "amount")
    private int amount;

    @Column(name = "stars")
    private int stars; // Just rating, from 0 to 5

    @Column(name = "smallPicURL")
    private String smallPicURL;

    @Column(name = "largePicURL")
    private String largePicURL;

    @Column(name = "addedDate")
    private Date addedDate;

    @Column(name = "inTopPage")
    private boolean inTopPage;

    @ManyToOne(fetch = FetchType.EAGER)
    Brand brand;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Category> categories;

//    @OneToMany(fetch = FetchType.EAGER)
//    List<Picture> pictures;

    public void addCategory(Category category){
        categories.add(category);
    }

//    public void addPicture(Picture picture){
//        pictures.add(picture);
//    }

    public void deleteCategory(Category category){
        categories.remove(category);
    }

    public int[] getFillStars(){
        return new int[stars];
    }

    public int[] getEmptyStars(){
        return new int[5-stars];
    }

    public ShopItem(Long id, String name, String description, double price, int amount, int stars, String smallPicURL, String largePicURL, Date addedDate, boolean inTopPage, Brand brand) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
        this.stars = stars;
        this.smallPicURL = smallPicURL;
        this.largePicURL = largePicURL;
        this.addedDate = addedDate;
        this.inTopPage = inTopPage;
        this.brand = brand;
    }
}
