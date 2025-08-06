package com.optimal.backend.springboot.security.service;

import com.optimal.backend.springboot.database.entity.User;
import com.optimal.backend.springboot.security.dto.AuthenticatedUserDto;
import com.optimal.backend.springboot.security.dto.RegistrationRequest;
import com.optimal.backend.springboot.security.dto.RegistrationResponse;

/**
 * Created on Ağustos, 2020
 *
 * @author Faruk
 */
public interface UserService {

	User findByUsername(String username);

	RegistrationResponse registration(RegistrationRequest registrationRequest);

	AuthenticatedUserDto findAuthenticatedUserByUsername(String username);

}
