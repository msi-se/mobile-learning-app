package de.htwg_konstanz.mobilelearning.models.external.menu;

import java.util.Date;

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

    public Menu getSpeiseplan() {
        return menu;
    }
}
