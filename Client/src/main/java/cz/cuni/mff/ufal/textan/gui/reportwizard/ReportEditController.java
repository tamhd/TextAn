package cz.cuni.mff.ufal.textan.gui.reportwizard;

import cz.cuni.mff.ufal.textan.gui.WindowController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

/**
 * Controls editing the report.
 */
public class ReportEditController extends WindowController {

    static final String TEST_TEXT = "Ahoj, toto je testovaci zprava urcena pro vyzkouseni vsech moznosti oznacovani textu.";

    @FXML
    TextArea textArea;

    @FXML
    ScrollPane scrollPane;

    @FXML
    private void cancel() {
        closeContainer();
    }

    @FXML
    private void next() {
        final ReportEntitiesController controller = nextFrame("03_ReportEntities.fxml");
        controller.setReport(textArea.getText());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textArea.setText(TEST_TEXT);
    }
}
