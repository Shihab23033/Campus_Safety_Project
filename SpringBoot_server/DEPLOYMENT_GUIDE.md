# Campus Safety - Deployment Guide

## Prerequisites

### System Requirements
- Java 17 or higher
- MySQL 8.0+
- Git
- Node.js/npm (for frontend integration)

### External Services
- Gmail account with App Password (for email notifications)
- Firebase project with service account credentials
- Render.com or Railway.app account (for deployment)

---

## Local Development Setup

### 1. Database Setup

```bash
# Create MySQL database
mysql -u root -p

# In MySQL terminal:
CREATE DATABASE campus_safety;
CREATE USER 'campus_user'@'localhost' IDENTIFIED BY 'CampusSafety123!';
GRANT ALL PRIVILEGES ON campus_safety.* TO 'campus_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Environment Configuration

Create `application-dev.properties`:

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/campus_safety
spring.datasource.username=campus_user
spring.datasource.password=CampusSafety123!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=your_super_secret_key_must_be_at_least_32_characters_long_change_this_in_production
jwt.expiration=86400000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
mail.from=noreply@campussafety.com

# File Upload
file.upload.dir=./uploads/audio/
file.upload.max-size=52428800

# Firebase
firebase.config.path=./firebase-config.json

# CORS
cors.allowed-origins=localhost:3000,localhost:4200,localhost:8081

# Logging
logging.level.root=INFO
logging.level.com.mbstu.campussafety=DEBUG
```

### 3. Run Application Locally

```bash
cd SpringBoot_server

# Build
./gradlew build -x test

# Run
./gradlew bootRun

# Application will be available at: http://localhost:8080/api
# Swagger UI: http://localhost:8080/api/swagger-ui.html
```

---

## Firebase Configuration

### Setup Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project or select existing one
3. Go to Project Settings → Service Accounts
4. Click "Generate New Private Key"
5. Save the JSON file as `firebase-config.json` in project root:

```
SpringBoot_server/
├── firebase-config.json  (place here)
├── src/
└── build.gradle
```

### Firebase Configuration in Application

```properties
# application.properties
firebase.config.path=./firebase-config.json
firebase.database-url=https://your-project.firebaseio.com
```

---

## Production Deployment

### Option 1: Deploy to Render.com

#### 1. Prepare Project for Deployment

```bash
# Add build.gradle configurations for production
```

Add to `build.gradle`:

```gradle
tasks.named('bootJar') {
    archiveFileName = 'app.jar'
}
```

#### 2. Create Render Configuration

Create `render.yaml`:

```yaml
services:
  - type: web
    name: campus-safety-api
    env: java
    buildCommand: ./gradlew build -x test
    startCommand: java -jar build/libs/app.jar
    envVars:
      - key: JAVA_TOOL_OPTIONS
        value: -Xmx1g
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: SPRING_DATASOURCE_URL
        fromDatabase:
          name: campus-safety-db
          property: connectionString
  
  - type: mysql
    name: campus-safety-db
    plan: standard
    ipAllowList:
      - source: 0.0.0.0/0
        description: Allow all
```

#### 3. Environment Variables on Render

Set these environment variables in Render dashboard:

```
SPRING_DATASOURCE_URL=mysql://user:password@host:3306/campus_safety
SPRING_DATASOURCE_USERNAME=dbuser
SPRING_DATASOURCE_PASSWORD=dbpassword
JWT_SECRET=production_secret_key_at_least_32_chars_minimum
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=gmail-app-password
FIREBASE_CONFIG_PATH=/var/data/firebase-config.json
CORS_ALLOWED_ORIGINS=yourdomain.com,www.yourdomain.com
```

#### 4. Upload Firebase Config

1. Create `/var/data/` directory
2. Upload `firebase-config.json` to Render

#### 5. Deploy

```bash
# Push to git repository connected to Render
git push origin main
```

---

### Option 2: Deploy to Railway.app

#### 1. Connect Repository

1. Go to [Railway.app](https://railway.app)
2. Create new project
3. Connect GitHub repository
4. Select `SpringBoot_server` as root directory

#### 2. Configure Build Settings

In Railway dashboard:

- Start command: `./gradlew bootRun`
- Build command: `./gradlew build -x test`

#### 3. Set Environment Variables

```
JAVA_OPTS=-Xmx1g
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=mysql://user:password@host/campus_safety
JWT_SECRET=your_production_secret_key
```

#### 4. Add MySQL Database Plugin

1. In Railway project, add MySQL plugin
2. Railway will auto-populate database URL

#### 5. Deploy

Railway auto-deploys on git push to main branch.

---

### Option 3: Docker Containerization

#### 1. Create Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
COPY firebase-config.json .

RUN chmod +x ./gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=0 /app/build/libs/app.jar .
COPY --from=0 /app/firebase-config.json .

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

#### 2. Create docker-compose.yml

```yaml
version: '3.8'

services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: campus_safety
      MYSQL_USER: campus_user
      MYSQL_PASSWORD: CampusSafety123!
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build: .
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/campus_safety
      SPRING_DATASOURCE_USERNAME: campus_user
      SPRING_DATASOURCE_PASSWORD: CampusSafety123!
      JWT_SECRET: development_secret_key_change_in_production
    ports:
      - "8080:8080"
    depends_on:
      - db

volumes:
  mysql_data:
```

#### 3. Build and Run

```bash
docker-compose up --build
```

---

## Database Migration

### Initial Setup (Auto with Hibernate)

Hibernate will auto-create tables on first run with:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Manual Migration Scripts

For production, create migration scripts:

```sql
-- V1__Initial_Schema.sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    is_verified BOOLEAN DEFAULT FALSE,
    latitude DOUBLE,
    longitude DOUBLE,
    fcm_token VARCHAR(255),
    last_active TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Additional tables...
```

---

## Production Configuration

### application-prod.properties

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# Database (Use managed service)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}

# File Upload
file.upload.dir=/app/uploads/
file.upload.max-size=52428800

# Firebase
firebase.config.path=${FIREBASE_CONFIG_PATH}

# CORS
cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

# Logging
logging.level.root=WARN
logging.level.com.mbstu.campussafety=INFO

# Performance
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

---

## Monitoring & Logging

### Enable Spring Boot Actuator

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Actuator Endpoints

```
GET /actuator/health
GET /actuator/metrics
GET /actuator/env
GET /actuator/loggers
```

### Logging Configuration

Create `logback-spring.xml`:

```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

---

## Security Checklist

### Pre-Deployment

- [ ] JWT_SECRET is strong (32+ chars, alphanumeric + symbols)
- [ ] Database password is strong and changed
- [ ] Email credentials stored securely
- [ ] Firebase service account credentials protected
- [ ] CORS origins set to specific domains (not *)
- [ ] HTTPS enabled
- [ ] Database backups configured
- [ ] Error logging doesn't expose sensitive info

### Post-Deployment

- [ ] SSL certificate installed
- [ ] Database encrypted
- [ ] Regular backups scheduled
- [ ] Monitoring alerts configured
- [ ] API rate limiting implemented
- [ ] Security headers set (HSTS, CSP)

---

## Troubleshooting

### Issue: Database Connection Failed
```
Solution:
1. Check MySQL is running
2. Verify credentials in application.properties
3. Check network firewall
4. Test connection: mysql -h host -u user -p
```

### Issue: JWT Token Invalid
```
Solution:
1. Verify JWT_SECRET is same across instances
2. Check token expiration time
3. Ensure clock sync between servers
```

### Issue: Email Not Sending
```
Solution:
1. Verify Gmail app password (not main password)
2. Enable "Less secure app access" if needed
3. Check SMTP settings (smtp.gmail.com:587)
4. Test with curl: telnet smtp.gmail.com 587
```

### Issue: Firebase Push Notifications Not Working
```
Solution:
1. Verify firebase-config.json exists
2. Check FCM tokens are registered
3. Test Firebase connection: firebase-admin verify
```

---

## Backup & Recovery

### Database Backup

```bash
# Backup
mysqldump -u user -p database_name > backup.sql

# Restore
mysql -u user -p database_name < backup.sql
```

### Automated Backups

```bash
#!/bin/bash
# backup.sh
BACKUP_DIR="/backups"
DB_NAME="campus_safety"
DB_USER="campus_user"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/backup_$TIMESTAMP.sql

# Keep only last 7 days
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete
```

---

## Performance Optimization

### Database Indexes

```sql
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_alert_status ON emergency_alerts(status);
CREATE INDEX idx_location_user ON locations(user_id, created_at);
CREATE INDEX idx_message_recipient ON chat_messages(recipient_id);
```

### Connection Pooling

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

### Caching (Redis - Optional)

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

---

## Monitoring Commands

```bash
# Check application logs
tail -f logs/application.log

# Monitor system resources
watch -n 1 free -m
watch -n 1 'ps aux | grep java'

# Database query monitoring
SHOW PROCESSLIST;
SHOW INNODB STATUS;

# API health check
curl http://localhost:8080/api/actuator/health
```

---

## Rollback Procedure

If deployment fails:

```bash
# Render.com
render.com dashboard → Select deployment → Rollback

# Railway.app
railway.app dashboard → Deployments → Redeploy previous version

# Docker
docker-compose down
docker image rm old_image:latest
docker-compose up -d
```

---

**Last Updated**: 2024  
**Version**: 1.0.0  
**Maintainer**: Campus Safety Team
