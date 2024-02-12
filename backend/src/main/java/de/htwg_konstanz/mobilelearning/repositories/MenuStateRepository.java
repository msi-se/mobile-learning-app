package de.htwg_konstanz.mobilelearning.repositories;




import de.htwg_konstanz.mobilelearning.models.external.menu.MenuState;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MenuStateRepository implements PanacheMongoRepository<MenuState> {

    public MenuState getLatestMenuState() {
        return findAll().firstResult();
    }
}
