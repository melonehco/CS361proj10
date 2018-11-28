
/*
 * File: FindReplaceController.java
 * F18 CS361 Project 9
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Find and Replace functionality adapted from Yue Lian, Mingchen Li, Peison Zhou, Zeb Keith-Hardy
 * Date: 11/20/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj10AbulhabFengMaoSavillo.controllers;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import proj10AbulhabFengMaoSavillo.JavaTabPane;

import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Find-replace controller handles find/replace-related actions.
 *
 * @author Zena Abulhab
 * @author Yi Feng
 * @author Melody Mao
 * @author Evan Savillo
 * @author Yue Lian
 * @author Mingchen Li
 * @author Peison Zhou
 * @author Zeb Keith-Hardy
 */
public class FindReplaceController{

    private JavaTabPane tabPane;
    private List<Integer> occurrenceIndices;
    // The target string to be found in handleFind
    private String targetText;
    // The index of the occurrence currently looked at
    private int curOccurrenceIndex;
    // The find and replace dialog
    private Dialog<ButtonType> findReplaceDialog;
    //The replace and replace all buttons in the find and replace dialog
    private ButtonType replace;
    private ButtonType replaceAll;


    public FindReplaceController(){
        this.occurrenceIndices = new ArrayList<Integer>();
        this.curOccurrenceIndex = 0;
    }


    public void setTabPane(JavaTabPane tabPane){
        this.tabPane = tabPane;
    }

    /**
     * Creates the Find and Replace Dialog
     */
    public void createFindReplaceDialog()
    {
        this.findReplaceDialog = new Dialog<>();

        findReplaceDialog.setTitle("Find & Replace");

        // Set the button types.
        ButtonType find = new ButtonType("Find Next");
        this.replace = new ButtonType("Replace");
        this.replaceAll = new ButtonType("Replace All");
        findReplaceDialog.getDialogPane().getButtonTypes().addAll(find, replace, replaceAll, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField findField = new TextField();
        TextField replaceField = new TextField();

        gridPane.add(new Label("Find:"), 0, 0);
        gridPane.add(findField, 0, 1);
        gridPane.add(new Label("Replace With:"), 0, 2);
        gridPane.add(replaceField, 0, 3);

        findReplaceDialog.getDialogPane().setContent(gridPane);

        this.enableReplaceReplaceAll(false);

        final Button findBT = (Button) findReplaceDialog.getDialogPane().lookupButton(find);
        findBT.addEventFilter(ActionEvent.ACTION, event ->
        {
            this.handleFind(findField.getText());
            event.consume();
        });

        final Button replaceBT = (Button) findReplaceDialog.getDialogPane().lookupButton(replace);
        replaceBT.addEventFilter(ActionEvent.ACTION, event ->
        {
            this.handleReplace(replaceField.getText());
            event.consume();

        });

        final Button replaceAllBT = (Button) findReplaceDialog.getDialogPane().lookupButton(replaceAll);
        replaceAllBT.addEventFilter(ActionEvent.ACTION, event ->
        {
            this.handleReplaceAll(replaceField.getText());
            event.consume();
        });

        findReplaceDialog.showAndWait();

        this.targetText = null;
    }

    /**
     * searches through the current CodeArea for all instances of findText and populates this.findIndices.
     * Allows the User to scroll through all of the find results if the findText does not change
     * between button clicks.
     *
     * @param findText text to find in the CodeArea
     */
    private void handleFind(String findText)
    {
        CodeArea codeArea = this.tabPane.getCurrentCodeArea();
        String codeAreaText = codeArea.getText();

        //Check if this is the first time to find the given text
        //Or if a empty text is given
        if (!findText.equals(this.targetText) && findText.length() != 0)
        {
            this.targetText = findText;
            this.occurrenceIndices.clear();
            this.curOccurrenceIndex = 0;

            //Find the index of the first occurrence of the text to find
            int firstIndex = codeAreaText.indexOf(findText);
            if (firstIndex == -1)
            {
                return;
            }
            this.enableReplaceReplaceAll(true);

            this.occurrenceIndices.add(firstIndex);

            //Find indices of all other occurrence of the given text
            //And append them to the indices list
            int idx = firstIndex + 1;
            while (idx != -1)
            {
                int nextIndex = codeAreaText.indexOf(findText, idx);
                if (nextIndex == -1)
                {
                    break;
                }
                this.occurrenceIndices.add(nextIndex);
                idx = nextIndex + 1;
            }
            codeArea.selectRange(firstIndex, firstIndex + findText.length());
        }

        //If the second or more time to find the given text
        else
        {
            //Check if there are any occurrence.
            if (this.occurrenceIndices.isEmpty())
            {
                this.enableReplaceReplaceAll(false);
                return;
            }

            //Increment the current occurrence looking at.
            this.curOccurrenceIndex += 1;
            if (this.curOccurrenceIndex >= this.occurrenceIndices.size()) { this.curOccurrenceIndex = 0;}

            int currentIdx = this.occurrenceIndices.get(this.curOccurrenceIndex);

            //Select the text found
            codeArea.selectRange(currentIdx, currentIdx + findText.length());

        }
    }

    /**
     * Uses the currently selected instance of findText and replaces it with replaceText
     *
     * @param replaceText text to replace the current findText
     */
    private void handleReplace(String replaceText)
    {
        //Check if there are any occurrence of the text to be found
        if (this.occurrenceIndices == null || this.occurrenceIndices.isEmpty())
        {
            return;
        }
        CodeArea codeArea = this.tabPane.getCurrentCodeArea();
        String codeAreaText = codeArea.getText();

        String newContent;
        String beforeFind;
        String afterFind;

        //Find the text indices of the text to replace
        int startReplace = this.occurrenceIndices.get(this.curOccurrenceIndex);
        int endReplace = startReplace + this.targetText.length();

        //Find the substring before and after the text to be replaced
        if (startReplace != 0)
        {
            beforeFind = codeAreaText.substring(0, startReplace);
            afterFind = codeAreaText.substring(endReplace);
        }
        else
        {
            beforeFind = "";
            afterFind = codeAreaText.substring(endReplace);
        }

        //Combine the substrings and the replacetext to get the new content in the codearea
        newContent = beforeFind + replaceText + afterFind;

        //Update the index of the occurrences
        int lengthDiff = replaceText.length() - this.targetText.length();
        for (int i = this.curOccurrenceIndex; i < this.occurrenceIndices.size(); i++)
        {
            this.occurrenceIndices.set(i, this.occurrenceIndices.get(i) + lengthDiff);
        }

        //Remove the index of the occurrence that was replaced
        int removeIdx = this.curOccurrenceIndex;
        this.occurrenceIndices.remove(removeIdx);

        //Update the index of the found text currently highlighted
        if (this.curOccurrenceIndex == this.occurrenceIndices.size())
        {
            this.curOccurrenceIndex = 0;
        }

        codeArea.replaceText(newContent);

        //High light the next found text if there are any
        if (!this.occurrenceIndices.isEmpty())
        {
            int currentIdx = this.occurrenceIndices.get(curOccurrenceIndex);
            codeArea.selectRange(currentIdx, currentIdx + this.targetText.length());
        }
        else
        {
            //Disable replace if there are no more found text
            this.enableReplaceReplaceAll(false);
        }
    }

    /**
     * Replaces all the text that is this.textToFind with replaceText
     *
     * @param replaceText text to replace this.textToFind
     */
    private void handleReplaceAll(String replaceText)
    {
        //Check if there are any occurrence of the text to be found
        if (this.occurrenceIndices == null || this.occurrenceIndices.size() == 0)
        {
            return;
        }
        CodeArea codeArea = this.tabPane.getCurrentCodeArea();
        String codeAreaText = codeArea.getText();

        //Replace all the found text with new replace text
        String newContent = codeAreaText.replaceAll(this.targetText, replaceText);
        this.occurrenceIndices.clear(); //all have been replaced.
        this.curOccurrenceIndex = 0;
        this.targetText = null;

        codeArea.replaceText(newContent);
        this.enableReplaceReplaceAll(false);
    }

    /**
     * A helper function which enables or disables Replace and ReplaceAll
     *
     * @param enable whether or not to enable the buttons
     */
    private void enableReplaceReplaceAll(boolean enable)
    {
        findReplaceDialog.getDialogPane().lookupButton(replace).setDisable(!enable);
        findReplaceDialog.getDialogPane().lookupButton(replaceAll).setDisable(!enable);
    }

}
