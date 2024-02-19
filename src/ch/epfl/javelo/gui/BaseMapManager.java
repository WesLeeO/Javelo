package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;


/**
 * BaseMapManager class
 * This class manages the graphical interface for the map
 *
 * @author Wesley Nana Davies(344592)
 */
public final class BaseMapManager {
    private final static int SIZE_TILE = 256;
    private static final int ZOOM_LEVEL_MIN = 8;
    private static final int ZOOM_LEVEL_MAX = 19;
    private static final int SCROLL_DELAY = 200;
    private final WayPointsManager wayPointsManager;
    private final TileManager tileManager;
    private final ObjectProperty<MapViewParameters> mapViewParametersP;
    private final ObjectProperty<Point2D> coordinatesMouseWhenPressed;
    private final Canvas canvas;
    private final Pane pane;
    private boolean redrawNeeded;

    /**
     * Constructor of the class
     *
     * @param tileManager        : a tileManager
     * @param wayPointsManager   : a wayPointsManager
     * @param mapViewParametersP : a MapViewParameters property
     */
    public BaseMapManager(TileManager tileManager, WayPointsManager wayPointsManager,
                          ObjectProperty<MapViewParameters> mapViewParametersP) {

        this.tileManager = tileManager;
        this.wayPointsManager = wayPointsManager;
        this.mapViewParametersP = mapViewParametersP;
        this.coordinatesMouseWhenPressed = new SimpleObjectProperty<>();
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);

        addPaneListeners();
        addCanvasProperties();
        addScrollListener();
        addDragListener();
        mapViewParametersP.addListener(event -> redrawOnNextPulse());
        redrawOnNextPulse();
    }


    /**
     * Adds listeners to the pane
     */
    private void addPaneListeners() {
        // stores the coordinates of the mouse when pressed
        pane.setOnMousePressed(e -> coordinatesMouseWhenPressed.setValue(new Point2D(e.getX(), e.getY())));

        pane.setOnMouseClicked(event -> {
            if (event.isStillSincePress()) {

                this.wayPointsManager.addWaypoint(
                        mapViewParametersP.get().xUpperLeftMapView() + event.getX(),
                        mapViewParametersP.get().yUpperLeftMapView() + event.getY()
                );
            }
        });

        // redraw the canvas when the dimensions of the pane have changed
        pane.widthProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        pane.heightProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
    }

    /**
     * This method adds the properties to the canvas
     */
    private void addCanvasProperties() {

        // bind canvas dimensions to pane dimensions
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // JavaFX calls redrawIfNeeded at each beat
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }


    /**
     * This method adds a scroll listener to the map to manage the zoom level
     */
    private void addScrollListener() {
        SimpleLongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {

            // minimizing the frequency which enables zoom level changes (4 zoomLevel max per second)
            if (e.getDeltaY() == 0d) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + SCROLL_DELAY);
            int zoomDelta = (int) Math.signum(e.getDeltaY());

            int currentZoomLevel = mapViewParametersP.get().zoomLevel();
            int newZoomLevel = Math2.clamp(ZOOM_LEVEL_MIN, currentZoomLevel + zoomDelta, ZOOM_LEVEL_MAX);
            int difference = newZoomLevel - currentZoomLevel;

            Point2D topLeftPoint = mapViewParametersP.get().topLeft();

            topLeftPoint = topLeftPoint.add(e.getX(), e.getY()).
                    multiply(Math.scalb(1, difference)).
                    subtract(e.getX(), e.getY());

            mapViewParametersP.setValue(new MapViewParameters(newZoomLevel, topLeftPoint.getX(), topLeftPoint.getY()));

        });

    }


    /**
     * This method adds a drag listener to the map to manage its displacement
     */
    private void addDragListener() {
        pane.setOnMouseDragged(event -> {
            Point2D point = mapViewParametersP.get().topLeft();
            point = point.add(coordinatesMouseWhenPressed.get()).subtract(event.getX(), event.getY());
            mapViewParametersP.setValue(mapViewParametersP.get().withMinXY(point.getX(), point.getY()));
            coordinatesMouseWhenPressed.setValue(new Point2D(event.getX(), event.getY()));
        });
    }


    /**
     * This method redraws the map if and only if redrawNeeded is true
     */
    private void redrawIfNeeded() {
        if (redrawNeeded) {
            redrawNeeded = false;

            GraphicsContext gc = canvas.getGraphicsContext2D();
            MapViewParameters mapViewParameters = mapViewParametersP.get();

            int tileX = (int) Math.floor(mapViewParameters.xUpperLeftMapView() / SIZE_TILE);
            int tileY = (int) Math.floor(mapViewParameters.yUpperLeftMapView() / SIZE_TILE);
            int xMax = (int) Math.ceil(canvas.getWidth() / SIZE_TILE);
            int yMax = (int) Math.ceil(canvas.getHeight() / SIZE_TILE);


            //Iterate and draw tiles in the canvas
            for (int i = 0; i <= xMax; i++) {
                for (int j = 0; j <= yMax; j++) {
                    try {
                        gc.drawImage(tileManager.getTileImage(new TileManager.TileId(mapViewParameters.zoomLevel(),
                                        i + tileX,
                                        j + tileY)),
                                (i + tileX) * SIZE_TILE - mapViewParameters.xUpperLeftMapView(),
                                (j + tileY) * SIZE_TILE - mapViewParameters.yUpperLeftMapView());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }

            }
        }
    }


    /**
     * This method forces a redrawing of the map by requesting a pulse.
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * This method simply returns the pane of BaseMap
     *
     * @return the JavaFX panel displaying the basemap.
     */
    public Pane pane() {
        return pane;
    }

}

