package cz.cuni.mff.ufal.textan.gui.reportwizard;

import java.io.IOException;
import java.util.Properties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import jfxtras.labs.scene.control.window.Window;
import org.controlsfx.dialog.Dialogs;

/**
 * Wizard for handling reports.
 */
public class ReportWizard extends Window {

    /** Initial title of the wizard. */
    static protected final String TITLE = "Report Wizard";

    /**
     * Settings of the application.
     * Handle with care, they're shared!
     */
    protected Properties settings;

    /** Flag whether the window maximized. */
    protected BooleanProperty maximized = new SimpleBooleanProperty();

    /** Stored width if window is maximized to restore it if needed. */
    protected double unmaximizedWidth;

    /** Stored height if window is maximized to restore it if needed. */
    protected double unmaximizedHeight;

    /** Stored x-coord if window is maximized to restore it if needed. */
    protected double unmaximizedX;

    /** Stored y-coord if window is maximized to restore it if needed. */
    protected double unmaximizedY;

    /**
     * Change listener for parent size changes.
     */
    protected ChangeListener<Number> sizeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            final Parent parent = getParent();
            if (parent instanceof Region || !maximized.get()) {
                adjustX(getLayoutX());
                adjustY(getLayoutY());
                adjustHeight(getPrefHeight());
                adjustWidth(getPrefWidth());
            }
        }
    };

    /**
     * Only constructor.
     * @param settings properties with settings
     */
    public ReportWizard(final Properties settings) {
        super(TITLE);
        this.settings = settings;
        //
        parentProperty().addListener(
            (ObservableValue<? extends Parent> ov, Parent oldVal, Parent newVal) -> {
                if (oldVal instanceof Region) {
                    final Region region = (Region) oldVal;
                    region.widthProperty().removeListener(sizeListener);
                    region.heightProperty().removeListener(sizeListener);
                }
                if (newVal instanceof Region) {
                    final Region region = (Region) newVal;
                    region.widthProperty().addListener(sizeListener);
                    region.heightProperty().addListener(sizeListener);
                    if (maximized.get()) {
                        adjustMaximized();
                    }
                }
            }
        );
        maximized.addListener(
            (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                adjustMaximized();
            }
        );
        //TODO init from settings
        maximized.set(settings.getProperty("report.wizard.maximized", "false").equals("true"));
        setPrefWidth(Double.parseDouble(settings.getProperty("report.wizard.width", "300")));
        setPrefHeight(Double.parseDouble(settings.getProperty("report.wizard.height", "200")));
        setLayoutX(Double.parseDouble(settings.getProperty("report.wizard.x", "0")));
        setLayoutY(Double.parseDouble(settings.getProperty("report.wizard.y", "0")));
        //
        getRightIcons().add(new MaximizeIcon(this));
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> this.toFront());
        layoutXProperty().addListener(
            (ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
                adjustX(newVal.doubleValue());
            }
        );
        layoutYProperty().addListener(
            (ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
                adjustY(newVal.doubleValue());
            }
        );
        prefWidthProperty().addListener(
            (ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
                adjustWidth(newVal.doubleValue());
            }
        );
        prefHeightProperty().addListener(
            (ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
                adjustHeight(newVal.doubleValue());
            }
        );
        //
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("01_ReportLoad.fxml"));
            final Parent root = (Parent) loader.load();
            final ReportLoadController controller = loader.getController();
            controller.setSettings(settings);
            controller.setWindow(this);
            getContentPane().getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
            Dialogs.create()
                    .title("Problém při načítání wizardu!")
                    .lightweight()
                    .showException(e);
        }
    }

    /**
     * Adjusts height to newVal if possible.
     * @param newVal new height
     */
    protected void adjustHeight(final double newVal) {
        if (!maximized.get()) {
            settings.setProperty("report.wizard.height", Double.toString(newVal));
        }
        final Parent p = getParent();
        if (p != null ) {
            final Bounds b = p.getLayoutBounds();
            if (b.getHeight() < getLayoutY() + newVal) {
                setPrefHeight(b.getHeight() - getLayoutY());
            }
        }
    }

    /**
     * (Un)bind size and position according to {@link #maximilized}.
     */
    protected void adjustMaximized() {
        if (maximized.get()) {
            unmaximizedHeight = getPrefHeight();
            unmaximizedWidth = getPrefWidth();
            unmaximizedX = layoutXProperty().get();
            unmaximizedY = layoutYProperty().get();
            layoutXProperty().set(0);
            layoutYProperty().set(0);
            final Parent parent = getParent();
            setResizableWindow(false);
            if (parent instanceof Region) {
                final Region region = (Region) parent;
                prefHeightProperty().bind(region.heightProperty());
                prefWidthProperty().bind(region.widthProperty());
            } else {
                //TODO binding to non-region parent's size
            }
        } else {
            prefHeightProperty().unbind();
            prefWidthProperty().unbind();
            setResizableWindow(true);
            setPrefHeight(unmaximizedHeight);
            setPrefWidth(unmaximizedWidth);
            setLayoutX(unmaximizedX);
            setLayoutY(unmaximizedY);
        }
    }

    /**
     * Adjusts width to newVal if possible.
     * @param newVal new width
     */
    protected void adjustWidth(final double newVal) {
        if (!maximized.get()) {
            settings.setProperty("report.wizard.width", Double.toString(newVal));
        }
        final Parent p = getParent();
        if (p != null ) {
            final Bounds b = p.getLayoutBounds();
            if (b.getWidth()< getLayoutX() + newVal) {
                setPrefWidth(b.getWidth() - getLayoutX());
            }
        }
    }

    /**
     * Adjusts x-coord to newVal if possible.
     * @param newVal new x-coord
     */
    protected void adjustX(final double newVal) {
        if (!maximized.get()) {
            settings.setProperty("report.wizard.x", Double.toString(newVal));
        }
        if (newVal < 0) {
            layoutXProperty().set(0);
        } else {
            final Parent p = getParent();
            if (p != null ) {
                final Bounds b = p.getLayoutBounds();
                if (b.getWidth() < newVal + getPrefWidth() && newVal != 0) {
                    layoutXProperty().set(b.getWidth() - getPrefWidth());
                }
            }
        }
    }

    /**
     * Adjusts y-coord to newVal if possible.
     * @param newVal new y-coord
     */
    protected void adjustY(final double newVal) {
        if (!maximized.get()) {
            settings.setProperty("report.wizard.y", Double.toString(newVal));
        }
        if (newVal < 0) {
            layoutYProperty().set(0);
        } else {
            final Parent p = getParent();
            if (p != null ) {
                final Bounds b = p.getLayoutBounds();
                if (b.getHeight() < newVal + getPrefHeight() && newVal != 0) {
                    layoutYProperty().set(b.getHeight()- getPrefHeight());
                }
            }
        }
    }

    @Override
    public void close() {
        super.close();
        //TODO save logic here
    }

    /**
     * Toggles the {@link #maximized} property.
     */
    public void toggleMaximize() {
        maximized.set(!maximized.get());
        settings.setProperty("report.wizard.maximized", maximized.get() ? "true" : "false");
    }
}
