# Secure Notes API

- Backend for secure note-taking app.
- Built with Node.js, Express, MongoDB.

## Features

- User registration & login.
- Short-lived access tokens (5 mins).
- Refresh tokens (30 days) for continued access.
- Create, read, update, delete notes.
- Secure API with rate limiting.
- GZIP compression for faster responses.
- HTTPS recommended for production.
- Swagger API documentation (`/api-docs`).

## Setup

1. **Clone:** `git clone <repo_url>`
2. **Install:** `npm install` or `yarn install`
3. **`.env`:** Configure `MONGO_URI`, `JWT_SECRET`, etc.
4. **Run Dev:** `npm run dev`
5. **Run Prod:** `NODE_ENV=production npm start`

## API Endpoints

- `/api/auth`: Registration, login, refresh, logout, user info.
- `/api/notes`: Create, read, update, delete notes (requires auth).
- `/api/config`: API configuration.
- `/api/status`: API status.

### `/api/auth`

- **`POST /api/auth/register`**: Register new user (`{ email, password }` -> `{ accessToken, refreshToken, expiresIn }`).
- **`POST /api/auth/login`**: Login user (`{ email, password }` -> `{ accessToken, refreshToken, expiresIn }`, sets cookie).
- **`POST /api/auth/refresh`**: Get new access token (`{ refreshToken }` or cookie -> `{ accessToken, expiresIn }`).
- **`POST /api/auth/logout`**: Logout user (optional `{ refreshToken }` or cookie -> `{ msg }`, clears cookie).
- **`GET /api/auth/user`**: Get logged-in user (requires Bearer token -> `{ _id, email, createdAt }`).

### `/api/notes` (Requires Bearer Token)

- **`GET /api/notes`**: Get all user notes (`[]`).
- **`GET /api/notes/:id`**: Get specific note (`{ _id, title, content, ... }`).
- **`POST /api/notes`**: Create new note (`{ title, content }` -> `{ _id, title, content, ... }`).
- **`PUT /api/notes/:id`**: Update note (`{ title, content }` -> `{ _id, title, content, ... }`).
- **`DELETE /api/notes/:id`**: Delete note (`{ msg }`).

### `/api/config`

- **`GET /api/config`**: Get API configuration (`{ apiUrl, accessTokenTtl, refreshTokenTtl, ... }`).

### `/api/status`

- **`GET /api/status`**: Get API status (`{ status, environment, timestamp, ... }`).


## Security Highlights

- Short access token expiry.
- Refresh token mechanism.
- Considerations for SSL pinning (Android).
- Rate limiting for protection.
- HTTP security headers.

## Docs

- Access Swagger UI at `/api-docs` (dev mode).


Step 1: Set up a basic Node.js backend

Create a simple Express server with user authentication
Set up CRUD operations for notes
Implement proper TLS/SSL for secure connections
Deploy locally and expose with ngrok for testing

Step 2: Create the Android client

Set up a basic notes app UI
Implement Retrofit for API communication
Configure proper SSL/TLS handling
Add SSL pinning to prevent MITM attacks

Step 3: Enhance security features

Implement token-based authentication
Add different security configurations for debug vs release
Configure network security config for cleartext traffic rules
Handle certificate validation properly

mkdir certificates
cd certificates

# Generate a private key
openssl genrsa -out private-key.pem 2048

# Generate a certificate signing request
openssl req -new -key private-key.pem -out csr.pem

# Generate a self-signed certificate (valid for 365 days)
openssl x509 -req -days 365 -in csr.pem -signkey private-key.pem -out certificate.pem


