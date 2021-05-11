package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.BadRequestException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playersRepo;

    @Autowired
    public PlayerService(PlayerRepository playersRepo) {
        this.playersRepo = playersRepo;
    }

    public List<Player> getAllPlayersWithFilter(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {
        return playersRepo.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize);
    }

    public Integer getAllPlayersCountWithFilter(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        return Math.toIntExact(playersRepo.getPlayersCount(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel));
    }

    public Player createAndAddPlayer(Player player) throws BadRequestException {
        String name = player.getName();
        String title = player.getTitle();
        Integer experience = player.getExperience();
        Date date = player.getBirthday();

        if (name == null || date == null || title == null || experience == null || player.getRace() == null || player.getProfession() == null) {
            throw new BadRequestException("Data Params IllegalArgumentException");
        }

        if (name.length() > 12 || name.equals("")) {
            throw new BadRequestException("name IllegalArgumentException");
        }
        if ( title.length() > 30) {
            throw new BadRequestException("title IllegalArgumentException");
        }

        if (experience > 10000000 || experience < 0) {
            throw new BadRequestException("experience IllegalArgumentException");
        }

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (localDate.getYear() < 2000 || localDate.getYear() > 3000) {
            throw new BadRequestException("birthday IllegalArgumentException");
        }

        int currentLevel = getCurrentLevel(experience);
        int untilNextLevel = getUntilNextLevel(experience, currentLevel);
        player.setLevel(currentLevel);
        player.setUntilNextLevel(untilNextLevel);
        return playersRepo.save(player);
    }

    private int getUntilNextLevel(Integer experience, int currentLevel) {
        return 50 * (currentLevel + 1) * (currentLevel + 2) - experience;
    }

    private Integer getCurrentLevel(Integer experience) {
        Double step = (Math.sqrt(2500.0 + 200.0 * experience) - 50);
        return step.intValue() / 100;
    }

    public Player update(Long id, Player player) throws BadRequestException {
        if (id < 1) {
            throw new BadRequestException("id < 1 IllegalArgumentException");
        }

        if (player == null) {
            return playersRepo.getById(id);
        }

        Player playerToUpdate = playersRepo.getById(id);
        if (playerToUpdate == null) return null;

        String name = player.getName();
        String title = player.getTitle();
        Integer experience = player.getExperience();
        Date date = player.getBirthday();
        Race race = player.getRace();
        Profession profession = player.getProfession();
        Boolean banned = player.getBanned();

        if (name == null && date == null && title == null && experience == null && race == null && profession == null && banned == null) {
            return playersRepo.getById(id);
        }

        if (name != null) {
            if (name.length() > 12 || name.equals(""))
                throw new BadRequestException(String.format("name ={%s} IllegalArgumentException", name));
            playerToUpdate.setName(name);
        }

        if (title != null) {
            if (title.length() > 30)
                throw new BadRequestException("name or title IllegalArgumentException");
            playerToUpdate.setTitle(title);
        }

        if (race != null) {
            playerToUpdate.setRace(race);
        }
        if (profession != null) {
            playerToUpdate.setProfession(profession);
        }
        if (banned != null) {
            playerToUpdate.setBanned(banned);
        }

        if (experience != null) {
            if (experience > 10000000 || experience < 0)
                throw new BadRequestException("experience IllegalArgumentException");
            int currentLevel = getCurrentLevel(experience);
            int untilNextLevel = getUntilNextLevel(experience, currentLevel);
            playerToUpdate.setExperience(experience);
            playerToUpdate.setLevel(currentLevel);
            playerToUpdate.setUntilNextLevel(untilNextLevel);
        }

        if (date != null) {
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (localDate.getYear() < 2000 || localDate.getYear() > 3000) {
                throw new BadRequestException("birthday IllegalArgumentException");
            } else {
                playerToUpdate.setBirthday(date);
            }
        }
        return playersRepo.save(playerToUpdate);
    }


    public Integer deletePlayerById(Long id) throws BadRequestException {
        if (id < 1) throw new BadRequestException("id < 1");
        return playersRepo.deletePlayerById(id);
    }

    public Player getPlayerById(Long id) throws BadRequestException {
        if (id < 1) throw new BadRequestException("id < 1");
        return playersRepo.getById(id);
    }
}
