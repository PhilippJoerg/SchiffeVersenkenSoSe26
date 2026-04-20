package ui;

@FunctionalInterface
public interface BoardClickListener {
    void onCellClicked(int col, int row);
}