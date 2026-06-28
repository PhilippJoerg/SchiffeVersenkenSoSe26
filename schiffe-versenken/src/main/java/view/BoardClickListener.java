/*
 * Datei: view/BoardClickListener.java
 * Funktionales Interface: Callback für Klicks auf einzelne Zellen des Boards.
 */
package view;

/**
 * de: Schnittstelle BoardClickListener definiert einen Callback für Klicks auf einzelne Zellen des Boards.
 * en: Interface BoardClickListener defines a callback for clicks on individual cells of the board.
 */
@FunctionalInterface
public interface BoardClickListener {
    /**
     * de: Wird aufgerufen, wenn eine Zelle des Boards angeklickt wird.
     * en: Called when a cell on the board is clicked.
     *
     * @param col de: Spalte der angeklickten Zelle. en: Column of the clicked cell.
     * @param row de: Zeile der angeklickten Zelle. en: Row of the clicked cell.
     */
    void onCellClicked(int col, int row);
} 