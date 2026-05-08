package eci.edu.byteProgramming.ejercicio.paper.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el sistema de pagos de la Tienda Virtual.
 * Cubre Abstract Factory, Observer, validaciones y flujo de estados.
 */
class auxiliaryTest {

    // -------------------------------------------------------------------------
    // Datos de prueba reutilizables
    // -------------------------------------------------------------------------
    private static final double VALID_AMOUNT    = 150.00;
    private static final String CUSTOMER_ID     = "CUST-001";
    private static final String DESCRIPTION     = "Compra de prueba";
    private static final String CUSTOMER_NAME   = "Ana García";
    private static final String CUSTOMER_EMAIL  = "ana@example.com";
    private static final String PRODUCT_ID      = "LAPTOP001";

    // Datos válidos de tarjeta
    private static final String CARD_NUMBER     = "4111111111111111";
    private static final String CARD_NAME       = "ANA GARCIA";
    private static final String CARD_EXPIRY     = "12/26";
    private static final String CARD_CVV        = "123";
    private static final String CARD_ADDRESS    = "Calle 100 #45-30";

    // Datos válidos de PayPal
    private static final String PAYPAL_EMAIL    = "ana@paypal.com";
    private static final String PAYPAL_TOKEN    = "TOKEN_VALIDO_12345";

    // Datos válidos de crypto
    private static final String WALLET_ADDRESS  = "1A2B3C4D5E6F7G8H9I0J1K2L3M4N5O6P";
    private static final String CRYPTO_TYPE     = "BTC";
    private static final double WALLET_BALANCE  = 500.00;

    // =========================================================================
    // 1. PRUEBAS DE ABSTRACT FACTORY — creación de objetos
    // =========================================================================

    @Test
    @DisplayName("CreditCardPaymentFactory crea un PaymentMethod con los datos correctos")
    void testCreditCardFactoryCreatesPaymentMethod() {
        PaymentFactory factory = new CreditCardPaymentFactory(
                CARD_NUMBER, CARD_NAME, CARD_EXPIRY, CARD_CVV, CARD_ADDRESS);

        PaymentMethod payment = factory.createPaymentMethod(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION);

        assertNotNull(payment, "El método de pago no debe ser nulo");
        assertEquals("CREDIT_CARD", payment.getPaymentMethod());
        assertEquals(VALID_AMOUNT, payment.getAmount());
        assertEquals(CUSTOMER_ID, payment.getCustomerId());
        assertEquals(DESCRIPTION, payment.getDescription());
        assertNotNull(payment.getTransactionId(), "El ID de transacción debe generarse automáticamente");
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    @DisplayName("PaypalPaymentFactory crea un PaymentMethod con los datos correctos")
    void testPaypalFactoryCreatesPaymentMethod() {
        PaymentFactory factory = new PaypalPaymentFactory(PAYPAL_EMAIL, PAYPAL_TOKEN);

        PaymentMethod payment = factory.createPaymentMethod(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION);

        assertNotNull(payment);
        assertEquals("PAYPAL", payment.getPaymentMethod());
        assertEquals(VALID_AMOUNT, payment.getAmount());
        assertEquals(CUSTOMER_ID, payment.getCustomerId());
    }

    @Test
    @DisplayName("CryptoPaymentFactory crea un PaymentMethod con los datos correctos")
    void testCryptoFactoryCreatesPaymentMethod() {
        PaymentFactory factory = new CryptoPaymentFactory(WALLET_ADDRESS, CRYPTO_TYPE, WALLET_BALANCE);

        PaymentMethod payment = factory.createPaymentMethod(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION);

        assertNotNull(payment);
        assertEquals("CRYPTOCURRENCY", payment.getPaymentMethod());
        assertEquals(VALID_AMOUNT, payment.getAmount());
    }

    @Test
    @DisplayName("Cada fábrica crea instancias distintas (no singleton)")
    void testFactoryCreatesNewInstancesEachTime() {
        PaymentFactory factory = new PaypalPaymentFactory(PAYPAL_EMAIL, PAYPAL_TOKEN);

        PaymentMethod p1 = factory.createPaymentMethod(100.0, "C1", "Desc1");
        PaymentMethod p2 = factory.createPaymentMethod(200.0, "C2", "Desc2");

        assertNotSame(p1, p2, "Cada llamada debe retornar una nueva instancia");
        assertNotEquals(p1.getTransactionId(), p2.getTransactionId(),
                "Cada transacción debe tener ID único");
    }

    @Test
    @DisplayName("createValidator retorna un ValidatePayment no nulo")
    void testFactoryCreatesValidator() {
        PaymentFactory factory = new CreditCardPaymentFactory(
                CARD_NUMBER, CARD_NAME, CARD_EXPIRY, CARD_CVV, CARD_ADDRESS);

        ValidatePayment validator = factory.createValidator();

        assertNotNull(validator, "El validador no debe ser nulo");
    }

    // =========================================================================
    // 2. PRUEBAS DE VALIDACIÓN — validatePaymentMethod()
    // =========================================================================

    @Test
    @DisplayName("Tarjeta de crédito válida pasa la validación")
    void testCreditCardValidationPasses() {
        CreditCardFactory card = new CreditCardFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CARD_NUMBER, CARD_NAME, CARD_EXPIRY, CARD_CVV, CARD_ADDRESS);

        assertTrue(card.validatePaymentMethod(), "La tarjeta con datos válidos debe pasar la validación");
    }

    @Test
    @DisplayName("Tarjeta de crédito con CVV inválido falla la validación")
    void testCreditCardInvalidCvvFails() {
        CreditCardFactory card = new CreditCardFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CARD_NUMBER, CARD_NAME, CARD_EXPIRY, "12", CARD_ADDRESS); // CVV muy corto

        assertFalse(card.validatePaymentMethod(), "CVV de 2 dígitos debe fallar la validación");
    }

    @Test
    @DisplayName("Tarjeta con fecha de expiración en formato incorrecto falla")
    void testCreditCardInvalidExpiryFails() {
        CreditCardFactory card = new CreditCardFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CARD_NUMBER, CARD_NAME, "2026-12", CARD_CVV, CARD_ADDRESS); // Formato incorrecto

        assertFalse(card.validatePaymentMethod(), "Formato de fecha incorrecto debe fallar");
    }

    @Test
    @DisplayName("PayPal con email inválido falla la validación")
    void testPaypalInvalidEmailFails() {
        PaypalFactory paypal = new PaypalFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                "correo-sin-arroba", PAYPAL_TOKEN);

        assertFalse(paypal.validatePaymentMethod(), "Email sin '@' debe fallar la validación");
    }

    @Test
    @DisplayName("PayPal con token válido pasa la validación")
    void testPaypalValidationPasses() {
        PaypalFactory paypal = new PaypalFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                PAYPAL_EMAIL, PAYPAL_TOKEN);

        assertTrue(paypal.validatePaymentMethod(), "PayPal con datos válidos debe pasar");
    }

    @Test
    @DisplayName("Cripto con saldo suficiente pasa la validación")
    void testCryptoValidationPasses() {
        CryptoFactory crypto = new CryptoFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                WALLET_ADDRESS, CRYPTO_TYPE, WALLET_BALANCE);

        assertTrue(crypto.validatePaymentMethod(), "Crypto con saldo suficiente debe pasar");
    }

    @Test
    @DisplayName("Cripto con saldo insuficiente falla la validación")
    void testCryptoInsufficientBalanceFails() {
        CryptoFactory crypto = new CryptoFactory(600.00, CUSTOMER_ID, DESCRIPTION,
                WALLET_ADDRESS, CRYPTO_TYPE, 100.00); // balance < amount

        assertFalse(crypto.validatePaymentMethod(), "Saldo insuficiente debe fallar");
    }

    @Test
    @DisplayName("Cripto con dirección de billetera inválida falla la validación")
    void testCryptoInvalidWalletFails() {
        CryptoFactory crypto = new CryptoFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                "CORTA", CRYPTO_TYPE, WALLET_BALANCE); // dirección demasiado corta

        assertFalse(crypto.validatePaymentMethod(), "Dirección de wallet demasiado corta debe fallar");
    }

    // =========================================================================
    // 3. PRUEBAS DE PAYMENTMETHOD — estado y datos de transacción
    // =========================================================================

    @Test
    @DisplayName("El ID de cliente se asigna correctamente en el constructor (bug corregido)")
    void testCustomerIdAssignedCorrectly() {
        PaypalFactory paypal = new PaypalFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                PAYPAL_EMAIL, PAYPAL_TOKEN);

        assertEquals(CUSTOMER_ID, paypal.getCustomerId(),
                "getCustomerId() debe retornar el ID pasado en el constructor");
    }

    @Test
    @DisplayName("El estado inicial de un pago es PENDING")
    void testInitialStatusIsPending() {
        CreditCardFactory card = new CreditCardFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CARD_NUMBER, CARD_NAME, CARD_EXPIRY, CARD_CVV, CARD_ADDRESS);

        assertEquals(PaymentStatus.PENDING, card.getStatus());
    }

    @Test
    @DisplayName("setStatus cambia el estado correctamente")
    void testStatusTransition() {
        CreditCardFactory card = new CreditCardFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CARD_NUMBER, CARD_NAME, CARD_EXPIRY, CARD_CVV, CARD_ADDRESS);

        card.setStatus(PaymentStatus.COMPLETED);

        assertEquals(PaymentStatus.COMPLETED, card.getStatus());
    }

    @Test
    @DisplayName("El timestamp no es nulo al crear un pago")
    void testTimestampIsSet() {
        PaypalFactory paypal = new PaypalFactory(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                PAYPAL_EMAIL, PAYPAL_TOKEN);

        assertNotNull(paypal.getTimestamp());
    }

    // =========================================================================
    // 4. PRUEBAS DEL PATRÓN OBSERVER — ECIPayment + PaymentEventObserver
    // =========================================================================

    @Test
    @DisplayName("Observer es notificado con onPaymentSuccess cuando el pago es válido")
    void testObserverNotifiedOnSuccess() {
        // Arrange
        PaymentObserver mockObserver = mock(PaymentObserver.class);
        ECIPayment eciPayment = new ECIPayment();
        eciPayment.addObserver(mockObserver);

        PaymentFactory factory = new PaypalPaymentFactory(PAYPAL_EMAIL, PAYPAL_TOKEN);

        // Act — PayPal válido debe completarse
        eciPayment.processPayment(factory, VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CUSTOMER_NAME, CUSTOMER_EMAIL, PRODUCT_ID);

        // Assert
        verify(mockObserver, times(1))
                .onPaymentSuccess(any(PaymentMethod.class), eq(CUSTOMER_NAME),
                        eq(CUSTOMER_EMAIL), eq(PRODUCT_ID));
        verify(mockObserver, never())
                .onPaymentFailed(any(), any());
    }

    @Test
    @DisplayName("Observer es notificado con onPaymentFailed cuando el pago es inválido")
    void testObserverNotifiedOnFailure() {
        // Arrange
        PaymentObserver mockObserver = mock(PaymentObserver.class);
        ECIPayment eciPayment = new ECIPayment();
        eciPayment.addObserver(mockObserver);

        // PayPal con token corto → falla validación
        PaymentFactory factory = new PaypalPaymentFactory("bad-email", "SHORT");

        // Act
        eciPayment.processPayment(factory, VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CUSTOMER_NAME, CUSTOMER_EMAIL, PRODUCT_ID);

        // Assert
        verify(mockObserver, times(1))
                .onPaymentFailed(any(PaymentMethod.class), eq(CUSTOMER_EMAIL));
        verify(mockObserver, never())
                .onPaymentSuccess(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Múltiples observers son notificados correctamente")
    void testMultipleObserversNotified() {
        PaymentObserver observer1 = mock(PaymentObserver.class);
        PaymentObserver observer2 = mock(PaymentObserver.class);
        ECIPayment eciPayment = new ECIPayment();
        eciPayment.addObserver(observer1);
        eciPayment.addObserver(observer2);

        PaymentFactory factory = new PaypalPaymentFactory(PAYPAL_EMAIL, PAYPAL_TOKEN);
        eciPayment.processPayment(factory, VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CUSTOMER_NAME, CUSTOMER_EMAIL, PRODUCT_ID);

        verify(observer1, times(1)).onPaymentSuccess(any(), any(), any(), any());
        verify(observer2, times(1)).onPaymentSuccess(any(), any(), any(), any());
    }

    @Test
    @DisplayName("removeObserver evita que el observer eliminado sea notificado")
    void testRemovedObserverNotNotified() {
        PaymentObserver observer = mock(PaymentObserver.class);
        ECIPayment eciPayment = new ECIPayment();
        eciPayment.addObserver(observer);
        eciPayment.removeObserver(observer);

        PaymentFactory factory = new PaypalPaymentFactory(PAYPAL_EMAIL, PAYPAL_TOKEN);
        eciPayment.processPayment(factory, VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION,
                CUSTOMER_NAME, CUSTOMER_EMAIL, PRODUCT_ID);

        verify(observer, never()).onPaymentSuccess(any(), any(), any(), any());
        verify(observer, never()).onPaymentFailed(any(), any());
    }

    // =========================================================================
    // 5. PRUEBAS DE INVENTARIO
    // =========================================================================

    @Test
    @DisplayName("Inventory descuenta stock correctamente al comprar")
    void testInventoryDiscountsStock() {
        Inventory inventory = new Inventory();
        int stockBefore = inventory.getStock(PRODUCT_ID);

        boolean result = inventory.discountProduct(PRODUCT_ID, 1);

        assertTrue(result, "El descuento debe ser exitoso si hay stock");
        assertEquals(stockBefore - 1, inventory.getStock(PRODUCT_ID));
    }

    @Test
    @DisplayName("Inventory retorna false si no hay stock suficiente")
    void testInventoryInsufficientStock() {
        Inventory inventory = new Inventory();

        boolean result = inventory.discountProduct(PRODUCT_ID, 999);

        assertFalse(result, "Debe retornar false si no hay stock suficiente");
    }

    @Test
    @DisplayName("Inventory.getProduct retorna el producto correcto")
    void testInventoryGetProduct() {
        Inventory inventory = new Inventory();

        Product product = inventory.getProduct(PRODUCT_ID);

        assertNotNull(product, "El producto debe existir");
        assertEquals(PRODUCT_ID, product.getProductId());
        assertEquals("Gaming Laptop", product.getName());
    }

    @Test
    @DisplayName("Inventory.getProduct retorna null para ID inexistente")
    void testInventoryProductNotFound() {
        Inventory inventory = new Inventory();

        Product product = inventory.getProduct("NO_EXISTE");

        assertNull(product, "Debe retornar null para un producto inexistente");
    }

    // =========================================================================
    // 6. PRUEBAS DE PAYMENTEVENTOBSERVER — integración Observer + módulos
    // =========================================================================

    @Test
    @DisplayName("PaymentEventObserver descuenta inventario al recibir onPaymentSuccess")
    void testPaymentEventObserverUpdatesInventory() {
        Inventory inventory = new Inventory();
        Facturation facturation = new Facturation();
        Notification notification = new Notification();
        PaymentEventObserver observer = new PaymentEventObserver(inventory, facturation, notification);

        PaymentMethod payment = new PaypalPaymentFactory(PAYPAL_EMAIL, PAYPAL_TOKEN)
                .createPaymentMethod(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION);
        payment.setStatus(PaymentStatus.COMPLETED);

        int stockBefore = inventory.getStock(PRODUCT_ID);
        observer.onPaymentSuccess(payment, CUSTOMER_NAME, CUSTOMER_EMAIL, PRODUCT_ID);

        assertEquals(stockBefore - 1, inventory.getStock(PRODUCT_ID),
                "El stock debe disminuir en 1 tras un pago exitoso");
    }

    @Test
    @DisplayName("PaymentEventObserver no falla si el producto no existe en inventario")
    void testPaymentEventObserverHandlesMissingProduct() {
        Inventory inventory = new Inventory();
        Facturation facturation = new Facturation();
        Notification notification = new Notification();
        PaymentEventObserver observer = new PaymentEventObserver(inventory, facturation, notification);

        PaymentMethod payment = new PaypalPaymentFactory(PAYPAL_EMAIL, PAYPAL_TOKEN)
                .createPaymentMethod(VALID_AMOUNT, CUSTOMER_ID, DESCRIPTION);

        assertDoesNotThrow(() ->
                observer.onPaymentSuccess(payment, CUSTOMER_NAME, CUSTOMER_EMAIL, "PRODUCTO_INEXISTENTE"),
                "No debe lanzar excepción si el producto no existe");
    }

    // =========================================================================
    // 7. PRUEBAS DE PAYMENTSTATUS — enum
    // =========================================================================

    @Test
    @DisplayName("PaymentStatus contiene todos los estados esperados")
    void testPaymentStatusValues() {
        assertEquals("Pendiente",   PaymentStatus.PENDING.getName());
        assertEquals("Procesando",  PaymentStatus.PROCESSING.getName());
        assertEquals("Completado",  PaymentStatus.COMPLETED.getName());
        assertEquals("Fallido",     PaymentStatus.FAILED.getName());
        assertEquals("Cancelado",   PaymentStatus.CANCELED.getName());
    }
}
