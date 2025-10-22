## backend directory


Commands to run:

# Core framework
npm install express cors dotenv

# TypeScript
npm install -D typescript @types/node @types/express @types/cors ts-node nodemon

# MongoDB
npm install mongodb mongoose

# Authentication
npm install google-auth-library jsonwebtoken
npm install -D @types/jsonwebtoken

# Real-time communication
npm install socket.io
npm install -D @types/socket.io

# Validation & utilities
npm install zod
npm install axios

# Firebase Admin (for push notifications)
npm install firebase-admin






# Folder Structure
- src
    - config
        - database.ts   **done**
        - firebase.ts   **done**
        - socket.ts     **done**
    - controllers
        - auth.controller.ts            **done**
        - group.controller.ts           **done**
        - matching.controller.ts        **done**
        - restaurant.controller.ts      **done**
        - user.controller.ts            **done**
    - middleware
        - auth.middleware.ts     **done**
        - errorHandler.ts        **done**
    - models
        - CrediblityLog.ts      **done**
        - Group.ts              **done**
        - Room.ts               **done**
        - User.ts               **done**   
    - routes
        - auth.routes.ts        **done**
        - group.routes.ts       **done**
        - matching.routes.ts    **done**
        - restaurant.routes.ts
        - user.routes.ts        **done**
    - services
        - authService.ts            **done**
        - crediblityService.ts      **done**
        - groupService.ts           **done**
        - matchingService.ts        **done**
        - notificationService.ts    **done**
        - restaurantService.ts      **done**
        - userService.ts            **done**
    - types
        - index.ts                 **done**
    - utils
        - socketManager.ts          **done**
    - server.ts                     **done**