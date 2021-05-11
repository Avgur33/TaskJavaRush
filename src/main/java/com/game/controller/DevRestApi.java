package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.BadRequestException;
import com.game.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/")
public class DevRestApi {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PlayerService playerService;

    @Autowired
    public DevRestApi(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("players/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(null, HttpStatus.resolve(400));
        }
        Player player = playerService.getPlayerById(id);
        if (player != null) {
            return new ResponseEntity<>(player, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.resolve(404));
        }
    }

    @DeleteMapping("players/{id}")
    public ResponseEntity<Integer> deletePlayerById(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(null, HttpStatus.resolve(400));
        }

        if (playerService.deletePlayerById(id) == 1) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.resolve(404));
        }
    }

    @PostMapping(value = "players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody(required = false) Player player) {
        try {
            Player newPlayer = playerService.update(id, player);
            if (newPlayer != null) {
                return new ResponseEntity<>(newPlayer, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.resolve(404));
            }
        } catch (BadRequestException e) {
            logger.info(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.resolve(400));
        }
    }

    @GetMapping("players/count")
    public ResponseEntity<Integer>
    getPlayersCount(@RequestParam(required = false) String name,
                    @RequestParam(required = false) String title,
                    @RequestParam(required = false) Race race,
                    @RequestParam(required = false) Profession profession,
                    @RequestParam(required = false) Long after,
                    @RequestParam(required = false) Long before,
                    @RequestParam(required = false) Boolean banned,
                    @RequestParam(required = false) Integer minExperience,
                    @RequestParam(required = false) Integer maxExperience,
                    @RequestParam(required = false) Integer minLevel,
                    @RequestParam(required = false) Integer maxLevel) {

        Integer count = playerService.getAllPlayersCountWithFilter(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping(value = "players")
    public ResponseEntity<List<Player>>
    getPlayers(@RequestParam(required = false) String name,
               @RequestParam(required = false) String title,
               @RequestParam(required = false) Race race,
               @RequestParam(required = false) Profession profession,
               @RequestParam(required = false) Long after,
               @RequestParam(required = false) Long before,
               @RequestParam(required = false) Boolean banned,
               @RequestParam(required = false) Integer minExperience,
               @RequestParam(required = false) Integer maxExperience,
               @RequestParam(required = false) Integer minLevel,
               @RequestParam(required = false) Integer maxLevel,
               @RequestParam(required = false) PlayerOrder order,
               @RequestParam(required = false) Integer pageNumber,
               @RequestParam(required = false) Integer pageSize) {

        List<Player> players =
                playerService.getAllPlayersWithFilter(name, title, race, profession,
                        after, before, banned, minExperience, maxExperience,
                        minLevel, maxLevel, order, pageNumber, pageSize);

        return new ResponseEntity<>(players, HttpStatus.OK);
/*
        if (pageSize == null) {
            Pageable paging = PageRequest.of(0, 3, Sort.by(PlayerOrder.ID.getFieldName()));
            //return new ResponseEntity<List<Player>>(playersRepo.findAll(paging).toList(), HttpStatus.OK);
            //return new ResponseEntity<>(myDao.select(3), HttpStatus.OK);
            return new ResponseEntity<>(myDao.exampleCriteriaPaging(), HttpStatus.OK);
        }
        Pageable paging = PageRequest.of(pageNumber,pageSize, Sort.by(order.getFieldName()));
        if (after != null ) {
            Date startDate = new Date(after);
        }
        if (before != null){
            Date stopDate = new Date(before);
        }

        *//*if (name!= null && title != null && race != null) {
            Specification<Player> specifications = Specification.where(PlayerSpecification.containsName(name).and(PlayerSpecification.hasRace(race)));
            return new ResponseEntity<List<Player>>(playersRepo.findAll(specifications,paging).toList(), HttpStatus.OK);
        }
        */
    }

    @PostMapping(value = "players/", consumes = "application/json")
    public ResponseEntity<Player> setPlayer(@RequestBody Player player) {
        try {
            return new ResponseEntity<>(playerService.createAndAddPlayer(player), HttpStatus.OK);
        } catch (BadRequestException e) {
            logger.info(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.resolve(400));
        }
    }
}
