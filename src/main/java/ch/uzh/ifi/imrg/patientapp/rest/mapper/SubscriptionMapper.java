package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ch.uzh.ifi.imrg.patientapp.entity.NotificationSubscription;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.PushSubscriptionDTO;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "p256dh", source = "keys.p256dh")
    @Mapping(target = "auth", source = "keys.auth")
    @Mapping(target = "patient", ignore = true)
    NotificationSubscription toEntity(PushSubscriptionDTO dto);

}
