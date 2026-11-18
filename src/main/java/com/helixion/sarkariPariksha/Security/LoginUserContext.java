package com.helixion.sarkariPariksha.Security;

public class LoginUserContext {

    private static final ThreadLocal<LoginUser> CURRENT_USER = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        CURRENT_USER.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return CURRENT_USER.get();
    }

    public static Long getUserId() {
        LoginUser user = CURRENT_USER.get();
        return user != null ? user.getUserId() : null;
    }

    public static Long getCandidateId() {
        LoginUser user = CURRENT_USER.get();
        return user != null ? user.getCandidateId() : null;
    }

    public static String getLogId() {
        LoginUser user = CURRENT_USER.get();
        return user != null ? user.getLogId() : null;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
