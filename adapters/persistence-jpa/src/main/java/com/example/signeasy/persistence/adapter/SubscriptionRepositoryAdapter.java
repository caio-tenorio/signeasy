package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.SubscriptionRepositoryPort;
import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.persistence.entity.SubscriptionJpaEntity;
import jakarta.persistence.EntityManager;
import com.example.signeasy.persistence.jpa.SubscriptionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.signeasy.persistence.mapper.SubscriptionJpaMapper;

import java.util.*;

@Repository
@Transactional
public class SubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {
    private final SubscriptionJpaRepository repo;
    private final SubscriptionJpaMapper mapper;
    private final EntityManager em;

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository repo, SubscriptionJpaMapper mapper, EntityManager em) {
        this.repo = repo;
        this.mapper = mapper;
        this.em = em;
    }

    @Override
    public Subscription create(Subscription subscription) {
        var entity = mapper.toEntity(subscription);
        em.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Subscription update(Subscription subscription) {
        // Reuses the managed entity when the service already loaded it in this transaction.
        var entity = em.find(SubscriptionJpaEntity.class, mapper.toKey(subscription.getKey()));
        if (entity == null) {
            throw new BusinessException("Subscription not found");
        }
        mapper.updateEntity(subscription, entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Subscription> findByIdAndTenantId(UUID id, String tenantId) {
        return repo.findByKeyIdAndKeyTenantId(id, tenantId).map(mapper::toDomain);
    }
}
