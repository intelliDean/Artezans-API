package com.api.artezans.category.data.dtos;

import com.api.artezans.category.data.model.ArtezanService;
import com.api.artezans.category.data.model.Category;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Named("toDTO")
    @Mapping(target = "services", source = "artezanServices")
    CategoryDTO toDTO(Category category);

    @IterableMapping(qualifiedByName = "toDTO")
    List<CategoryDTO> toDTOList(List<Category> categories);

    default List<String> mapServices(List<ArtezanService> services) {
        if (services == null) return null;
        return services.stream().map(ArtezanService::getServiceName).toList();
    }
}