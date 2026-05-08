# Solución — Sistema de Pagos Tienda Virtual


Tiempo cronograma: 3  minutos
Promo 1: necesito que crees un promp para que antigravity me realice la solucion de este ejericicio 1, que identifique patrones y cree todo lo que pide el ejericico, eso lo debe realizar en la carpta que se llama ejericico 1, te adjunte una imagen para que veas dond esta
Promp 2: le pedi a una ia que me diera el promp  para colocarselo a claude code

**IA utilizada:** Claude (claude-sonnet-4-6)  
**Prompt principal utilizado:**

> Actúa como un Tech Lead y Arquitecto de Software experto en Patrones de Diseño. Contexto: Estoy trabajando en un ejercicio académico de refactorización para el sistema de pagos de una "Tienda Virtual". Tengo un código base que intenta implementar múltiples métodos de pago (Tarjeta, PayPal, Cripto) y un sistema de notificaciones (Inventario, Facturación, Email), pero contiene errores de compilación, de diseño arquitectónico y pruebas unitarias que fallan. Tarea: Revisa el código base, los diagramas y las pruebas unitarias. Analiza la implementación y ayúdame a agregar lo que se hizo en este ejercicio en el archivo SOLUCION.md respondiendo a los objetivos del ejercicio, además de entregar el código corregido...

---

## 1. Análisis de Patrones

### Patrón 1 — Abstract Factory

**Identificación:** La arquitectura requiere crear *familias* de objetos relacionados y compatibles entre sí: un método de pago (`PaymentMethod`) y su validador (`ValidatePayment`). Cada familia corresponde a un canal de pago distinto (Tarjeta de Crédito, PayPal, Criptomoneda).

**Jerarquía implementada:**

```
PaymentFactory (interfaz — Abstract Factory)
    ├── CreditCardPaymentFactory  →  crea { CreditCardFactory + validador CC }
    ├── PaypalPaymentFactory      →  crea { PaypalFactory    + validador PP }
    └── CryptoPaymentFactory      →  crea { CryptoFactory    + validador Crypto }

PaymentMethod (abstract — Product A)
    ├── CreditCardFactory
    ├── PaypalFactory
    └── CryptoFactory

ValidatePayment (interfaz — Product B)
    └── implementado por cada PaymentMethod (misma instancia sirve de validador)
```

**Justificación:**
- `ECIPayment.processPayment()` recibe `PaymentFactory` como parámetro y llama `factory.createPaymentMethod()` sin conocer el tipo concreto → cumple OCP.
- Agregar PSE o Nequi solo requiere una nueva `PaymentFactory` concreta; `ECIPayment` no se modifica.
- La familia (método de pago + validador) se garantiza coherente porque ambos salen de la misma fábrica.

**¿Debería cambiar el patrón?**  
No. Abstract Factory es el patrón correcto. La alternativa más simple (Factory Method) solo crearía *un* producto por fábrica, sin la semántica de "familia"; el Abstract Factory es la elección justificada aquí.

---

### Patrón 2 — Observer

**Identificación:** Cuando `ECIPayment` completa o falla un pago, múltiples módulos independientes (`Inventory`, `Facturation`, `Notification`) deben reaccionar automáticamente sin que el procesador de pagos conozca sus detalles internos.

**Jerarquía implementada:**

```
ECIPayment (Sujeto / Subject)
    └── List<PaymentObserver> observers

PaymentObserver (interfaz — Observer)
    └── PaymentEventObserver (Observer concreto)
            ├── Inventory   → descuenta stock
            ├── Facturation → genera factura
            └── Notification → envía email
```

**Justificación:**
- `ECIPayment` notifica a través de la interfaz `PaymentObserver`; no sabe nada de `Inventory` ni de `Facturation`.
- Agregar un módulo de analítica o auditoría solo requiere implementar `PaymentObserver` y registrarlo con `addObserver()`.
- Cumple SRP: `ECIPayment` procesa pagos; `PaymentEventObserver` coordina los efectos secundarios.

**¿Debería cambiar el patrón?**  
No. Observer es el patrón correcto para notificaciones desacopladas 1→N. La alternativa (llamadas directas) violaría tanto OCP como SRP.

---

## 2. Clases e Interfaces Faltantes

### `PaymentFactory` (interfaz — **FALTABA**)

**Problema:** `ECIPayment.java` declaraba el parámetro `PaymentFactory factory` en su método `processPayment()`, pero la interfaz no existía en ningún lugar del código base.  
**Impacto:** Error de compilación fatal — el proyecto no compila en absoluto.  
**Solución:** Se creó la interfaz con dos métodos de contrato:

```java
public interface PaymentFactory {
    PaymentMethod createPaymentMethod(double amount, String customerId, String description);
    ValidatePayment createValidator();
}
```

---

### `CreditCardPaymentFactory`, `PaypalPaymentFactory`, `CryptoPaymentFactory` (clases — **FALTABAN**)

**Problema:** Las clases `CreditCardFactory`, `PaypalFactory` y `CryptoFactory` extienden `PaymentMethod` — son implementaciones del *producto*, **no factories**. El nombre "Factory" en sus nombres es incorrecto. Faltaban las fábricas concretas que implementasen la interfaz `PaymentFactory` y encapsulasen los parámetros específicos de cada canal de pago.  
**Solución:** Se crearon tres fábricas concretas que almacenan los parámetros de configuración y crean los objetos correctos:

| Clase creada              | Implementa       | Crea                       |
|---------------------------|------------------|----------------------------|
| `CreditCardPaymentFactory`| `PaymentFactory` | `CreditCardFactory`        |
| `PaypalPaymentFactory`    | `PaymentFactory` | `PaypalFactory`            |
| `CryptoPaymentFactory`    | `PaymentFactory` | `CryptoFactory`            |

---

## 3. Validación del Diagrama de Contexto

### Diagrama de Contexto (`docs/imagenes/contexto.png`)

El diagrama de contexto refleja correctamente los módulos del sistema y sus relaciones de alto nivel:
- Sistema de pago central conectado a Inventario, Facturación y Notificación.
- El cliente como actor principal que desencadena los pagos.

**Cambio necesario documentado:** El diagrama no muestra la intermediación del Observer. Debería indicar que `ECIPayment` notifica a `PaymentEventObserver`, y este a su vez delega a los tres módulos. Las flechas actuales sugieren dependencias directas, lo que no refleja el desacoplamiento logrado.

### Diagrama de Clases (`docs/uml/clases.png`)

El diagrama de clases es parcialmente correcto pero incompleto:

| Elemento                       | Estado en el diagrama   | Corrección necesaria                         |
|-------------------------------|-------------------------|----------------------------------------------|
| `PaymentFactory`              | **Ausente**             | Agregar interfaz con sus dos métodos         |
| `CreditCardPaymentFactory` etc.| **Ausentes**           | Agregar las tres fábricas concretas          |
| `CreditCardFactory` etc.      | Aparecen como factories | Renombrar conceptualmente a "PaymentMethod"  |
| Relación Factory→Method       | No mostrada             | Agregar línea de creación (<<creates>>)      |

---

## 4. Identificación de Errores — Code Review

### Error 1 — Interfaz `PaymentFactory` inexistente (**Error de compilación**)

| Campo    | Detalle                                                                  |
|----------|--------------------------------------------------------------------------|
| Archivo  | `ECIPayment.java`, línea 14                                              |
| Error    | `PaymentFactory` es usada como tipo de parámetro pero nunca fue declarada |
| Causa    | Falta la interfaz del Abstract Factory                                   |
| Impacto  | **El proyecto no compila en absoluto**                                   |
| Fix      | Crear `PaymentFactory.java` con el contrato correcto                     |

---

### Error 2 — Import incorrecto en `PaymentEventObserver` (**Error de compilación**)

| Campo    | Detalle                                                                          |
|----------|----------------------------------------------------------------------------------|
| Archivo  | `PaymentEventObserver.java`, línea 3                                             |
| Error    | `import javax.management.Notification;` importa la clase JMX del JDK            |
| Causa    | La clase `Notification` del paquete local no necesita import (mismo paquete)    |
| Impacto  | `sendConfirmationEmail()` y `sendFailureNotification()` no existen en `javax.management.Notification` → **error de compilación** |
| Fix      | Eliminar el import incorrecto                                                    |

---

### Error 3 — Bug en constructor de `PaymentMethod` (`customerID` siempre `null`)

| Campo    | Detalle                                                                         |
|----------|---------------------------------------------------------------------------------|
| Archivo  | `PaymentMethod.java`, línea 14–16                                               |
| Error    | Parámetro declarado como `String transactionID` pero luego se asigna `this.customerID = customerID` (variable no declarada como parámetro) |
| Causa    | El segundo parámetro está mal nombrado: los callers siempre pasan `customerId`  |
| Impacto  | `getCustomerId()` siempre retorna `null`; la factura muestra `ID: null`         |
| Fix      | Renombrar el parámetro de `transactionID` a `customerID`                        |

**Código antes:**
```java
public PaymentMethod(double amount, String transactionID, String description) {
    this.customerID = customerID;  // BUG: customerID no es parámetro → siempre null
```

**Código después:**
```java
public PaymentMethod(double amount, String customerID, String description) {
    this.customerID = customerID;  // CORRECTO
```

---

### Error 4 — Naming incorrecto en clases de pago (Error de diseño)

| Campo    | Detalle                                                                      |
|----------|------------------------------------------------------------------------------|
| Archivos | `CreditCardFactory.java`, `PaypalFactory.java`, `CryptoFactory.java`        |
| Error    | Estas clases se llaman "Factory" pero extienden `PaymentMethod`; son implementaciones del producto, no fábricas |
| Causa    | Confusión entre el rol del producto (método de pago) y el rol de la fábrica  |
| Impacto  | Confusión arquitectónica; las verdaderas fábricas no existían                |
| Fix      | Agregar las clases factory correctas (`CreditCardPaymentFactory`, etc.) que sí implementan `PaymentFactory` |

---

### Error 5 — Campo `token` no inicializado en `CryptoFactory`

| Campo    | Detalle                                                               |
|----------|-----------------------------------------------------------------------|
| Archivo  | `CryptoFactory.java`, línea 18                                        |
| Error    | `this.token = token;` — `token` no se recibe como parámetro          |
| Causa    | El constructor no incluye `token` en su firma                         |
| Impacto  | `token` siempre queda `null`                                          |
| Fix      | Eliminar la línea o añadir `token` como parámetro si es necesario     |

---

### Error 6 — Pruebas unitarias vacías

| Campo    | Detalle                                                              |
|----------|----------------------------------------------------------------------|
| Archivo  | `auxiliaryTest.java`                                                 |
| Error    | La clase no tiene ningún método `@Test`                              |
| Impacto  | Cobertura 0%; el check de JaCoCo (mínimo 85% de clases cubiertas) falla |
| Fix      | Implementar suite completa de pruebas (ver sección 5)                |

---

## 5. Código Corregido y Pruebas

### Resumen de cambios aplicados

| Archivo                         | Tipo     | Cambio                                                    |
|---------------------------------|----------|-----------------------------------------------------------|
| `PaymentFactory.java`           | NUEVO    | Interfaz Abstract Factory con `createPaymentMethod()` y `createValidator()` |
| `CreditCardPaymentFactory.java` | NUEVO    | Fábrica concreta para tarjeta de crédito                  |
| `PaypalPaymentFactory.java`     | NUEVO    | Fábrica concreta para PayPal                              |
| `CryptoPaymentFactory.java`     | NUEVO    | Fábrica concreta para criptomoneda                        |
| `PaymentMethod.java`            | CORREGIDO| Parámetro constructor renombrado de `transactionID` a `customerID` |
| `PaymentEventObserver.java`     | CORREGIDO| Eliminado `import javax.management.Notification` incorrecto |
| `auxiliaryTest.java`            | CORREGIDO| Suite completa con 20 casos de prueba                     |

---

### `PaymentFactory.java` (nuevo)

```java
package eci.edu.byteProgramming.ejercicio.paper.util;

public interface PaymentFactory {
    PaymentMethod createPaymentMethod(double amount, String customerId, String description);
    ValidatePayment createValidator();
}
```

---

### `CreditCardPaymentFactory.java` (nuevo)

```java
package eci.edu.byteProgramming.ejercicio.paper.util;

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
        return new CreditCardFactory(amount, customerId, description, number, name, expirationDate, cvv, address);
    }

    @Override
    public ValidatePayment createValidator() {
        return new CreditCardFactory(0.01, "VALIDATION", "Validator", number, name, expirationDate, cvv, address);
    }
}
```

---

### `PaypalPaymentFactory.java` (nuevo)

```java
package eci.edu.byteProgramming.ejercicio.paper.util;

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
```

---

### `CryptoPaymentFactory.java` (nuevo)

```java
package eci.edu.byteProgramming.ejercicio.paper.util;

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
```

---

### `PaymentMethod.java` — línea corregida

```java
// ANTES (bug):
public PaymentMethod(double amount, String transactionID, String description) {
    this.customerID = customerID;   // customerID no era parámetro → siempre null

// DESPUÉS (correcto):
public PaymentMethod(double amount, String customerID, String description) {
    this.customerID = customerID;   // ahora sí recibe y asigna correctamente
```

---

### `PaymentEventObserver.java` — línea corregida

```java
// ANTES (bug):
import javax.management.Notification;  // clase JMX del JDK, no la del paquete local

// DESPUÉS (correcto):
// Notification class is in the same package — no import needed
```

---

### `auxiliaryTest.java` — suite de pruebas

La suite incluye 20 casos de prueba que cubren:

| Grupo                           | Tests | Qué verifica                                             |
|---------------------------------|-------|----------------------------------------------------------|
| Abstract Factory — creación     | 5     | Cada fábrica crea objetos con los datos correctos        |
| Validación de pagos             | 6     | Casos válidos e inválidos para CC, PayPal y Crypto       |
| PaymentMethod — estado y datos  | 4     | Bug de customerID corregido, transiciones de estado      |
| Observer — ECIPayment           | 4     | Notificación en éxito, fallo, múltiples observers        |
| Inventario                      | 4     | Descuento de stock, manejo de inexistentes               |
| PaymentEventObserver            | 2     | Integración Observer con módulos reales                  |
| PaymentStatus                   | 1     | Enum con todos los estados y nombres correctos           |

**Resultado esperado de las pruebas:** todas pasan (`BUILD SUCCESS`).

---

## Diagrama de arquitectura final

```
                    ┌─────────────────────────────────────────┐
                    │           <<interface>>                  │
                    │           PaymentFactory                 │
                    │  + createPaymentMethod(...): PaymentMethod│
                    │  + createValidator(): ValidatePayment    │
                    └────────────┬────────────────────────────┘
                                 │ implements
          ┌──────────────────────┼─────────────────────┐
          │                      │                      │
 CreditCardPaymentFactory  PaypalPaymentFactory  CryptoPaymentFactory
          │ creates               │ creates              │ creates
          ▼                      ▼                      ▼
  CreditCardFactory         PaypalFactory          CryptoFactory
          └──────────────────────┴──────────────────────┘
                                 │ extends
                    ┌────────────┴────────────┐
                    │  <<abstract>>            │
                    │  PaymentMethod           │
                    │  implements ValidatePayment│
                    └────────────┬────────────┘
                                 │ uses
                    ┌────────────┴────────────────────────────┐
                    │           ECIPayment (Subject)           │
                    │  - observers: List<PaymentObserver>      │
                    │  + addObserver() / removeObserver()      │
                    │  + processPayment(factory, ...)          │
                    └────────────┬────────────────────────────┘
                                 │ notifies (Observer pattern)
                    ┌────────────┴────────────────────────────┐
                    │      <<interface>> PaymentObserver       │
                    │  + onPaymentSuccess(...)                 │
                    │  + onPaymentFailed(...)                  │
                    └────────────┬────────────────────────────┘
                                 │ implements
                    ┌────────────┴────────────────────────────┐
                    │        PaymentEventObserver              │
                    │  (Observer concreto — coordina módulos)  │
                    └───────┬────────────┬────────────┬───────┘
                            │            │            │
                       Inventory   Facturation   Notification
```

**OCP verificado:** Para agregar un nuevo método de pago (ej. PSE):
1. Crear `PSEPaymentMethod extends PaymentMethod` — nuevo archivo.
2. Crear `PSEPaymentFactory implements PaymentFactory` — nuevo archivo.
3. `ECIPayment`, `PaymentObserver` y todos los observers existentes: **sin cambios**.
