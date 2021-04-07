package eu.tomasznowicki.dvt.algorithm;

import eu.tomasznowicki.dvt.raport.ToStrings;

public class DNormalization {

    public static double DEFAULT_THRESHOLD = 0.5;

    public final long multiple;
    public final double raw, value, rest, threshold, recomendation;

    public DNormalization(double dRawValue, double dResolution, double dThreshold) {

        raw = dRawValue;

        if (dThreshold > 1) {
            threshold = 1;
        } else if (dThreshold < 0) {
            threshold = 0;
        } else {
            threshold = dThreshold;
        }
        
        if(dRawValue >0){
            multiple = Math.round(dRawValue / dResolution);
            value = multiple * dResolution;
            rest = dRawValue - value;
            recomendation = rest / dResolution > threshold ? value : value + dResolution;
        } else{
            multiple = 0;
            value = 0;
            rest = 0;
            recomendation = 0;
        }

    }

    public DNormalization(double dRawValue, double dResolution){
        
        this(dRawValue, dResolution, DEFAULT_THRESHOLD);
    }
    
    public static double getValue(double dRawValue, double dResolution) {

        return (new DNormalization(dRawValue, dResolution)).value;
    }

    public static double getRecomendation(double dRawValue, double dResolution, double dThreshold) {

        return (new DNormalization(dRawValue, dResolution,dThreshold)).recomendation;
    }

    public static double getRecomendation(double dRawValue, double dResolution) {

        return getRecomendation(dRawValue, dResolution, DEFAULT_THRESHOLD);
    }

    @Override
    public String toString() {

        return ToStrings.kitNormalizationToString(this);
    }

}

/*
public class DKitDose {

    public final long multiple;

    public final double raw, value, rest; //Znormalizowana do rozdzielczości i reszta


    public DKitDose(double insulin, double resolution) {

        raw = insulin;
        
        multiple = Math.round(insulin / resolution);

        if (multiple == 0) {
            
            value = 0;
            rest = insulin;
            
        } else {
            
            value = multiple * resolution;
            rest = insulin - value;
        }
    }
    
    @Override
    public String toString(){
        
        return ToStrings.fromKitDose(this);
    }
    
    
    

}
//    public static String fromKitDose(DKitDose dKitDose){
//        
//        return "raw=" + dKitDose.raw + " -> " + "value=" + dKitDose.value + " rest=" + dKitDose.rest;
//        
//    }


*/
