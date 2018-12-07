/*
 * File: Controller.java
 * F18 CS361 Project 10
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 12/6/18
 * This file contains the Main controller class, handling actions evoked by the Main window.
 */

package proj10AbulhabFengMaoSavillo.controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import proj10AbulhabFengMaoSavillo.JavaCodeArea;
import proj10AbulhabFengMaoSavillo.JavaTabPane;

import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;


/**
 * Main controller handles actions evoked by the Main window.
 *
 * @author Zena Abulhab
 * @author Yi Feng
 * @author Melody Mao
 * @author Evan Savillo
 */
public class Controller
{
    /**
     * ToolbarController handling toolbar actions
     */
    private ToolBarController toolbarController;
    /**
     * FileMenuController handling File menu actions
     */
    private FileMenuController fileMenuController;
    /**
     * EditMenuController handling Edit menu actions
     */
    private EditMenuController editMenuController;
    /**
     * PreferencesMenuController handling the theme preferences
     */
    private PreferencesMenuController preferencesMenuController;
    /**
     * treeStructure View Controller handling the current file's treeStructure View
     */
    private StructureViewController structureViewController;
    /**
     * Controller for the directory
     */
    private DirectoryViewController directoryViewController;
    /**
     * TabPane defined in Main.fxml
     */
    @FXML
    private JavaTabPane javaTabPane;
    /**
     * the console pane defined in Main.fxml
     */
    @FXML
    private StyleClassedTextArea console;
    /**
     * Close menu item of the File menu defined in Main.fxml
     */
    @FXML
    private MenuItem closeMenuItem;
    /**
     * Save menu item of the File menu defined in Main.fxml
     */
    @FXML
    private MenuItem saveMenuItem;
    /**
     * Save As menu item of the File menu defined in Main.fxml
     */
    @FXML
    private MenuItem saveAsMenuItem;
    /**
     * Dark theme menu item of the Preferences menu defined in Main.fxml
     */
    @FXML
    private MenuItem lightThemeMenuItem;
    /**
     * Edit menu defined in Main.fxml
     */
    @FXML
    private Menu editMenu;

    /**
     * the tree view representing the directory
     */
    @FXML
    private TreeView directoryTreeView;
    /**
     * the tree view representing the current file structure
     */
    /**
     * The VBox for the IDE
     */
    @FXML
    private VBox VBox;
    /**
     * The file structure view tree that lists classes, fields, and methods
     */
    @FXML
    private TreeView structureTreeView;
    /**
     * Checkbox which currently toggles File treeStructure View
     */
    @FXML
    private CheckBox checkBox;
    /**
     * Split pane which contains File treeStructure View on left and the rest on right
     */
    @FXML
    private SplitPane horizontalSplitPane;
    @FXML
    private Button scanButton;
    @FXML
    private Button scanAndParseButton;

    /**
     * This function is called after the FXML fields are populated.
     * Sets up references to the sub Controllers.
     * Sets up bindings.
     * Sets focus to console.
     */
    @FXML
    public void initialize()
    {
        //instantiate each controller before setup
        this.fileMenuController = new FileMenuController();
        this.editMenuController = new EditMenuController();
        this.toolbarController = new ToolBarController();
        this.directoryViewController = new DirectoryViewController();
        this.structureViewController = new StructureViewController();

        // set up the sub controllers
        this.setupEditMenuController();
        this.setupFileMenuController();
        this.setupToolbarController();
        this.setupPreferencesMenuController();
        this.setupStructureViewController();
        this.setupDirectoryController();

        this.setButtonBinding();
        this.setupEventAwareness();

        // Sets focus to console on startup
        this.console.requestFocus();
    }

    /**
     * Sets up listening and handling of various events and whatnot
     */
    private void setupEventAwareness()
    {
        // Detects presses to tab (overriding the system default that deletes the selection) and calls tabOrUntab
        {
            this.javaTabPane.addEventFilter(KeyEvent.KEY_PRESSED, event ->
            {
                // if tab or shift+tab pressed
                if (event.getCode() == KeyCode.TAB)
                {
                    tabOrUntab(event);
                }
            });
        }

        // Structure View various
        {
            SplitPane.Divider divider = this.horizontalSplitPane.getDividers().get(0);

            // Toggles the side panel
            this.checkBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                                                         {
                                                             if (!newValue)
                                                                 divider.setPosition(0.0);
                                                             else
                                                                 divider.setPosition(0.25);
                                                         });

            // Prevents user from resizing split pane when closed
            divider.positionProperty().addListener(((observable, oldValue, newValue) ->
            {
                if (!this.checkBox.isSelected()) divider.setPosition(0.0);
            }));

            // Updates the file structure view whenever a key is typed
            this.javaTabPane.addEventFilter(KeyEvent.KEY_RELEASED, event ->
            {
                this.updateStructureView();

            });

            // Updates the file structure view whenever the tab selection changes
            // e.g., open tab, remove tab, select another tab
            this.javaTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) ->
                                                                                    {
                                                                                        this.updateStructureView();
                                                                                    });
        }
    }

    /**
     * Depending on whether or not shift was held down with tab, tab or untab the selection
     *
     * @param event the key event, whether that be tab or shift+tab
     */
    private void tabOrUntab(KeyEvent event)
    {
        JavaCodeArea currentCodeArea = this.javaTabPane.getCurrentCodeArea();
        if (currentCodeArea != null)
        {
            if (event.isShiftDown())
            { // Shift was held down with tab
                editMenuController.handleUnindentation(currentCodeArea);
            }
            else // Tab only
            {
                editMenuController.handleIndentation(currentCodeArea);
            }

        }
        event.consume();
    }

    /**
     * Parses and generates the structure view for the currently open code area
     */
    private void updateStructureView()
    {
        JavaCodeArea currentCodeArea = this.javaTabPane.getCurrentCodeArea();
        File currentFile = this.javaTabPane.getCurrentFile();

        // if the code area is open
        if (currentCodeArea != null)
        {
            // if this is not an unsaved file
            if (currentFile != null)
                // if this is a java file
                if (currentFile.getName().endsWith(".java") ||
                        currentFile.getName().endsWith(".btm"))
                {
                    // Re-generates the tree
                    this.structureViewController.generateStructureTree(currentCodeArea.getText());
                    return;
                }
        }

        // Gets rid of open structure view if not appropriate
        this.resetStructureView();
    }


    /**
     * Clears the currently open structure view of all nodes
     */
    public void resetStructureView()
    {
        this.structureViewController.resetRootNode();
    }

    /**
     * Binds the Close, Save, Save As menu items of the File menu,
     * the Edit menu, with the condition whether the tab pane is empty.
     */
    private void setButtonBinding()
    {
        this.closeMenuItem.disableProperty().bind(this.fileMenuController.tablessProperty());
        this.saveMenuItem.disableProperty().bind(this.fileMenuController.tablessProperty());
        this.saveAsMenuItem.disableProperty().bind(this.fileMenuController.tablessProperty());
        this.editMenu.disableProperty().bind(this.fileMenuController.tablessProperty());
        this.scanButton.disableProperty().bind(this.fileMenuController.tablessProperty());
        this.scanAndParseButton.disableProperty().bind(this.fileMenuController.tablessProperty());
    }

    /**
     * Creates a reference to the ToolbarController and passes in window items and other sub Controllers when necessary.
     */
    private void setupToolbarController()
    {
        this.toolbarController.setConsole(this.console);
        this.toolbarController.setFileMenuController(this.fileMenuController);
        this.toolbarController.initialize();
    }

    /**
     * Creates a reference to the FileMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupFileMenuController()
    {
        this.fileMenuController.setTabPane(this.javaTabPane);
        this.fileMenuController.setCheckBox(this.checkBox);
        this.fileMenuController.setDirectoryViewController(this.directoryViewController);
        this.fileMenuController.setEditMenu(this.editMenu);
    }

    /**
     * Creates a reference to the EditMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupEditMenuController()
    {
        this.editMenuController.setTabPane(this.javaTabPane);
    }

    /**
     * Creates a reference to the PreferencesMenuController and sets its VBox.
     * Also Passes into the constructor the reference to the initial theme's menu item
     */
    private void setupPreferencesMenuController()
    {
        // Initialize the light theme menu item as being selected first, since this is the default theme
        this.preferencesMenuController = new PreferencesMenuController(this.lightThemeMenuItem);
        preferencesMenuController.setVBox(this.VBox);
    }

    /**
     * Creates a reference to the StructureViewController and passes in relevant items
     */
    private void setupStructureViewController()
    {
        this.structureViewController.setTreeView(this.structureTreeView);
    }

    /**
     * Creates a reference to the StructureViewController and passes in relevant items
     */
    private void setupDirectoryController()
    {
        this.directoryViewController.setFileMenuController(this.fileMenuController);
        this.directoryViewController.setTabPane(this.javaTabPane);
        this.directoryViewController.setTreeView(this.directoryTreeView);
    }

    /**
     * Calls the method that handles the Scan button action from the toolbarController.
     */
    @FXML
    private void handleScanButtonAction(Event event)
    {
        this.toolbarController.handleScanButtonAction(event, this.javaTabPane.getCurrentTab());
    }

    /**
     * Calls the method that handles the Scan and Parse button action from the toolbarController.
     */
    @FXML
    private void handleScanAndParseButtonAction(Event event)
    {
        this.toolbarController.handleScanAndParseButtonAction(event, this.javaTabPane.getCurrentTab());
    }

    /**
     * Calls the method that handles About menu item action from the fileMenuController.
     */
    @FXML
    private void handleAboutAction()
    {
        this.fileMenuController.handleAboutAction();
    }

    /**
     * Calls the method that handles the New menu item action from the fileMenuController.
     */
    @FXML
    private void handleNewAction()
    {
        this.fileMenuController.handleNewAction();
        //this.updateStructureView();
    }

    /**
     * Calls the method that handles the Open menu item action from the fileMenuController.
     */
    @FXML
    private void handleOpenAction()
    {
        this.fileMenuController.handleOpenAction();
    }

    /**
     * Calls the method that handles the Close menu item action from the fileMenuController.
     *
     * @param event Event object
     */
    @FXML
    private void handleCloseAction(Event event)
    {
        this.fileMenuController.handleCloseAction(event);
    }

    /**
     * Calls the method that handles the Save As menu item action from the fileMenuController.
     */
    @FXML
    private void handleSaveAsAction()
    {
        this.fileMenuController.handleSaveAsAction();
        this.updateStructureView(); //eventually implement some kind of listener here?
    }

    /**
     * Calls the method that handles the Save menu item action from the fileMenuController.
     */
    @FXML
    private void handleSaveAction()
    {
        this.fileMenuController.handleSaveAction();
        this.updateStructureView();
    }

    /**
     * Calls the method that handles the Exit menu item action from the fileMenuController.
     *
     * @param event Event object
     */
    @FXML
    public void handleExitAction(Event event)
    {
        this.fileMenuController.handleExitAction(event);
    }

    /**
     * Calls the method that handles the Edit menu action from the editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleEditMenuAction(ActionEvent event)
    {
        this.editMenuController.handleEditMenuAction(event);
    }

    /**
     * Calls the method that handles the Preferences menu action from the preferencesMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handlePreferencesMenuAction(ActionEvent event)
    {
        this.preferencesMenuController.handlePreferencesMenuAction(event);
    }

    /**
     * Calls the method that handles the Keyword color menu item from the settingMenuController.
     */
    @FXML
    public void handleKeywordColorAction() { this.preferencesMenuController.handleKeywordColorAction(); }

    /**
     * Calls the method that handles the Parentheses/Brackets color menu item from the settingMenuController.
     */
    @FXML
    public void handleParenColorAction() { this.preferencesMenuController.handleParenColorAction(); }

    /**
     * Calls the method that handles the String color menu item from the settingMenuController.
     */
    @FXML
    public void handleStrColorAction() { this.preferencesMenuController.handleStrColorAction(); }

    /**
     * Calls the method that handles the Int color menu item from the settingMenuController.
     */
    @FXML
    public void handleIntColorAction() { this.preferencesMenuController.handleIntColorAction(); }

    /**
     * Jump to the line where the selected class/method/field is declared.
     */
    @FXML
    private void handleTreeItemClicked()
    {
        this.structureViewController.handleTreeItemClicked(this.javaTabPane);
    }

    /**
     * Calls the method that handles the Find and Replace menu item action from the editMenuController.
     */
    @FXML
    public void handleFindReplace()
    {
        this.editMenuController.handleFindReplace();
    }

}
