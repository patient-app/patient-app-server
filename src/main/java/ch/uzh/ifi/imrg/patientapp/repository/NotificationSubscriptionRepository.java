package ch.uzh.ifi.imrg.patientapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.uzh.ifi.imrg.patientapp.entity.NotificationSubscription;

public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, String> {

    List<NotificationSubscription> findAllByPatient_Id(String patientId);

    void deleteByEndpoint(String endpoint);

    Optional<NotificationSubscription> findByEndpoint(String endpoint);
}
