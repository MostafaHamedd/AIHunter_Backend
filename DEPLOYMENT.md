# Backend Deployment Guide

## Option 1: Railway (Recommended - Easiest)

### Steps:
1. Go to [railway.app](https://railway.app) and sign up/login
2. Click "New Project" → "Deploy from GitHub repo"
3. Select your `AIHunter_Backend` repository
4. Railway will automatically detect it's a Java/Maven project
5. Add environment variables:
   - `SPRING_PROFILES_ACTIVE=production`
   - `SPRING_R2DBC_URL` (Railway will auto-create PostgreSQL, use the DATABASE_URL)
   - `SPRING_R2DBC_USERNAME` (from Railway PostgreSQL)
   - `SPRING_R2DBC_PASSWORD` (from Railway PostgreSQL)
   - `SPRING_WEB_CORS_ALLOWED_ORIGINS` (your Vercel frontend URL, e.g., `https://your-app.vercel.app`)
6. Deploy!

Railway will automatically:
- Build with Maven
- Run `java -jar target/hunter-ai-backend-1.0.0.jar`
- Provide a public URL

---

## Option 2: Render

### Steps:
1. Go to [render.com](https://render.com) and sign up/login
2. Click "New" → "Web Service"
3. Connect your GitHub repository (`AIHunter_Backend`)
4. Configure:
   - **Name**: `hunter-ai-backend`
   - **Environment**: `Java`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/hunter-ai-backend-1.0.0.jar`
5. Add PostgreSQL database:
   - Click "New" → "PostgreSQL"
   - Copy the connection details
6. Add environment variables:
   - `SPRING_PROFILES_ACTIVE=production`
   - `SPRING_R2DBC_URL=r2dbc:postgresql://...` (from PostgreSQL)
   - `SPRING_R2DBC_USERNAME=...`
   - `SPRING_R2DBC_PASSWORD=...`
   - `SPRING_WEB_CORS_ALLOWED_ORIGINS=https://your-app.vercel.app`
7. Deploy!

---

## Option 3: Fly.io

### Steps:
1. Install Fly CLI: `curl -L https://fly.io/install.sh | sh`
2. Login: `fly auth login`
3. Initialize: `cd backend && fly launch`
4. Deploy: `fly deploy`

---

## Environment Variables Needed

```bash
# Database (use PostgreSQL in production)
SPRING_R2DBC_URL=r2dbc:postgresql://host:port/database
SPRING_R2DBC_USERNAME=your_username
SPRING_R2DBC_PASSWORD=your_password

# CORS (your Vercel frontend URL)
SPRING_WEB_CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app

# Optional
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

---

## Update Frontend API URL

After deploying, update your frontend's API base URL:

In `src/services/api.ts`, change:
```typescript
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
```

Then set `VITE_API_URL` in Vercel environment variables to your backend URL.

---

## Quick Deploy Commands

### Railway (after connecting repo):
```bash
# Railway auto-deploys on git push
git push origin main
```

### Render (after setup):
```bash
# Render auto-deploys on git push
git push origin main
```

### Fly.io:
```bash
cd backend
fly deploy
```

