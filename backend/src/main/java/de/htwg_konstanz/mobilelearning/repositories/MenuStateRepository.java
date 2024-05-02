package de.htwg_konstanz.mobilelearning.repositories;

import de.htwg_konstanz.mobilelearning.models.external.menu.MenuState;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MenuStateRepository implements PanacheMongoRepository<MenuState> {

    public MenuState getLatestMenuState() {
        // sort on timestamp descending and return first
        return this.listAll(Sort.by("timestamp", Sort.Direction.Descending)).stream().findFirst().orElse(null);
    }
}
