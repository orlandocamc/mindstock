// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/interfaces/Detectable.java
//
// CONCEPTO POO: Interface
// Contrato para objetos que pueden ser detectados por la IA
// ============================================================
package com.ibero.mindstock.model.interfaces;

public interface Detectable {
    String getIdentificadorVisual();
    boolean coincideCon(String labelDetectado, double confidence);
}