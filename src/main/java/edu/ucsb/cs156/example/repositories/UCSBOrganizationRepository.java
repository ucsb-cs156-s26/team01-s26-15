package edu.ucsb.cs156.example.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/** The UCSBOrganizationsRepository is a repository for UCSBOrganization entities. */
@Repository
@RepositoryRestResource(exported = false)
public interface UCSBOrganizationRepository extends CrudRepository<UCSBOrganization, Long> {}
