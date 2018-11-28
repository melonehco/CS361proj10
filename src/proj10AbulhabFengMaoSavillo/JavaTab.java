/*
 * File: JavaTab
 * F18 CS361 Project 9
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 11/20/18
 */

package proj10AbulhabFengMaoSavillo;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;

import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;


/**
 * Subclass of the JavaFX Tab class.
 */
public class JavaTab extends Tab{

    private JavaCodeArea javaCodeArea;
    private File file;

    /**
     * Constructor of Java Tab, initialize code area and set tab name and content
     * Set up the handler for close request
     * @param contentString
     * @param file
     * @param handler
     * @param rightClickMenu
     */
    public JavaTab(String contentString, File file, EventHandler<Event> handler, ObservableList<MenuItem> rightClickMenu){

        this.javaCodeArea = new JavaCodeArea(rightClickMenu);
        this.javaCodeArea.appendText(contentString);

        this.setContent(new VirtualizedScrollPane<>(this.javaCodeArea));

        this.setFile(file);
        this.setTabName();

        this.setOnCloseRequest(handler);
    }

    /**
     *
     * @return file
     */
    public File getFile() {
        return file;
    }

    /**
     *
     * @return javaCodeArea
     */
    public JavaCodeArea getJavaCodeArea() {
        return javaCodeArea;
    }


    /**
     * set file
     * @param file
     */
    public void setFile(File file){
        this.file = file;
    }



    /**
     * Sets the name of the tab to untitled if the file is new, or to the name of an existing file
     */
    private void setTabName()
    {
        if (file == null)
        {
            this.setText("Untitled");
        }
        else
        {
            this.setText(this.file.getName());
        }
    }


}
