 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tomasznowicki.dvt.time;

import java.util.Arrays;

/**
 *
 * @author tomek
 */
public class DUtils {
    
    public static void setDoubleDAY(double[] sens, double... dSettings) {
        Arrays.fill(sens, 0);
        System.arraycopy(dSettings, 0, sens, 0, Math.min(dSettings.length, sens.length));
        for (int i = 0; i < sens.length; i++) {
            if (sens[i] < 0) {
                sens[i] = 0;
            }
        }
    }
    
}
