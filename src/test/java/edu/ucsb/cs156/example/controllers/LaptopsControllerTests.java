package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Laptop;
import edu.ucsb.cs156.example.repositories.LaptopRepository;

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

@WebMvcTest(controllers = LaptopsController.class)
@Import(TestConfig.class)
public class LaptopsControllerTests extends ControllerTestCase {

        @MockBean
        LaptopRepository LaptopRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/laptops/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/laptops/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/laptops/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/laptops?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/laptops/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/laptops/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/laptops/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Laptop omen = Laptop.builder()
                                .name("OMEN 16t-k000")
                                .cpu("Intel Core i5-12500H")
                                .gpu("NVIDIA GeForce RTX 3050 Laptop")
                                .description("Cheap but still fast")
                                .build();

                when(LaptopRepository.findById(1L)).thenReturn(Optional.of(omen));

                // act
                MvcResult response = mockMvc.perform(get("/api/laptops?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(LaptopRepository, times(1)).findById(1L);
                String expectedJson = mapper.writeValueAsString(omen);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(LaptopRepository.findById(12934L)).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/laptops?id=12934"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(LaptopRepository, times(1)).findById(12934L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Laptop with id 12934 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_laptops() throws Exception {

                // arrange

                Laptop omen = Laptop.builder()
                                .name("OMEN 16t-k000")
                                .cpu("Intel Core i5-12500H")
                                .gpu("NVIDIA GeForce RTX 3050 Laptop")
                                .description("Cheap but still fast")
                                .build();

                Laptop asus = Laptop.builder()
                                .name("ASUS E210")
                                .cpu("Intel Pentium N4020")
                                .gpu("Integrated Intel UHD Graphics")
                                .description("Extremely cheap and functional")
                                .build();

                ArrayList<Laptop> expectedLaptops = new ArrayList<>();
                expectedLaptops.addAll(Arrays.asList(omen, asus));

                when(LaptopRepository.findAll()).thenReturn(expectedLaptops);

                // act
                MvcResult response = mockMvc.perform(get("/api/laptops/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(LaptopRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedLaptops);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_laptop() throws Exception {
                // arrange

                Laptop alienware = Laptop.builder()
                                .name("Alienware m18")
                                .cpu("Intel Core i7-13650HX")
                                .gpu("NVIDIA GeForce RTX 4050 Laptop")
                                .description("Extremely fast but expensive")
                                .build();

                when(LaptopRepository.save(eq(alienware))).thenReturn(alienware);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/laptops/post?name=Alienware m18&cpu=Intel Core i7-13650HX&gpu=NVIDIA GeForce RTX 4050 Laptop&description=Extremely fast but expensive")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(LaptopRepository, times(1)).save(alienware);
                String expectedJson = mapper.writeValueAsString(alienware);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_laptop() throws Exception {
                // arrange

                Laptop alienware = Laptop.builder()
                                .name("Lenovo Ideapad 3i")
                                .cpu("Intel Core i3-1115G4")
                                .gpu("Integrated Intel UHD Graphics")
                                .description("Touchscreen")
                                .build();

                when(LaptopRepository.findById(15L)).thenReturn(Optional.of(alienware));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/laptops?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(LaptopRepository, times(1)).findById(15L);
                verify(LaptopRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Laptop with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_laptop_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(LaptopRepository.findById(1233L)).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/laptops?id=1233")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(LaptopRepository, times(1)).findById(1233L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Laptop with id 1233 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_laptop() throws Exception {
                // arrange

                Laptop omenOrig = Laptop.builder()
                                .name("OMEN 16t-k000")
                                .cpu("Intel Core i5-12500H")
                                .gpu("NVIDIA GeForce RTX 3050 Laptop")
                                .description("Cheap but still fast")
                                .build();

                Laptop omenEdited = Laptop.builder()
                                .name("OMEN 17t-k000")
                                .cpu("Intel Core i6-12500H")
                                .gpu("NVIDIA GeForce RTX 3060 Laptop")
                                .description("Fast and cheap")
                                .build();

                String requestBody = mapper.writeValueAsString(omenEdited);

                when(LaptopRepository.findById(123L)).thenReturn(Optional.of(omenOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/laptops?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(LaptopRepository, times(1)).findById(123L);
                verify(LaptopRepository, times(1)).save(omenEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_laptop_that_does_not_exist() throws Exception {
                // arrange

                Laptop editedLaptop = Laptop.builder()
                                .name("Quantum 3000")
                                .cpu("Some crazy quantum cpu")
                                .gpu("Some crazy quantum gpu")
                                .description("Doesn't exist")
                                .build();

                String requestBody = mapper.writeValueAsString(editedLaptop);

                when(LaptopRepository.findById(124L)).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/laptops?id=124")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(LaptopRepository, times(1)).findById(124L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Laptop with id 124 not found", json.get("message"));

        }
}
