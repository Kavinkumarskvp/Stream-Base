package kavin.personal_project.streambase.mapper;

import kavin.personal_project.streambase.dto.CreateVideoRequest;
import kavin.personal_project.streambase.dto.UpdateVideoRequest;
import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.entity.VideoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    @Mapping(target = "name", source = "title")
    @Mapping(target = "location", source = "url")
    @Mapping(target = "author", source = "uploadedBy")
    @Mapping(target = "uploadedTime", source = "createdAt")
    VideoDto toDto(VideoEntity videoEntity);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "uploadedBy", source = "author")
    @Mapping(target = "status", ignore = true)
    VideoEntity toEntity(CreateVideoRequest request);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(UpdateVideoRequest request, @MappingTarget VideoEntity entity);
}
