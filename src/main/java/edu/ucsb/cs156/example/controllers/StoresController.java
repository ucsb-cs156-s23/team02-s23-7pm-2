package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Stores;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.StoresRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;

@Api(description = "Stores")
@RequestMapping("/api/Stores")
@RestController
@Slf4j
public class StoresController extends ApiController {

    @Autowired
    StoresRepository storesRepository;

    @ApiOperation(value = "List all stores")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Stores> allStores() {
        Iterable<Stores> allStore = storesRepository.findAll();
        return allStore;
    }

    @ApiOperation(value = "Get a single store")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Stores getById(
            @ApiParam("id") @RequestParam Long id) {
        Stores stores = storesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Stores.class, id));

        return stores;
    }

    @ApiOperation(value = "Create a new store")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Stores postStores(
            @ApiParam("location") @RequestParam String location,
            @ApiParam("name") @RequestParam String name,
            @ApiParam("price") @RequestParam String price,
            @ApiParam("sales") @RequestParam String sales
        ) {
        Stores stores = new Stores();
        stores.setLocation(location);
        stores.setName(name);
        stores.setPrice(price);
        stores.setSales(sales);

        Stores savedStore = storesRepository.save(stores);

        return savedStore;
    }

    @ApiOperation(value = "Delete a Stores")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteStores(
            @ApiParam("id") @RequestParam Long id) {
        Stores stores = storesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Stores.class, id));

        storesRepository.delete(stores);
        return genericMessage("Stores with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single store")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Stores updateStores(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Stores incoming) {

        Stores stores = storesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Stores.class, id));

        stores.setLocation(incoming.getLocation());
        stores.setName(incoming.getName());
        stores.setPrice(incoming.getPrice());
        stores.setSales(incoming.getName());

        storesRepository.save(stores);

        return stores;
    }
}
