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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
}
