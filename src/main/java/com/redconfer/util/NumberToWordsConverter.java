package com.redconfer.util;

public class NumberToWordsConverter {
    private static final String[] UNIDADES = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"};
    private static final String[] DECENAS = {"", "DIEZ", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
    private static final String[] DIECIS = {"DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE"};
    private static final String[] VEINTES = {"VEINTE", "VEINTIUNO", "VEINTIDOS", "VEINTITRES", "VEINTICUATRO", "VEINTICINCO", "VEINTISEIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"};
    private static final String[] CENTENAS = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};

    public static String convert(double number) {
        long wholePart = (long) number;
        int cents = (int) Math.round((number - wholePart) * 100);
        
        if (wholePart == 0) {
            return "CERO PESOS M/CTE";
        }
        
        String words = convertToString(wholePart);
        
        // Custom adjustments for plural / singular
        if (words.trim().equals("UN")) {
            words = "UN PESO";
        } else {
            words = words + " PESOS";
        }
        
        if (cents > 0) {
            words += String.format(" CON %02d/100 M/CTE", cents);
        } else {
            words += " M/CTE";
        }
        
        return words.replaceAll("\\s+", " ").trim();
    }

    private static String convertToString(long number) {
        if (number == 0) {
            return "";
        }
        
        if (number < 10) {
            return UNIDADES[(int) number];
        }
        if (number < 20) {
            return DIECIS[(int) (number - 10)];
        }
        if (number < 30) {
            return VEINTES[(int) (number - 20)];
        }
        if (number < 100) {
            long u = number % 10;
            long d = number / 10;
            if (u == 0) {
                return DECENAS[(int) d];
            } else {
                return DECENAS[(int) d] + " Y " + UNIDADES[(int) u];
            }
        }
        if (number < 1000) {
            if (number == 100) {
                return "CIEN";
            }
            long rest = number % 100;
            long c = number / 100;
            return CENTENAS[(int) c] + " " + convertToString(rest);
        }
        if (number < 1000000) {
            long thousandPart = number / 1000;
            long rest = number % 1000;
            
            String thousandStr = "";
            if (thousandPart == 1) {
                thousandStr = "MIL";
            } else {
                thousandStr = convertToString(thousandPart) + " MIL";
            }
            return thousandStr + " " + convertToString(rest);
        }
        if (number < 1000000000L) {
            long millionPart = number / 1000000;
            long rest = number % 1000000;
            
            String millionStr = "";
            if (millionPart == 1) {
                millionStr = "UN MILLÓN";
            } else {
                millionStr = convertToString(millionPart) + " MILLONES";
            }
            return millionStr + " " + convertToString(rest);
        }
        
        return String.valueOf(number); // Fallback for extremely large numbers
    }
}
