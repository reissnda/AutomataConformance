# AutomataConformance (align-web)

##Build and run for production with Docker

### Build codebase

At the project root, run:

`gradle clean build -x test`

### Build and run Docker image

At the project root, run:

`docker build -t align-web .`

Run with Docker:

To expose the service on port 8091, run:

`docker run -d -p 8091:8080 align-web`

To check service health, call:

http://localhost:8091/actuator/health 

Expected response:

```json
{
  status: "UP"
}
```

