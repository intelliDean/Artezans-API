package com.api.artezans.users.dto;



import com.api.artezans.users.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Named;
import org.mapstruct.IterableMapping;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)// makes it a Spring bean
public interface UserMapper {

    // Simple mapping — same field names map automatically
    @Named("toDTO")
    UserDTO toDTO(User user); // Address inside User maps automatically via AddressMapper

//    void updateUser(UserDTO request, @MappingTarget User user);

    // Handles different field names with @Mapping
//    @Mapping(source = "emailAddress", target = "email")
//    UserDTO toDTOWithEmail(User user);
//
    // Reverse mapping
//    @Mapping(source = "email", target = "emailAddress")
//    User toEntity(UserDTO dto);

    // Map a list
    @IterableMapping(qualifiedByName = "toDTO")
    List<UserDTO> toDTOList(List<User> users);

//    @Mapping(source = "emailAddress", target = "email")
//    @Mapping(target = "password", ignore = true)
        // never expose password in DTO
//    UserDTO toDTOIgnorePassword(User user);

//    @Mapping(source = "emailAddress", target = "email")
//    @Mapping(target = "role", constant = "USER")
//    UserDTO toDTORole(User user);

//    @Mapping(source = "emailAddress", target = "email")
//    @Mapping(target = "fullName",
//            expression = "java(user.getFirstName() + ' ' + user.getLastName())")
//    UserDTO toDTOWithExpression(User user);

}
