package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.BadRequestException;
import com.game.repository.MyDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class PlayerService {

    private final MyDaoImpl myDao;

    @Autowired
    public PlayerService(MyDaoImpl myDao) {
        this.myDao = myDao;
    }

    public List<Player> getAllPlayersWithFilter(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {
        return myDao.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize);
    }


    public Player createAndAddPlayer(Player player) throws BadRequestException {
        String name = player.getName();
        String title = player.getTitle();
        Integer experience = player.getExperience();
        Date date = player.getBirthday();

        if (name == null || date == null || title == null || experience == null || player.getRace() == null || player.getProfession() == null) {
            throw new BadRequestException("Data Params IllegalArgumentException");
        }

        if (name.length() > 12 || title.length() > 30) {
            throw new BadRequestException("name or title IllegalArgumentException");
        }
        if (name.equals("")) {
            throw new BadRequestException("name or title IllegalArgumentException");
        }

        if (experience > 10000000 || experience < 0) {
            throw new BadRequestException("experience IllegalArgumentException");
        }

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (localDate.getYear() < 2000 || localDate.getYear() > 3000) {
            throw new BadRequestException("birthday IllegalArgumentException");
        }

        int currentLevel = getCurrentLevel(experience);
        int untilNextLevel = getUntilNextLevel(experience, currentLevel);
        player.setLevel(currentLevel);
        player.setUntilNextLevel(untilNextLevel);

        return myDao.savePlayer(player);
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

        if (player == null){
            List<Player> players = myDao.getPlayerById(id);
            return players.size() > 0 ? players.get(0): null;
        }

        String name = player.getName();
        String title = player.getTitle();
        Integer experience = player.getExperience();
        Date date = player.getBirthday();

        if (name == null && date == null && title == null && experience == null && player.getRace() == null && player.getProfession() == null) {
            List<Player> players = myDao.getPlayerById(id);
            return players.size() > 0 ? players.get(0): null;
        }


        if (name != null && (name.length() > 12 || name.equals(""))) {
            throw new BadRequestException(String.format("name ={%s} IllegalArgumentException", name));
        }

        if (title != null && title.length() > 30) {
            throw new BadRequestException("name or title IllegalArgumentException");
        }

        if (experience != null && (experience > 10000000 || experience < 0)) {
            throw new BadRequestException("experience IllegalArgumentException");
        }else if( experience != null){
            int currentLevel = getCurrentLevel(experience);
            int untilNextLevel = getUntilNextLevel(experience, currentLevel);
            player.setLevel(currentLevel);
            player.setUntilNextLevel(untilNextLevel);
        }

        if (date != null) {
            LocalDate localDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (localDate.getYear() < 2000 || localDate.getYear() > 3000) {
                throw new BadRequestException("birthday IllegalArgumentException");
            }
        }
        return myDao.updatePlayer(id, player);
    }

    public Integer getAllPlayersCountWithFilter(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        return Math.toIntExact( myDao.getPlayersCount(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel));
    }

    public Integer deletePlayerById(Long id) {
        return myDao.deletePlayerById(id);
    }

    public Player getPlayerById(Long id) {
        List<Player> players = myDao.getPlayerById(id);
        if (players.size() == 0) {return null;}
        return players.get(0);
    }
}
