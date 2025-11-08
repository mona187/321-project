ARG PORT=3000
FROM node:20.6-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# Build TypeScript code
RUN npm run build

# Set PORT as environment variable for runtime
ENV PORT=${PORT}

EXPOSE ${PORT}

CMD ["npm", "start"]
