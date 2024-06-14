package org.apache.cybershuttle.holder;

import org.apache.airavata.model.security.AuthzToken;

public class UserContext {

    private static final ThreadLocal<AuthzToken> AUTHZ_TOKEN = new ThreadLocal<>();

    public static AuthzToken authzToken() {
        return AUTHZ_TOKEN.get();
    }

    public static void setAuthzToken(AuthzToken token) {
        AUTHZ_TOKEN.set(token);
    }
}
