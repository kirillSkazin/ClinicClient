# Клиент

## Запуск

### 1. Запустите сервер

Рекомендуемый способ - Docker из проекта `Server`:

```powershell
cd ClinicServer
copy .env.example .env
docker compose up --build
```

Если запускаете сервер без Docker:

```powershell
cd ClinicServer
mvn clean package
java -jar target/clinic-server.jar
```

### 2. Запустите клиент

Из IDE: Run на классе `Launcher`.

Или из терминала:

```powershell
cd ClinicClient
mvn javafx:run
```

### 3. Сборка fat-jar

```powershell
cd ClinicClient
mvn clean package
java -jar target/clinic-client.jar
```

> Класс `Launcher` нужен именно для fat-jar (без него JVM ругается:
> "JavaFX runtime components are missing").

