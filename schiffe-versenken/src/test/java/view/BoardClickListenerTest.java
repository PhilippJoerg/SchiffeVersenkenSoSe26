package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse BoardClickListener.
 * en: Tests the BoardClickListener class.
 */
class BoardClickListenerTest {

    /**
     * de: Reagiert auf das Ereignis "TestLambdaImplementationReceivesCoordinates".
     * en: Responds to the "TestLambdaImplementationReceivesCoordinates" event.
     *
     */
    @Test
    void testLambdaImplementationReceivesCoordinates() {
        int[] hit = new int[2];
        BoardClickListener listener = (col, row) -> {
            hit[0] = col;
            hit[1] = row;
        };

        listener.onCellClicked(3, 5);
        assertEquals(3, hit[0]);
        assertEquals(5, hit[1]);
    }
}
