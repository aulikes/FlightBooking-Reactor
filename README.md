# Flight Booking Reactive System ✈️

## 🌟 Visión General
Este proyecto es una **prueba de concepto (POC)** para demostrar un sistema de reserva de vuelos utilizando una arquitectura basada en eventos, totalmente reactiva y distribuida. Aunque no representa un proceso completo en producción, **implementa las piezas clave del dominio**, como creación de reservas, verificación de disponibilidad de asientos, publicación de eventos asincrónicos y manejo de estados con persistencia reactiva.

Su propósito es **explorar cómo construir una solución moderna y desacoplada**, aplicando buenas prácticas arquitectónicas, patrones de diseño y tecnologías de última generación como **WebFlux, Reactor Core, Redis y Kafka**.

## ⚙️ Tecnologías Clave
- **Spring Boot 3 + WebFlux**
- **Project Reactor (Reactor Core)**
- **PostgreSQL (con R2DBC)**
- **Redis** para estado temporal y control de TTL
- **Kafka** para eventos distribuidos
- **Liquibase** con scripts en formato YAML para control de versiones de base de datos
- **Arquitectura Hexagonal (Ports & Adapters)**
- **Domain-Driven Design (DDD)**
- **Lombok**

## 🧠 Enfoque Arquitectónico

### ✅ Arquitectura Hexagonal + DDD
Separación estricta entre:
- **Dominio puro:** lógica central sin dependencias externas
- **Aplicación:** casos de uso y orquestación
- **Infraestructura:** persistencia, colas, Redis, controladores

### 🧩 Event-Driven Architecture 

Cada evento tiene su propio:

- **Publisher**: encapsula la lógica de publicación, ahora implementado 100% con `KafkaSender` de **Reactor Kafka**, sin romper el flujo reactivo.
- **Listener**: desacopla y responde de forma no bloqueante con `KafkaReceiver`, evitando el uso de `@KafkaListener` imperativo.

Además:

- Se creó una **fábrica de KafkaSender y KafkaReceiver** (`KafkaSenderFactory`, `KafkaReceiverFactory`) que permite configurar dinamicamente productores y consumidores sin definir beans individuales por evento.
- La (de)serialización se maneja con clases reactivas (`ReactiveJsonEncoder`, `ReactiveJsonDecoder`), eliminando llamadas bloqueantes.
- Se evita el uso de `@PostConstruct` y se emplea `ApplicationReadyEvent` sólo donde estrictamente necesario, o se suscriben los `KafkaReceiver` directamente en cada clase.
- Todos los `Publisher` y `Listener` fueron refactorizados para usar `Mono<Void>` como contrato uniforme.

Cada evento tiene su propio:
- **Publisher:** encapsula lógica de publicación
- **Listener:** desacopla y responde de forma reactiva

Esto permite trazabilidad, resiliencia y mantenimiento independiente.

### 🧠 Uso de Redis
Redis se utiliza como:
- **Repositorio distribuido temporal** para validaciones por evento
- Con **TTL configurado**, se garantiza la expiración automática si la reserva no es confirmada
- Se implementa un patrón **de agregación reactiva temporal distribuida**

### 🎫 Flujo de Emisión de Ticket y Check-In (NUEVO)
- Cuando una reserva es confirmada (`ReservationConfirmedEvent`), se crea automáticamente un **Ticket** con su estado inicial.
- El usuario puede realizar **Check-in**, el cual es validado internamente por el estado del `Ticket`.
- Todo esto se implementa mediante casos de uso reactivos, eventos asincrónicos y un **modelo de dominio rico** basado en máquina de estados.

### ⚡ Flujo 100% Asíncrono y No Bloqueante
Gracias al uso combinado de WebFlux + Reactor Core:
- No hay bloqueo de hilos
- Se aprovechan eficientemente los recursos
- La lógica se suscribe correctamente en todos los puntos críticos (`subscribe()` ubicado solo donde se requiere)

## 🛫 Flujo de Reserva de Vuelo
1. El usuario **crea una reserva** → `ReservationCreatedEvent`
2. Se verifica la disponibilidad de asientos → `FlightSeatConfirmed` o `FlightSeatRejected`
3. Se actualiza el estado de la reserva
4. Si no hay respuesta a tiempo → Redis marca como `FAILED`
5. Si se confirma la reserva → se crea automáticamente un **Ticket**
6. El usuario puede luego hacer **Check-in**

## 🔍 Mejores Prácticas Aplicadas
- Eventos **versionados** y trazables (`traceId`)
- `IntegrationEventWrapper` como contrato de publicación
- No se usan eventos genéricos universales
- Dominios inmutables, controlados mediante **máquina de estados**
- Separación completa entre **infraestructura y lógica de negocio**
- **Value Objects** y entidades con responsabilidad encapsulada
- Separación entre `command`, `use case`, `controller`, `publisher`, `listener`

## ✅ Conclusión
Este proyecto representa un ejemplo moderno, modular y realista de cómo abordar sistemas distribuidos reactivos en Java. Es ideal para estudios de arquitectura avanzada, diseño de eventos, y adopción de WebFlux en entornos exigentes.

---

## 🗂️ Estructura de Proyecto: Arquitectura Hexagonal

```
└── src
    └── main
        └── java
            └── com
                └── aug
                    └── flightbooking
                        ├── application         -> Casos de uso, orquestación, publicación y consumo de eventos
                        │   ├── handler         -> Listeners reactivos de eventos Kafka
                        │   ├── service         -> Lógica de casos de uso: crear reserva, check-in, emitir ticket
                        │   └── gateway         -> Interfaces que abstraen Kafka, Redis, y persistencia
                        ├── domain              -> Entidades puras (Reservation, Ticket), Value Objects, lógica de negocio
                        ├── infrastructure      -> Implementaciones concretas (R2DBC, Kafka, Redis)
                        │   ├── repository      -> Persistencia reactiva (PostgreSQL)
                        │   ├── publisher       -> Publicación de eventos Kafka
                        │   ├── listener        -> Adaptadores de eventos entrantes
                        │   └── config          -> Configuración de Beans, Kafka, Redis
                        └── adapter
                            └── rest            -> Controladores WebFlux (API REST reactiva)
```