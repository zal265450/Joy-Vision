package org.luckyjourney.authority;

import org.luckyjourney.util.JwtUtils;

import javax.servlet.http.HttpServletRequest;

public class BaseAuthority implements AuthorityVerify {
    @Override
    public Boolean authorityVerify(HttpServletRequest request, String[] permissions) {
        if (!JwtUtils.checkToken(request)) {
            return false;
        }
        // 获取当前用户权限
        String uId = JwtUtils.getMemberIdByJwtToken(request);
        for (String permission : permissions) {
            if (!AuthorityUtils.verify(uId,permission)) {
                return false;
            }
        }

        return true;
    }
}
