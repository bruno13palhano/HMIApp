# HMIApp

A modern Android application for building customizable Human-Machine Interfaces (HMIs) with real-time communication support via **MQTT**, **HTTP**, and other IoT protocols. Designed with **Jetpack Compose**, **MVI architecture**, and **Room database** for state management and persistence.

---

## 🚀 Features

- 📱 **Dynamic Widget System**  
  Create and position widgets such as gauges, switches, and displays.

- 🌐 **Real-Time Communication**  
  Communicates with IoT devices using MQTT protocol (via HiveMQ client).

- 🧩 **Modular Architecture**  
  Uses clean MVI pattern, separation of concerns, and dependency injection (Hilt).

- 🏠 **Persistent Layouts**  
  Saves widget layout and configuration using Room database.

- 🖱️ **Drag & Drop Editor (WIP)**  
  Interactive interface for placing and arranging widgets.

- 📊 **Data Monitoring**  
  Display real-time values from sensors and controllers.

---

## 🧠 Architecture

This project follows a **modular, scalable, and testable** architecture using:

- **Jetpack Compose** for declarative UI
- **MVI pattern** with container-based state management
- **Room** for local persistence
- **HiveMQ MQTT Client** for device communication
- **Hilt** for dependency injection
- **Kotlin Coroutines / Flows** for reactive programming

---

## 📂 Modules

```
HMIApp/
├── app/                    # Main Android app
├── core/
│   ├── data/               # Repositories and data sources
│   ├── model/              # Domain models
│   └── network/            # MQTT and HTTP clients
├── ui/
│   ├── components/         # Shared UI elements
│   └── widgets/            # Dynamic widget definitions
└── build.gradle            # Project-level config
```

---

## 📸 Screenshots

> Coming soon!

---

## 🛠️ Setup

### Requirements

- Android Studio Hedgehog or newer
- Android SDK 33+
- Kotlin 1.9+
- Gradle 8+

### Clone the Repository

```bash
git clone https://github.com/bruno13palhano/HMIApp.git
cd HMIApp
```

---

## 🏃 Run the App

Open with Android Studio and press **Run**, or use:

```bash
./gradlew installDebug
```

---

## 📡 Communication Protocols

### ✅ MQTT (Implemented)

- Uses **HiveMQ MQTT Client**
- Asynchronous, non-blocking
- Configure topics and broker URL in `MqttClientManager`

### 🛠 Planned Protocols

- HTTP (REST polling)
- Modbus TCP (for industrial controllers)

---

## 🧪 Testing

Unit tests are included for ViewModels and repositories.

Run them with:

```bash
./gradlew test
```

Instrumented UI tests are coming soon.

---

## 📌 Roadmap

- [x] MQTT communication (HiveMQ)
- [x] Dynamic widget layout system
- [x] Room database for widget persistence
- [x] Drag-and-drop interface for widget positioning
- [x] Import/export configuration
- [ ] HTTP and Modbus communication
- [ ] Custom widget creation
- [ ] Widget visibility rules / triggers
- [ ] Authentication and user profiles

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Bruno Barbosa de Sousa**  
GitHub: [@bruno13palhano](https://github.com/bruno13palhano)
