// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/interfaces/Notificable.java
//
// CONCEPTO POO: Interface
// Define un contrato: cualquier clase que implemente esto
// puede enviar notificaciones. Esto permite polimorfismo.
// ============================================================
package com.ibero.mindstock.model.interfaces;

public interface Notificable {
    void enviarNotificacion(String destinatario, String mensaje);
    boolean notificacionEnviada();
}