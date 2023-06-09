package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "stores")
public class Stores {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String location;
  private String name;  
  private String price;
  private String sales;
  /*
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String quarterYYYYQ;
  private String name;  
  private LocalDateTime localDateTime;
  */
} 