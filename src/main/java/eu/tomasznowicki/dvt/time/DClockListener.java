
package eu.tomasznowicki.dvt.time;


public interface DClockListener {
        
    public void syncReceived(DClockEventSync event); // -> Ustawia czas
    
    public void tickCGM(DClockEventAction action); //-> Uruchamia cgm
    
    
    public void tickController(DClockEventAction action); //-> Uruchamia kontroler
    public void tickOutfit(DClockEventAction action); //-> Uruchamia melabox i kit
    public void tickPatient(DClockEventAction action); // -> Uruchamia pacjeta
}