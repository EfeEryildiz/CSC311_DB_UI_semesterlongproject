package viewmodel;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.event.ActionEvent;
import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Logger;

public class AboutController {
    private static final Logger logger = Logger.getLogger(AboutController.class.getName());

    @FXML
    private void openSupportLink(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.farmingdale.edu/information-technology/helpdesk.shtml"));
        } catch (Exception e) {
            logger.warning("Failed to open support link: " + e.getMessage());
        }
    }
}