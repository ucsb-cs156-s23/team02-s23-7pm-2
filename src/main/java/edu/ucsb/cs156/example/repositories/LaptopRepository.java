package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Laptop;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LaptopRepository extends CrudRepository<Laptop, Long> {
  Iterable<Laptop> findAllByName(String name);
}