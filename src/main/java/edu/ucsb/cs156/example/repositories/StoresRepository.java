package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Stores;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
<<<<<<< HEAD
public interface StoresRepository extends CrudRepository<Stores, Long> {
  Iterable<Stores> findAllByName(String name);
}
=======
public interface StoresRepository extends CrudRepository<Store, Long> {
  Iteratable<Store> findAllByName(String name);
} 
>>>>>>> fd19c72239daf8464727d1fc228c8903beca6d07
