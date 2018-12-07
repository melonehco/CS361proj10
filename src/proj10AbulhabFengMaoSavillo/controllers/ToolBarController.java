/*
 * File: ToolBarController.java
 * F18 CS361 Project 10
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 12/6/2018
 * This file contains the ToolBarController class, handling Toolbar related actions.
 */

package proj10AbulhabFengMaoSavillo.controllers;

import javafx.application.Platform;
import javafx.concurrent.Service;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.event.Event;
import proj10AbulhabFengMaoSavillo.JavaCodeArea;
import proj10AbulhabFengMaoSavillo.JavaTab;
import proj10AbulhabFengMaoSavillo.bantam.ast.Program;
import proj10AbulhabFengMaoSavillo.bantam.lexer.Scanner;
import proj10AbulhabFengMaoSavillo.bantam.lexer.Token;
import proj10AbulhabFengMaoSavillo.bantam.util.CompilationException;
import proj10AbulhabFengMaoSavillo.bantam.util.Error;
import proj10AbulhabFengMaoSavillo.bantam.util.ErrorHandler;
import proj10AbulhabFengMaoSavillo.bantam.parser.Parser;
import proj10AbulhabFengMaoSavillo.bantam.treedrawer.Drawer;

import java.util.List;

import javafx.concurrent.Task;


/**
 * ToolbarController handles Toolbar related actions.
 *
 * @author Evan Savillo
 * @author Yi Feng
 * @author Zena Abulhab
 * @author Melody Mao
 */
public class ToolBarController
{
    /**
     * The FileMenuController
     */
    private FileMenuController fileMenuController;
    /**
     * Console defined in Main.fxml
     */
    private StyleClassedTextArea console;
    private ScanWorker scanWorker;
    private ParseWorker parseWorker;
    private Drawer drawer;

    /**
     * Initializes the ToolBarController controller.
     * Sets the Drawer, and the Scan- and Parse-Workers.
     * Also sets OnStatus behavior of workers.
     */
    public void initialize()
    {
        this.drawer = new Drawer();

        // setup scan worker
        this.scanWorker = new ScanWorker();
        this.scanWorker.setOnSucceeded(event ->
                                       {
                                           /*
                                           On success, get the completed value, which is a string
                                           to be 'printed' to the console informing the user
                                           of the results
                                            */
                                           ((ScanWorker) event.getSource()).resetFields();

                                           // Clear the console before printing
                                           console.clear();
                                           console.appendText((String) (event.getSource().getValue()));
                                       });
        this.scanWorker.setOnCancelled(event ->
                                       {
                                           ((ScanWorker) event.getSource()).resetFields();
                                       }
        );

        //setup parse worker
        this.parseWorker = new ParseWorker();
        this.parseWorker.setOnSucceeded(event ->
                                        {
                                            /*
                                           On success, get the completed value, which is a string
                                           to be 'printed' to the console informing the user
                                           of the results

                                           If the parse is error free, also draws the tree
                                            */

                                            if (((ParseWorker) event.getSource()).isErrorFree)
                                            {
                                                String filename = ((ParseWorker) event.getSource()).filename;
                                                Program ast = ((ParseWorker) event.getSource()).root;

                                                String[] splitFilename = filename.split("/");
                                                this.drawer.draw(splitFilename[splitFilename.length - 1], ast);
                                            }

                                            ((ParseWorker) event.getSource()).resetFields();

                                            // Clear the console before printing
                                            console.clear();
                                            console.appendText((String) (event.getSource().getValue()));
                                        });
        this.parseWorker.setOnCancelled(event ->
                                        {
                                            ((ParseWorker) event.getSource()).resetFields();
                                        }
        );
    }

    /**
     * Sets the console pane.
     *
     * @param console StyleClassedTextArea defined in Main.fxml
     */
    public void setConsole(StyleClassedTextArea console)
    {
        this.console = console;
    }

    /**
     * Sets the FileMenuController.
     *
     * @param fileMenuController FileMenuController created in main Controller.
     */
    public void setFileMenuController(FileMenuController fileMenuController)
    {
        this.fileMenuController = fileMenuController;
    }

    /**
     * First ensures the user agrees to save file, then scans and parses the current file.
     *
     * @param event the event triggered
     * @param tab   the current tab
     */
    public void handleScanAndParseButtonAction(Event event, JavaTab tab)
    {
        // user selects cancel button
        if (this.fileMenuController.checkSaveBeforeScan() == 2)
        {
            event.consume();
        }
        else
        {
            // cancel any currently running parse
            this.parseWorker.cancel();

            ErrorHandler errorHandler = new ErrorHandler();
            String filename = tab.getFile().getAbsolutePath();

            this.parseWorker.setErrorHandler(errorHandler);
            this.parseWorker.setFilename(filename);

            // prime the state, then restart
            this.parseWorker.reset();
            this.parseWorker.restart();
        }
    }

    /**
     * Handles when the scan button is clicked; the current file is run through a lexical scanner.
     *
     * @param event the event triggered
     * @param tab   the current tab
     */
    public void handleScanButtonAction(Event event, JavaTab tab)
    {
        // user selects cancel button
        if (this.fileMenuController.checkSaveBeforeScan() == 2)
        {
            event.consume();
        }
        else
        {
            this.scanWorker.cancel();

            ErrorHandler errorHandler = new ErrorHandler();
            String filename = tab.getFile().getAbsolutePath();
            // Request that the filemenucontroller create a new tab in which to print
            JavaCodeArea outputArea = requestAreaForOutput();

            this.scanWorker.setErrorHandler(errorHandler);
            this.scanWorker.setFilename(filename);
            this.scanWorker.setOutputArea(outputArea);

            this.scanWorker.reset();
            this.scanWorker.restart();
        }

    }

    /**
     * Helper method, Request a new tab be made
     *
     * @return the code area in the newly made tab
     */
    private JavaCodeArea requestAreaForOutput()
    {
        return this.fileMenuController.giveNewCodeArea();
    }


    /**
     * Parse Worker which manages the Task of creating a Parser, parsing a scanned file,
     * and reporting the results (viz the root node and whether or not parsing encountered errors
     */
    protected static class ParseWorker extends Service<String>
    {
        public boolean isErrorFree; /* whether or not there were any errors parsing,
                                        determining whether or not a tree will be drawn
                                      */
        private ErrorHandler errorHandler;
        private String filename;
        private Parser parser;
        private Program root;

        public void resetFields()
        {
            this.errorHandler = null;
            this.filename = null;
            this.parser = null;
            this.root = null;
            this.isErrorFree = false;
        }

        public void setFilename(String filename)
        {
            this.filename = filename;
        }

        public void setErrorHandler(ErrorHandler errorHandler)
        {
            this.errorHandler = errorHandler;
        }

        /**
         * Attempts to parse the file as set before (re)starting this worker
         *
         * @return
         */
        @Override
        protected Task<String> createTask()
        {
            this.parser = new Parser(this.errorHandler);

            return new Task<String>()
            {
                @Override
                protected String call() throws Exception
                {
                    StringBuilder results = new StringBuilder();

                    try
                    {
                        root = parser.parse(filename);
                    }
                    catch (CompilationException e)
                    {
                        errorHandler.register(Error.Kind.LEX_ERROR, "Compilation Error:\n" + e.getMessage());
                    }

                    // Detect any errors
                    List<Error> errorList = errorHandler.getErrorList();
                    int errorCount = errorList.size();
                    if (errorCount == 0)
                    {
                        isErrorFree = true;
                        results.append("No errors detected\n");
                    }
                    else
                    {
                        isErrorFree = false;
                        final int[] parseErrorCount = {0};
                        errorList.forEach((error) ->
                                          {
                                              results.append(error.toString()).append("\n");

                                              if (error.getKind() == Error.Kind.PARSE_ERROR)
                                                  parseErrorCount[0]++;
                                          });
                        results.append(String.format("Found %d (parse) error(s)\n", parseErrorCount[0]));
                    }

                    return results.toString();
                }
            };
        }
    }


    /**
     * Scan Worker which manages the Task of creating a Scanner, scanning through a file,
     * and reporting results
     */
    protected static class ScanWorker extends Service<String>
    {
        private ErrorHandler errorHandler;
        private String filename;
        private Scanner scanner;
        private JavaCodeArea outputArea;

        public void setFilename(String filename)
        {
            this.filename = filename;
        }

        public void resetFields()
        {
            this.errorHandler = null;
            this.filename = null;
            this.scanner = null;
            this.outputArea = null;
        }

        public void setErrorHandler(ErrorHandler errorHandler)
        {
            this.errorHandler = errorHandler;
        }

        public void setOutputArea(JavaCodeArea outputArea)
        {
            this.outputArea = outputArea;
        }

        @Override
        protected Task<String> createTask()
        {
            this.scanner = new Scanner(filename, errorHandler);

            this.outputArea.setEditable(false); //user no touch until we're done

            return new Task<String>()
            {
                @Override
                protected String call()
                {
                    StringBuilder results = new StringBuilder();

                    try
                    {
                        // Scan the file and retrieve each token
                        Token currentToken = scanner.scan();
                        while (currentToken.kind != Token.Kind.EOF)
                        {
                            if (outputArea != null)
                            {
                                String s = currentToken.toString();
                                Platform.runLater(() -> outputArea.appendText(s + "\n"));
                            }

                            currentToken = scanner.scan();
                        }
                    }
                    catch (CompilationException e)
                    {
                        errorHandler.register(Error.Kind.LEX_ERROR, "Compilation Error:\n" + e.getMessage());
                    }

                    // Detect any errors
                    List<Error> errorList = errorHandler.getErrorList();
                    int errorCount = errorList.size();
                    if (errorCount == 0)
                    {
                        results.append("No errors detected\n");
                    }
                    else
                    {
                        errorList.forEach((error) ->
                                          {
                                              results.append(error.toString()).append("\n");
                                          });
                        results.append(String.format("Found %d error(s)\n", errorCount));
                    }

                    return results.toString();
                }
            };
        }
    }
}
