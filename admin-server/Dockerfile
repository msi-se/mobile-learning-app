FROM node:14

WORKDIR /app

COPY . .

RUN npm ci

EXPOSE 3000

ENTRYPOINT ["node", "index.js"]