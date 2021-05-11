package com.game.repository;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class MyDaoImpl implements MyDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Player> select(long id) {
        TypedQuery<Player> query = entityManager.createQuery("select a from Player a where a.id = :id", Player.class);
        query.setParameter("id", id);
        List<Player> list = query.getResultList();
        return CollectionUtils.isEmpty(list) ? null : list;
    }

    public Player savePlayer(Player player){
        entityManager.persist(player);
        entityManager.flush();
        return player;
    }

    public List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before,
                                   Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel,
                                   Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Player> cq = cb.createQuery(Player.class);
        Root<Player> playerRoot = cq.from(Player.class);
        List<Predicate> predicates = new ArrayList<>();

        int pageNumb = 0;
        int pageSiz = 3;

        predicates = createPredicateList(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, cb, playerRoot, predicates);

        if (predicates.size() == 0) {
            cq.select(playerRoot);
        } else {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        if (order != null) {
            cq.orderBy(cb.asc(playerRoot.get(order.getFieldName())));
        } else {
            cq.orderBy(cb.asc(playerRoot.get("id")));
        }

        if (pageNumber != null) {
            pageNumb = pageNumber;
        }

        if (pageSize != null) {
            pageSiz = pageSize;
        }

        TypedQuery<Player> typedQuery = entityManager.createQuery(cq);

        typedQuery.setFirstResult(pageNumb + (pageNumb * pageSiz - pageNumb));
        typedQuery.setMaxResults(pageSiz);

        return typedQuery.getResultList();
    }

    public Long getPlayersCount(String name, String title, Race race, Profession profession, Long after, Long before,
                                Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel,
                                Integer maxLevel) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Player> playerRoot = cq.from(Player.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates = createPredicateList(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, cb, playerRoot, predicates);

        if (predicates.size() != 0) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        cq.select(cb.count(playerRoot));

        TypedQuery<Long> typedQuery = entityManager.createQuery(cq);

        return typedQuery.getResultList().get(0);
    }

    private List<Predicate> createPredicateList(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, CriteriaBuilder cb, Root<Player> playerRoot, List<Predicate> predicates) {
        if (name != null) {
            predicates.add(cb.like(playerRoot.get("name"), "%" + name + "%"));
        }

        if (title != null) {
            predicates.add(cb.like(playerRoot.get("title"), "%" + title + "%"));
        }

        if (race != null) {
            predicates.add(cb.equal(playerRoot.get("race"), race));
        }

        if (profession != null) {
            predicates.add(cb.equal(playerRoot.get("profession"), profession));
        }

        if (after != null) {
            predicates.add(cb.greaterThanOrEqualTo(playerRoot.get("birthday"), new Date(after)));
        }

        if (before != null) {
            predicates.add(cb.lessThanOrEqualTo(playerRoot.get("birthday"), new Date(before)));
        }

        if (banned != null) {
            predicates.add(cb.equal(playerRoot.get("banned"), banned));
        }

        if (minExperience != null) {
            predicates.add(cb.ge(playerRoot.get("experience"), minExperience));
        }

        if (maxExperience != null) {
            predicates.add(cb.le(playerRoot.get("experience"), maxExperience));
        }

        if (minLevel != null) {
            predicates.add(cb.ge(playerRoot.get("level"), minLevel));
        }
        if (maxLevel != null) {
            predicates.add(cb.le(playerRoot.get("level"), maxLevel));
        }
        return predicates;
    }

    public List<Player> getPlayerById(Long id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
        Root<Player> playerRoot = criteriaQuery.from(Player.class);
        criteriaQuery.where(criteriaBuilder.equal(playerRoot.get("id"),id));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public Integer deletePlayerById(Long id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<Player> criteriaDelete = criteriaBuilder.createCriteriaDelete(Player.class);
        Root<Player> rootPlayer = criteriaDelete.from(Player.class);
        criteriaDelete.where(criteriaBuilder.equal(rootPlayer.get("id"), id));
        return entityManager.createQuery(criteriaDelete).executeUpdate();
    }

    public Player updatePlayer(Long id, Player player) {

        List<Player> players;

        /*players = getPlayerById(id);
        if (players.size() < 1) return null;*/

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Player> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Player.class);
        Root<Player> rootPlayer = criteriaUpdate.from(Player.class);

        if (player.getName() != null) {
            criteriaUpdate.set("name", player.getName());
        }
        if (player.getTitle() != null) {
            criteriaUpdate.set("title", player.getTitle());
        }

        if (player.getRace() != null) {
            criteriaUpdate.set("race", player.getRace());
        }

        if (player.getProfession() != null) {
            criteriaUpdate.set("profession", player.getProfession());
        }

        if (player.getBirthday() != null) {
            criteriaUpdate.set("birthday",player.getBirthday());
        } else {
           // criteriaUpdate.set(rootPlayer.get("birthday"), players.get(0).getBirthday());
        }

        if (player.getBanned() != null) {
            criteriaUpdate.set("banned", player.getBanned());
        }

        if (player.getExperience() != null) {
            criteriaUpdate.set("experience", player.getExperience());
            criteriaUpdate.set("level", player.getLevel());
            criteriaUpdate.set("untilNextLevel", player.getUntilNextLevel());
        }

        criteriaUpdate.where(criteriaBuilder.equal(rootPlayer.get("id"), id));
        if ( entityManager.createQuery(criteriaUpdate).executeUpdate() == 0){
            return null;
        }
        entityManager.flush();
        players = getPlayerById(id);
        return players.size() > 0 ? players.get(0): null;
    }
}
