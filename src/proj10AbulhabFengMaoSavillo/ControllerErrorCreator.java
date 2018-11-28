package proj10AbulhabFengMaoSavillo;

import javafx.scene.control.Alert;

/**
 * Contains the static error method used by all controllers
 */
public class ControllerErrorCreator {
    /**
     * Creates a error dialog displaying message of any error encountered.
     *
     * @param errorTitle  String of the error title
     * @param errorString String of error message
     */
    public static void createErrorDialog(String errorTitle, String errorString)
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(errorTitle + " Error");
        alert.setHeaderText("Error for " + errorTitle);
        alert.setContentText(errorString);
        alert.showAndWait();
    }
}
