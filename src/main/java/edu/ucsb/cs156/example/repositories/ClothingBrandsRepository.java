package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.ClothingBrands;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClothingBrandsRepository extends CrudRepository<ClothingBrands, String> {
}