package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.SubscriptionRepositoryPort;
import com.example.signeasy.domain.model.Subscription;
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

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository repo, SubscriptionJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Subscription save(Subscription s) {
        var entity = repo.findById(mapper.toKey(s.getKey())).orElse(null);
        if (entity == null) {
            entity = mapper.toEntity(s);
        } else {
            mapper.updateEntity(s, entity);
        }
        return mapper.toDomain(repo.saveAndFlush(entity));
    }

    @Override
    public Optional<Subscription> findByIdAndTenantId(UUID id, String tenantId) {
        return repo.findByKeyIdAndKeyTenantId(id, tenantId).map(mapper::toDomain);
    }
}
