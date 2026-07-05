package com.jala.backend.user.mapper;

import com.jala.backend.user.dto.request.CreateUserRequest;
import com.jala.backend.user.dto.response.UserResponse;
import com.jala.backend.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    User toEntity(CreateUserRequest request);

    @Mapping(source = "role.name", target = "role")
    UserResponse toResponse(User user);

}