package ch.uzh.ifi.imrg.patientapp.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

import ch.uzh.ifi.imrg.patientapp.config.VapidProperties;
import ch.uzh.ifi.imrg.patientapp.entity.NotificationSubscription;
import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.NotificationSubscriptionRepository;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PushSubscriptionDTO;
import ch.uzh.ifi.imrg.patientapp.rest.mapper.SubscriptionMapper;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Service
@Transactional
public class PushNotificationService {

    private final NotificationSubscriptionRepository subRepo;
    private final PushService pushService;
    private final SubscriptionMapper mapper;

    public PushNotificationService(NotificationSubscriptionRepository subRepo, VapidProperties vapid,
            SubscriptionMapper mapper) throws GeneralSecurityException {
        this.subRepo = subRepo;
        this.mapper = mapper;

        Security.addProvider(new BouncyCastleProvider());

        this.pushService = new PushService(vapid.getPublicKey(), vapid.getPrivateKey());
    }

    public void subscribe(PushSubscriptionDTO dto, Patient patient) {
        NotificationSubscription sub = subRepo.findByEndpoint(dto.getEndpoint()).orElseGet(() -> mapper.toEntity(dto));
        sub.setPatient(patient);
        subRepo.save(sub);
    }

    public void unsubscribe(PushSubscriptionDTO dto) {
        subRepo.deleteByEndpoint(dto.getEndpoint());
    }

    public void sendToPatient(String patientId, String title, String body) {
        String payload = new JSONObject()
                .put("title", title)
                .put("body", body)
                .toString();

        List<NotificationSubscription> subs = subRepo.findAllByPatient_Id(patientId);

        System.out.println("sendToPatient: found " + subs.size() + " subscriptions for patient " + patientId);

        for (NotificationSubscription sub : subs) {

            System.out.println("  sending to endpoint " + sub.getEndpoint());

            try {
                pushService.send(new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payload.getBytes(),
                        3600));

                System.out.println("  send() completed without exception");

            } catch (
                    GeneralSecurityException | IOException | JoseException | ExecutionException
                    | InterruptedException e) {

                System.out.println("  send() failed for endpoint " + sub.getEndpoint() + " — removing subscription");
                e.printStackTrace();

                subRepo.delete(sub);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
