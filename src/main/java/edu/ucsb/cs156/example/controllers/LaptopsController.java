package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Laptop;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.LaptopRepository;
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

@Api(description = "Laptops")
@RequestMapping("/api/laptops")
@RestController
@Slf4j
public class LaptopsController extends ApiController {

    @Autowired
    LaptopRepository laptopRepository;

    @ApiOperation(value = "List all laptops")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Laptop> allLaptops() {
        Iterable<Laptop> laptops = laptopRepository.findAll();
        return laptops;
    }

    @ApiOperation(value = "Get a single laptop")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Laptop getById(
            @ApiParam("id") @RequestParam Long id) {
        Laptop laptop = laptopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Laptop.class, id));

        return laptop;
    }

    @ApiOperation(value = "Create a new laptop")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Laptop postLaptop(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("cpu") @RequestParam String cpu,
            @ApiParam("gpu") @RequestParam String gpu,
            @ApiParam("description") @RequestParam String description
    ) {

        Laptop laptop = new Laptop();
        laptop.setName(name);
        laptop.setCpu(cpu);
        laptop.setGpu(gpu);
        laptop.setDescription(description);

        Laptop savedLaptop = laptopRepository.save(laptop);

        return savedLaptop;
    }

    @ApiOperation(value = "Delete a laptop")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteLaptop(
            @ApiParam("id") @RequestParam Long id) {
        Laptop laptop = laptopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Laptop.class, id));

        laptopRepository.delete(laptop);
        return genericMessage("Laptop with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single laptop")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Laptop updateLaptop(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Laptop incoming) {

        Laptop laptop = laptopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Laptop.class, id));

        laptop.setName(incoming.getName());
        laptop.setCpu(incoming.getCpu());
        laptop.setGpu(incoming.getGpu());
        laptop.setDescription(incoming.getDescription());

        laptopRepository.save(laptop);

        return laptop;
    }
}
