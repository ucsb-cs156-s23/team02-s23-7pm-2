package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RestaurantController.class)
@Import(TestConfig.class)
public class RestaurantControllerTests extends ControllerTestCase {

        @MockBean
        RestaurantRepository RestaurantRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/Restaurant/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/Restaurant/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/Restaurant/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/Restaurant?code=carrillo"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/Restaurant/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Restaurant/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Restaurant/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Restaurant restaurant = Restaurant.builder()
                                .name("Carrillo")
                                .code("carrillo")
                                .hasTakeOutMeal(false)
                                .latitude(34.409953)
                                .longitude(-119.85277)
                                .build();

                when(RestaurantRepository.findById(eq("carrillo"))).thenReturn(Optional.of(restaurant));

                // act
                MvcResult response = mockMvc.perform(get("/api/Restaurant?code=carrillo"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RestaurantRepository, times(1)).findById(eq("carrillo"));
                String expectedJson = mapper.writeValueAsString(restaurant);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(RestaurantRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/Restaurant?code=munger-hall"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(RestaurantRepository, times(1)).findById(eq("munger-hall"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Restaurant with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_Restaurant() throws Exception {

                // arrange

                Restaurant carrillo = Restaurant.builder()
                                .name("Carrillo")
                                .code("carrillo")
                                .hasTakeOutMeal(false)
                                .latitude(34.409953)
                                .longitude(-119.85277)
                                .build();

                Restaurant dlg = Restaurant.builder()
                                .name("De La Guerra")
                                .code("de-la-guerra")
                                .hasTakeOutMeal(false)
                                .latitude(34.409811)
                                .longitude(-119.845026)
                                .build();

                ArrayList<Restaurant> expectedrestaurant = new ArrayList<>();
                expectedrestaurant.addAll(Arrays.asList(carrillo, dlg));

                when(RestaurantRepository.findAll()).thenReturn(expectedrestaurant);

                // act
                MvcResult response = mockMvc.perform(get("/api/Restaurant/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RestaurantRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedrestaurant);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_restaurant() throws Exception {
                // arrange

                Restaurant ortega = Restaurant.builder()
                                .name("Ortega")
                                .code("ortega")
                                .hasTakeOutMeal(true)
                                .latitude(34.410987)
                                .longitude(-119.84709)
                                .build();

                when(RestaurantRepository.save(eq(ortega))).thenReturn(ortega);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/Restaurant/post?name=Ortega&code=ortega&hasSackMeal=true&hasTakeOutMeal=true&hasDiningCam=true&latitude=34.410987&longitude=-119.84709")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).save(ortega);
                String expectedJson = mapper.writeValueAsString(ortega);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Restaurant portola = Restaurant.builder()
                                .name("Portola")
                                .code("portola")
                                .hasTakeOutMeal(true)
                                .latitude(34.417723)
                                .longitude(-119.867427)
                                .build();

                when(RestaurantRepository.findById(eq("portola"))).thenReturn(Optional.of(portola));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Restaurant?code=portola")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("portola");
                verify(RestaurantRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurant with id portola deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_restaurant_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(RestaurantRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Restaurant?code=munger-hall")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurant with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_restaurant() throws Exception {
                // arrange

                Restaurant carrilloOrig = Restaurant.builder()
                                .name("Carrillo")
                                .code("carrillo")
                                .hasTakeOutMeal(false)
                                .latitude(34.409953)
                                .longitude(-119.85277)
                                .build();

                Restaurant carrilloEdited = Restaurant.builder()
                                .name("Carrillo Dining Hall")
                                .code("carrillo")
                                .hasTakeOutMeal(true)
                                .latitude(34.409954)
                                .longitude(-119.85278)
                                .build();

                String requestBody = mapper.writeValueAsString(carrilloEdited);

                when(RestaurantRepository.findById(eq("carrillo"))).thenReturn(Optional.of(carrilloOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Restaurant?code=carrillo")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("carrillo");
                verify(RestaurantRepository, times(1)).save(carrilloEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_restaurant_that_does_not_exist() throws Exception {
                // arrange

                Restaurant editedrestaurant = Restaurant.builder()
                                .name("Munger Hall")
                                .code("munger-hall")
                                .hasTakeOutMeal(false)
                                .latitude(34.420799)
                                .longitude(-119.852617)
                                .build();

                String requestBody = mapper.writeValueAsString(editedrestaurant);

                when(RestaurantRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Restaurant?code=munger-hall")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurant with id munger-hall not found", json.get("message"));

        }
}
