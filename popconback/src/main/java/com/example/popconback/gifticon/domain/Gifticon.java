package com.example.popconback.gifticon.domain;

import com.example.popconback.user.domain.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="gifticon")
public class Gifticon {

    @Id
    @Column(name="barcode_num")
    private String barcodeNum;

    @ManyToOne
    @JoinColumn(name="hash")
    @JsonManagedReference
    private User user;

    @ManyToOne
    @JoinColumn(name="brand_name")
    @JsonManagedReference
    private Brand brand;
    private String product;
    private Date due;
    private int price;
    @JsonManagedReference
    @OneToMany(mappedBy = "gifticon", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<GifticonFiles> filesList = new ArrayList<>();



    private int state;
    private String memo;

}
