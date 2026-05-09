package models;

import java.util.EnumMap;
import java.util.Map;

import view.CellState;

public class ShipPlacementModel {

    private final CellState[][] ownBoard;
    private final EnumMap<ShipType, Integer> remainingShips;
    private ShipOrientation currentOrientation;

    public ShipPlacementModel() {
        ownBoard = BoardUtils.createEmptyCellBoard();
        remainingShips = createRemainingShips();
        currentOrientation = ShipOrientation.HORIZONTAL;
    }

    public CellState[][] getOwnBoard() {
        return ownBoard;
    }

    public Map<ShipType, Integer> getRemainingShips() {
        return new EnumMap<>(remainingShips);
    }

    public ShipOrientation getCurrentOrientation() {
        return currentOrientation;
    }

    public void rotateCurrentShip() {
        currentOrientation = currentOrientation == ShipOrientation.HORIZONTAL
                ? ShipOrientation.VERTICAL
                : ShipOrientation.HORIZONTAL;
    }

    public boolean isPlacementFinished() {
        for (Integer remaining : remainingShips.values()) {
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean placeShipFromDrag(ShipType shipType, int col, int row, ShipOrientation orientation) {
        return placeShip(shipType, col, row, orientation);
    }

    public boolean placeCurrentShip(int col, int row) {
        if (isPlacementFinished()) {
            return false;
        }

        ShipType shipType = getNextShipType();
        if (shipType == null) {
            return false;
        }

        return placeShip(shipType, col, row, currentOrientation);
    }

    public boolean placeShip(ShipType shipType, int col, int row, ShipOrientation orientation) {
        if (shipType == null) {
            return false;
        }

        if (getRemainingCount(shipType) <= 0) {
            return false;
        }

        if (!canPlaceShip(shipType, col, row, orientation)) {
            return false;
        }

        BoardUtils.placeShip(ownBoard, shipType, col, row, orientation);
        decrementRemaining(shipType);
        return true;
    }

    public boolean canPlaceShip(ShipType shipType, int startCol, int startRow, ShipOrientation orientation) {
        return BoardUtils.canPlaceShip(ownBoard, shipType, startCol, startRow, orientation);
    }

    public ShipType getNextShipType() {
        for (ShipType type : ShipType.values()) {
            if (getRemainingCount(type) > 0) {
                return type;
            }
        }
        return null;
    }

    public int getRemainingCount(ShipType shipType) {
        return remainingShips.getOrDefault(shipType, 0);
    }

    private void decrementRemaining(ShipType shipType) {
        remainingShips.put(shipType, getRemainingCount(shipType) - 1);
    }

    private EnumMap<ShipType, Integer> createRemainingShips() {
        EnumMap<ShipType, Integer> result = new EnumMap<>(ShipType.class);
        for (ShipType type : ShipType.values()) {
            result.put(type, type.getAmount());
        }
        return result;
    }
}
