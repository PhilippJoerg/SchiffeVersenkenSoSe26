/*
 * Datei: view/BoardClickListener.java
 * Funktionales Interface: Callback für Klicks auf einzelne Zellen des Boards.
 */
package view;

@FunctionalInterface
public interface BoardClickListener {
    void onCellClicked(int col, int row);
} 