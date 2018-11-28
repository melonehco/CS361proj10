/*
 * File: Main.java
 * F18 CS361 Project 9
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 11/20/2018
 */

package proj10AbulhabFengMaoSavillo;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import proj10AbulhabFengMaoSavillo.controllers.Controller;


/**
 * This class creates a stage, as specified in Main.fxml, that contains a
 * set of tabs, embedded in a tab pane, with each tab window containing a
 * code area; a menu bar containing File and Edit menu; and a toolbar of
 * buttons for compiling, running, and stopping code; and a program console
 * that takes in standard input, displays standard output and program message.
 *
 * @author Zena Abulhab
 * @author Yi Feng
 * @author Melody Mao
 * @author Evan Savillo
 */
public class Main extends Application
{
    private static Parent parentRoot;
    
    /**
     * main function of Main class
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * Creates a stage as specified in Main.fxml, that contains a set of tabs,
     * embedded in a tab pane, with each tab window containing a code area; a menu
     * bar containing File and Edit menu; and a toolbar of buttons for compiling,
     * running, and stopping code; and a program console that takes in standard
     * input, displays standard output and program message.
     *
     * @param stage The stage that contains the window content
     */
    @Override
    public void start(Stage stage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/proj10AbulhabFengMaoSavillo/resources/Main.fxml"));
        Parent root = loader.load();
        Main.parentRoot = root;
        Controller controller = loader.getController();

        // initialize a scene and add features specified in the css file to the scene
        Scene scene = new Scene(root, 640 + 160, 480 + 120);
        scene.getStylesheets().add(getClass().getResource(
                "/proj10AbulhabFengMaoSavillo/resources/Main.css").toExternalForm());

        // configure the stage
        stage.setTitle("AbulhabFengMaoSavillo's Project 10");
        stage.sizeToScene();
        stage.setScene(scene);
        stage.setOnCloseRequest(controller::handleExitAction);
        stage.show();
    }

    /**
     * Gets the parent root of the main program.
     * @return the parent root of the main program
     */
    static public Parent getParentRoot(){ return Main.parentRoot; }
}