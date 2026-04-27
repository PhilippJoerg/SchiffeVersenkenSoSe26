<<<<<<< HEAD:schiffe-versenken/src/main/java/ui/ShipDragData.java
package ui;
=======
<<<<<<< HEAD:schiffe-versenken/src/main/java/view/ShipDragData.java
package view;
=======
package ui;
>>>>>>> 4f43638 (added drag n drop for ships):schiffe-versenken/src/main/java/ui/ShipDragData.java
>>>>>>> 44456cf (added drag n drop for ships):schiffe-versenken/src/main/java/view/ShipDragData.java

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;

<<<<<<< HEAD:schiffe-versenken/src/main/java/ui/ShipDragData.java
=======
<<<<<<< HEAD:schiffe-versenken/src/main/java/view/ShipDragData.java
import models.ShipOrientation;
import models.ShipType;

=======
>>>>>>> 4f43638 (added drag n drop for ships):schiffe-versenken/src/main/java/ui/ShipDragData.java
>>>>>>> 44456cf (added drag n drop for ships):schiffe-versenken/src/main/java/view/ShipDragData.java
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