package kavin.personal_project.streambase.mapper;

import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.entity.VideoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    @Mapping(target = "name" , source = "title")
    @Mapping(target = "location" , source = "url")
    @Mapping(target = "author" , source = "uploadedBy")
    @Mapping(target = "uploadedTime" , source = "createdAt")
    VideoDto toDto(VideoEntity videoEntity);
}
