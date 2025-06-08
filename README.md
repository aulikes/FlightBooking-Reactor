# ✈️ FlightBooking Service - Sistema de Gestión de Reservas de Vuelos

> Proyecto de referencia para arquitecturas modernas basadas en eventos, con enfoque en diseño limpio, programación reactiva y sistemas distribuidos.

---

## 🚀 Descripción General

**FlightBooking** es un microservicio desarrollado para gestionar el ciclo de vida de reservas de vuelo, incluyendo validación de asientos, actualización de disponibilidad y publicación de eventos de negocio. El objetivo es ofrecer una base de estudio robusta y profesional que combine:

- 🧠 Lógica de negocio clara y desacoplada
- 🏗️ Arquitectura hexagonal (puertos y adaptadores)
- 🌀 Programación reactiva end-to-end
- 📬 Comunicación asíncrona basada en eventos (Kafka)
- ⚙️ Persistencia reactiva con R2DBC y PostgreSQL

Este servicio no es una demo más. Fue diseñado y construido como material serio de estudio, ideal para desarrolladores que quieren llevar su nivel al siguiente paso.

---

## 📚 Lógica del Negocio

### 📌 Flujo principal: **Reserva de vuelo**

1. **Creación de la reserva**:
   - Se recibe un `ReservationCreatedEvent`.
   - Se delega la validación al servicio de vuelo (Flight).

2. **Validación de disponibilidad**:
   - Se verifica si hay asientos disponibles.
   - Si hay cupo, se reserva un asiento (`flight.reserveSeat()`).
   - Se actualiza el número de asientos disponibles en base de datos.

3. **Publicación de eventos**:
   - Si hay cupo: se publica `FlightSeatConfirmedEvent`.
   - Si no hay cupo: se publica `FlightSeatRejectedEvent`.

Todo esto ocurre **de forma reactiva**, **sin bloquear hilos** y con una arquitectura desacoplada basada en eventos.

---

## 🧱 Arquitectura

- **Arquitectura hexagonal (Ports & Adapters)**: separación entre dominio, aplicación, infraestructura y controladores/eventos.
- **Bounded Contexts separados**: `reservation` y `flight` no comparten clases. Solo se comunican mediante eventos.
- **Publicadores e interfaces desacopladas**: cada evento tiene su publisher, versión y contrato.
- **Event wrapping profesional**: los eventos se envuelven con metadata (`eventType`, `version`, `traceId`, `timestamp`).

---

## 🛠️ Tecnologías utilizadas

| Capa        | Tecnología                               |
|-------------|-------------------------------------------|
| Lenguaje    | Java 17                                   |
| Framework   | Spring Boot 3.x                           |
| Reactive    | Spring WebFlux, Reactor Core              |
| Persistencia| Spring Data R2DBC, PostgreSQL             |
| Broker      | Apache Kafka                              |
| Serialización | Jackson                                 |
| Build Tool  | Gradle                                    |
| Gestión de eventos | Topic por evento, sin eventos genéricos |

---

## 📂 Estructura del Proyecto

```bash
com.aug.flightbooking
├── domain                  # Modelos de dominio (Flight, Airline, etc.)
├── application
│   ├── port.in             # Use cases
│   ├── port.out            # Interfaces de salida (Publisher, Repos)
│   └── service             # Coordinadores de lógica de negocio
├── infrastructure
│   ├── persistence         # Repositorio R2DBC, entidades, mappers
│   └── messaging           # Kafka publishers, IntegrationEventWrapper
└── adapters                # Listeners Kafka que llaman a la lógica de negocio
```

---

## 📈 Puntos fuertes del diseño

- ✅ Cada evento tiene una versión (`v1`) y un contrato específico.
- ✅ Los eventos nunca se comparten entre bounded contexts.
- ✅ Los listeners no contienen lógica. Delegan en servicios.
- ✅ Se usan interfaces para puertos de entrada/salida.
- ✅ Se utiliza Redis y Kafka con buenas prácticas.

---

