# 🥗 App Plan: Registro Alimentario Clínico

> Aplicación para compartir registros fotográficos y escritos de la alimentación diaria con el equipo de salud (nutricionista, psicología y psiquiatría).

---

## 🎯 Objetivo

Crear un espacio seguro y privado donde el usuario pueda registrar cada comida del día con contexto emocional, conductual y clínico, y compartirlo de forma controlada con su equipo de salud.

---

## 👥 Usuarios

| Rol | Acceso |
|-----|--------|
| **Paciente** | Crea y edita sus propios registros |
| **Nutricionista** | Ve registros + notas de alimentación |
| **Psicología** | Ve registros + notas emocionales y conductuales |
| **Psiquiatría** | Acceso completo a todos los registros |

---

## 📋 Estructura de un Registro de Comida

Cada entrada de comida incluye los siguientes campos:

### 🕐 Información básica
- **Fecha y hora** del registro
- **Tipo de comida** (desayuno, media mañana, almuerzo, merienda, cena, snack nocturno, otro)
- **Descripción escrita** de lo que se comió
- **Fotos** (una o varias, opcionales)
- **Lugar** donde se comió (casa, trabajo, restaurante, etc.)
- **Acompañantes** (solo, con familia, con amigos, etc.)

### 🔍 Evaluación conductual
- **¿Percibiste que fue un atracón?** (Sí / No / No estoy seguro/a)
  - Si sí: ¿qué crees que lo desencadenó?
- **¿Tuviste deseos de vomitar o purgar?** (Sí / No)
  - Si sí: ¿actuaste sobre ese deseo? (Sí / No)
- **¿Hubo chequeo del cuerpo** antes, durante o después de comer? (Sí / No)
  - Ej: mirarse al espejo, pellizcar áreas del cuerpo, medirse, pesarse

### 💭 Estado emocional y mental
- **Emociones antes de comer** (selector múltiple + texto libre)
  - Ansiedad, tristeza, enojo, soledad, aburrimiento, alegría, neutralidad, otro
- **Emociones después de comer** (mismo selector)
- **Pensamientos presentes** durante o después de la comida (texto libre)
  - Ej: pensamientos sobre el cuerpo, culpa, comparaciones, etc.
- **Comentarios de otras personas** que afectaron el momento (texto libre, opcional)

### 📝 Notas adicionales
- Campo libre para cualquier cosa que el usuario quiera agregar
- Visibilidad configurable por nota (¿compartir con todo el equipo o solo con uno?)

---

## 🔒 Privacidad y Compartir

- El usuario **elige qué comparte** con cada profesional
- Opción de marcar una entrada como **privada** (solo visible para el usuario)
- Los profesionales **no pueden editar** los registros, solo comentar
- Sistema de **comentarios clínicos** por parte del equipo (el paciente puede verlos)

---

## 📊 Funcionalidades principales

### Para el paciente
- [ ] Crear registro de comida (foto + texto + evaluación)
- [ ] Ver historial de registros propios
- [ ] Editar o eliminar registros propios
- [ ] Compartir registros con profesionales seleccionados
- [ ] Recibir comentarios del equipo de salud
- [ ] Ver resumen semanal (días registrados, patrones)

### Para los profesionales
- [ ] Ver registros compartidos por el paciente
- [ ] Agregar comentarios clínicos a registros específicos
- [ ] Ver resumen y estadísticas del paciente
- [ ] Filtrar por tipo de conducta (atracones, purgas, etc.)
- [ ] Descargar reportes en PDF para sesiones

---

## 🗂️ Modelo de datos (propuesto)

```
Registro
├── id
├── usuario_id
├── fecha_hora          ← timestamp exacto
├── tipo_comida         ← enum
├── descripcion         ← texto
├── fotos[]             ← URLs de imágenes
├── lugar               ← texto libre
├── acompanantes        ← texto libre
├── fue_atracón         ← bool / no_se
├── desencadenante_atracon ← texto (condicional)
├── deseos_purgar       ← bool
├── actuo_sobre_purga   ← bool (condicional)
├── chequeo_cuerpo      ← bool
├── emociones_antes[]   ← array de enum + texto
├── emociones_despues[] ← array de enum + texto
├── pensamientos        ← texto libre
├── comentarios_externos← texto libre
├── notas_adicionales   ← texto libre
├── visibilidad         ← ["nutricionista", "psicologia", "psiquiatria", "privado"]
└── comentarios_clinicos[]
    ├── profesional_id
    ├── rol
    ├── texto
    └── fecha
```

---

## 🛠️ Stack tecnológico sugerido

| Capa | Opción A (sencilla) | Opción B (escalable) |
|------|---------------------|----------------------|
| **Frontend** | Android (Kotlin + Jetpack Compose) | Android (Kotlin + XML Views) |
| **Backend** | Firebase Firestore | Node.js + PostgreSQL |
| **Almacenamiento fotos** | Firebase Storage | AWS S3 |
| **Auth** | Firebase Auth | Auth0 |
| **Notificaciones** | Firebase FCM | WorkManager + local notifications |

> 💡 **Recomendación inicial:** Firebase + Kotlin/Jetpack Compose — ecosistema unificado, ideal para MVP.

---

## 🚀 Fases de desarrollo

### Fase 1 — MVP (4–6 semanas)
- [ ] Registro básico de comida (texto + foto + hora)
- [ ] Evaluación conductual (atracón, purga, chequeo)
- [ ] Emociones antes/después
- [ ] Compartir con profesionales
- [ ] Vista del profesional (lectura)

### Fase 2 — Mejoras (4–6 semanas)
- [ ] Comentarios clínicos del equipo
- [ ] Notificaciones recordatorio para registrar
- [ ] Historial y calendario visual
- [ ] Filtros por conductas para profesionales

### Fase 3 — Avanzado (a futuro)
- [ ] Reportes PDF para sesiones
- [ ] Estadísticas y patrones (gráficos)
- [ ] Integración con wearables (ritmo cardíaco durante la comida)
- [ ] Modo offline con sincronización posterior

---

## ⚠️ Consideraciones clínicas importantes

- El diseño debe ser **no estigmatizante** — lenguaje neutro y compasivo
- Evitar gamificación que pueda generar presión (sin "rachas" ni puntos)
- Incluir siempre acceso rápido a **recursos de crisis**
- Los recordatorios deben ser **suaves y opcionales**
- Considerar **supervisión de un profesional** antes de lanzar al público

---

## 📅 Próximos pasos

1. Validar el plan con el equipo clínico
2. Definir stack tecnológico final
3. Crear wireframes / mockups
4. Desarrollar MVP

---

*Creado: {{date}}*
*Vault: luis_vault*
