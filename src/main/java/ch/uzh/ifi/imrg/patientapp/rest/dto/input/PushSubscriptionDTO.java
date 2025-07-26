package ch.uzh.ifi.imrg.patientapp.rest.dto.input;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "PushSubscription", description = "The browser’s push subscription details, as returned by ServiceWorkerRegistration.pushManager.getSubscription()")
public class PushSubscriptionDTO {

    @Schema(description = "The push service endpoint URL where notifications should be sent", example = "https://fcm.googleapis.com/fcm/send/abc123...")
    public String endpoint;

    @Schema(description = "Keys used by the browser to encrypt and authenticate the notification payload", example = "{ \"p256dh\": \"BNc...\", \"auth\": \"abcd...\" }")
    public Map<String, String> keys;

}
