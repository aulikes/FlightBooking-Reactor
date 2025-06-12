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
- **Arquitectura Hexagonal (Ports & Adapters)**
- **Domain-Driven Design (DDD)**
- **Lombok**
- **WebTestClient + Mockito** para pruebas

## 🧠 Enfoque Arquitectónico

### ✅ Arquitectura Hexagonal + DDD
Separación estricta entre:
- **Dominio puro:** lógica central sin dependencias externas
- **Aplicación:** casos de uso y orquestación
- **Infraestructura:** persistencia, colas, Redis, controladores

### 🧩 Event-Driven Architecture
Cada evento (`ReservationCreated`, `FlightSeatConfirmed`, `FlightSeatRejected`) tiene su propio:
- **Publisher:** encapsula lógica de publicación
- **Listener:** desacopla y responde de forma reactiva

Esto permite trazabilidad, resiliencia y mantenimiento independiente.

### 🧠 Uso de Redis
Redis se utiliza como:
- **Repositorio distribuido temporal** para validaciones por evento
- Con **TTL configurado**, se garantiza la expiración automática si la reserva no es confirmada
- Se implementa un patrón **de agregación reactiva temporal distribuida**

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

## 🔍 Mejores Prácticas Aplicadas
- Eventos **versionados** y trazables (`traceId`)
- `IntegrationEventWrapper` como contrato de publicación
- No se usan eventos genéricos universales
- Dominios inmutables, controlados mediante **máquina de estados**
- Separación completa entre **infraestructura y lógica de negocio**

## 🧪 Pruebas
- Pruebas unitarias con Mockito
- Pruebas de integración con WebTestClient

## ✅ Conclusión
Este proyecto representa un ejemplo moderno, modular y realista de cómo abordar sistemas distribuidos reactivos en Java. Es ideal para estudios de arquitectura avanzada, diseño de eventos, y adopción de WebFlux en entornos exigentes.



### Arquitectura Hexagonal


```
└── src
    └── main
        └── java
            └── com
                └── aug
                    └── flightbooking
                        ├── application         -> Contiene los casos de uso del negocio, orquestación y lógica de aplicación.
                        │   ├── handler         -> Maneja eventos del dominio o externos (Listeners).
                        │   ├── service         -> Casos de uso que procesan comandos o consultas.
                        │   └── gateway         -> Interfaces que abstraen integraciones con tecnologías externas (ej. Kafka, Redis).
                        ├── domain              -> Contiene el modelo de dominio puro (entidades, objetos de valor, lógica de negocio).
                        ├── infrastructure      -> Implementaciones tecnológicas específicas: acceso a BD, Kafka, Redis, etc.
                        │   ├── repository      -> Adaptadores de persistencia para R2DBC (ej. PostgreSQL).
                        │   ├── publisher       -> Implementación de publicadores Kafka.
                        │   ├── listener        -> Adaptadores que consumen eventos de Kafka.
                        │   └── config          -> Configuraciones generales (Kafka, Redis, Beans).
                        └── adapter             -> Adaptadores de entrada (ej. API REST Controllers).
                            └── rest            -> Controladores que exponen endpoints y manejan DTOs.
```
