package edu.ucsb.cs156.example.entities;


import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "clothingbrands")
public class ClothingBrands {
  @Id
  private String brand;
  private String price;
}