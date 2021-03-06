/*
 * Copyright (c) 2012-2014 Veniamin Isaias.
 *
 * This file is part of web4thejob.
 *
 * Web4thejob is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Web4thejob is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.web4thejob.security;

/**
 * <p>Service for providing application wide security functions.</p>
 *
 * @author Veniamin Isaias
 * @since 1.0.0
 */

public interface SecurityService {
    // -------------------------- OTHER METHODS --------------------------
    public static final String BEAN_AUTHENTICATION_MANAGER = "authenticationManager";
    public static final String EXPIRED_USER_NAME = "web4thejob-EXPIRED_USER_NAME";

    public String encodePassword(UserIdentity userIdentity, String value);

    public UserIdentity getAdministratorIdentity();

    public UserIdentity getUserIdentity(String userName);

    public boolean isPasswordValid(UserIdentity userIdentity, String rawPassword);

    boolean renewPassword(UserIdentity userIdentity, String oldPassword, String newPassword);

    public <T> T authenticate(String username, String password);

    public <T> T authenticate(String username, String password, boolean useIfValid);
}
