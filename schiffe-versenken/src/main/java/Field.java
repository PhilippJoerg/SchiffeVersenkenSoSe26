public class Field {
    // Eigenschaften
    private boolean fieldWasShot;
    private boolean shipIsOnField;

    // Konstruktor
    public Field() {
        fieldWasShot = false;
        shipIsOnField = false;
    }

    // Methoden
    public boolean shootField() {
        fieldWasShot = true;
        if (shipIsOnField) {
            sinkShip();
            return true;
        } else
            return false;
    }

    public void setShip() {
        shipIsOnField = true;
    }

    public void sinkShip() {
        shipIsOnField = false;
    }

    public boolean isFieldWasShot() {
        return fieldWasShot;
    }

    public boolean isShip() {
        return shipIsOnField;
    }
}
