package com.evolunteer.service;

import java.util.Map;

public interface UserAccountService {

    Map<Object, Object> changePassword(String accountId, String oldPassword, String newPassword);

    Map<Object, Object> resetPassword(String accountId);

    boolean exists(String accountId);
}
