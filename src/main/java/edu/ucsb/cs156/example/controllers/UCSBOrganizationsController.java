package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBOrganizations */
@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * List all UCSB organizations
   *
   * @return an iterable of UCSBOrganization
   */
  @Operation(summary = "List all ucsb organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allUCSBOrganizations() {
    Iterable<UCSBOrganization> organizations = ucsbOrganizationRepository.findAll();
    return organizations;
  }

  /**
   * Create a new date
   *
   * @param orgCode the organization code
   * @param orgTranslationShort the translation of code shortened
   * @param orgTranslation the translation of code
   * @param inactive whether the organization is inactive
   * @return the saved ucsborganization
   */
  @Operation(summary = "Create a new organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postUCSBOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam Boolean inactive) {

    UCSBOrganization ucsbOrganization = new UCSBOrganization();
    ucsbOrganization.setOrgCode(orgCode);
    ucsbOrganization.setOrgTranslationShort(orgTranslationShort);
    ucsbOrganization.setOrgTranslation(orgTranslation);
    ucsbOrganization.setInactive(inactive);

    UCSBOrganization savedUcsbOrganization = ucsbOrganizationRepository.save(ucsbOrganization);

    return savedUcsbOrganization;
  }

  /**
   * This method returns a single organization.
   *
   * @param orgCode code of the organization
   * @return a single organization
   */
  @Operation(summary = "Get a single organization")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganization getById(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganization organizations =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    return organizations;
  }

  /**
   * Update a single organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the organization
   * @param incoming the new organizations contents
   * @return the updated commons object
   */
  @Operation(summary = "Update a single organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBOrganization updateOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @RequestBody @Valid UCSBOrganization incoming) {

    UCSBOrganization organizations =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    organizations.setOrgTranslation(incoming.getOrgTranslation());
    organizations.setOrgTranslationShort(incoming.getOrgTranslationShort());
    organizations.setInactive(incoming.getInactive());

    ucsbOrganizationRepository.save(organizations);

    return organizations;
  }

  /**
   * Delete an organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the organization
   * @return a message indiciating the organization was deleted
   */
  @Operation(summary = "Delete a UCSBOrganization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteOrganization(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganization organizations =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    ucsbOrganizationRepository.delete(organizations);
    return genericMessage("UCSBOrganization with id %s deleted".formatted(orgCode));
  }
}
