/*
 * File: ToolBarController.java
 * F18 CS361 Project 9
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 11/20/2018
 * This file contains the ToolBarController class, handling Toolbar related actions.
 */

package proj10AbulhabFengMaoSavillo.controllers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.event.Event;
import proj10AbulhabFengMaoSavillo.JavaCodeArea;
import proj10AbulhabFengMaoSavillo.bantam.ast.Program;
import proj10AbulhabFengMaoSavillo.bantam.lexer.Scanner;
import proj10AbulhabFengMaoSavillo.bantam.lexer.Token;
import proj10AbulhabFengMaoSavillo.bantam.util.CompilationException;
import proj10AbulhabFengMaoSavillo.bantam.util.Error;
import proj10AbulhabFengMaoSavillo.bantam.util.ErrorHandler;
import proj10AbulhabFengMaoSavillo.bantam.parser.Parser;
import proj10AbulhabFengMaoSavillo.bantam.treedrawer.Drawer;

import java.util.List;
import java.util.concurrent.*;
import java.io.*;

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
     * Console defined in Main.fxml
     */
    private StyleClassedTextArea console;
    /**
     * Process currently compiling or running a Java file
     */
    private Process currentProcess;
    /**
     * Thread representing the Java program input stream
     */
    private Thread inThread;
    /**
     * Thread representing the Java program output stream
     */
    private Thread outThread;
    /**
     * Mutex lock to control input and output threads' access to console
     */
    private Semaphore mutex;
    /**
     * The consoleLength of the output on the console
     */
    private int consoleLength;
    /**
     * The FileMenuController
     */
    private FileMenuController fileMenuController;

    private Thread thread;

    /**
     * Initializes the ToolBarController controller.
     * Sets the Semaphore, the CompileWorker and the CompileRunWorker.
     */
    public void initialize()
    {
        this.mutex = new Semaphore(1);
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
     * Handles when the scan button is clicked; the current file is run through a lexical scanner.
     *
     * @param event the event triggered
     * @param file  the current file
     */
    public void handleScanButtonAction(Event event, File file)
    {
        // user select cancel button
        if (this.fileMenuController.checkSaveBeforeCompile() == 2)
        {
            event.consume();
        }
        else
        {
            // This may or may not solve a hard-to-replicate bug.
            if (this.thread != null)
            {
                if (this.thread.isAlive())
                {
                    try
                    {
                        this.thread.join(3000);
                    }
                    catch (Exception e)
                    {
                        System.out.println("threading headaches1");
                    }
                    finally
                    {
                        if (!this.thread.isInterrupted())
                            this.thread.interrupt();
                        this.thread = null;
                    }
                }
            }

            // Clear the console before printing
            Platform.runLater(() ->
                              {
                                  this.console.clear();
                                  consoleLength = 0;
                              });

            this.generateFile(file.getAbsolutePath());
        }

    }

    public void generateFile(String filename)
    {
        JavaCodeArea outputArea = requestAreaForOutput();

        Task task = new Task()
        {
            @Override
            protected Object call() throws Exception
            {
                ErrorHandler errorHandler = new ErrorHandler();
                Scanner scanner = new Scanner(filename, errorHandler);

                // Request that the filemenucontroller create a new tab in which to print

                // Scan the file and retrieve each token
                Token currentToken = scanner.scan();
                while (currentToken.kind != Token.Kind.EOF)
                {
                    String s = currentToken.toString();
                    Platform.runLater(() -> outputArea.appendText(s + "\n"));
                    currentToken = scanner.scan();
                }

                outputArea.setEditable(true);  // set the codeArea to editable after we're done writing to it

                List<Error> errorList = errorHandler.getErrorList();
                int errorCount = errorList.size();
                if (errorCount == 0)
                {
                    Platform.runLater(() -> console.appendText("No errors detected\n"));
                }
                else
                {
                    errorList.forEach((error) ->
                                      {
                                          Platform.runLater(() -> console.appendText(error.toString() + "\n"));
                                      });
                    String msg = String.format("Found %d error(s)\n", errorCount);
                    Platform.runLater(() -> console.appendText(msg));
                }

                return null;
            }
        };

        this.thread = new Thread(task);
        this.thread.setDaemon(true);
        this.thread.start();
    }

    /**
     * Request a new tab be made
     *
     * @return the code area in the newly made tab
     */
    private JavaCodeArea requestAreaForOutput()
    {
        return this.fileMenuController.giveNewCodeArea();
    }

    /**
     * TODO doc
     */
    public void handleScanAndParseButtonAction(Event event, File file)
    {
        // user select cancel button
        if (this.fileMenuController.checkSaveBeforeCompile() == 2)
        {
            event.consume();
        }
        else
        {
            // This may or may not solve a hard-to-replicate bug.
            if (this.thread != null)
            {
                if (this.thread.isAlive())
                {
                    try
                    {
                        this.thread.join(3000);
                    }
                    catch (Exception e)
                    {
                        System.out.println("threading headaches1");
                    }
                    finally
                    {
                        if (!this.thread.isInterrupted())
                            this.thread.interrupt();
                        this.thread = null;
                    }
                }
            }

            // Clear the console before printing
            Platform.runLater(() ->
                              {
                                  this.console.clear();
                                  consoleLength = 0;
                              });

            this.drawTree(file.getAbsolutePath());
        }
    }

    public void drawTree(String filename)
    {
        Task task = new Task()
        {
            @Override
            protected Object call() throws Exception
            {
                ErrorHandler errorHandler = new ErrorHandler();
                Scanner scanner = new Scanner(filename, errorHandler);

                // Request that the filemenucontroller create a new tab in which to print

                // Scan the file and retrieve each token
                Token currentToken = scanner.scan();
                while (currentToken.kind != Token.Kind.EOF)
                {
                    currentToken = scanner.scan();
                }

                Parser parser = new Parser(errorHandler);
                Program ast = null;
                try
                {
                    ast = parser.parse(filename);
                }
                catch (CompilationException e) { }
                finally
                {
                    List<Error> errorList = errorHandler.getErrorList();
                    int errorCount = errorList.size();
                    if (errorCount == 0)
                    {
                        String[] splitfilename = filename.split("/");
                        Platform.runLater(() -> console.appendText("No errors detected\n"));
                        new Drawer().draw(splitfilename[splitfilename.length - 1], ast);
                    }
                    else
                    {
                        errorList.forEach((error) ->
                                          {
                                              Platform.runLater(() -> console.appendText(error.toString() + "\n"));
                                          });
                        String msg = String.format("Found %d error(s)\n", errorCount);
                        Platform.runLater(() -> console.appendText(msg));
                    }
                }

                return null;
            }
        };

        this.thread = new Thread(task);
        this.thread.setDaemon(true);
        this.thread.start();
    }
}
