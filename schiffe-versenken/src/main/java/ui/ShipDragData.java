package ui;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;

public final class ShipDragData implements Serializable {
    public static final DataFlavor FLAVOR = new DataFlavor(ShipDragData.class, "ShipDragData");

    private final ShipType shipType;
    private final ShipOrientation orientation;

    public ShipDragData(ShipType shipType, ShipOrientation orientation) {
        this.shipType = shipType;
        this.orientation = orientation;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public ShipOrientation getOrientation() {
        return orientation;
    }
}