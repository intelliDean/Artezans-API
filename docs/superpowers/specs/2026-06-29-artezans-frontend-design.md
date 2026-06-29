# Artezans Frontend Design Specification

**Date:** 2026-06-29  
**Status:** Brainstormed & Approved  
**Topic:** Marketplace & Customer Flow Frontend (Task Hub / Artezans)

---

## 1. Executive Summary

This document specifies the frontend architecture and design for the **Artezans (Task Hub)** marketplace. The implementation focuses on the **Marketplace & Customer Flow**, delivering a fast, accessible, and premium-feeling user interface using modern React and Vanilla CSS. 

Key design choices include:
*   **Vite + React SPA**: High-performance client-side application.
*   **Vite-based Build & Dev Loop**: Instant Hot Module Replacement (HMR) and optimized builds.
*   **Vanilla CSS Design System**: Custom theme variables (HSL-based, supporting dark mode), rich micro-animations, and glassmorphic aesthetics.
*   **Hybrid Landing Page**: Direct provider search alongside a live custom task board and a "Post a Task" trigger.
*   **Quick-Auth Modals**: Modal-based login/signup to prevent user context redirection.
*   **Tabbed Customer Dashboard**: Mobile-friendly overview of active tasks, offers, bookings, and settings.
*   **Embedded Payment Checkout**: Stripe Elements and PayPal SDK buttons integrated directly into the booking panel.
*   **Toast + Bell Notification System**: Dynamic toast alerts in the viewport and a persisted bell dropdown in the navigation header.

---

## 2. Technology Stack & Dependencies

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Core Library** | React 19 (or latest) | Component lifecycle and UI logic |
| **Build Tooling** | Vite | Rapid bundler and dev server |
| **Styling** | Vanilla CSS | Custom styling tokens, layout flex/grid, transitions, variables |
| **Routing** | React Router v6 | Client-side routing with nested layouts |
| **Data Fetching** | Axios + TanStack Query v5 | API communication, query caching, polling feeds |
| **Payments** | `@stripe/stripe-js`, `@stripe/react-stripe-js` | In-app credit card payment processing |

---

## 3. Directory Structure & Code Organization

```
artezans-frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── assets/
│   │   ├── fonts/           ← Outfit / Inter modern typography
│   │   └── images/          ← Icons and brand assets
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.jsx   ← Styled button with micro-animations
│   │   │   ├── Input.jsx    ← Underlined or outlined inputs with `:user-valid` state
│   │   │   ├── Modal.jsx    ← Accessible modal component with backdrop blur
│   │   │   └── Spinner.jsx  ← Inline loading indicator
│   │   ├── auth/
│   │   │   └── LoginModal.jsx ← Unified login and signup modal with sliding tab
│   │   ├── booking/
│   │   │   ├── BookingDetailsCard.jsx
│   │   │   └── CheckoutWidget.jsx ← Inline Stripe & PayPal toggle widget
│   │   └── marketplace/
│   │       ├── SearchBar.jsx ← Search input for services/tasks
│   │       ├── CategoryGrid.jsx ← Service categories selection
│   │       └── TaskBoardFeed.jsx ← Polled list of recent tasks using TanStack Query
│   ├── context/
│   │   ├── AuthContext.jsx  ← User session, login, and logout state provider
│   │   └── ToastContext.jsx ← Toast container and trigger function (`showToast`)
│   ├── hooks/
│   │   ├── useAuth.js       ← Shortcut to consume AuthContext
│   │   └── useAxios.js      ← Returns Axios instance injected with interceptors
│   ├── layouts/
│   │   ├── PublicLayout.jsx  ← Navigation Header + Footer for guests
│   │   ├── CustomerLayout.jsx← Auth-guarded customer portal (contains Header)
│   │   └── ProviderLayout.jsx← Auth-guarded provider portal (contains Header)
│   ├── pages/
│   │   ├── Home.jsx         ← Hybrid landing page
│   │   ├── Listings.jsx     ← Browse/search service listings
│   │   ├── Tasks.jsx        ← Browse posted tasks
│   │   ├── CustomerDashboard.jsx ← Tabbed view (Tasks, Bookings, Settings)
│   │   └── NotFound.jsx
│   ├── services/
│   │   └── api.js           ← Axios base settings and token refresh interceptors
│   ├── App.jsx              ← React Router root wrapper
│   ├── index.css            ← Theme CSS tokens, dark mode triggers, base utility styles
│   └── main.jsx             ← React DOM root & client setups
├── package.json
└── vite.config.js
```

---

## 4. Routing Configuration (React Router v6)

The application uses nested routes with layout wrappers to enforce security checks and present matching header/footer modules:

```jsx
// src/App.jsx Example Setup
<Routes>
  {/* Public Routes */}
  <Route element={<PublicLayout />}>
    <Route path="/" element={<Home />} />
    <Route path="/listings" element={<Listings />} />
    <Route path="/tasks" element={<Tasks />} />
  </Route>

  {/* Customer Protected Routes */}
  <Route element={<CustomerLayout />}>
    <Route path="/dashboard/customer" element={<CustomerDashboard />} />
  </Route>

  {/* Provider Protected Routes */}
  <Route element={<ProviderLayout />}>
    <Route path="/dashboard/provider" element={<ProviderDashboard />} />
  </Route>

  <Route path="*" element={<NotFound />} />
</Routes>
```

---

## 5. Session State & Authentication Flow

### 5.1 Token Security
To balance convenience with defense against Cross-Site Scripting (XSS) and Session Hijacking:
*   **In-Memory Access Token**: The `accessToken` is stored in the React `AuthContext` state. It is not saved in localStorage.
*   **LocalStorage Refresh Token**: The `refreshToken` is saved in `localStorage` under a unique key (`artezans_refresh`).
*   **Session Restore**: On page mount, `AuthContext` checks if a `refreshToken` is present in `localStorage`. If found, it automatically calls the token refresh endpoint to bootstrap the `accessToken` and user info into memory.

### 5.2 Axios Token Refresh Interceptor
An interceptor intercepts all API calls to verify authorization. If a request returns `401 Unauthorized` (indicating the Access Token has expired):
1.  It catches the failure.
2.  It queues any subsequent requests.
3.  It fires a single request to the refresh token endpoint (`POST /api/v1/auth/refresh`) using the `Authorization: Bearer <refreshToken>` header.
4.  On success:
    *   It stores the new `accessToken` in memory.
    *   It retries all queued requests with the updated token.
5.  On failure (refresh token expired/revoked):
    *   It clears the session context.
    *   It redirects the user or raises the `LoginModal` to request password sign-in.

---

## 6. Layout & Key Component Specifications

### 6.1 Hybrid Homepage
*   **Header Navigation**: Shows logo, search bar, links, unread notification bell icon, and user profile avatar.
*   **Hero Banner**: Centered design with a glassmorphic look. Contains the search input for services alongside a "Post a Task" CTA.
*   **Visual Pathway Grid**: Two distinct cards comparing listing search vs task posting.
*   **Recent Task Feed**: Sidebar or bottom component showing the last 5 tasks looking for bids. Polled every 30 seconds using TanStack Query to give the page an active, live feel.

### 6.2 Quick-Auth Modal
*   Renders inside an HTML5 `<dialog>` component or customized overlay with `backdrop-filter: blur(10px)`.
*   Features a sliding tab control to toggle between "Login" and "Sign Up".
*   If triggered during a user action (e.g., booking a listing), it stores the target action details in state and automatically executes it once authentication finishes.

### 6.3 Tabbed Customer Dashboard
The customer panel is presented as a tabbed navigation interface:
*   **Tab 1: Posted Tasks**: Lists customer-created tasks. Clicking a task opens a details side-drawer showing:
    *   Budget, descriptions, and active status.
    *   **Bids Feed**: Lists all provider proposals. Clicking "Accept Offer" locks the provider in and creates an unpaid booking.
*   **Tab 2: Active Bookings**: Shows bookings with their current states.
    *   **Checkout Panel**: When a booking is `ACCEPTED` (unpaid), it displays an embedded checkout option. Users select Credit Card (Stripe Elements iframe) or PayPal (SDK button checkout).
*   **Tab 3: Settings**: General details, profile picture upload (Cloudinary integration), and notification settings.

---

## 7. Payments & Checkout Integration

The checkout process occurs inside the booking panel using Stripe and PayPal REST libraries:
*   **Stripe**: Uses `@stripe/react-stripe-js` to render the `CardElement`. The frontend coordinates with the backend's Stripe endpoint (`/api/v1/stripe/payment-intent`) to retrieve the `client_secret` and calls `stripe.confirmCardPayment` directly inside the app.
*   **PayPal**: Uses the official PayPal JavaScript SDK (configured dynamically with the client ID). Renders the smart buttons container inside the checkout widget to complete authorizations securely.

---

## 8. Notification System

*   **App Notifications**: Polled or pushed (using SSE/polling) to keep the header bell count updated.
*   **Bell Dropdown**: Lists the last 5 notifications. Clicking a notification redirects to the corresponding task or booking, marking it as read on the backend (`PATCH /api/v1/notifications/{id}/read`).
*   **Toast Alerts**: Component mounted at the root of the layout. Triggered via the `ToastContext` API, displaying 4-second animated toasts in the corner of the screen for real-time user events.

---

## 9. Verification & Testing Strategy

### Automated Tests
*   **Unit Tests**: Test core components (buttons, search inputs, modal triggers) using React Testing Library.
*   **State & Hook Tests**: Test interceptor logic and auth-context hooks to confirm silent refresh rotation and access controls.

### Manual Verification
*   Verify loading states, token expiration simulation (manual deletions in console), and mock payments inside staging/test environments.
