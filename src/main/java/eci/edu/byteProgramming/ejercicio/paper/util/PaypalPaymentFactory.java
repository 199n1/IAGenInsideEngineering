package eci.edu.byteProgramming.ejercicio.paper.util;

/**
 * Concrete Factory para pagos con PayPal.
 * Implementa PaymentFactory y encapsula las credenciales de PayPal,
 * creando objetos coherentes entre sí.
 */
public class PaypalPaymentFactory implements PaymentFactory {

    private final String email;
    private final String authToken;

    public PaypalPaymentFactory(String email, String authToken) {
        this.email = email;
        this.authToken = authToken;
    }

    @Override
    public PaymentMethod createPaymentMethod(double amount, String customerId, String description) {
        return new PaypalFactory(amount, customerId, description, email, authToken);
    }

    @Override
    public ValidatePayment createValidator() {
        return new PaypalFactory(0.01, "VALIDATION", "Validator", email, authToken);
    }
}
