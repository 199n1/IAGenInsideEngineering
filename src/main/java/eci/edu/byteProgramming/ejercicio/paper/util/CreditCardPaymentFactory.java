package eci.edu.byteProgramming.ejercicio.paper.util;

/**
 * Concrete Factory para pagos con tarjeta de crédito.
 * Implementa PaymentFactory y encapsula todos los parámetros
 * propios de la tarjeta, creando objetos coherentes entre sí.
 */
public class CreditCardPaymentFactory implements PaymentFactory {

    private final String number;
    private final String name;
    private final String expirationDate;
    private final String cvv;
    private final String address;

    public CreditCardPaymentFactory(String number, String name,
                                    String expirationDate, String cvv, String address) {
        this.number = number;
        this.name = name;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.address = address;
    }

    @Override
    public PaymentMethod createPaymentMethod(double amount, String customerId, String description) {
        return new CreditCardFactory(amount, customerId, description,
                number, name, expirationDate, cvv, address);
    }

    @Override
    public ValidatePayment createValidator() {
        return new CreditCardFactory(0.01, "VALIDATION", "Validator",
                number, name, expirationDate, cvv, address);
    }
}
