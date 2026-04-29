package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Articles */
@Tag(name = "Articles")
@RestController
@RequestMapping("/api/articles")
@Slf4j
class ArticlesController extends ApiController {
  @Autowired ArticlesRepository ArticlesRepository;

  /**
   * List all Articles
   *
   * @return an iterable of Articles
   */
  @Operation(summary = "List all articles")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Articles> allArticles() {
    Iterable<Articles> articles = ArticlesRepository.findAll();
    return articles;
  }

  /**
   * Create a Article
   *
   * @param title the title of the article
   * @param url the URL of the article
   * @param explanation the explanation of the article
   * @param email the email of the person submitting the article
   * @param dateAdded the date the article was added
   * @return the saved Article
   */
  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Articles postArticle(
      @Parameter(name = "title") @RequestParam String title,
      @Parameter(name = "url") @RequestParam String url,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "email") @RequestParam String email,
      @Parameter(
              name = "dateAdded",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SSZ; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateAdded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime dateAdded)
      throws JsonProcessingException {
    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateAdded={}", dateAdded);
    Articles article = new Articles();
    article.setTitle(title);
    article.setUrl(url);
    article.setExplanation(explanation);
    article.setEmail(email);
    article.setDateAdded(dateAdded);
    Articles savedArticle = ArticlesRepository.save(article);
    return savedArticle;
  }

  /**
   * Get a single article by id
   *
   * @param id the id of the article
   * @return an Articles object
   */
  @Operation(summary = "Get a single article")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Articles getById(@Parameter(name = "id") @RequestParam Long id) {
    Articles article =
        ArticlesRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    return article;
  }

  /**
   * Update a single article
   *
   * @param id id of the article to update
   * @param incoming the new article
   * @return the updated article object
   */
  @Operation(summary = "Update a single article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public Articles updateArticle(
      @Parameter(name = "id") @RequestParam Long id,
      @org.springframework.web.bind.annotation.RequestBody @Valid Articles incoming) {

    Articles article =
        ArticlesRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    article.setTitle(incoming.getTitle());
    article.setUrl(incoming.getUrl());
    article.setExplanation(incoming.getExplanation());
    article.setEmail(incoming.getEmail());
    article.setDateAdded(incoming.getDateAdded());

    Articles savedArticle = ArticlesRepository.save(article);
    return savedArticle;
  }

  /**
   * Delete a article
   *
   * @param id the id of the article to delete
   * @return a message indicating the article was deleted
   */
  @Operation(summary = "Delete a article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteArticle(@Parameter(name = "id") @RequestParam Long id) {
    Articles article =
        ArticlesRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    ArticlesRepository.delete(article);
    return genericMessage("Article with id %s deleted".formatted(id));
  }
}
