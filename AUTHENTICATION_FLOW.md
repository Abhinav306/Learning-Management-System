# LMS Authentication & Authorization Flow

This document details the architectural design and sequence flows for the authentication and authorization processes in the AI-Powered Learning Management System (LMS).

---

## 1. User Registration / Sign Up Flow

The Sign Up flow registers a new user with standard credentials. The password is hashed using **BCrypt** before database persistence.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant Service as AuthService
    participant Repo as UserRepository
    participant Encoder as PasswordEncoder

    Client->>Controller: POST /api/v1/auth/signup (SignupRequest)
    Note over Client,Controller: Validated by Jakarta Validation annotations
    Controller->>Service: registerUser(SignupRequest)
    Service->>Repo: existsByEmail(email)
    alt Email already registered
        Repo-->>Service: true
        Service-->>Controller: throw DuplicateResourceException (409 Conflict)
        Controller-->>Client: ApiResponse.error("Email already exists")
    else Email is unique
        Repo-->>Service: false
        Service->>Encoder: encode(rawPassword)
        Encoder-->>Service: hashedPass
        Note over Service: Map request to User entity with encoded password
        Service->>Repo: save(User)
        Repo-->>Service: User (saved)
        Service-->>Controller: UserResponse
        Controller-->>Client: ApiResponse.created(UserResponse)
    end
```

---

## 2. User Log In / Authentication Flow

Authentication verifies the user's password and returns a stateless access token alongside a stateful refresh token.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant Manager as AuthenticationManager
    participant Service as AuthService
    participant JwtService as JwtService
    participant RefreshRepo as RefreshTokenRepository

    Client->>Controller: POST /api/v1/auth/login (LoginRequest)
    Controller->>Manager: authenticate(UsernamePasswordAuthenticationToken)
    Note over Manager: Verifies email & credentials via CustomUserDetailsService
    alt Authentication Fails
        Manager-->>Controller: BadCredentialsException
        Controller-->>Client: ApiResponse.error("Invalid credentials")
    else Authentication Succeeds
        Manager-->>Controller: Authentication Object
        Controller->>Service: generateAuthTokenResponse(Authentication)
        Service->>JwtService: generateAccessToken(UserPrincipal)
        JwtService-->>Service: accessToken (JWT)
        Service->>Service: createRefreshToken(User)
        Note over Service: Generate random UUID for Refresh Token
        Service->>RefreshRepo: save(RefreshToken)
        RefreshRepo-->>Service: RefreshToken (saved)
        Service-->>Controller: JwtAuthenticationResponse
        Controller-->>Client: ApiResponse.success(JwtAuthenticationResponse)
    end
```

---

## 3. Stateless Request Authentication Filter

For protected API resources, the incoming request intercepts a filter that authenticates the user by inspecting the access token.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Filter as JwtAuthenticationFilter
    participant JwtService as JwtService
    participant SecurityContext as SecurityContextHolder
    participant UserDetails as CustomUserDetailsService
    participant Endpoint as Business Controller

    Client->>Filter: Request to protected path /api/v1/users/... with Bearer JWT
    Filter->>Filter: Extract JWT from "Authorization" Header
    alt Token Missing or Invalid
        Filter-->>Client: Returns HTTP 401 Unauthorized via JwtAuthenticationEntryPoint
    else Token Valid
        Filter->>JwtService: extractUsername(token)
        JwtService-->>Filter: emailAddress
        Filter->>UserDetails: loadUserByUsername(emailAddress)
        UserDetails-->>Filter: UserPrincipal
        Filter->>JwtService: isTokenValid(token, UserPrincipal)
        JwtService-->>Filter: true
        Note over Filter: Construct UsernamePasswordAuthenticationToken
        Filter->>SecurityContext: setAuthentication(auth)
        Note over SecurityContext: Authentication context established for this thread
        Filter->>Endpoint: Proceed chain
        Endpoint-->>Client: ApiResponse (Success)
    end
```

---

## 4. Refresh Token Rotation Flow

Allows the client to obtain a fresh access token without re-entering credentials. Implements token rotation by generating a new refresh token and deleting the old one.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant Service as AuthService
    participant RefreshRepo as RefreshTokenRepository
    participant JwtService as JwtService

    Client->>Controller: POST /api/v1/auth/refresh (TokenRefreshRequest)
    Controller->>Service: refreshSession(TokenRefreshRequest)
    Service->>RefreshRepo: findByToken(refreshTokenStr)
    alt Token Not Found
        RefreshRepo-->>Service: Optional.empty
        Service-->>Controller: throw TokenRefreshException
        Controller-->>Client: ApiResponse.error("Invalid refresh token")
    else Token Found
        RefreshRepo-->>Service: RefreshToken
        Service->>Service: verifyExpiration(RefreshToken)
        alt Token Expired
            Service->>RefreshRepo: delete(RefreshToken)
            Service-->>Controller: throw TokenRefreshException
            Controller-->>Client: ApiResponse.error("Refresh token expired")
        else Token Active (Valid)
            Service->>JwtService: generateAccessToken(UserPrincipal)
            JwtService-->>Service: newAccessToken
            Service->>Service: rotateRefreshToken(RefreshToken)
            Note over Service: Delete old token and save new RefreshToken
            Service->>RefreshRepo: delete(oldToken)
            Service->>RefreshRepo: save(newToken)
            RefreshRepo-->>Service: newToken
            Service-->>Controller: JwtAuthenticationResponse
            Controller-->>Client: ApiResponse.success(JwtAuthenticationResponse)
        end
    end
```
