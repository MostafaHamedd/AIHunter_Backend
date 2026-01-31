# Railway Deployment Setup Guide

## ✅ Pre-Deployment Checklist

- [x] Maven build successful
- [x] JAR file generated: `target/hunter-ai-backend-1.0.0.jar`
- [x] Production profile configured
- [x] Railway configuration file created
- [x] Environment variables ready

## 🚀 Deployment Steps

### 1. Connect Repository to Railway

1. Go to [railway.app](https://railway.app)
2. Sign up/Login with GitHub
3. Click **"New Project"**
4. Select **"Deploy from GitHub repo"**
5. Choose your repository: `AIHunter_Backend`

### 2. Add PostgreSQL Database

1. In your Railway project, click **"New"**
2. Select **"Database"** → **"Add PostgreSQL"**
3. Railway will automatically create a PostgreSQL instance
4. Note the connection details (you'll need them for environment variables)

### 3. Configure Environment Variables

In Railway project settings → **Variables**, add:

```bash
# Profile
SPRING_PROFILES_ACTIVE=production

# Database (from Railway PostgreSQL service)
SPRING_R2DBC_URL=r2dbc:postgresql://[HOST]:[PORT]/[DATABASE]
SPRING_R2DBC_USERNAME=[USERNAME]
SPRING_R2DBC_PASSWORD=[PASSWORD]

# CORS (your Vercel frontend URL)
SPRING_WEB_CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app

# Port (Railway sets this automatically, but you can override)
PORT=8080
```

**Important:** 
- Railway provides `DATABASE_URL` automatically. You may need to parse it or use Railway's provided variables.
- For R2DBC, convert the JDBC URL format to R2DBC format:
  - JDBC: `jdbc:postgresql://host:port/db`
  - R2DBC: `r2dbc:postgresql://host:port/db`

### 4. Railway Auto-Detection

Railway will automatically:
- Detect it's a Java/Maven project
- Run: `mvn clean package -DskipTests`
- Start: `java -jar target/hunter-ai-backend-1.0.0.jar`

The `railway.json` file ensures correct build and start commands.

### 5. Get Your Backend URL

After deployment:
1. Railway will provide a public URL (e.g., `https://your-app.railway.app`)
2. Your API will be available at: `https://your-app.railway.app/api/...`

### 6. Update Frontend

In Vercel, add environment variable:
- `VITE_API_URL=https://your-app.railway.app/api`

## 🔧 Troubleshooting

### Build Fails
- Check Railway logs for Maven errors
- Ensure all dependencies are in `pom.xml`
- Verify Java 17 is available

### Database Connection Fails
- Verify `SPRING_R2DBC_URL` format is correct (starts with `r2dbc:postgresql://`)
- Check PostgreSQL service is running in Railway
- Verify credentials match Railway PostgreSQL service

### CORS Errors
- Ensure `SPRING_WEB_CORS_ALLOWED_ORIGINS` includes your Vercel frontend URL
- Check the URL has no trailing slash

### Port Issues
- Railway sets `PORT` automatically
- Application uses `${PORT:8080}` as fallback

## 📝 Notes

- Railway uses Nixpacks builder which auto-detects Java projects
- The `railway.json` file provides explicit build/start commands
- Database migrations run automatically via `schema.sql` on startup
- Logs are available in Railway dashboard

## ✅ Verification

After deployment, test your API:
```bash
curl https://your-app.railway.app/api/applications
```

Should return: `[]` (empty array if no applications)

