package org.netway.dongnehankki.store.imfrastructure;

import org.netway.dongnehankki.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByStoreId(Long storeId);
}
