import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.*;
import java.io.*;
import java.util.*;

class Com {
    private static String role;		    // Rolle: Server oder Client.
    private static BufferedReader in;	// Verpackung des Socket-Eingabestroms.
    private static Writer out;		    // Verpackung des Socket-Ausgabestroms.

    //Klassenvaribale für "size" umd die werte abzuspeichern
    private static int numRows;
    private static int numCols;

    private static void send(String msg) throws IOException {
        out.write( msg + "\n");
        out.flush();
    }
    
    //variblae dfür den Münzstart
    private static boolean myCoinTurn;

    //Zustände damit nicht unerlaubte spielzüge etc. gemacht werden können (als konstante)
    enum State { START,  
                 WAITING_FOR_SIZE_DONE,     //Server hat spielfeldgröße geschickt und wratet aud die bestätigung "done" von client das er die Größeninformation erhalten hat
                 WAITING_DONE_SHIPS,        //Server sagt welche schiffe es gibt und wartt auf die bestätigung von client, dass er diese info erhalten hat
                 WAITING_READY,             //Server ist bereit und wartet bis CLient auch bereit ist
                 MY_TURN, 
                 ENEMY_TURN,
                 WAITING_PASS,
                 WAITING_ANSWER,
                 WAITING_OK_SAVE,           //Save wurde gesendet und man wrtet auf die bestätigung mit ok
                 WAITING_OK_LOAD,           //Server hat ein load gesendet und wartet auf ok antwrot 
                 GAME_OVER }

    private static State currentState = State.START;    //Merkt sich die zustände im laufe des ziels (am anfang als start initialisert)
    private static State previousState;                 //damit später das spiel abspeichert werden kann

    //Speichert die schifflänge
    private static int[] shipLengths;

    //platzhalter für die spiel logik - wird später auf true gesetzz
    private static boolean allEnemyShipsSunk = false;
    private static boolean allMyShipsSunk = false;

    //Dummy 
    private static void saveGameState(long ID) {};
    private static void loadGameState(long ID) {};
    
    //Methoden zum testen - muss enterfnt werden später
    public static void saveGame() throws IOException {
    // Aktuellen Zustand merken, damit wir nach dem OK zurückspringen können
    previousState = currentState;
    long id = System.currentTimeMillis();
    send("save " + id);
    currentState = State.WAITING_OK_SAVE;
    System.out.println("Spiel gespeichert mit ID: " + id);
}

public static void fireShot(int row, int col) throws IOException {
    if (currentState != State.MY_TURN) {
        System.out.println("Nicht dein Zug!");
        return;
    }
    send("shot " + row + " " + col);
    currentState = State.WAITING_ANSWER;
}
    

//___________________________________________________________________________________________________________________
 // Hauptprogramm.
    public static void main (String [] args) throws IOException {
	// Verwendete Portnummer (vgl. Server.java).
	final int port = 50000;

	// Socketverbindung zur anderen "Seite" herstellen.
	Socket s;
	if (args.length == 0) {
	    role = "Server";

	    // Die eigene(n) IP-Adresse(n) ausgeben,
	    // damit der Benutzer sie dem Benutzer des Clients mitteilen kann.
	    System.out.print("My IP address(es):");
	    Enumeration<NetworkInterface> nis =
				    NetworkInterface.getNetworkInterfaces();
	    while (nis.hasMoreElements()) {
		NetworkInterface ni = nis.nextElement();
		Enumeration<InetAddress> ias = ni.getInetAddresses();
		while (ias.hasMoreElements()) {
		    InetAddress ia = ias.nextElement();
		    if (!ia.isLoopbackAddress()) {
			System.out.print(" " + ia.getHostAddress());
		    }
		}
	    }
	    System.out.println();
	    System.out.println("Waiting for client connection ...");

	    ServerSocket ss = new ServerSocket(port);
	    s = ss.accept();
	}
	else {
	    role = "Client";
	    s = new Socket(args[0], port);
	}
	System.out.println("Connection established.");

	// Ein- und Ausgabestrom des Sockets ermitteln
	// und als BufferedReader bzw. Writer verpacken.
	in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	out = new OutputStreamWriter(s.getOutputStream());
//______________________________________________________

    //Start des Spiels mit einem ZUfallsprinzip
    //zufalls "zahl" wird generiert
    Random random = new Random();

    //Varibale für den zwischenspeicher
    int coin;

    if(role.equals("Server")) {
        boolean result = random.nextBoolean();      //Zufalls - boolean wird in result gespeichert
        if (result){
            coin = 1;                               //Speichert das ergebnis 1 in coin
        } else {
            coin = 0;
        }
        send("COIN " + coin);                       //sendet das ergebnis an client
        myCoinTurn = (coin == 1);                   //server beinngt wenn coin 1 ist
    } else {
        String coinLine = in.readLine(); //liest die übergeben zahl
        coin = Integer.parseInt(coinLine.split(" ")[1]);//zerlegt die zeichenkette und wendetl es in int um und speicherte das zeite element "1" in coin
        myCoinTurn = (coin == 0); //Clientbeginnt wenn int 0 ist
    }

    //hiermit startet der Nachruchten austausch
    //Rollenprüfung und wechsel von coin in size
    if(role.equals("Server")){
        send("size 10"); //Dummy hilfsfunktion - wird später durch GUI ersetzt:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
        //send("size " + numRows); //sendet aktiv die spielfeldgröße
        currentState = State.WAITING_FOR_SIZE_DONE;
    } 
//___________________________________________________________________________________________________________
//Methode nur fürs testen - entferne später
// Temporärer Konsolen-Thread für eigene Züge
Thread inputThread = new Thread(() -> {
    try {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("Eingabe (z.B. 3 4 für Schuss, 'save' zum Speichern): ");
            String input = console.readLine();
            if (input == null) break;
            input = input.trim();
            if (input.equalsIgnoreCase("save")) {
                saveGame();
            } else {
                String[] parts = input.split("\\s+");
                if (parts.length == 2) {
                    try {
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        fireShot(row, col);
                    } catch (NumberFormatException e) {
                        System.out.println("Ungültige Zahlen. Bsp: 3 4");
                    }
                } else {
                    System.out.println("Falsches Format. Nutze: <zeile> <spalte> oder 'save'");
                }
            }
        }
    } catch (IOException e) {
        System.out.println("Eingabefehler: " + e.getMessage());
    }
});
inputThread.setDaemon(true); // Beendet sich automatisch, wenn das Programm endet
inputThread.start();

//________________________________________________________________________________________________________________________
    //Nachrichten austausch 
    //verarbeitet nur eingehende nachrichten
	while (true) {
	    String line = in.readLine(); //Nachricht wird separiert um den Befehl und die Zahl zu trennen
	    if (line == null) break;

        String[] parts = line.split(" "); //zerlegt die empfangene zeichenkette da wo ein leerzeichen kommt und wird dann als seperates string im array gespeichert "size 10" -> ["size", "10"]
        String cmd = parts[0]; // das erte element wird geholt ("size 10" -> size)
        switch (cmd) {
            
            //Spielfeldgrröße wird mitgeteilt
            case "size": 
                //Größe des Spieles aus der Nachricht rausnehmen
                int rows = Integer.parseInt(parts[1]); //part 1 -> "10" aus dem string wird ein int 10
                int cols = rows; //größe 10 wird auch cols übergeben
                
                //in der klassenvaribale abspeichern, damit man im laufe des spiels noch drauf zugreifen kann
                numRows = rows;
                numCols = cols;

                send("done");

                //spielfeldgröße info ist eingagen, client wartet auf die nächste nachricht (schiffsliste)
                currentState = State.WAITING_DONE_SHIPS;

                break;

            //Server teilt mit dass die Flotte aus Schiffen der Länge length besteht
            case "ships":
                int anzahl = parts.length -1; //anazhl der elemente im Array und ziehen mit -1 "ships" ab
                shipLengths = new int[anzahl];//neues int array mit der genauen anzahl von plätzen wird erzeugt 

                for(int i = 0; i < anzahl; i++){
                    shipLengths[i] = Integer.parseInt(parts[ i + 1]); //wandelt den String in int um  
                                                                    //i+1, weil i=0 ship ist, also erstn i=1 die anzahl
                }

                send("done");

                //client hat die nachricht erhalten und wartet jetzt das server ready sendet
                currentState = State.WAITING_READY;
                break;

            //Client teilt mit das er, die Infos zu Größe und schiffen erhalen hat
            case "done":
                if(role.equals("Client")){ //0ter Fall (Client empfängt kein done)
                    break;
                }

                if(role.equals("Server")) { //1ter Fall
                if(currentState == State.WAITING_FOR_SIZE_DONE) {  
                    send("ships 5 4 3 2 1"); //Dummy muss entfernt werden
                   //send("ships"); //ich brauche hier noch die schiffslänge:::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                   currentState = State.WAITING_DONE_SHIPS;
                } else if (currentState == State.WAITING_DONE_SHIPS) {  //2ter Fall
                   send("ready"); 
                   currentState = State.WAITING_READY;
                }
              } 
              break;
              
            case "ready":
                if (role.equals("Client")){  //wenn client
                    if (currentState == State.WAITING_READY) { //ist client im waiting ready zustand
                    send("ready"); //client sagt das er ready ist
                    currentState = myCoinTurn ? State.MY_TURN : State.ENEMY_TURN; //myCoinTurn wird nur noch eingelesen, wenn true mein zug sonst gegner
                    }
                } else if(role.equals("Server")) {
                   if (currentState == State.WAITING_READY)
                    currentState = myCoinTurn ? State.MY_TURN : State.ENEMY_TURN;
                   } 
                break;

            case "shot":
                int shotRow = Integer.parseInt(parts[1]); //koordinaten row und und col werden rausgenommen
                int shotCol = Integer.parseInt(parts[2]); //array wird in int umgewandelt

                //mit shotRow und shotCol wird geprüft ob ein schiff getroffen wurde
                //answerD muss dadanch ersetzt werden !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                
                //shot wird nurbetretten es im enemey_turn zustand ist
                if(currentState == State.ENEMY_TURN){

                //dummy, muss ersetzt durch die Spielogik werden - schuss wird ausgewertet::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                int answerD = 0;
                
                    send("answer " + answerD); 

                    if(answerD == 0){ 
                        currentState = State.WAITING_PASS; //wasser wurde getroffen -> gegner schickt mir ein pass
                    } else if (answerD == 1){
                        currentState = State.ENEMY_TURN; //ein treffer der gegner darf nochmal schießen
                    } else if (answerD == 2 && allMyShipsSunk == true){
                        currentState = State.GAME_OVER; //wenn alle schiffe versänkt -> gameover
                    } else if (answerD == 2 && allMyShipsSunk == false){ 
                        currentState = State.ENEMY_TURN; //wenn nicht alle meine schiffe versänkt wurden -> gegner ist dran
                    }
                }
            break;

            case "answer":
                int answer;

                if(currentState == State.WAITING_ANSWER){ //wartet auf eine antwort
                    answer = Integer.parseInt(parts[1]); //antwirt wird in int umgewandelt
                if( answer == 0){
                    send("pass"); //wasser -> pass und gegner ist dran
                    currentState = State.ENEMY_TURN;
                } else if (answer == 1) { //1 treffer -> ich darf noch mal
                    currentState = State.MY_TURN;
                } else if (answer == 2 && allEnemyShipsSunk == true) {          //hier muss noch geprüft werden ob alle gegnerischen schiffe versenkt wurden , dann game over:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                    currentState = State.GAME_OVER;
                } else if (answer == 2 && allEnemyShipsSunk == false) {         //hier muss noch geprüft werden ob alle gegnerischen schiffe versenkt wurden:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                    currentState = State.MY_TURN;
                }
            }          
            break;

            case "pass":
                if(currentState == State.WAITING_PASS)      //ist man im Waitiing pass zustand
                    currentState = State.MY_TURN;           //dann wechsle in den my turn zustanf
            break;

            case "save":
            long ID;
            
            //wenn der empfänger eine anfrage auf Save bekommt
             ID = Long.parseLong(parts[1]);     //id wird gespeichert aus der nachricht
             send("ok");                   //sendet zurück das man es erhalten hat mit ok

            //SPiel logik Spielstand speichern :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            //dummy -hilfsmthode
            saveGameState(ID);                 //speichert den spielstand
            break;

            case "load":
                ID = Long.parseLong(parts[1]); //ID wird in long umgewandelt
                send("ok");                //mit ok bestätigen

                //SPiellogik spielstandladen:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
                //Dummy - hilfsmethode
                loadGameState(ID);

                currentState = State.ENEMY_TURN; //muss ersetzt werden durch die Spiel logik :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            break;

            case "ok":
                if(currentState == State.WAITING_OK_SAVE){          //Speicher anfrage wurde bestätigt
                    currentState = previousState;                   //alter zustand wird wieder hergestellt
                } else if (currentState == State.WAITING_OK_LOAD)  //Client bestätigt load
                   currentState = State.MY_TURN;                   //muss auch ersetzt werden durch die spiel logik::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            break;
        }

        if (currentState == State.GAME_OVER)
        break;




           
            

	}

	// EOF ins Socket "schreiben" und das Programm explizit beenden
	// (weil es sonst weiterlaufen würde, bis der Benutzer das Hauptfenster 
	// schließt).
	s.shutdownOutput();
	System.out.println("Connection closed.");
	System.exit(0);
    }
}
