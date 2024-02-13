package de.htwg_konstanz.mobilelearning.services.external;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import de.htwg_konstanz.mobilelearning.models.external.menu.MenuState;
import de.htwg_konstanz.mobilelearning.models.external.menu.Menu;
import de.htwg_konstanz.mobilelearning.repositories.MenuStateRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/external/menu")
public class MenuService {

    @Inject
    private MenuStateRepository menuStateRepository;

    public MenuService() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public MenuState getMenu() {

        // get latest menu state from database
        MenuState menuState = menuStateRepository.getLatestMenuState();

        // if it is older than 10 minutes, update it
        if (menuState == null || new Date().getTime() - menuState.timestamp.getTime() > 10 * 60 * 1000) {
            menuState = updateMenu();
        }

        return menuState;

    }

    private MenuState updateMenu() {
        try {

            // fetch menu from mensa xml service
            String xmlString = "";
            InputStream xmlStream = URI.create("https://www.max-manager.de/daten-extern/seezeit/xml/mensa_htwg/speiseplan.xml").toURL().openStream();
            xmlString = new String(xmlStream.readAllBytes(), StandardCharsets.UTF_8);
            xmlStream.close();

            // parse xml
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            Menu menueFromXML = xmlMapper.readValue(xmlString, Menu.class);

            // save menu to database
            MenuState menuState = new MenuState(new Date(), menueFromXML);
            menuStateRepository.persist(menuState);

            return menuState;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}