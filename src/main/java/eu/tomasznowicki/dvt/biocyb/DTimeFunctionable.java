package eu.tomasznowicki.dvt.biocyb;

import eu.tomasznowicki.dvt.time.DDiscreteTimeFunction;
import eu.tomasznowicki.dvt.time.DTimeStamp;

/**
 * Osadza wielomian w czasie symulacji. Bolus oraz Meal nie używają Eventów,
 * eventy są dopiero w pacjecie.
 * 
 *
 * @author www.tomasznowicki.eu
 */
public interface DTimeFunctionable {

    /**
     *
     * @param dTime Czas w którym ma być podana wartość
     * @return Wartość z przedziału [minuta poprzednia, ta minuta]
     */
    double valueAt(DTimeStamp dTime); //-------------> Do przewidywania

    /**
     * Ile już zostało wypuszczone. Tutaj horyzont to początek Eventu.
     *
     * @param dTime
     * @return
     */
    double pastAt(DTimeStamp dTime); //------------------> Do przewidywania

    /**
     * Ile zostało wypuszczone od wskazanej chwili do "horyzont" minut wstecz.
     *
     * @param dTime
     * @param horizont
     * @return
     */
    double pastAt(DTimeStamp dTime, int horizont); // -----------------> Do przewidywania

    /**
     * Ile zostało do wypuszczenia od wskazanej chwili do końca Eventu
     *
     * @param dTime
     * @return
     */
    double futureAt(DTimeStamp dTime); // -------------------> Do przewidywania

    /**
     * Ile zostało do wypszczenie od wskazanej chwili do "horyzont" minut do
     * przodu.
     *
     * @param dTime
     * @param horizont
     * @return
     */
    double futureAt(DTimeStamp dTime, int horizont); //--------------> Do przewidywania

    /**
     * Przeliczenie podanego czasu currTime na czas w dystrybucji
     *
     * @param startTime Start time wielomianu
     * @param currTime Czas symulacji
     * @return Minuta w wielomianie. Bo tam się liczy od zera.
     */
    static int eventMinute(DTimeStamp startTime, DTimeStamp currTime) {
        return (int) (currTime.total - startTime.total);
    }
    
    
    public DDiscreteTimeFunction toTimeFunction();

}

//Zrobić skwantowane wg jakieś rozdzielczości, bo to później doctor przyrównuje do zera
