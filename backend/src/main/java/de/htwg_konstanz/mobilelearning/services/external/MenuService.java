package de.htwg_konstanz.mobilelearning.services.external;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import de.htwg_konstanz.mobilelearning.helper.Speiseplan;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/external/menu")
public class MenuService {

    public MenuService() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getMenu() {

        this.updateMenu();

        return "";

    }

    private void updateMenu() {
        try {

            // fetch menu from mensa xml service
            String xmlString = "";
            InputStream xmlStream = URI.create("https://www.max-manager.de/daten-extern/seezeit/xml/mensa_htwg/speiseplan.xml").toURL().openStream();
            xmlString = new String(xmlStream.readAllBytes(), StandardCharsets.UTF_8);
            xmlStream.close();

            // parse xml
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            Speiseplan menueFromXML = xmlMapper.readValue(xmlString, Speiseplan.class);

            System.out.println(menueFromXML.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}