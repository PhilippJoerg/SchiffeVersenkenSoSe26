package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse ShipPlacementModel.
 * en: Tests the ShipPlacementModel class.
 */
class ShipPlacementModelTest {

    private ShipPlacementModel model;

    /**
     * de: Reagiert auf das Ereignis "SetUp".
     * en: Responds to the "SetUp" event.
     *
     */
    @BeforeEach
    void setUp() {
        model = new ShipPlacementModel(GameSettings.defaultSettings());
    }

    /**
     * de: Reagiert auf das Ereignis "TestInitialStateHasAllShipsRemaining".
     * en: Responds to the "TestInitialStateHasAllShipsRemaining" event.
     *
     */
    @Test
    void testInitialStateHasAllShipsRemaining() {
        assertFalse(model.isPlacementFinished());
        assertEquals(1, model.getRemainingCount(ShipType.BATTLESHIP));
        assertEquals(2, model.getRemainingCount(ShipType.CRUISER));
        assertEquals(3, model.getRemainingCount(ShipType.DESTROYER));
        assertEquals(4, model.getRemainingCount(ShipType.SUBMARINE));
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
    }

    /**
     * de: Reagiert auf das Ereignis "TestRotateCurrentShipTogglesOrientation".
     * en: Responds to the "TestRotateCurrentShipTogglesOrientation" event.
     *
     */
    @Test
    void testRotateCurrentShipTogglesOrientation() {
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
        model.rotateCurrentShip();
        assertEquals(ShipOrientation.VERTICAL, model.getCurrentOrientation());
        model.rotateCurrentShip();
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
    }

    /**
     * de: Reagiert auf das Ereignis "TestPlaceCurrentShipAndRemainingCounts".
     * en: Responds to the "TestPlaceCurrentShipAndRemainingCounts" event.
     *
     */
    @Test
    void testPlaceCurrentShipAndRemainingCounts() {
        boolean result = model.placeCurrentShip(0, 0);
        assertTrue(result);
        assertEquals(0, model.getRemainingCount(ShipType.BATTLESHIP));
        assertEquals(2, model.getRemainingCount(ShipType.CRUISER));
    }

    /**
     * de: Reagiert auf das Ereignis "TestPlaceShipFromDragWithNullReturnsFalse".
     * en: Responds to the "TestPlaceShipFromDragWithNullReturnsFalse" event.
     *
     */
    @Test
    void testPlaceShipFromDragWithNullReturnsFalse() {
        assertFalse(model.placeShipFromDrag(null, 0, 0, ShipOrientation.HORIZONTAL));
    }

    /**
     * de: Reagiert auf das Ereignis "TestAutoPlaceShipsFinishesPlacement".
     * en: Responds to the "TestAutoPlaceShipsFinishesPlacement" event.
     *
     */
    @Test
    void testAutoPlaceShipsFinishesPlacement() {
        model.autoPlaceShips();
        assertTrue(model.isPlacementFinished());
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
    }

    /**
     * de: Reagiert auf das Ereignis "TestGetNextShipTypeReturnsNullAfterAllPlaced".
     * en: Responds to the "TestGetNextShipTypeReturnsNullAfterAllPlaced" event.
     *
     */
    @Test
    void testGetNextShipTypeReturnsNullAfterAllPlaced() {
        model.autoPlaceShips();
        assertTrue(model.isPlacementFinished());
        assertEquals(null, model.getNextShipType());
    }
}
