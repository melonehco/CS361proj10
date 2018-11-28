/*
 * File: EditMenuController.java
 * F18 CS361 Project 9
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 11/20/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj10AbulhabFengMaoSavillo.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import proj10AbulhabFengMaoSavillo.JavaTabPane;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.Selection;


/**
 * Edit menu controller handles Edit menu related actions.
 *
 * @author Zena Abulhab
 * @author Yi Feng
 * @author Melody Mao
 * @author Evan Savillo
 */
public class EditMenuController {
    /**
     * javaTabPane defined in Main.fxml
     */
    @FXML
    private JavaTabPane javaTabPane;
    private FindReplaceController findReplaceController;


    /**
     * Constructor for the EditMenuController
     */
    public EditMenuController()
    {

        findReplaceController = new FindReplaceController();
    }


    /**
     * Sets the tab pane.
     *
     * @param javaTabPane TabPane defined in Main.fxml
     */
    public void setTabPane(JavaTabPane javaTabPane)
    {
        this.javaTabPane = javaTabPane;
    }

    /**
     * Handles the Edit menu action.
     *
     * @param event ActionEvent object
     */
    public void handleEditMenuAction(ActionEvent event)
    {
        // get the code area embedded in the selected tab window
        CodeArea activeCodeArea = this.javaTabPane.getCurrentCodeArea();
        if (activeCodeArea == null) // edit menu will be disabled anyway
            return;

        MenuItem clickedItem = (MenuItem) event.getTarget();
        switch (clickedItem.getId())
        {
            case "undoMenuItem":
                activeCodeArea.undo();
                break;
            case "redoMenuItem":
                activeCodeArea.redo();
                break;
            case "cutMenuItem":
                activeCodeArea.cut();
                break;
            case "copyMenuItem":
                activeCodeArea.copy();
                break;
            case "pasteMenuItem":
                activeCodeArea.paste();
                break;
            case "selectMenuItem":
                activeCodeArea.selectAll();
                break;
            case "tabMenuItem":
                this.handleIndentation(activeCodeArea);
                break;
            case "untabMenuItem":
                this.handleUnindentation(activeCodeArea);
                break;
            case "commentMenuItem":
                this.handleToggleCommenting(activeCodeArea);
                break;
            case "findReplaceMenuItem":
                this.handleFindReplace();
            default:
        }
    }

    /**
     * Handler for the "Find & Replace" menu item in the "Edit" menu.
     */
    public void handleFindReplace()
    {
        if (this.javaTabPane.getCurrentCodeArea() != null)
        {
            findReplaceController.setTabPane(javaTabPane);
            findReplaceController.createFindReplaceDialog();
        }
    }

    /**
     * Handles the indentation of the selected text in the code area.
     * Called from the tab key or the menu item
     *
     * @param selectedCodeArea the active codearea
     */
    public void handleIndentation(CodeArea selectedCodeArea) {
        Selection<?, ?, ?> selection = selectedCodeArea.getCaretSelectionBind();
        int startIdx = selection.getStartParagraphIndex(); // multi-line start line
        int endIdx = selection.getEndParagraphIndex(); // multi-line end line
        int initialCaretCol = selectedCodeArea.getCaretColumn();

        // Make cursor follow tab if only one line was tabbed in
        if(startIdx == endIdx){
            selectedCodeArea.insertText(startIdx, initialCaretCol, "\t");
        }
        else // multiple lines tabbed in
        {
            for (int lineNum = startIdx; lineNum <= endIdx; lineNum++) {
                selectedCodeArea.insertText(lineNum, 0, "\t");
            }
        }
    }

    /**
     * Handles unindentation of the selected text by removing white space from the start.
     * Works one full tab at a time, or for any extra space(s)
     * @param selectedCodeArea
     */
    public void handleUnindentation(CodeArea selectedCodeArea) {
        Selection<?, ?, ?> selection = selectedCodeArea.getCaretSelectionBind();
        int startIdx = selection.getStartParagraphIndex();
        int endIdx = selection.getEndParagraphIndex();
        for (int lineNum = startIdx; lineNum <= endIdx; lineNum++) {
            // full tab(s) present at the start of the line
            if (selectedCodeArea.getParagraph(lineNum).getText().startsWith("\t")) {
                selectedCodeArea.deleteText(lineNum, 0, lineNum, 1);
            }
            // space(s) present at the start of the line, but not a full tab
            else if (selectedCodeArea.getParagraph(lineNum).getText().startsWith(" ")) {
                while (selectedCodeArea.getParagraph(lineNum).getText().startsWith(" ")) {
                    selectedCodeArea.deleteText(lineNum, 0, lineNum, 1);
                }
            }

        }
    }

    /**
     * Handles commenting and uncommenting of the selected text in the code area
     * @param selectedCodeArea
     */
    public void handleToggleCommenting(CodeArea selectedCodeArea)
    {

        // get the start paragraph and the end paragraph of the selection
        Selection<?, ?, ?> selection = selectedCodeArea.getCaretSelectionBind();
        int startIdx = selection.getStartParagraphIndex();
        int endIdx = selection.getEndParagraphIndex();

        // If there is one line that is not commented in the selected paragraphs,
        // comment all selected paragraphs.
        boolean shouldComment = false;
        for (int lineNum = startIdx; lineNum <= endIdx; lineNum++)
        {
            if (!(selectedCodeArea.getParagraph(lineNum).getText().startsWith("//")))
            {
                shouldComment = true;
            }
        }

        // If we should comment all paragraphs, comment all paragraphs.
        // If all selected the paragraphs are commented,
        // uncomment the selected paragraphs.
        if (shouldComment)
        {
            for (int lineNum = startIdx; lineNum <= endIdx; lineNum++)
            {
                selectedCodeArea.insertText(lineNum, 0, "//");
            }
        }
        else
        {
            for (int lineNum = startIdx; lineNum <= endIdx; lineNum++)
            {
                selectedCodeArea.deleteText(lineNum, 0, lineNum, 2);
            }
        }
    }



}