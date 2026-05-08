package eci.edu.byteProgramming.ejercicio.paper.util;

/**
 * Concrete Factory para pagos con criptomoneda.
 * Implementa PaymentFactory y encapsula los datos de la billetera,
 * creando objetos coherentes entre sí.
 */
public class CryptoPaymentFactory implements PaymentFactory {

    private final String walletAddress;
    private final String cryptoType;
    private final double walletBalance;

    public CryptoPaymentFactory(String walletAddress, String cryptoType, double walletBalance) {
        this.walletAddress = walletAddress;
        this.cryptoType = cryptoType;
        this.walletBalance = walletBalance;
    }

    @Override
    public PaymentMethod createPaymentMethod(double amount, String customerId, String description) {
        return new CryptoFactory(amount, customerId, description, walletAddress, cryptoType, walletBalance);
    }

    @Override
    public ValidatePayment createValidator() {
        return new CryptoFactory(0.01, "VALIDATION", "Validator", walletAddress, cryptoType, walletBalance);
    }
}
