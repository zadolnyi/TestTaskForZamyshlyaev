package vit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Created by zadol on 21.02.2019.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    @Id
    private Long id;
    private Long participantId;
    private String participantIdentifier;
}