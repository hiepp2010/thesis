FROM node:20-alpine AS build
WORKDIR /app

# Copy package.json files first for better caching
COPY package*.json ./
RUN npm ci

# Copy the rest of the code
COPY . .

# Build the Next.js app
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app

# Set environment to production
ENV NODE_ENV=production

# Copy necessary files from build stage
COPY --from=build /app/package*.json ./
COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
COPY --from=build /app/node_modules ./node_modules

# Start the Next.js app
EXPOSE 3000
CMD ["npm", "start"] 