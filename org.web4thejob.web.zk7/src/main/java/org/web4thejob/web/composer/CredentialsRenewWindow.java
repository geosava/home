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

package org.web4thejob.web.composer;

import org.web4thejob.context.ContextUtil;
import org.web4thejob.message.Message;
import org.web4thejob.message.MessageArgEnum;
import org.web4thejob.message.MessageEnum;
import org.web4thejob.message.MessageListener;
import org.web4thejob.security.SecurityContext;
import org.web4thejob.security.SecurityService;
import org.web4thejob.security.UserIdentity;
import org.web4thejob.web.dialog.PasswordDialog;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Window;

/**
 * @author Veniamin Isaias
 * @since 1.0.0
 */

public class CredentialsRenewWindow implements Composer<Window>, MessageListener {
    private UserIdentity userIdentity;

    public void doAfterCompose(Window comp) throws Exception {

        if (ContextUtil.getSessionContext().getSecurityContext().getUserIdentity() == null) {
            if (Executions.getCurrent().getSession().getAttribute(SecurityService.EXPIRED_USER_NAME) == null) {
                Executions.sendRedirect("/");
                return;
            } else {
                userIdentity = ContextUtil.getSecurityService().getUserIdentity(Executions.getCurrent().getSession()
                        .getAttribute(SecurityService.EXPIRED_USER_NAME).toString());
                Executions.getCurrent().getSession()
                        .setAttribute(SecurityService.EXPIRED_USER_NAME, null);
            }
        } else {
            userIdentity = ContextUtil.getSessionContext().getSecurityContext().getUserIdentity();
        }


        Window hostWindow = comp;
        hostWindow.setBorder(false);

        PasswordDialog passwordDialog = ContextUtil.getDefaultDialog(PasswordDialog.class,
                userIdentity, true);
        passwordDialog.show(this);
    }

    public void processMessage(Message message) {
        SecurityContext securityContext = ContextUtil.getSessionContext().getSecurityContext();
        if (MessageEnum.AFFIRMATIVE_RESPONSE == message.getId()) {
            String newPassword = message.getArg(MessageArgEnum.ARG_ITEM, String.class);
            String oldPassword = message.getArg(MessageArgEnum.ARG_OLD_ITEM, String.class);
            if (!ContextUtil.getSecurityService().renewPassword(userIdentity, oldPassword, newPassword)) {
                //this is not good at all...
                ContextUtil.getSessionContext().getSecurityContext().clearContext();
            }
        }

        Executions.sendRedirect("/");
    }

}
