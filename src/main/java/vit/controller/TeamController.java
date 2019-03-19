package vit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vit.domain.Team;
import vit.rabbitmq.Publisher;
import vit.repository.TeamRepository;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zadol on 21.02.2019.
 */
@RestController
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private Publisher publisher;

    private static DateFormat df = new SimpleDateFormat("dd-MM-yy hh-mm-ss-S");
    private static Logger logger = LoggerFactory.getLogger(TeamController.class);

    @PostMapping("team")
    public synchronized ResponseEntity<Object> setCommander(@RequestParam Long teamId, @RequestParam Long participantId, @RequestParam String participantIdentifier) {
        Map<String, Boolean> mapToResponce = Collections.singletonMap("success", Boolean.TRUE);
        ResponseEntity responseEntity = null;
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            if (team.getParticipantId() == null) {
                team.setParticipantId(participantId);
                team.setParticipantIdentifier(participantIdentifier);
                teamRepository.save(team);
                responseEntity = ResponseEntity.ok(mapToResponce);
            } else {
                Map<String, Boolean> mapToResponceFalse = Collections.singletonMap("success", Boolean.FALSE);
                responseEntity = ResponseEntity.ok(mapToResponceFalse);
            }
        } else {
            Team newTeam = teamRepository.save(new Team(teamId, participantId, participantIdentifier));
            logger.debug("newTeam teamId=" + teamId + " participantId="+participantId);
            URI newURI = URI.create("team/" + String.valueOf(newTeam.getId()));
            responseEntity = ResponseEntity.created(newURI).body(mapToResponce);
        }
        return responseEntity;
    }

    @GetMapping("/team/{teamId}")
    public HashMap getCommander(@PathVariable Long teamId) {
        HashMap<String, String> map = new HashMap<>();
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            if (team != null && team.getParticipantId() != null) {
                map.put("participantId", String.valueOf(team.getParticipantId()));
                map.put("participantIdentifier", team.getParticipantIdentifier());
            }
        }
        return map;     // { "participantId": team.getParticipantId(), "participantIdentifier": team.getParticipantIdentifier() }
    }

    @PutMapping("team")
    public void refuseCommander(@RequestParam Long teamId, @RequestParam Long participantId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            if (team.getParticipantId() == participantId) {
                team.setParticipantId(null);
                team.setParticipantIdentifier(null);
                teamRepository.save(team);
                publisher.produceMsg("(" + df.format(new Date())+ ")Команда " + teamId + " теперь без капитана");
            }
        }
    }
}