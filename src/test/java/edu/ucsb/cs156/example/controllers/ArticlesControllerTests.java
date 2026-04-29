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
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticlesRepository articlesRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/articles/admin/all
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/articles/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                // Use the parameters defined in your ArticlesController
                .param("title", "Test Title")
                .param("url", "https://example.com")
                .param("explanation", "Test Explanation")
                .param("email", "test@ucsb.edu")
                .param("dateAdded", "2026-04-24T19:07:18.613Z")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsbdates() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    ZonedDateTime zdt1 = ZonedDateTime.parse("2026-04-24T19:07:18.613Z");
    Articles article1 =
        Articles.builder()
            .title("To be or not To be")
            .url("https://www.amazon.com/hz/mobile")
            .explanation("aa")
            .email("xuanbo@ucsb.edu")
            .dateAdded(zdt1)
            .build();

    ArrayList<Articles> expectedArticles = new ArrayList<>();
    expectedArticles.add(article1);

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(articlesRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    // arrange

    ZonedDateTime zdt1 = ZonedDateTime.parse("2026-04-24T19:07:18.613Z");

    Articles article1 =
        Articles.builder()
            .title("To be or not To be")
            .url("https://www.amazon.com/hz/mobile")
            .explanation("aa")
            .email("xuanbo@ucsb.edu")
            .dateAdded(zdt1)
            .build();

    when(articlesRepository.save(eq(article1))).thenReturn(article1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post?title=To be or not To be&url=https://www.amazon.com/hz/mobile&explanation=aa&email=xuanbo@ucsb.edu&dateAdded=2026-04-24T19:07:18.613Z")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).save(article1);
    String expectedJson = mapper.writeValueAsString(article1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/articles").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(articlesRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(articlesRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

    // arrange
    ZonedDateTime zdt1 = ZonedDateTime.parse("2026-04-24T19:07:18.613Z");

    Articles article1 =
        Articles.builder()
            .title("To be or not To be")
            .url("https://www.amazon.com/hz/mobile")
            .explanation("aa")
            .email("xuanbo@ucsb.edu")
            .dateAdded(zdt1)
            .build();
    when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(articlesRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(article1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_article() throws Exception {
    // arrange

    ZonedDateTime zdt1 = ZonedDateTime.parse("2026-04-24T19:07:18.613Z");
    ZonedDateTime zdt2 = ZonedDateTime.parse("2026-04-14T19:07:18.613Z");

    Articles article1 =
        Articles.builder()
            .id(67L)
            .title("To be or not To be")
            .url("https://www.amazon.com/hz/mobile")
            .explanation("aa")
            .email("xuanbo@ucsb.edu")
            .dateAdded(zdt1)
            .build();

    Articles editedArticle =
        Articles.builder()
            .id(67L)
            .title("To be")
            .url(
                "https://docs.google.com/presentation/d/1BTg6PEWBjqGG7nBHlcOk1WL7SDR4TywB-SPjUVFyZmY/edit?slide=id.p13#slide=id.p13")
            .explanation("aaa")
            .email("zhao@ucsb.edu")
            .dateAdded(zdt2)
            .build();

    String requestBody = mapper.writeValueAsString(editedArticle);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(article1));
    when(articlesRepository.save(article1)).thenAnswer(invocation -> invocation.getArgument(0));
    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("To be"))
            .andExpect(
                jsonPath("$.url")
                    .value(
                        "https://docs.google.com/presentation/d/1BTg6PEWBjqGG7nBHlcOk1WL7SDR4TywB-SPjUVFyZmY/edit?slide=id.p13#slide=id.p13"))
            .andExpect(jsonPath("$.explanation").value("aaa"))
            .andExpect(jsonPath("$.email").value("zhao@ucsb.edu"))
            .andExpect(jsonPath("$.dateAdded").value("2026-04-14T19:07:18.613Z"))
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(67L);
    verify(articlesRepository, times(1)).save(article1);

    // ✅ Add these to kill the surviving mutations
    assertEquals("To be", article1.getTitle());
    assertEquals(
        "https://docs.google.com/presentation/d/1BTg6PEWBjqGG7nBHlcOk1WL7SDR4TywB-SPjUVFyZmY/edit?slide=id.p13#slide=id.p13",
        article1.getUrl());
    assertEquals("aaa", article1.getExplanation());
    assertEquals("zhao@ucsb.edu", article1.getEmail());
    assertEquals(zdt2, article1.getDateAdded());

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
    // arrange

    ZonedDateTime zdt1 = ZonedDateTime.parse("2026-04-24T19:07:18.613Z");

    Articles article1 =
        Articles.builder()
            .title("To be or not To be")
            .url("https://www.amazon.com/hz/mobile")
            .explanation("aa")
            .email("xuanbo@ucsb.edu")
            .dateAdded(zdt1)
            .build();

    String requestBody = mapper.writeValueAsString(article1);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_article() throws Exception {
    // arrange

    ZonedDateTime zdt1 = ZonedDateTime.parse("2026-04-24T19:07:18.613Z");

    Articles article1 =
        Articles.builder()
            .title("To be or not To be")
            .url("https://www.amazon.com/hz/mobile")
            .explanation("aa")
            .email("xuanbo@ucsb.edu")
            .dateAdded(zdt1)
            .build();

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(article1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/articles").param("id", "67").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(67L);
    verify(articlesRepository, times(1)).delete(eq(article1));

    Map<String, Object> json = responseToJson(response);
    assertEquals("Article with id 67 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_article_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(articlesRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/articles").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 15 not found", json.get("message"));
  }
}
