# Stage 1: Build a aplicação com Gradle
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia apenas os arquivos de build para aproveitar o cache do Docker
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Garante que o wrapper do Gradle seja executável
RUN chmod +x gradlew

# Copia o código-fonte
COPY src src

# Executa o build da aplicação, pulando os testes
RUN ./gradlew clean bootJar -x test

# Stage 2: Cria a imagem final de execução (runtime)
FROM eclipse-temurin:21-jre-alpine AS run
WORKDIR /app

# Copia o .jar da imagem de build para a imagem final
COPY --from=build /app/build/libs/*.jar gatekeeper-backend.jar

# Expõe a porta que a aplicação Spring Boot usa
EXPOSE 8080

# Define o comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "gatekeeper-backend.jar"]
