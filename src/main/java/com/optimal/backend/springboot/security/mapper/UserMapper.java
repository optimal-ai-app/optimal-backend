package com.optimal.backend.springboot.security.mapper;

import org.mapstruct.factory.Mappers;

import com.optimal.backend.springboot.database.entity.User;
import com.optimal.backend.springboot.security.dto.AuthenticatedUserDto;
import com.optimal.backend.springboot.security.dto.RegistrationRequest;

/**
 * Created on Ağustos, 2020
 *
 * @author Faruk
 */
public interface UserMapper {

	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	User convertToUser(RegistrationRequest registrationRequest);

	AuthenticatedUserDto convertToAuthenticatedUserDto(User user);

	User convertToUser(AuthenticatedUserDto authenticatedUserDto);

}
