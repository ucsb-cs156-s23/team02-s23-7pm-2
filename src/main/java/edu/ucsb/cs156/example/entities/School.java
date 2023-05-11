package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
<<<<<<< HEAD
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
=======
import javax.persistence.Id;
>>>>>>> 9a9298285ab389972d42c594cca963a499de5800

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "schools")
public class School {
  @Id
<<<<<<< HEAD
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
=======
>>>>>>> 9a9298285ab389972d42c594cca963a499de5800
  private String name;
  private String rank;
  private String description;
}