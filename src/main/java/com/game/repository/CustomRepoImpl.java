package com.game.repository;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public class CustomRepoImpl implements CustomRepo {

    @PersistenceContext
    private EntityManager entityManager;

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

    public Integer deletePlayerById(Long id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<Player> criteriaDelete = criteriaBuilder.createCriteriaDelete(Player.class);
        Root<Player> rootPlayer = criteriaDelete.from(Player.class);
        criteriaDelete.where(criteriaBuilder.equal(rootPlayer.get("id"), id));
        return entityManager.createQuery(criteriaDelete).executeUpdate();
    }
 }
