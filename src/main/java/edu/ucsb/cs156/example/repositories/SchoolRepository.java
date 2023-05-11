package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.School;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface SchoolRepository extends CrudRepository<School, Long> {
  Iterable<School> findAllByName(String name);
}