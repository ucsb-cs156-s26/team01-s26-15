package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
            .inactive(false)
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
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.save(eq(ucsbOrganization1))).thenReturn(ucsbOrganization1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganizations/post?orgCode=AACF&orgTranslationShort=AsianAmericanChristianFellowship&orgTranslation=UCSBAsianAmericanChristianFellowship&inactive=false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(ucsbOrganization1);
    String expectedJson = mapper.writeValueAsString(ucsbOrganization1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
