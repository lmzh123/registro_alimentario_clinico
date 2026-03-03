# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Registro Alimentario Clínico** — A clinical food diary app where patients log meals with behavioral/emotional context and share records with their health team (nutritionist, psychologist, psychiatrist). Designed for eating disorder treatment support.

Full project plan in [`App Plan - Registro Alimentario.md`](./App%20Plan%20-%20Registro%20Alimentario.md).

## Planned Tech Stack

| Layer | Choice |
|-------|--------|
| Frontend | Android — Kotlin + Jetpack Compose |
| Backend/Auth/DB | Firebase (Firestore + Auth) |
| Photo storage | Firebase Storage |
| Push notifications | Firebase FCM |

## Role-Based Access

Four roles with different data visibility:
- **Paciente** — creates/edits own records
- **Nutricionista** — sees food notes only
- **Psicología** — sees emotional/behavioral notes
- **Psiquiatría** — full access to all fields

Each `Registro` has a `visibilidad` array controlling which roles can see it. Professionals can only comment, never edit.

## Core Data Model[App Plan - Registro Alimentario.md](App%20Plan%20-%20Registro%20Alimentario.md)

The central `Registro` (food entry) includes: timestamp, meal type (enum), description, photos (URLs), location, companions, behavioral flags (`fue_atracón`, `deseos_purgar`, `actuo_sobre_purga`, `chequeo_cuerpo`), emotion arrays before/after (enum + free text), thoughts, external comments, additional notes, visibility settings, and clinical comments from professionals.

## Clinical Design Requirements

- Language must be **non-stigmatizing** and compassionate (neutral tone throughout UI)
- No gamification — no streaks, points, or achievement systems
- Always include quick access to **crisis resources**
- Reminders must be soft and optional
- Validate UI/UX with clinical team before public release