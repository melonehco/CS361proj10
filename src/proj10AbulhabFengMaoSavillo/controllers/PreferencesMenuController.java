/*
 * File: PreferencesMenuController.java
 * F18 CS361 Project 9
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Implemented using code from proj6AhnDeGrawHangSlager and proj6JiangQuanMarcello
 * Date: 11/20/2018
 * This file contains the Preferences Menu controller class, handling actions evoked by the Preferences Menu
 */
package proj10AbulhabFengMaoSavillo.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import proj10AbulhabFengMaoSavillo.ControllerErrorCreator;
import proj10AbulhabFengMaoSavillo.Main;

/**
 * Preferences Menu controller handles preference menu actions .
 *
 * @author Zena Abulhab
 * @author Yi Feng
 * @author Melody Mao
 * @author Evan Savillo
 */
public class PreferencesMenuController {

    /**
     * Reference to the VBox that Controller passes in
     */
    private VBox vBox;

    /**
     * The menu item that was last pressed, representing which theme the IDE was switched to
     */
    private MenuItem lastPressedMenuItem;

    /**
     * Maps string names for colors to the Colors assigned to them in our CSS
     */
    private HashMap<String, Color> colorNamesToColors;
    
    /**
     * Stores the path to the CSS file that is modified by preference selections
     */
    private String preferenceFilePath; //path for reloading CSS
    private String preferenceURLString;
    private String preferenceWritingPath; //path for writing to CSS file (needs src or bin in front of it)
    
    
    /**
     * Constructor in which we initialize the last pressed menu item and disable it; in our case,
     * we want it to be the light theme menu item because we start off with this as the default
     */
    public PreferencesMenuController(MenuItem initialSelectedMenuItem){
        initialSelectedMenuItem.setDisable(true);
        this.lastPressedMenuItem = initialSelectedMenuItem;
        this.preferenceFilePath = "../resources/ColorPreferences.css";
        this.preferenceURLString = getClass().getResource(this.preferenceFilePath).toExternalForm();
        this.preferenceWritingPath = "proj9AbulhabFengMaoSavillo/resources/ColorPreferences.css";
        this.setupColorChoices();
    }

    /**
     * Sets the vBox field of this controller
     * @param vBox the VBox to store
     */
    public void setVBox(VBox vBox){
        this.vBox = vBox;
    }

    /**
     * Sets up the fields storing the color preference choices and their CSS
     */
    private void setupColorChoices()
    {
    	//set up color map for setting color-chooser window colors
    	this.colorNamesToColors = new HashMap<String, Color>();
    	this.colorNamesToColors.put("Purple", Color.PURPLE);
    	this.colorNamesToColors.put("Black", Color.BLACK);
    	this.colorNamesToColors.put("RoyalBlue", Color.ROYALBLUE);
    	this.colorNamesToColors.put("Teal", Color.TEAL);
    	this.colorNamesToColors.put("Orchid", Color.ORCHID);
    	this.colorNamesToColors.put("Orange", Color.ORANGE);
    	this.colorNamesToColors.put("Firebrick", Color.FIREBRICK);
    	this.colorNamesToColors.put("Grey", Color.GREY);
    	this.colorNamesToColors.put("SkyBlue", Color.SKYBLUE);
    }

    /**
     * Get the action that the user chose and execute the correct methos
     * @param event
     */
    public void handlePreferencesMenuAction(ActionEvent event){
        lastPressedMenuItem.setDisable(false); // enable the former last pressed menu item
        lastPressedMenuItem = (MenuItem) event.getTarget();
        switch (lastPressedMenuItem.getId()) {
            case "lightThemeMenuItem":
                handleNormalMode();
                break;
            case "darkThemeMenuItem":
                loadAlternateTheme("/proj9AbulhabFengMaoSavillo/resources/DarkTheme.css");
                break;
            case "halloweenThemeMenuItem":
                loadAlternateTheme("/proj9AbulhabFengMaoSavillo/resources/HallowTheme.css");
                break;
            default:
        }
        lastPressedMenuItem.setDisable(true); // disable the menu item we just pressed
    }

    /**
     * Helper method to use a non-default theme
     * @param themeCSS which theme to change to
     */
    private void loadAlternateTheme(String themeCSS){
        if(this.vBox.getStylesheets().size() > 1){
            this.vBox.getStylesheets().remove(this.vBox.getStylesheets().size()-1);
        }
        this.vBox.getStylesheets().add(themeCSS);
    }

    /**
     * Helper method to reset the IDE color theme, removing any alternate styling
     */
    @FXML
    public void handleNormalMode(){
        this.vBox.getStylesheets().remove(vBox.getStylesheets().size()-1);
    }

    /**
     * Handles Keyword Color menu item action.
     * Pops up a window displaying the color preference for the keywords.
     * By selecting a color from the drop-down menu, the color of the keywords will change accordingly.
     */
    public void handleKeywordColorAction(){
        Stage keywordColorWin = new Stage();
        keywordColorWin.setTitle("Keyword Color");

        VBox keywordColorRoot = new VBox();
        keywordColorRoot.setAlignment(Pos.CENTER);
        keywordColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.PURPLE);

        ChoiceBox<String> keywordColorCB = new ChoiceBox<String>(FXCollections.observableArrayList(
    			"Purple", "Black","RoyalBlue","Teal","Orchid","Orange", "Firebrick"));
        keywordColorCB.setValue("Purple");

        Text message = new Text("Keyword Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.PURPLE);

        keywordColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	String selectedColor = keywordColorCB.getValue();
            	
            	//update color-chooser coloring
            	Color newColor = colorNamesToColors.get(selectedColor);
            	rect.setFill(newColor);
            	message.setFill(newColor);
            	
            	updateKeywordPreference(selectedColor);
            }
        });

        keywordColorRoot.getChildren().addAll(message,rect, keywordColorCB);
        Scene keywordColorScene = new Scene(keywordColorRoot, 200,200);
        keywordColorWin.setScene(keywordColorScene);
        keywordColorWin.show();
    }
    
    /**
     * Updates the keyword class's preference CSS to the given color
     * @param color new keyword color
     */
    private void updateKeywordPreference(String color)
    {
    	//build string for new style
    	String newCSS = "\n\n.keyword {" +
						"\n\t-fx-fill: " + color.toLowerCase() + ";" +
						"\n\t-fx-font-weight: bold;" +
						"\n}";
    	//update CSS file
    	this.updatePreferenceCSS(newCSS);
    }

    /**
     * Handles Parentheses/Brackets Color menu item action.
     */
    public void handleParenColorAction(){
        Stage parenColorWin = new Stage();
        parenColorWin.setTitle("Parentheses/Brackets Color");

        VBox parenColorRoot = new VBox();
        parenColorRoot.setAlignment(Pos.CENTER);
        parenColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.TEAL);

        ChoiceBox<String> parenColorCB = new ChoiceBox<String>(FXCollections.observableArrayList(
                "Teal", "Black","RoyalBlue","Grey","Orchid","Orange", "Firebrick"));
        parenColorCB.setValue("Teal");

        Text message = new Text("Parentheses/Brackets Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.TEAL);

        parenColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	String selectedColor = parenColorCB.getValue();
            	
            	//update color-chooser coloring
            	Color newColor = colorNamesToColors.get(selectedColor);
            	rect.setFill(newColor);
            	message.setFill(newColor);
            	
            	//write new style into CSS file storing preferences
            	updateParenthesisPreference(selectedColor);
            }
        });

        parenColorRoot.getChildren().addAll(message,rect, parenColorCB);
        Scene keywordColorScene = new Scene(parenColorRoot, 230,200);
        parenColorWin.setScene(keywordColorScene);
        parenColorWin.show();
    }
    
    /**
     * Helper method: Updates the parenthesis class's preference CSS to the given color
     * @param color new parenthesis/bracket/brace color
     */
    private void updateParenthesisPreference(String color)
    {
    	//build string for new style
    	String cssColor = color.toLowerCase();
    	String newCSS = "\n\n.paren {" +
						"\n\t-fx-fill: " + cssColor + ";" +
						"\n\t-fx-font-weight: bold;" +
						"\n}" +
						"\n\n.bracket {" +
						"\n\t-fx-fill: " + cssColor + ";" +
						"\n\t-fx-font-weight: bold;" +
						"\n}" +
						"\n\n.brace {" +
						"\n\t-fx-fill: " + cssColor + ";" +
						"\n\t-fx-font-weight: bold;" +
						"\n}";
    	//update CSS file
    	this.updatePreferenceCSS(newCSS);
    }

    /**
     * Handles String Color menu item action.
     */
    public void handleStrColorAction(){
        Stage strColorWin = new Stage();
        strColorWin.setTitle("String Color");

        VBox strColorRoot = new VBox();
        strColorRoot.setAlignment(Pos.CENTER);
        strColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.ROYALBLUE);

        ChoiceBox<String> strColorCB = new ChoiceBox<String>(FXCollections.observableArrayList(
        		"RoyalBlue", "Black","Teal","SkyBlue","Orchid","Orange", "Firebrick"));
        strColorCB.setValue("RoyalBlue");

        Text message = new Text("String Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.ROYALBLUE);

        strColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	String selectedColor = strColorCB.getValue();
            	
            	//update color-chooser coloring
            	Color newColor = colorNamesToColors.get(selectedColor);
            	rect.setFill(newColor);
            	message.setFill(newColor);
            	
            	updateStringPreference(selectedColor);
            }
        });

        strColorRoot.getChildren().addAll(message,rect, strColorCB);
        Scene keywordColorScene = new Scene(strColorRoot, 230,200);
        strColorWin.setScene(keywordColorScene);
        strColorWin.show();
    }
    
    /**
     * Helper method: Updates the string class's preference CSS to the given color
     * @param color new string color
     */
    private void updateStringPreference(String color)
    {
    	//build string for new style
    	String cssColor = color.toLowerCase();
    	String newCSS = "\n\n.string {" +
						"\n\t-fx-fill: " + cssColor + ";" +
						"\n}";
    	//update CSS file
    	this.updatePreferenceCSS(newCSS);
    }

    /**
     * Handles int Color menu item action.
     */
    public void handleIntColorAction(){
        Stage intColorWin = new Stage();
        intColorWin.setTitle("int Color");

        VBox intColorRoot = new VBox();
        intColorRoot.setAlignment(Pos.CENTER);
        intColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75,75, Color.FIREBRICK);

        ChoiceBox<String> intColorCB = new ChoiceBox<String>(FXCollections.observableArrayList(
                "Firebrick", "Black","RoyalBlue","SkyBlue","Orchid","Orange", "Teal"));
        intColorCB.setValue("Firebrick");

        Text message = new Text("Integer(int) Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.FIREBRICK);

        intColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String selectedColor = intColorCB.getValue();
            	
            	//update color-chooser coloring
            	Color newColor = colorNamesToColors.get(selectedColor);
            	rect.setFill(newColor);
            	message.setFill(newColor);
            	
            	updateIntPreference(selectedColor);
            }
        });

        intColorRoot.getChildren().addAll(message,rect, intColorCB);
        Scene keywordColorScene = new Scene(intColorRoot, 230,200);
        intColorWin.setScene(keywordColorScene);
        intColorWin.show();
    }
    
    /**
     * Helper method: Updates the string class's preference CSS to the given color
     * @param color new string color
     */
    private void updateIntPreference(String color)
    {
    	//build string for new style
    	String cssColor = color.toLowerCase();
    	String newCSS = "\n\n.integer {" +
						"\n\t-fx-fill: " + cssColor + ";" +
						"\n}";
    	//update CSS file
    	this.updatePreferenceCSS(newCSS);
    }
    
    /**
     * Helper method: writes the given new CSS style into the preferences CSS file
     * and reloads the application stylesheets
     * Does not check for valid CSS
     * @param newStyle new CSS style as a String
     */
    private void updatePreferenceCSS(String newStyle)
    {
    	Parent root = Main.getParentRoot();
        
        //write new style into CSS file
    	try
    	{
    		//update src copy
    		BufferedWriter writer = new BufferedWriter( new FileWriter( "src/" + preferenceWritingPath, true ) );
        	writer.write(newStyle);
        	writer.close();
        	
        	//update bin copy
        	writer = new BufferedWriter( new FileWriter( "bin/" + preferenceWritingPath, true ) );
        	writer.write(newStyle);
        	writer.close();
    	}
    	catch (IOException e)
    	{
    		ControllerErrorCreator.createErrorDialog("Updating Preferences", "Preference file could not be accessed");
    		return;
    	}
    	
    	//reload styling
    	root.getStylesheets().remove(preferenceURLString);
    	preferenceURLString = getClass().getResource(preferenceFilePath).toExternalForm();
    	root.getStylesheets().add(preferenceURLString);
    }
}
