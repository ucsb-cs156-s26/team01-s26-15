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
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "1")
                .param("reviewerEmail", "test@example.com")
                .param("stars", "5")
                .param("dateReviewed", "2026-04-27T12:30:00")
                .param("comments", "Great food")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "1")
                .param("reviewerEmail", "test@example.com")
                .param("stars", "5")
                .param("dateReviewed", "2026-04-27T12:30:00")
                .param("comments", "Great food")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menuitemreviews() throws Exception {
    LocalDateTime dateReviewed1 = LocalDateTime.parse("2026-04-27T12:30:00");

    MenuItemReview review1 =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("test1@example.com")
            .stars(5)
            .dateReviewed(dateReviewed1)
            .comments("Great food")
            .build();

    LocalDateTime dateReviewed2 = LocalDateTime.parse("2026-04-28T13:45:00");

    MenuItemReview review2 =
        MenuItemReview.builder()
            .itemId(2)
            .reviewerEmail("test2@example.com")
            .stars(4)
            .dateReviewed(dateReviewed2)
            .comments("Pretty good")
            .build();

    ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
    expectedReviews.addAll(Arrays.asList(review1, review2));

    when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedReviews);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {
    LocalDateTime dateReviewed = LocalDateTime.parse("2026-04-27T12:30:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("test@example.com")
            .stars(5)
            .dateReviewed(dateReviewed)
            .comments("Great food")
            .build();

    when(menuItemReviewRepository.save(eq(review))).thenReturn(review);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreview/post")
                    .param("itemId", "1")
                    .param("reviewerEmail", "test@example.com")
                    .param("stars", "5")
                    .param("dateReviewed", "2026-04-27T12:30:00")
                    .param("comments", "Great food")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).save(review);

    String expectedJson = mapper.writeValueAsString(review);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/menuitemreview").param("id", "7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_id_exists() throws Exception {

    LocalDateTime dateReviewed = LocalDateTime.parse("2026-04-27T12:30:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("test@example.com")
            .stars(5)
            .dateReviewed(dateReviewed)
            .comments("Great food")
            .build();

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(review));

    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreview").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));

    String expectedJson = mapper.writeValueAsString(review);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_cannot_get_by_id_when_id_does_not_exist() throws Exception {
    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/menuitemreview").param("id", "7")).andExpect(status().isNotFound());

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_menuitemreview() throws Exception {

    LocalDateTime dateReviewed1 = LocalDateTime.parse("2026-04-27T12:30:00");
    LocalDateTime dateReviewed2 = LocalDateTime.parse("2026-04-28T13:45:00");

    MenuItemReview reviewOrig =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("old@example.com")
            .stars(3)
            .dateReviewed(dateReviewed1)
            .comments("Original comment")
            .build();

    MenuItemReview reviewEdited =
        MenuItemReview.builder()
            .itemId(2)
            .reviewerEmail("new@example.com")
            .stars(5)
            .dateReviewed(dateReviewed2)
            .comments("Updated comment")
            .build();

    String requestBody = mapper.writeValueAsString(reviewEdited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.of(reviewOrig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(67L);
    verify(menuItemReviewRepository, times(1)).save(reviewEdited);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {

    LocalDateTime dateReviewed = LocalDateTime.parse("2026-04-27T12:30:00");

    MenuItemReview reviewEdited =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("test@example.com")
            .stars(5)
            .dateReviewed(dateReviewed)
            .comments("Great food!")
            .build();

    String requestBody = mapper.writeValueAsString(reviewEdited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(67L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_menuitemreview() throws Exception {

    LocalDateTime dateReviewed = LocalDateTime.parse("2026-04-27T12:30:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("test@example.com")
            .stars(5)
            .dateReviewed(dateReviewed)
            .comments("Great food")
            .build();

    when(menuItemReviewRepository.findById(eq(15L))).thenReturn(Optional.of(review));

    MvcResult response =
        mockMvc
            .perform(delete("/api/menuitemreview").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(15L);
    verify(menuItemReviewRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_menuitemreview_that_does_not_exist() throws Exception {

    when(menuItemReviewRepository.findById(eq(15L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/menuitemreview").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(15L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 15 not found", json.get("message"));
  }
}
