package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.School;
import edu.ucsb.cs156.example.repositories.SchoolRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.h2.value.ExtTypeInfo;
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

@WebMvcTest(controllers = SchoolsController.class)
@Import(TestConfig.class)
public class SchoolControllerTests extends ControllerTestCase {

        @MockBean
        SchoolRepository SchoolRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/Restaurant/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/schools/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/schools/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/schools?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/schools/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/schools/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/schools/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                School school = School.builder()
                                .name("UCSD")
                                .rank("34")
                                .description("Public land-grant research university in La Jolla, California. It is ranked among the best universities in the world.")
                                .build();

                when(SchoolRepository.findById(eq(7L))).thenReturn(Optional.of(school));

                // act
                MvcResult response = mockMvc.perform(get("/api/schools?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(SchoolRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(school);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(SchoolRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/schools?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(SchoolRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("School with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_schools() throws Exception {

                // arrange

                School usc = School.builder()
                                .name("USC")
                                .rank("25")
                                .description("A private research university in Los Angeles, the oldest private research university." )
                                .build();

                School ucsd = School.builder()
                                .name("De La Guerra")
                                .rank("34")
                                .description("Public land-grant research university in La Jolla, California. It is ranked among the best universities in the world.")
                                .build();

                ArrayList<School> expectedSchools = new ArrayList<>();
                expectedSchools.addAll(Arrays.asList(usc, ucsd));

                when(SchoolRepository.findAll()).thenReturn(expectedSchools);

                // act
                MvcResult response = mockMvc.perform(get("/api/schools/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(SchoolRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedSchools);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_schools() throws Exception {
                // arrange

                School mit = School.builder()
                                .name("MIT")
                                .rank("2")
                                .description("A%20private%20land-grant%20research%20university%20in%20Cambridge,%20Massachusetts.%20Has%20played%20a%20significant%20role%20in%20the%20development%20of%20many%20areas%20of%20modern%20technology%20and%20science."  )
                                .build();

                when(SchoolRepository.save(eq(mit))).thenReturn(mit);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/schools/post?name=MIT&rank=2&description=A%20private%20land-grant%20research%20university%20in%20Cambridge,%20Massachusetts.%20Has%20played%20a%20significant%20role%20in%20the%20development%20of%20many%20areas%20of%20modern%20technology%20and%20science.")
                                               .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(SchoolRepository, times(1)).save(mit);
                String expectedJson = mapper.writeValueAsString(mit);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                School ucsb  = School.builder()
                                .name("UCSB")
                                .rank("32")
                                .description("A public land-grand research university in Santa Barbara, California. It is the third-oldest UC.")
                                .build();

                when(SchoolRepository.findById(eq(15L))).thenReturn(Optional.of(ucsb));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/schools?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(SchoolRepository, times(1)).findById(15L);
                verify(SchoolRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("School with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_school_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(SchoolRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/schools?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(SchoolRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("School with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_school() throws Exception {
                // arrange

                School ucsdOrig = School.builder()
                                .name("UCSD")
                                .rank("34")
                                .description("Public land-grant research university in La Jolla, California. It is ranked among the best universities in the world.")
                                .build();

                School ucsdEdited = School.builder()
                                .name("UCSD")
                                .rank("30")
                                .description("Public land-grant research university in La Jolla, California. It is ranked among the best universities in the world.")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsdEdited);

                when(SchoolRepository.findById(eq(67L))).thenReturn(Optional.of(ucsdOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/schools?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(SchoolRepository, times(1)).findById(67L);
                verify(SchoolRepository, times(1)).save(ucsdEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_restaurant_that_does_not_exist() throws Exception {
                // arrange

                School editedSchool = School.builder()
                                .name("UCSA")
                                .rank("37")
                                .description("Public land-grant research university in La Jolla, California. It is ranked among the best universities in the world.")
                                .build();

                String requestBody = mapper.writeValueAsString(editedSchool);

                when(SchoolRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/schools?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(SchoolRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("School with id 67 not found", json.get("message"));

        }
}
