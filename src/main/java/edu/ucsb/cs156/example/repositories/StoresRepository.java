package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Stores;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StoresRepository extends CrudRepository<Stores, Long> {
  Iterable<Stores> findAllByName(String name);
}
