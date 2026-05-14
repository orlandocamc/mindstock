// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/interfaces/Prestable.java
//
// CONCEPTO POO: Interface
// Contrato para objetos que pueden ser prestados
// ============================================================
package com.ibero.mindstock.model.interfaces;

public interface Prestable {
    boolean estaDisponible();
    boolean retirar(int cantidad);
    void devolver(int cantidad);
}