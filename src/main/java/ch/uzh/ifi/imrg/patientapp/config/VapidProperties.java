package ch.uzh.ifi.imrg.patientapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "push.vapid")
public class VapidProperties {

    private String publicKey;

    private String privateKey;

    private String subject;

}
