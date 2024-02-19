package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

import static java.lang.Double.NaN;

/**
 * ElevationProfileManager class
 * This class manages the graphical interface representing the elevation profile of a route
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */
public final class ElevationProfileManager {
    private static final Insets insets = new Insets(10, 10, 20, 40);
    private static final int MIN_PIXELS_HORIZONTAL = 25;
    private static final int MIN_PIXELS_VERTICAL = 50;
    private static final int[] ELE_STEPS = new int[]{5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000};
    private static final int[] POS_STEPS = new int[]{1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000};
    private static final int KM_TO_METERS_FACTOR = 1000;
    private static final Font FONT = Font.font("Avenir", 10);
    private static final int SHIFT_LABELS_POSITION = 2;
    private static final double PREF_WIDTH_RATIO = 1 / 2d;
    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile;
    private final DoubleProperty highlightedPosition;
    private final ObjectProperty<Rectangle2D> rectangle;
    private final ObjectProperty<Transform> screenToWorldP;
    private final ObjectProperty<Transform> worldToScreenP;
    private final DoubleProperty mousePositionOnProfileProperty;
    private final BorderPane borderPane;
    private final Pane centralPane = new Pane();
    private final Polygon profileGraph = new Polygon();
    private final Line highlightedPositionLine = new Line();
    private final Path grid = new Path();
    private final Group gridLabels = new Group();
    private VBox vBox;

    /**
     * Constructor that initializes the GUI and creates bindings and listeners
     *
     * @param elevationProfileRO  Elevation profile of the Route (Read Only Property)
     * @param highlightedPosition Currently highlighted position
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfileRO,
                                   ReadOnlyDoubleProperty highlightedPosition) {
        this.elevationProfile = elevationProfileRO;
        this.highlightedPosition = (DoubleProperty) highlightedPosition;
        this.rectangle = new SimpleObjectProperty<>();
        this.screenToWorldP = new SimpleObjectProperty<>();
        this.worldToScreenP = new SimpleObjectProperty<>();
        this.mousePositionOnProfileProperty = new SimpleDoubleProperty(Double.NaN);

        this.borderPane = createGui();
        addBindings();
        addListeners();
    }


    /**
     * This method binds the rectangle containing the elevation graph to a new rectangle each time
     * the dimensions of the pane change.
     */
    private void addBindings() {
        rectangle.bind(Bindings.createObjectBinding(() -> {
            double xValue = Math.max(0, centralPane.getWidth() - (insets.getLeft() + insets.getRight()));
            double yValue = Math.max(0, centralPane.getHeight() - (insets.getTop() + insets.getBottom()));
            return new Rectangle2D(insets.getLeft(), insets.getTop(), xValue, yValue);
        }, centralPane.widthProperty(), centralPane.heightProperty()));
    }

    /**
     * This method adds listeners to the pane, the rectangle, and the elevation profile.
     */
    private void addListeners() {

        centralPane.setOnMouseMoved(e -> {
            if (rectangle.get().contains(e.getX(), e.getY()))
                mousePositionOnProfileProperty.setValue(screenToWorldP.get().transform(e.getX(), 0).getX());
            else
                mousePositionOnProfileProperty.set(NaN);
        });

        centralPane.setOnMouseExited(e -> mousePositionOnProfileProperty.setValue(NaN));

        rectangle.addListener(e -> {
            try {
                generateNewAffineFunctions();
                redrawProfile();
                drawGridAndLabels();
                highlightedPositionLine.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                                worldToScreenP.get().transform(this.highlightedPosition.get(), 0).getX(),
                        this.highlightedPosition, worldToScreenP));
                highlightedPositionLine.startYProperty().bind(Bindings.select(rectangle, "minY"));
                highlightedPositionLine.endYProperty().bind(Bindings.select(rectangle, "maxY"));
                highlightedPositionLine.visibleProperty().bind(highlightedPosition.greaterThanOrEqualTo(0));
            } catch (NonInvertibleTransformException ex) {
                ex.printStackTrace();
            }
        });

        elevationProfile.addListener(e -> {
            if (elevationProfile.get() != null) {
                try {
                    generateNewAffineFunctions();
                } catch (NonInvertibleTransformException ex) {
                    ex.printStackTrace();
                }
                redrawProfile();
                drawGridAndLabels();
                createProfileDataBox();
            }
        });

    }

    /**
     * This method returns the border pane of the elevationProfileManager
     *
     * @return the border pane
     */
    public Pane pane() {
        return borderPane;
    }

    /**
     * Returns a read-only property containing the position of the mouse pointer along the profile
     *
     * @return the position (in meters, rounded to the nearest integer),
     * or NaN if the mouse pointer is not above the profile
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionOnProfileProperty;
    }


//----------------------------------Section for Creating and Drawing GUI elements----------------------------------

    /**
     * This method creates the GUI structure that will display the elevation profile
     *
     * @return the BorderPane that is on the root of the structure
     */
    private BorderPane createGui() {
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add("elevation_profile.css");
        vBox = new VBox();
        vBox.setId("profile_data");
        borderPane.setCenter(initializeInternalPane());
        borderPane.setBottom(vBox);
        return borderPane;
    }

    /**
     * This method creates the profile data box that will be displayed in the lower part of the graphical interface.
     */
    private void createProfileDataBox() {

        vBox.getChildren().clear();
            Text text = new Text();
            String s = "Longueur : %.1f km".formatted(elevationProfile.get().length() / KM_TO_METERS_FACTOR) +
                    "     Montée : %.0f m".formatted(elevationProfile.get().totalAscent()) +
                    "     Descente : %.0f m".formatted(elevationProfile.get().totalDescent()) +
                    "     Altitude : de %.0f m à %.0f m".formatted(elevationProfile.get().minElevation(),
                            elevationProfile.get().maxElevation());
            text.setText(s);
            vBox.getChildren().add(text);
        
    }

    /**
     * This method initializes the internal pane that will contain the grid and the graph
     *
     * @return the pane
     */
    private Pane initializeInternalPane() {
        grid.setId("grid");
        profileGraph.setId("profile");
        centralPane.getChildren().addAll(grid, profileGraph, gridLabels, highlightedPositionLine);
        return centralPane;
    }

    /**
     * This method redraws the profile (polygon)
     */
    private void redrawProfile() {
        profileGraph.getPoints().clear();
        ObservableList<Double> profileGraphPoints = profileGraph.getPoints();
        profileGraphPoints.addAll(insets.getLeft(), insets.getTop() + rectangle.get().getHeight());
        Transform screenToWorld = screenToWorldP.get();
        Transform worldToScreen = worldToScreenP.get();

        for (double x = insets.getLeft(); x <= insets.getLeft() + rectangle.get().getWidth(); x++) {
            double xValue = screenToWorld.transform(x, 0).getX();
            double elevation = elevationProfile.get().elevationAt(xValue);
            double yValue = worldToScreen.transform(0, elevation).getY();
            profileGraphPoints.addAll(x, yValue);
        }

        profileGraphPoints.addAll(insets.getLeft() + rectangle.get().getWidth(),
                insets.getTop() + rectangle.get().getHeight());
    }

    /**
     * This method draws the grid and its labels
     */
    private void drawGridAndLabels() {
        grid.getElements().clear();
        gridLabels.getChildren().clear();

        double nbPixelsPerMeterY = rectangle.get().getHeight() /
                (elevationProfile.get().maxElevation() - elevationProfile.get().minElevation());

        int spaceBetweenHorizontalLines = chooseSpaceBetweenLines(nbPixelsPerMeterY, ELE_STEPS, MIN_PIXELS_HORIZONTAL);

        int heightDisplayed = (int) (spaceBetweenHorizontalLines *
                Math.ceil(elevationProfile.get().minElevation() / spaceBetweenHorizontalLines));

        while (heightDisplayed <= elevationProfile.get().maxElevation()) {
            double y_pixels = worldToScreenP.get().transform(0, heightDisplayed).getY();
            PathElement lineExtremity1 = new MoveTo(insets.getLeft(), y_pixels);
            PathElement lineExtremity2 = new LineTo(insets.getLeft() + rectangle.get().getWidth(), y_pixels);
            grid.getElements().addAll(lineExtremity1, lineExtremity2);

            Text text = new Text(Integer.toString(heightDisplayed));
            text.getStyleClass().addAll("grid_label", "vertical");
            text.setTextOrigin(VPos.CENTER);
            text.setFont(FONT);
            text.setX(insets.getLeft() - (text.prefWidth(0) + SHIFT_LABELS_POSITION));
            text.setY(y_pixels);
            gridLabels.getChildren().add(text);
            heightDisplayed += spaceBetweenHorizontalLines;
        }

        double nbPixelsPerMeterX = rectangle.get().getWidth() / elevationProfile.get().length();
        int spaceBetweenVerticalLines = chooseSpaceBetweenLines(nbPixelsPerMeterX, POS_STEPS, MIN_PIXELS_VERTICAL);

        int lengthDisplayed = 0;
        while (lengthDisplayed * KM_TO_METERS_FACTOR <= elevationProfile.get().length()) {
            double pixelsX = worldToScreenP.get().transform(lengthDisplayed * KM_TO_METERS_FACTOR, 0).getX();
            PathElement lineExtremity1 = new MoveTo(pixelsX, insets.getTop() + rectangle.get().getHeight());
            PathElement lineExtremity2 = new LineTo(pixelsX, insets.getTop());
            grid.getElements().addAll(lineExtremity1, lineExtremity2);

            Text text = new Text(Integer.toString(lengthDisplayed));
            text.getStyleClass().addAll("grid_label", "horizontal");
            text.setTextOrigin(VPos.TOP);
            text.setFont(FONT);
            text.setX(pixelsX - text.prefWidth(0) * PREF_WIDTH_RATIO);
            text.setY(insets.getTop() + rectangle.get().getHeight());
            gridLabels.getChildren().add(text);
            lengthDisplayed += spaceBetweenVerticalLines / KM_TO_METERS_FACTOR;
        }
    }


//---------------------------------------Section for relative position methods---------------------------------------

    /**
     * This method generates affine properties that will be used
     * convert screen coordinates to world coordinates and inversely
     *
     * @throws NonInvertibleTransformException when the transformation is not invertible (it is in our case)
     */
    private void generateNewAffineFunctions() throws NonInvertibleTransformException {

        /*  translate the rectangle origin to borderpane origin
            scale rectangle to borderpane
            reposition rectangle to original position  */

        if (elevationProfile.get() != null) {
            Affine screenToWorld = new Affine();
            screenToWorld.prependTranslation(-insets.getLeft(), -insets.getTop());
            screenToWorld.prependScale(elevationProfile.get().length() / rectangle.get().getWidth(),
                    (elevationProfile.get().minElevation() - elevationProfile.get().maxElevation())
                            / rectangle.get().getHeight());
            screenToWorld.prependTranslation(0, elevationProfile.get().maxElevation());
            screenToWorldP.setValue(screenToWorld);
            worldToScreenP.setValue(screenToWorld.createInverse());
        }
    }

    /**
     * Returns the correcting spacing between lines depending on the number of pixels per meter
     *
     * @param pixelsPerMeter  number of pixels per meter in the direction
     * @param steps           Different spacing possible for this direction
     * @param minimalDistance Minimal distance between 2 values
     * @return the spacing between 2 lines (vertical or horizontal)
     */
    private int chooseSpaceBetweenLines(double pixelsPerMeter, int[] steps, int minimalDistance) {
        int space = steps[steps.length - 1];
        for (int spacing : steps) {
            double value = spacing * pixelsPerMeter;
            if (value >= minimalDistance) {
                space = spacing;
                break;
            }
        }
        return space;
    }
}