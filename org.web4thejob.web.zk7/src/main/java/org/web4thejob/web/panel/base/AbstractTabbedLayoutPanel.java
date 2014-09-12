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

package org.web4thejob.web.panel.base;

import org.apache.commons.lang.StringEscapeUtils;
import org.web4thejob.context.ContextUtil;
import org.web4thejob.message.Message;
import org.web4thejob.message.MessageArgEnum;
import org.web4thejob.message.MessageEnum;
import org.web4thejob.setting.SettingEnum;
import org.web4thejob.util.CoreUtil;
import org.web4thejob.web.panel.Attributes;
import org.web4thejob.web.panel.*;
import org.web4thejob.web.panel.Panel;
import org.web4thejob.web.panel.base.zk.AbstractZkLayoutPanel;
import org.web4thejob.web.util.ZkUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Veniamin Isaias
 * @since 1.0.0
 */

public abstract class AbstractTabbedLayoutPanel extends AbstractZkLayoutPanel implements EventListener<Event> {
    // ------------------------------ FIELDS ------------------------------

    protected AbstractTabbedLayoutPanel() {
        ZkUtil.setParentOfChild((Component) base, tabbox);
        tabbox.setWidth("100%");
        tabbox.setVflex("true");
        tabbox.addEventListener(Events.ON_SELECT, this);
        new Tabs().setParent(tabbox);
        new Tabpanels().setParent(tabbox);
        //tabbox.setMold("accordion-lite");
        //tabbox.setOrient("vertical");
    }

    private static final int DEFAULT_CHILDREN_COUNT = 10;
    private static final String ATTRIB_STARTUP = "startup";
    private final Tabbox tabbox = new Tabbox();
    private Tab startupTab;

    // --------------------------- CONSTRUCTORS ---------------------------
    private int indexToReplace = -1;

    @Override
    public void dispatchMessage(Message message) {
        if (MessageEnum.TITLE_CHANGED == message.getId()) {
            if (!getSettingValue(SettingEnum.DISABLE_DYNAMIC_TAB_TITLE, false)) {
                Panel panel = (Panel) message.getSender();
                Tabpanel tabpanel = findHostingTabpanel(panel);
                if (tabpanel != null) {
                    setTabTitle(tabpanel.getLinkedTab(), panel.toString());
                }
            }
        } else {
            super.dispatchMessage(message);
        }
    }

    // ------------------------ INTERFACE METHODS ------------------------

    // --------------------- Interface EventListener ---------------------

    public void onEvent(Event event) throws Exception {
        if (Events.ON_CLOSE.equals(event.getName())) {
            final Tab tab = (Tab) event.getTarget();
            final Panel panel = (Panel) tab.getLinkedPanel().getAttribute(Attributes.ATTRIB_PANEL);
            subpanels.remove(panel);
        } else if (Events.ON_SELECT.equals(event.getName())) {
            final Tab tab = (Tab) event.getTarget();
            final Panel panel = (Panel) tab.getLinkedPanel().getAttribute(Attributes.ATTRIB_PANEL);
            flushCache(panel);

            // Issue #6
            dispatchMessage(ContextUtil.getMessage(MessageEnum.ACTIVATED, this, MessageArgEnum.ARG_ITEM, panel));
        }
    }

    @Override
    protected void afterAdd(Panel panel) {
        super.afterAdd(panel);

        // Issue #6
        dispatchMessage(ContextUtil.getMessage(MessageEnum.ACTIVATED, this, MessageArgEnum.ARG_ITEM, panel));
    }

    // --------------------- Interface MessageListener ---------------------

    @Override
    public void processMessage(Message message) {
        if (MessageEnum.ADOPT_ME == message.getId() && getSettingValue(SettingEnum.HONOR_ADOPTION_REQUEST,
                false) && message.getSender() instanceof Panel) {
            Panel panel = ((Panel) message.getSender());
            boolean designMode = isInDesignMode();
            boolean localizationMode = getL10nMode();
            if (panel instanceof DesignModeAware) {
                designMode = ((DesignModeAware) panel).isInDesignMode();
            }
            if (panel instanceof I18nAware) {
                localizationMode = ((I18nAware) panel).getL10nMode();
            }
            panel.setParent(this);
            if (panel instanceof DesignModeAware) {
                ((DesignModeAware) panel).setInDesignMode(designMode);
            }
            if (panel instanceof I18nAware) {
                ((I18nAware) panel).setL10nMode(localizationMode);
            }
            panel.render();
        } else {
            super.processMessage(message);
        }
    }

    // --------------------- Interface Panel ---------------------

    @Override
    public void render() {
        super.render();

        arrangeForStartupTab();

        if (tabbox.getTabs().getChildren().size() == 1 && startupTab != null) {
            addNewTab();
        }

        for (Component item : tabbox.getTabs().getChildren()) {
            if (!item.equals(startupTab)) {
                ((Tab) item).setClosable(isClosable((Tab) item));
            }
        }
    }

    // --------------------- Interface ParentCapable ---------------------

    public boolean accepts(Panel panel) {
        return getSettingValue(SettingEnum.CHILDREN_COUNT, DEFAULT_CHILDREN_COUNT) > subpanels.size();
    }

    // -------------------------- OTHER METHODS --------------------------

    @Override
    protected void afterRemove(Panel panel) {
        super.afterRemove(panel);
        panel.removeAttribute(Attributes.ATTRIB_PANEL);
        final Tabpanel tabpanel = findHostingTabpanel(panel);
        final Tab tab = tabpanel.getLinkedTab();
        int index = tab.getIndex();
        boolean removedSelected = index == tabbox.getSelectedIndex();
        tab.detach();
        tabpanel.detach();
        if (removedSelected) {
            adjustSelectedTab(index);
        }
        arrangeForStartupTab();
    }

    protected Tabpanel findHostingTabpanel(Panel panel) {
        for (final Component tabpanel : tabbox.getTabpanels().getChildren()) {
            if (tabpanel.getAttribute(Attributes.ATTRIB_PANEL) instanceof Panel) {
                if (tabpanel.getAttribute(Attributes.ATTRIB_PANEL).equals(panel)) {
                    return (Tabpanel) tabpanel;
                }
            }
        }
        return null;
    }

    private void adjustSelectedTab(int index) {
        if (tabbox.getTabs().getChildren().size() > 0) {
            Tab candidate = null;
            if (index == tabbox.getTabs().getChildren().size()) {
                candidate = (Tab) tabbox.getTabs().getChildren().get(index - 1);
            } else if (index < tabbox.getTabs().getChildren().size()) {
                candidate = (Tab) tabbox.getTabs().getChildren().get(index);
            }

            if (candidate != null) {
                if (candidate.equals(startupTab)) {
                    candidate = (Tab) startupTab.getPreviousSibling();
                }
                if (candidate != null) {
                    candidate.setSelected(true);
                    // Issue #6
                    dispatchMessage(ContextUtil.getMessage(MessageEnum.ACTIVATED, this, MessageArgEnum.ARG_ITEM,
                            candidate.getLinkedPanel().getAttribute(Attributes.ATTRIB_PANEL)));
                }
            }
        }
    }

    private void addNewTab() {
        PlaceholderPanel placeholderPanel = ContextUtil.getDefaultPanel(PlaceholderPanel.class);
        subpanels.add(placeholderPanel);
        placeholderPanel.render();
    }

    private void arrangeForStartupTab() {
        boolean showStartupTab = getSettingValue(SettingEnum.SHOW_STARTUP_TAB, false);
        if (showStartupTab && startupTab == null) {
            startupTab = new Tab("+");
            startupTab.setParent(tabbox.getTabs());
            startupTab.setClosable(false);
            Tabpanel tabpanel = new Tabpanel();
            tabpanel.setParent(tabbox.getTabpanels());
            tabpanel.setAttribute(ATTRIB_STARTUP, true);
            startupTab.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Tab, Tab>>() {
                public void onEvent(SelectEvent<Tab, Tab> event) throws Exception {
                    addNewTab();
                }
            });
        } else if (!showStartupTab && startupTab != null) {
            startupTab.getLinkedPanel().detach();
            startupTab.detach();
            startupTab = null;
        }
    }

    @Override
    protected void afterReplace(Panel oldItem, Panel newItem) {
        indexToReplace = -1;
    }

    @Override
    protected void beforeReplace(Panel oldItem, Panel newItem) {
        indexToReplace = findHostingTabpanel(oldItem).getIndex();
    }

    private void setTabTitle(Tab tab, String title) {
        tab.setLabel(title);
        tab.setStyle("max-width: 300px;");

        if (title.length() > 30 || !StringEscapeUtils.unescapeHtml(title).equals(title)) {
            Popup popup = (Popup) tab.getAttribute("tooltip");
            if (popup == null) {
                popup = new Popup();
                popup.setPage(Executions.getCurrent().getDesktop().getFirstPage());
                tab.setAttribute("tooltip", popup);
                tab.setTooltip(popup);
                Html html = new Html();
                html.setParent(popup);
                html.setVflex("true");
                html.setHflex("true");
            }

            ((Html) popup.getFirstChild()).setContent(title);
        } else {
            tab.setTooltip((Popup) null);
            Popup popup = (Popup) tab.getAttribute("tooltip");
            if (popup != null) {
                popup.detach();
                tab.removeAttribute("tooltip");
            }
        }
    }

    @Override
    protected void beforeAdd(Panel panel) {
        super.beforeAdd(panel);

        final Tab tab = new Tab();
        setTabTitle(tab, panel.toString());
        tab.setImage(panel.getImage());
        final Tabpanel tabpanel = new Tabpanel();

        Tabpanel startupTabpanel;
        if (indexToReplace >= 0 && indexToReplace <= tabbox.getTabs().getChildren().size() - 1) {
            final Tab tabRef = (Tab) tabbox.getTabs().getChildren().get(indexToReplace);
            final Tabpanel panRef = (Tabpanel) tabbox.getTabpanels().getChildren().get(indexToReplace);
            tabbox.getTabpanels().insertBefore(tabpanel, panRef);
            tabbox.getTabs().insertBefore(tab, tabRef);
        } else if (startupTab == null) {
            tab.setParent(tabbox.getTabs());
            tabpanel.setParent(tabbox.getTabpanels());
        } else {
            startupTabpanel = startupTab.getLinkedPanel();
            tabbox.getTabs().insertBefore(tab, startupTab);
            tabbox.getTabpanels().insertBefore(tabpanel, startupTabpanel);
        }
        tab.addEventListener(Events.ON_CLOSE, this);
        tab.setSelected(true);
        tab.setClosable(isClosable(tab));

        panel.attach(tabpanel);
        tabpanel.setAttribute(Attributes.ATTRIB_PANEL, panel);
    }

    private boolean isClosable(Tab tab) {
        int fixedTabs = getSettingValue(SettingEnum.FIXED_TABS, 0);
        if (fixedTabs > 0 && tab.getIndex() + 1 <= fixedTabs) {
            return false;
        }
        return getSettingValue(SettingEnum.CLOSEABLE_TABS, false);
    }

    @Override
    protected void beforePersistencePhase() {
        setSettingValue(SettingEnum.SELECTED_INDEX, tabbox.getSelectedIndex());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        arrangeForStartupTab();
        arrangeForSelectedIndex();
    }

    private void arrangeForSelectedIndex() {
        int index = getSettingValue(SettingEnum.SELECTED_INDEX, -1);
        if (index >= 0 && index < tabbox.getTabs().getChildren().size()) {
            tabbox.setSelectedIndex(index);
        }
    }

    protected int getSelectedIndex() {
        return tabbox.getSelectedIndex();
    }

    protected void setSelectedIndex(int index) {
        tabbox.setSelectedIndex(index);
    }

    @Override
    protected Collection<Panel> getRenderedOrderOfChildren() {
        final List<Panel> subpanels = new ArrayList<Panel>();
        for (final Component tabpanel : tabbox.getTabpanels().getChildren()) {
            if (tabpanel.getAttribute(Attributes.ATTRIB_PANEL) instanceof Panel) {
                subpanels.add((Panel) tabpanel.getAttribute(Attributes.ATTRIB_PANEL));
            }
        }
        return subpanels;
    }

    @Override
    protected <T extends Serializable> void onSettingValueChanged(SettingEnum id, T oldValue, T newValue) {
        if (id.equals(SettingEnum.SELECTED_INDEX)) {
            arrangeForSelectedIndex();
        } else if (id.equals(SettingEnum.MOLD)) {
            tabbox.setMold(getSettingValue(SettingEnum.MOLD, (String) null));
        } else {
            super.onSettingValueChanged(id, oldValue, newValue);
        }
    }

    @Override
    protected void registerSettings() {
        super.registerSettings();
        registerSetting(SettingEnum.CHILDREN_COUNT, DEFAULT_CHILDREN_COUNT);
        registerSetting(SettingEnum.SELECTED_INDEX, -1);
        registerSetting(SettingEnum.SHOW_STARTUP_TAB, false);
        registerSetting(SettingEnum.CLOSEABLE_TABS, true);
        registerSetting(SettingEnum.FIXED_TABS, 0);
        registerSetting(SettingEnum.HONOR_ADOPTION_REQUEST, false);
        registerSetting(SettingEnum.DISABLE_DYNAMIC_TAB_TITLE, false);
        registerSetting(SettingEnum.MOLD, null);
        registerSetting(SettingEnum.DISABLE_CROSS_TAB_BINDING, false);
    }

    @Override
    protected boolean cancelDispatchForSubpanel(Panel panel, Message message) {
        if (!getSettingValue(SettingEnum.DISABLE_CROSS_TAB_BINDING, false)) {
            return false;
        } else if (!CoreUtil.isSelectionMessage(message)) {
            return false;
        } else if (panel.equals(message.getSender())) {
            return false;
        } else if (panel instanceof ParentCapable && message.getSender() instanceof Panel) {
            return !isContained((ParentCapable) panel, (Panel) message.getSender());
        } else {
            return true;
        }
    }

}
