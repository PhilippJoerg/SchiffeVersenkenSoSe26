/*
 * Datei: view/ShipDragData.java
 * Einfache Serializable-Klasse, die beim Drag-and-Drop die Informationen über Schiffstyp
 * und Ausrichtung transportiert.
 */
package view;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;

import models.ShipOrientation;
import models.ShipType;

/**
 * de: Die Klasse ShipDragData.
 * en: The class ShipDragData.
 */
public final class ShipDragData implements Serializable {
    /**
     * de: Das Feld FLAVOR.
     * en: The field FLAVOR.
     *
     * @param ShipDragData.class de: Parameter ShipDragData.class. en: Parameter ShipDragData.class.
     * @param "ShipDragData" de: Parameter "ShipDragData". en: Parameter "ShipDragData".
     */
    public static final DataFlavor FLAVOR = new DataFlavor(ShipDragData.class, "ShipDragData");

    private final ShipType shipType;
    private final ShipOrientation orientation;

    /**
     * de: Konstruktor für ShipDragData.
     * en: Constructor for ShipDragData.
     *
     * @param shipType de: Der Schiffstyp. en: The ship type.
     * @param orientation de: Die Ausrichtung des Schiffs. en: The orientation of the ship.
     */
    public ShipDragData(ShipType shipType, ShipOrientation orientation) {
        this.shipType = shipType;
        this.orientation = orientation;
    }

    /**
     * de: Gibt den Schiffstyp zurück.
     * en: Returns the ship type.
     *
     * @return de: Der Schiffstyp. en: The ship type.
     */
    public ShipType getShipType() {
        return shipType;
    }

    /**
     * de: Gibt die Ausrichtung des Schiffs zurück.
     * en: Returns the orientation of the ship.
     *
     * @return de: Die Ausrichtung des Schiffs. en: The orientation of the ship.
     */
    public ShipOrientation getOrientation() {
        return orientation;
    }
}