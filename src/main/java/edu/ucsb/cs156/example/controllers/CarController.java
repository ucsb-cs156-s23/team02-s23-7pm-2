package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Car;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.CarRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

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

import java.time.LocalDateTime;

@Api(description = "Cars")
@RequestMapping("/api/cars")
@RestController
@Slf4j
public class CarController extends ApiController {

	@Autowired
	CarRepository carRepository;

	@ApiOperation(value = "List all cars")
	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/all")
	public Iterable<Car> allCars() {
		Iterable<Car> cars = carRepository.findAll();
		return cars;
	}

	@ApiOperation(value = "Get a single car")
	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("")
	public Car getById(
			@ApiParam("id") @RequestParam Long id) {
		Car car = carRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(Car.class, id));

		return car;
	}

	@ApiOperation(value = "Create a new car")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/post")
	public Car postCar(
			@ApiParam("make") @RequestParam String make,
			@ApiParam("model") @RequestParam String model,
			@ApiParam("model") @RequestParam String year)
			throws JsonProcessingException {

		// For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		// See: https://www.baeldung.com/spring-date-parameters

		Car car = new Car();
		car.setMake(make);
		car.setModel(model);
		car.setYear(year);

		Car savedCar = carRepository.save(car);

		return savedCar;
	}

	@ApiOperation(value = "Delete a car")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@DeleteMapping("")
	public Object deleteUCSBDate(
			@ApiParam("id") @RequestParam Long id) {
		Car car = carRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(Car.class, id));

		carRepository.delete(car);
		return genericMessage("Car with id %s deleted".formatted(id));
	}

	@ApiOperation(value = "Update a single car")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PutMapping("")
	public Car updateUCSBDate(
			@ApiParam("id") @RequestParam Long id,
			@RequestBody @Valid Car incoming) {

		Car car = carRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(Car.class, id));

		car.setMake(incoming.getMake());
		car.setModel(incoming.getModel());
		car.setYear(incoming.getYear());

		carRepository.save(car);

		return car;
	}
}
