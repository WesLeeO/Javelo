package ch.epfl.javelo.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Error Manager class
 * This class handles errors signaled from the gui
 *
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public final class ErrorManager {
    private static final String DURATION_FADE_1 = "200ms";
    private static final String DURATION_FADE_2 = "500ms";
    private static final String DURATION_STILL = "2000ms";
    private static final double MIN_OPACITY = 0;
    private static final double MAX_OPACITY = 0.8;
    private final VBox vBox;
    private SequentialTransition sequentialTransition;


    /**
     * Constructor which takes no parameters
     */
    public ErrorManager() {
        vBox = new VBox();
        vBox.setMouseTransparent(true);
        vBox.getStylesheets().add("error.css");
        sequentialTransition = new SequentialTransition();
    }

    /**
     * Returns the pane displaying errors
     *
     * @return vBox
     */
    public Pane pane() {
        return vBox;
    }

    /**
     * This method displays an error on the screen
     *
     * @param s : string which should be displayed
     */
    public void displayError(String s) {

        // stops the current animation if one is currently taking place
        sequentialTransition.stop();
        vBox.getChildren().clear();

        Text t = new Text();
        t.setText(s);
        vBox.getChildren().add(t);

        FadeTransition f1 = new FadeTransition(Duration.valueOf(DURATION_FADE_1));
        f1.setFromValue(MIN_OPACITY);
        f1.setToValue(MAX_OPACITY);

        PauseTransition pauseTransition = new PauseTransition(Duration.valueOf(DURATION_STILL));

        FadeTransition f2 = new FadeTransition(Duration.valueOf(DURATION_FADE_2));
        f2.setFromValue(MAX_OPACITY);
        f2.setToValue(MIN_OPACITY);

        sequentialTransition = new SequentialTransition(vBox, f1, pauseTransition, f2);
        sequentialTransition.play();
        java.awt.Toolkit.getDefaultToolkit().beep();
    }
}
