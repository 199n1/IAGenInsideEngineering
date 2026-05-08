package eci.edu.byteProgramming.ejercicio.paper.util;

/**
 * Abstract Factory interface — define el contrato para crear familias de objetos
 * relacionados: un método de pago y su validador correspondiente.
 *
 * Cumple OCP: agregar un nuevo método de pago sólo requiere una nueva
 * implementación de esta interfaz; ECIPayment no necesita modificarse.
 */
public interface PaymentFactory {

    /**
     * Crea el objeto de pago configurado con los parámetros de la transacción.
     */
    PaymentMethod createPaymentMethod(double amount, String customerId, String description);

    /**
     * Crea el validador asociado al tipo de pago de esta fábrica.
     * Permite validar credenciales de forma independiente al procesamiento.
     */
    ValidatePayment createValidator();
}
