/*
 * Datei: view/ShipTransferable.java
 * Transferable-Implementierung für Drag-and-Drop: liefert `ShipDragData` als Transfer-Daten.
 */
package view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * de: Die Klasse ShipTransferable implementiert das Transferable-Interface für Drag-and-Drop.
 * en: The class ShipTransferable implements the Transferable interface for drag-and-drop.
 */
public final class ShipTransferable implements Transferable {
    private final ShipDragData data;

    /**
     * de: Konstruktor für ShipTransferable.
     * en: Constructor for ShipTransferable.
     *
     * @param data de: Die zu übertragenden Daten. en: The data to be transferred.
     */
    public ShipTransferable(ShipDragData data) {
        this.data = data;
    }

    /**
     * de: Gibt die unterstützten DataFlavors zurück.
     * en: Returns the supported DataFlavors.
     *
     * @return de: Die unterstützten DataFlavors. en: The supported DataFlavors.
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{ShipDragData.FLAVOR};
    }

    /**
     * de: Prüft, ob ein bestimmter DataFlavor unterstützt wird.
     * en: Checks if a specific DataFlavor is supported.
     *
     * @param flavor de: Der zu prüfende DataFlavor. en: The DataFlavor to check.
     * @return de: true, wenn der DataFlavor unterstützt wird, sonst false. en: true if the DataFlavor is supported, otherwise false.
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return ShipDragData.FLAVOR.equals(flavor);
    }

    /**
     * de: Gibt die Transferdaten für einen bestimmten DataFlavor zurück.
     * en: Returns the transfer data for a specific DataFlavor.
     *
     * @param flavor de: Der zu prüfende DataFlavor. en: The DataFlavor to check.
     * @return de: Die Transferdaten. en: The transfer data.
     * @throws UnsupportedFlavorException de: Wenn der DataFlavor nicht unterstützt wird. en: If the DataFlavor is not supported.
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return data;
    }
}