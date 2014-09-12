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

package org.web4thejob.web.panel.base.zk;

import org.web4thejob.command.Command;
import org.web4thejob.command.CommandEnum;
import org.web4thejob.context.ContextUtil;
import org.web4thejob.web.dialog.DefaultSettingsDialog;
import org.web4thejob.web.dialog.Dialog;
import org.web4thejob.web.dialog.SettingsDialogListener;
import org.web4thejob.web.panel.base.AbstractMasterDetailTypeAwarePanel;
import org.web4thejob.web.util.ZkUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;

/**
 * @author Veniamin Isaias
 * @since 1.0.0
 */

public abstract class AbstractZkMasterDetailTypeAwarePanel extends AbstractMasterDetailTypeAwarePanel {

    public void attach(Object container) {
        if (getParent() != null) throw new IllegalStateException("Cannot attach a child controller.");

        ((Component) base).setParent((Component) container);
    }

    @Override
    public void clearBusy() {
        super.clearBusy();
        if (base instanceof HtmlBasedComponent) {
            ((HtmlBasedComponent) base).setStyle("cursor:auto;");
        }
    }

    @Override
    protected void processValidCommand(Command command) {
        final Dialog dialog;
        if (CommandEnum.CONFIGURE_SETTINGS.equals(command.getId())) {
            dialog = ContextUtil.getDialog(DefaultSettingsDialog.class, this);
            dialog.show(new SettingsDialogListener(this));
        } else if (CommandEnum.HIGHLIGHT_PANEL.equals(command.getId())) {
            hightlightPanel((Boolean) command.getValue());
        } else {
            super.processValidCommand(command);
        }

    }

    public void detach() {
        if (getParent() != null) throw new IllegalStateException("Cannot detach a child controller.");

        ((Component) base).detach();
    }

    public Object getAttribute(String name) {
        return ((Component) base).getAttribute(name);
    }

    public boolean hasAttribute(String name) {
        return ((Component) base).hasAttribute(name);
    }

    @Override
    protected Object initBaseComponent() {
        return ZkUtil.initBaseComponent(this);
    }

    public boolean isAttached() {
        return ((Component) base).getParent() != null;
    }

    public Object removeAttribute(String name) {
        return ((Component) base).removeAttribute(name);
    }

    public <T> void setAttribute(String name, T value) {
        ((Component) base).setAttribute(name, value);
    }

    @Override
    public void showBusy() {
        super.showBusy();
        if (base instanceof HtmlBasedComponent) {
            ((HtmlBasedComponent) base).setStyle("cursor:wait;");
        }
    }

    @Override
    protected void displayMessage(String message, boolean error) {
        ZkUtil.displayMessage(message, error, (Component) base);
    }

    public void hightlightPanel(boolean highlight) {
        ZkUtil.hightlightComponent((HtmlBasedComponent) base, highlight, isPersisted() ? "green" : "rgb(225,79,35)");
        if (hasCommand(CommandEnum.HIGHLIGHT_PANEL)) {
            getCommand(CommandEnum.HIGHLIGHT_PANEL).setValue(highlight);
        }

    }

}
