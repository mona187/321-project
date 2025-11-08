FROM node:20.6-alpine AS builder

WORKDIR /app

COPY package.json ./
RUN npm ci

COPY . .
RUN npm run build


FROM node:20.6-alpine

WORKDIR /app

COPY --from=builder /app/package.json ./
RUN npm ci --omit=dev

COPY --from=builder /app/dist ./dist

EXPOSE 3000

CMD ["node", "dist/server.js"]
