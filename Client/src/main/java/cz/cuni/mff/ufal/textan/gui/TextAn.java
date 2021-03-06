package cz.cuni.mff.ufal.textan.gui;

import cz.cuni.mff.ufal.textan.commons.Document;
import cz.cuni.mff.ufal.textan.commons.ISimpleWebService;
import cz.cuni.mff.ufal.textan.utils.UnclosableStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import org.controlsfx.dialog.Dialogs;

/**
 * TextAn application.
 */
public class TextAn extends Application {

    /** Path to default property file. The file should be inside the jar. */
    private static final String DEFAULT_JAR_PROPERTIES = "TextAn.defprops";

    /** Default path to property file. */
    private static final String DEFAULT_PROPERTIES = "TextAn.properties";

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /** Settings of the application. */
    Properties settings;

    /** File containing properties. Used to save properties. */
    String propertyFile;

    /** Application controller. */
    TextAnController controller;

    @Override
    public void start(final Stage stage) throws Exception {

        Service service = Service.create(new URL("http://localhost:9100/soap/simple?wsdl"), new QName("http://server.textan.ufal.mff.cuni.cz/", "SimpleWebService"));
        // Endpoint Address
        String endpointAddress = "http://localhost:9100/soap/simple";

        // Add a port to the Service
        service.addPort(new QName("http://server.textan.ufal.mff.cuni.cz/SimpleWebService", "SimpleWebServicePort"), SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);

        ISimpleWebService hw = service.getPort(ISimpleWebService.class);
        System.out.println(hw.hello("World"));

        Document doc = hw.toDocument("Testing document");
        System.out.println(doc.getText());


        System.out.printf("Starting...\n");
        //load default properties
        settings = new Properties(loadDefaultJarProperties());

        //parse arguments from command line, ie. load property file
        final List<String> args = getParameters().getRaw();
        if (helpNeeded(args)) {
            //TODO
            System.out.printf("USAGE: java -jar TextAn.jar [property_file]\n");
            System.out.printf("If property_file argument is not provided, default property file \"%s\" will be used\n", DEFAULT_PROPERTIES);
            System.out.printf("If property_file argument is '-', standard input will be read\n");
            System.exit(0);
        }
        if (args.size() == 1) {
            System.out.printf("Loading property file\n");
            propertyFile = args.get(0);
            try (final InputStream is =
                    propertyFile.equals("-") ? new UnclosableStream(System.in)
                        : new FileInputStream(propertyFile)) {
            	 settings.load(is);
            } catch(Exception e) {
                System.err.printf("Error with specified property file\n");
                e.printStackTrace();
            }
        } else /*if (args.length == 0)*/ { //no property file provided
            System.out.printf("No property file specified, using default path.\n");
            propertyFile = DEFAULT_PROPERTIES;
            try (final InputStream is = new FileInputStream(DEFAULT_PROPERTIES)) {
                settings.load(is);
            } catch(Exception e) {
                System.err.printf(String.format("Error with default property file \"%s\"\n", DEFAULT_PROPERTIES));
                e.printStackTrace();
            }
        }

        //ask for login if needed
        if (settings.getProperty("username", "").equals("")) {
            final String login = Dialogs.create()
                    .title(TextAnController.TITLE)
                    .masthead("Zadejte uživatelské jméno")
                    .message("Login:")
                    .showTextInput(System.getProperty("user.name", ""));
            if (login == null) {
                Dialogs.create()
                        .title(TextAnController.TITLE)
                        .masthead("Neplatné přihlášení!")
                        .message("Pro pokračování je nutné zadat přihlašovací jméno.")
                        .showError();
                Platform.exit();
            } else {
                settings.setProperty("username", login);
            }
        }

        //create javafx controls
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("TextAn.fxml"));
        final Parent root = (Parent) loader.load();
        controller = loader.getController();
        controller.setSettings(settings);
        final Scene scene = new Scene(root);
        scene.getStylesheets().addAll(TextAn.class.getResource("/org/controlsfx/dialog/dialogs.css").toExternalForm()); //without this the first dialog in too small window could mess up its content
        stage.setScene(scene);
        stage.titleProperty().bind(controller.titleProperty());
        stage.setOnCloseRequest(e -> stop());
        stage.show();
        stage.toFront();
    }

    /**
     * Checks whether arguments suggest printing usage.
     * @param args parameters from command line
     * @return true if help needed, false otherwise
     */
    private boolean helpNeeded(final List<String> args) {
        if (args.size() > 1) {
            return true;
        }
        if (args.size() == 1) {
            final String param = args.get(0);
            return param.equals("--help") || param.equals("-h") || param.equals("/?");
        }
        return false;
    }

    /**
     * Loads and returns default properties from {@link #DEFAULT_JAR_PROPERTIES}.
     * @return default properties
     */
    private Properties loadDefaultJarProperties() {
        System.out.printf("Loading default properties.\n");
        final Properties res = new Properties();
        try {
            res.load(getClass().getClassLoader().getResourceAsStream(DEFAULT_JAR_PROPERTIES));
        } catch(Exception e) {
            //someone messed with our properties!
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void stop() {
        System.out.println("Closing...\n");
        controller.stop();
        if (!propertyFile.equals("-")) {
            try (final OutputStream os = new FileOutputStream(propertyFile)) {
                settings.store(os, null);
            } catch(IOException e) {
                System.err.printf("Error while storing settings to \"%1$s\"", propertyFile);
                e.printStackTrace();
            }
        }
    }
}
