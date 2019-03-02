package vit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vit.domain.Team;
import vit.rabbitmq.Publisher;
import vit.repository.TeamRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

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

    @PostMapping("setCommander")
    public synchronized HashMap setCommander(@RequestParam Long teamId, @RequestParam Long participantId, @RequestParam String participantIdentifier) {
        logger.debug("setCommander teamId=" + teamId + " participantId="+participantId);
        boolean success = false;
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            if (team.getParticipantId() == null) {
                team.setParticipantId(participantId);
                team.setParticipantIdentifier(participantIdentifier);
                teamRepository.save(team);
                success = true;
            }
        } else {
            teamRepository.save(new Team (teamId, participantId, participantIdentifier));
            success = true;
        }
        if (success) {
            publisher.produceMsg("(" + df.format(new Date()) + ")Команда " + teamId + " теперь имеет капитана " + participantIdentifier);
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("success", Boolean.toString(success));
        logger.debug("setCommander map=" + map);
        return map;
    }

    @GetMapping("getCommander")
    public HashMap getCommander(@RequestParam Long teamId) {
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

    @PostMapping("refuseCommander")
    public void refuseCommander(@RequestParam Long teamId, @RequestParam Long participantId) {
        logger.debug("refuseCommander teamId=" + teamId + " participantId="+participantId);
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