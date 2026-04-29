package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganizations/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganizations/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganizations/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganizations/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    UCSBOrganization ucsbOrganization1 =
        UCSBOrganization.builder()
            .orgCode("AACF")
            .orgTranslationShort("Asian American Christian Fellowship")
            .orgTranslation("UCSB Asian American Christian Fellowship")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.add(ucsbOrganization1);

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganizations/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsborganization() throws Exception {

    UCSBOrganization ucsbOrganization1 =
        UCSBOrganization.builder()
            .orgCode("AACF")
            .orgTranslationShort("AsianAmericanChristianFellowship")
            .orgTranslation("UCSBAsianAmericanChristianFellowship")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(ucsbOrganization1))).thenReturn(ucsbOrganization1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganizations/post?orgCode=AACF&orgTranslationShort=AsianAmericanChristianFellowship&orgTranslation=UCSBAsianAmericanChristianFellowship&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(ucsbOrganization1);
    String expectedJson = mapper.writeValueAsString(ucsbOrganization1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganizations").param("orgCode", "a2f"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBOrganization organizations =
        UCSBOrganization.builder()
            .orgCode("A2F")
            .orgTranslationShort("Acts2Fellowship")
            .orgTranslation("UCSBActs2Fellowship")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("A2F"))).thenReturn(Optional.of(organizations));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganizations").param("orgCode", "A2F"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("A2F"));
    String expectedJson = mapper.writeValueAsString(organizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganizations").param("orgCode", "munger-hall"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("munger-hall"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization epicOrig =
        UCSBOrganization.builder()
            .orgCode("Epic")
            .orgTranslation("EpicMovementCru")
            .orgTranslationShort("EpicMovement")
            .inactive(true)
            .build();

    UCSBOrganization epicEdited =
        UCSBOrganization.builder()
            .orgCode("Epic")
            .orgTranslation("UCSBEpicMovementCru")
            .orgTranslationShort("UCSBEpicMovement")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(epicEdited);

    when(ucsbOrganizationRepository.findById(eq("Epic"))).thenReturn(Optional.of(epicOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganizations")
                    .param("orgCode", "Epic")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("Epic");
    verify(ucsbOrganizationRepository, times(1))
        .save(epicEdited); // should be saved with updated info
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganization editedOrganization =
        UCSBOrganization.builder()
            .orgCode("MG")
            .orgTranslation("munger-hall-ucsb")
            .orgTranslationShort("munger-hall")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrganization);

    when(ucsbOrganizationRepository.findById(eq("MG"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganizations")
                    .param("orgCode", "MG")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("MG");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id MG not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_organization() throws Exception {
    // arrange

    UCSBOrganization kcm =
        UCSBOrganization.builder()
            .orgCode("kcm")
            .orgTranslation("KristosCampusMinistry")
            .orgTranslationShort("UCSBKCM")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("kcm"))).thenReturn(Optional.of(kcm));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganizations").param("orgCode", "kcm").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("kcm");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id kcm deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganizations").param("orgCode", "munger-hall").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("munger-hall");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
  }
}
