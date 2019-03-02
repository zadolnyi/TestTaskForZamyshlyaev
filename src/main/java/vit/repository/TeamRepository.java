package vit.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vit.domain.Team;

/**
 * Created by zadol on 21.02.2019.
 */
@Repository
public interface TeamRepository extends MongoRepository<Team, Long> {
}