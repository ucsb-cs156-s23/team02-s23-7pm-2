package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
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


@Api(description = "Restaurant")
@RequestMapping("/api/Restaurant")
@RestController
@Slf4j
public class RestaurantController extends ApiController {

    @Autowired
    RestaurantRepository RestaurantRepository;

    @ApiOperation(value = "List all ucsb dining restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurant> allrestaurants() {
        Iterable<Restaurant> restaurant = RestaurantRepository.findAll();
        return restaurant;
    }

    @ApiOperation(value = "Get a single restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Restaurant getById(
            @ApiParam("code") @RequestParam String code) {
        Restaurant restaurant = RestaurantRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, code));

        return restaurant;
    }

    @ApiOperation(value = "Create a new restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurant postrestaurant(
        @ApiParam("code") @RequestParam String code,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("hasTakeOutMeal") @RequestParam boolean hasTakeOutMeal,
        @ApiParam("latitude") @RequestParam double latitude,
        @ApiParam("longitude") @RequestParam double longitude
        )
        {

        Restaurant restaurant = new Restaurant();
        restaurant.setCode(code);
        restaurant.setName(name);
        restaurant.setHasTakeOutMeal(hasTakeOutMeal);
        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);

        Restaurant savedrestaurant = RestaurantRepository.save(restaurant);

        return savedrestaurant;
    }

    @ApiOperation(value = "Delete a Restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleterestaurant(
            @ApiParam("code") @RequestParam String code) {
        Restaurant restaurant = RestaurantRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, code));

        RestaurantRepository.delete(restaurant);
        return genericMessage("Restaurant with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Restaurant updaterestaurant(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid Restaurant incoming) {

        Restaurant restaurant = RestaurantRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, code));


        restaurant.setName(incoming.getName());  
        restaurant.setHasTakeOutMeal(incoming.getHasTakeOutMeal());
        restaurant.setLatitude(incoming.getLatitude());
        restaurant.setLongitude(incoming.getLongitude());

        RestaurantRepository.save(restaurant);

        return restaurant;
    }
}
