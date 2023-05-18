package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.ClothingBrands;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ClothingBrandsRepository;
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
    ClothingBrandsRepository clothingBrandsRepository;

    @ApiOperation(value = "List all clothing brands")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<ClothingBrands> allBrands() {
        Iterable<ClothingBrands> brands = clothingBrandsRepository.findAll();
        return brands;
    }

    @ApiOperation(value = "Get a single clothing brand")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public ClothingBrands getById(
            @ApiParam("code") @RequestParam String code) {
        ClothingBrands brands = clothingBrandsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(ClothingBrands.class, code));

        return brands;
    }

    @ApiOperation(value = "Create a new clothing brand")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public ClothingBrands postBrands(
        @ApiParam("code") @RequestParam String code,
        @ApiParam("brand") @RequestParam String brand,
        @ApiParam("price") @RequestParam String price
        )
        {

        ClothingBrands brands = new ClothingBrands();
        brands.setCode(code);
        brands.setBrand(brand);
        brands.setPrice(price);

        ClothingBrands savedBrands = clothingBrandsRepository.save(brands);

        return savedBrands;
    }

    @ApiOperation(value = "Delete a ClothingBrands")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteBrands(
            @ApiParam("code") @RequestParam String code) {
        ClothingBrands brands = clothingBrandsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(ClothingBrands.class, code));

        clothingBrandsRepository.delete(brands);
        return genericMessage("ClothingBrands with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single clothing brand")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public ClothingBrands updateBrands(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid ClothingBrands incoming) {

        ClothingBrands brands = clothingBrandsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(ClothingBrands.class, code));


        brands.setCode(incoming.getCode());  
        brands.setBrand(incoming.getBrand());  
        brands.setPrice(incoming.getPrice());

        clothingBrandsRepository.save(brands);

        return brands;
    }
}
