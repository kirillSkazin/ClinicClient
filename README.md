# Клиент

## Запуск

### 1. Запустите сервер

Рекомендуемый способ - Docker из проекта `Server`:

```powershell
cd Server
copy .env.example .env
docker compose up --build
```

Если запускаете сервер без Docker:

```powershell
cd Server
mvn clean package
java -jar target/clinic-server.jar
```

### 2. Запустите клиент

Из IDE: Run на классе `Launcher`.

Или из терминала:

```powershell
cd Client
mvn javafx:run
```

### 3. Сборка fat-jar

```powershell
cd Client
mvn clean package
java -jar target/clinic-client.jar
```

> Класс `Launcher` нужен именно для fat-jar (без него JVM ругается:
> "JavaFX runtime components are missing").

