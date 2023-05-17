package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Stores;
import edu.ucsb.cs156.example.repositories.StoresRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = StoresController.class)
@Import(TestConfig.class)
public class StoresControllerTests extends ControllerTestCase {

        @MockBean
        StoresRepository storesRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/stores/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/stores/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/stores/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/stores?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/stores/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/stores/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/stores/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Stores stores = Stores.builder()
                                .name("IV Market")
                                .location("939 Embarcadero del Mar")
                                .price("$$")
                                .sales("Low")
                                .build();
                                
                when(storesRepository.findById(eq(7L))).thenReturn(Optional.of(stores));

                // act
                MvcResult response = mockMvc.perform(get("/api/stores?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(storesRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(stores);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(storesRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/stores?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(storesRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Stores with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_stores() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Stores stores1 = Stores.builder()
                                .name("Target")
                                .location("6865 Hollister Ave")
                                .price("$$")
                                .sales("High")
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                Stores stores2 = Stores.builder()
                                .name("Home Depot")
                                .location("6975 Market Pl Dr")
                                .price("$$$")
                                .sales("High")
                                .build();

                ArrayList<Stores> expectedStores = new ArrayList<>();
                expectedStores.addAll(Arrays.asList(stores1, stores2));

                when(storesRepository.findAll()).thenReturn(expectedStores);

                // act
                MvcResult response = mockMvc.perform(get("/api/stores/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(storesRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedStores);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdate() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Stores stores1 = Stores.builder()
                                .name("Target")
                                .location("6865 Hollister Ave")
                                .price("$$")
                                .sales("High")
                                .build();

                
                when(storesRepository.save(eq(stores1))).thenReturn(stores1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/stores/post?name=Target&location=6865 Hollister Ave&price=$$&sales=High")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(storesRepository, times(1)).save(stores1);
                String expectedJson = mapper.writeValueAsString(stores1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Stores stores1 = Stores.builder()
                                .name("Target")
                                .location("6865 Hollister Ave")
                                .price("$$")
                                .sales("High")
                                .build();

                when(storesRepository.findById(eq(15L))).thenReturn(Optional.of(stores1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/stores?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(storesRepository, times(1)).findById(15L);
                verify(storesRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Stores with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(storesRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/stores?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(storesRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Stores with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                Stores storesOrig = Stores.builder()
                                .name("Target")
                                .location("6865 Hollister Ave")
                                .price("$$")
                                .sales("High")
                                .build();

                Stores storesEdited = Stores.builder()
                                .name("Trader Joe's")
                                .location("5767 Calle Real")
                                .price("$$")
                                .sales("Medium")
                                .build();

                String requestBody = mapper.writeValueAsString(storesEdited);

                when(storesRepository.findById(eq(67L))).thenReturn(Optional.of(storesOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/stores?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(storesRepository, times(1)).findById(67L);
                verify(storesRepository, times(1)).save(storesEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Stores ucsbEditedDate = Stores.builder()
                                .name("Target")
                                .location("6865 Hollister Ave")
                                .price("$$")
                                .sales("High")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedDate);

                when(storesRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/stores?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(storesRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Stores with id 67 not found", json.get("message"));

        }
}
