package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.ClothingBrands;
import edu.ucsb.cs156.example.repositories.ClothingBrandsRepository;

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

@WebMvcTest(controllers = ClothingBrandsController.class)
@Import(TestConfig.class)
public class ClothingBrandsControllerTests extends ControllerTestCase {

        @MockBean
        ClothingBrandsRepository clothingBrandsRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/clothingbrands/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/clothingbrands/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/clothingbrands/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/clothingbrands?code=supreme"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/clothingbrands/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/clothingbrands/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/clothingbrands/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                ClothingBrands brands = ClothingBrands.builder()
                                .code("supreme")
                                .brand("supreme")
                                .price("$$$")
                                .build();

                when(clothingBrandsRepository.findById(eq("supreme"))).thenReturn(Optional.of(brands));

                // act
                MvcResult response = mockMvc.perform(get("/api/clothingbrands?code=supreme"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(clothingBrandsRepository, times(1)).findById(eq("supreme"));
                String expectedJson = mapper.writeValueAsString(brands);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(clothingBrandsRepository.findById(eq("forever21"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/clothingbrands?code=forever21"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(clothingBrandsRepository, times(1)).findById(eq("forever21"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("ClothingBrands with id forever21 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_clothingbrands() throws Exception {

                // arrange

                ClothingBrands supreme = ClothingBrands.builder()
                                .code("supreme")
                                .brand("supreme")
                                .price("$$$")
                                .build();

                ClothingBrands guess = ClothingBrands.builder()
                                .code("guess")
                                .brand("guess")
                                .price("$$")
                                .build();

                ArrayList<ClothingBrands> expectedCommons = new ArrayList<>();
                expectedCommons.addAll(Arrays.asList(supreme, guess));

                when(clothingBrandsRepository.findAll()).thenReturn(expectedCommons);

                // act
                MvcResult response = mockMvc.perform(get("/api/clothingbrands/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(clothingBrandsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCommons);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_brands() throws Exception {
                // arrange

                ClothingBrands gap = ClothingBrands.builder()
                                .code("gap")
                                .brand("gap")
                                .price("$")
                                .build();

                when(clothingBrandsRepository.save(eq(gap))).thenReturn(gap);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/clothingbrands/post?code=gap&brand=gap&price=$")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(clothingBrandsRepository, times(1)).save(gap);
                String expectedJson = mapper.writeValueAsString(gap);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                ClothingBrands chromehearts = ClothingBrands.builder()
                                .code("chromehearts")
                                .brand("Chrome Heart")
                                .price("$$$$")
                                .build();

                when(clothingBrandsRepository.findById(eq("chromehearts"))).thenReturn(Optional.of(chromehearts));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/clothingbrands?code=chromehearts")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(clothingBrandsRepository, times(1)).findById("chromehearts");
                verify(clothingBrandsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("ClothingBrands with id chromehearts deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_brands_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(clothingBrandsRepository.findById(eq("forever21"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/clothingbrands?code=forever21")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(clothingBrandsRepository, times(1)).findById("forever21");
                Map<String, Object> json = responseToJson(response);
                assertEquals("ClothingBrands with id forever21 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_brands() throws Exception {
                // arrange

                ClothingBrands supremeOrig = ClothingBrands.builder()
                                .code("supreme")
                                .brand("supreme")
                                .price("$$$")
                                .build();

                ClothingBrands supremeEdited = ClothingBrands.builder()
                                .code("bape")
                                .brand("bape")
                                .price("$$")
                                .build();

                String requestBody = mapper.writeValueAsString(supremeEdited);

                when(clothingBrandsRepository.findById(eq("supreme"))).thenReturn(Optional.of(supremeOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/clothingbrands?code=supreme")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(clothingBrandsRepository, times(1)).findById("supreme");
                verify(clothingBrandsRepository, times(1)).save(supremeEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_brands_that_does_not_exist() throws Exception {
                // arrange

                ClothingBrands editedBrands = ClothingBrands.builder()
                                .code("forever21")
                                .brand("forever21")
                                .price("$")
                                .build();

                String requestBody = mapper.writeValueAsString(editedBrands);

                when(clothingBrandsRepository.findById(eq("forever21"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/clothingbrands?code=forever21")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(clothingBrandsRepository, times(1)).findById("forever21");
                Map<String, Object> json = responseToJson(response);
                assertEquals("ClothingBrands with id forever21 not found", json.get("message"));

        }
}
