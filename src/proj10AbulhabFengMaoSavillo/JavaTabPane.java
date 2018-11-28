/*
 * File: JavaTabPane
 * F18 CS361 Project 9
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 11/20/18
 */

package proj10AbulhabFengMaoSavillo;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import java.io.File;

/**
 * This class extends the TabPane class from JavaFx to handle JavaTabs.
 *
 * @author Evan Savillo
 * @author Yi Feng
 * @author Zena Abulhab
 * @author Melody Mao
 *
 */
public class JavaTabPane extends TabPane {


    /**
     * Create a new tab
     * @param contentString content of the new tab
     * @param file
     * @param handler
     * @param rightClickMenu
     */
    public void createTab(String contentString, File file, EventHandler<Event> handler, ObservableList<MenuItem> rightClickMenu)
    {

        JavaTab newTab = new JavaTab(contentString,file,handler,rightClickMenu);

        this.getSelectionModel().select(newTab);

        this.getTabs().add(newTab);
    }

    /**
     * remove a tab from the tabpane and the tablist
     * @param tab
     */
    public void removeTab(JavaTab tab){
        this.getTabs().remove(tab);
    }


    /**
     * Returns the current tab
     * @return the current tab if there is one, return null otherwise.
     */
    public JavaTab getCurrentTab()
    {
        return (JavaTab)this.getSelectionModel().getSelectedItem();

    }


    /**
     * Returns the code area currently being viewed in the current tab
     * @return the JavaCodeArea object of the open tab if there is one,
     *         return null otherwise.
     */
    public JavaCodeArea getCurrentCodeArea()
    {
        JavaTab selectedTab = getCurrentTab();
        if (selectedTab != null)
        {
            return selectedTab.getJavaCodeArea();
        }
        else
            return null;
    }

    /**
     * Returns the file object in the current tab
     * @return the File object of the item selected in the tab pane if there is one,
     *          return null otherwise.
     */
    public File getCurrentFile()
    {
        JavaTab selectedTab = getCurrentTab();
        if (selectedTab != null)
        {
            return selectedTab.getFile();
        }
        else return null;
    }




}
