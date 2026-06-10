package kavin.personal_project.streambase.mapper;

import kavin.personal_project.streambase.dto.NotificationDto;
import kavin.personal_project.streambase.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(NotificationEntity entity);
}
