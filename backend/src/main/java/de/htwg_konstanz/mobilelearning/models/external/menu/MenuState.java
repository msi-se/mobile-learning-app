package de.htwg_konstanz.mobilelearning.models.external.menu;

import java.util.Date;

/**
 * Structure of a menu state.
 * Current state of the mensa menu provided by Seezeit.
 */
public class MenuState {
    
    public Date timestamp;
    public Menu menu;

    public MenuState(Date timestamp, Menu menu) {
        this.timestamp = timestamp;
        this.menu = menu;
    }

    public MenuState() {
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Menu getMenu() {
        return menu;
    }
}
