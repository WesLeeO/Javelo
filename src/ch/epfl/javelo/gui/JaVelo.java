package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;


/**
 * JaVelo class
 * Main Class of the project, starts the whole program.
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public final class JaVelo extends Application {

    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //------Create JaVelo objects--------
        Graph graph = Graph.loadFrom(Path.of("javelo-data"));
        TileManager tileManager = new TileManager(Path.of("./osm-cache"), "tile.openstreetmap.org");
        CostFunction cityBikeCF = new CityBikeCF(graph);

        RouteComputer rc = new RouteComputer(graph, cityBikeCF);
        RouteBean rb = new RouteBean(rc);

        ErrorManager errorManager = new ErrorManager();

        AnnotatedMapManager annotatedMapManager =
                new AnnotatedMapManager(graph, tileManager, rb, errorManager::displayError);

        ElevationProfileManager elevationProfileManager = new ElevationProfileManager(rb.getElevationProfileProperty(),
                rb.getHighlightedPositionP());

        ReadOnlyObjectProperty<ElevationProfile> elevationProfileP = rb.getElevationProfileProperty();

        rb.getHighlightedPositionP().bind(Bindings.
                when(annotatedMapManager.mousePositionOnRouteProperty().greaterThanOrEqualTo(0)).
                then(annotatedMapManager.mousePositionOnRouteProperty()).
                otherwise(elevationProfileManager.mousePositionOnProfileProperty()));

        //------Create central Pane--------
        SplitPane splitPane = new SplitPane(annotatedMapManager.pane());
        splitPane.setOrientation(Orientation.VERTICAL);

        elevationProfileP.addListener((p, oldV, newV) -> {
            if (oldV == null && newV != null) {
                Pane borderPane = elevationProfileManager.pane();
                SplitPane.setResizableWithParent(borderPane, false);
                splitPane.getItems().add(elevationProfileManager.pane());
            } else if (oldV != null && newV == null) {
                splitPane.getItems().remove(splitPane.getItems().size() - 1);
            }
        });
        StackPane stackPane = new StackPane(splitPane, errorManager.pane());

        //------Menu Object--------

        MenuItem menuItem = new MenuItem("Exporter GPX");
        menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> rb.getRouteProperty().get() == null,
                rb.getRouteProperty()));

        menuItem.setOnAction(e -> {
            try {
                GpxGenerator.writeGPX("javelo.gpx", rb.getRouteProperty().get(), elevationProfileP.get());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        MenuBar menuBar = new MenuBar(new Menu("Fichier", new Pane(), menuItem));

        //------Global Pane (root)--------

        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.setTop(menuBar);
        mainBorderPane.setCenter(stackPane);

        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(new Scene(mainBorderPane));
        primaryStage.setTitle("JaVelo");
        primaryStage.show();
    }
}
