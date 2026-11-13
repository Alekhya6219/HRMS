# PeoplePulse HRMS

A full-stack HR Management System built with React + Vite, Java Spring Boot, Spring Data JPA and PostgreSQL.

## Features
- Dashboard KPIs
- Employee CRUD and search
- Attendance marking and attendance history
- Leave requests and approve/reject workflow
- Payroll records and net salary calculation
- Department list
- Responsive UI

## Prerequisites
- Node.js 20+
- Java 17+
- Maven 3.9+
- PostgreSQL 14+ (or a hosted PostgreSQL database)

## 1. Database
Create a PostgreSQL database:
```sql
CREATE DATABASE peoplepulse;
```

## 2. Run backend
```bash
cd backend
```
Set environment variables (PowerShell example):
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/peoplepulse"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```
Run:
```bash
mvn spring-boot:run
```
Backend: http://localhost:8080
Health check: http://localhost:8080/api/health

The application seeds sample departments and employees on first startup if the database is empty.

## 3. Run frontend
Open a second terminal:
```bash
cd frontend
npm install
```
Create `.env` from `.env.example` and set:
```env
VITE_API_URL=http://localhost:8080/api
```
Then:
```bash
npm run dev
```
Open the URL printed by Vite (usually http://localhost:5173).

## 4. Build frontend
```bash
npm run build
```

## 5. Deploy
### Database (Neon)
1. Create a PostgreSQL project at https://neon.tech.
2. Copy the connection details.
3. For Render, set `DB_URL` to the JDBC form:
   `jdbc:postgresql://HOST/DB?sslmode=require`
4. Set `DB_USERNAME` and `DB_PASSWORD`.

### Backend (Render)
1. Push this project to GitHub.
2. In Render, create **New + > Web Service**, connect the repository.
3. Set Root Directory to `backend`.
4. Build Command: `./mvnw clean package` if you add a Maven wrapper, otherwise `mvn clean package`.
5. Start Command: `java -jar target/peoplepulse-0.0.1-SNAPSHOT.jar`
6. Add environment variables:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `FRONTEND_URL` = your Vercel URL
7. Deploy and copy the backend URL.

### Frontend (Vercel)
1. Import the same GitHub repository into Vercel.
2. Set Root Directory to `frontend`.
3. Framework preset: Vite.
4. Build command: `npm run build`.
5. Output directory: `dist`.
6. Add environment variable `VITE_API_URL=https://YOUR-BACKEND.onrender.com/api`.
7. Deploy.

### CORS
Set backend environment variable `FRONTEND_URL` to the exact deployed Vercel origin, e.g. `https://peoplepulse-hrms.vercel.app`. For local development, localhost:5173 is already allowed.

## Demo data
The backend seeds example departments and employees. Add your own records through the UI before sharing the assessment link.

## Important production note
This is an assessment-ready MVP, not a production HR/payroll system. Before real company use, add authentication and role-based authorization, audit logs, input validation policies, backups, privacy controls and payroll/tax compliance. Do not put real employee or salary data into the demo.
