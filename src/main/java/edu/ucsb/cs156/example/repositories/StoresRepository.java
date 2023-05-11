package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Store;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StoresRepository extends CrudRepository<Store, Long> {
  Iteratable<Store> findAllByName(String name);
} 