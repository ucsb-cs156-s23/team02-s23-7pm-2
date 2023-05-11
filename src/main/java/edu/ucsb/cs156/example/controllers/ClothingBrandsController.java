package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.ClothingBrand;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ClothingBrandRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(description = "ClothingBrands")
@RequestMapping("/api/clothingbrands")
@RestController
@Slf4j
public class ClothingBrandsController extends ApiController {

    @Autowired
    ClothingBrandRepository clothingbrandRepository;

    @ApiOperation(value = "List all clothingbrands")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<ClothingBrand> allClothingBrands() {
        Iterable<ClothingBrand> clothingbrands = clothingbrandRepository.findAll();
        return clothingbrands;
    }

    @ApiOperation(value = "Get a single clothingbrand")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public ClothingBrand getById(
            @ApiParam("id") @RequestParam Long id) {
        ClothingBrand clothingbrand = clothingbrandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ClothingBrand.class, id));

        return clothingbrand;
    }

    @ApiOperation(value = "Create a new clothingbrand")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public ClothingBrand postClothingBrand(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("cpu") @RequestParam String cpu,
            @ApiParam("gpu") @RequestParam String gpu,
            @ApiParam("description") @RequestParam String description
    ) {

        ClothingBrand clothingbrand = new ClothingBrand();
        clothingbrand.setName(name);
        clothingbrand.setCpu(cpu);
        clothingbrand.setGpu(gpu);
        clothingbrand.setDescription(description);

        ClothingBrand savedClothingBrand = clothingbrandRepository.save(clothingbrand);

        return savedClothingBrand;
    }

    @ApiOperation(value = "Delete a clothingbrand")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteClothingBrand(
            @ApiParam("id") @RequestParam Long id) {
        ClothingBrand clothingbrand = clothingbrandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ClothingBrand.class, id));

        clothingbrandRepository.delete(clothingbrand);
        return genericMessage("ClothingBrand with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single clothingbrand")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public ClothingBrand updateClothingBrand(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid ClothingBrand incoming) {

        ClothingBrand clothingbrand = clothingbrandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ClothingBrand.class, id));

        clothingbrand.setName(incoming.getName());
        clothingbrand.setCpu(incoming.getCpu());
        clothingbrand.setGpu(incoming.getGpu());
        clothingbrand.setDescription(incoming.getDescription());

        clothingbrandRepository.save(clothingbrand);

        return clothingbrand;
    }
}