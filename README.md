# REST API Versioning with Spring Web MVC

API versioning is a complicated topic and has many aspects that should be thought about before implementing a solution.

## Conceptual considerations

Before looking into technical implementations a little of theory...

### Business Domains

APIs are normally structured according to the [business domain](https://en.wikipedia.org/wiki/Domain-driven_design) 
an entity belongs to. This normally results in an easy-to-understand API structure and fulfills basic concepts 
of the [API Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html).

Typical domains with their entities are:

```
User - Domain:
- User
  
Company - Domain:
- Company
- Employee
  
Project - Domain:
- Project
- Task
```

### Services & APIs

Each business domain is responsible for a set of entities. In a microservice landscape this is normally represented 
by (at least) one service per business domain.

Over time entities and their endpoints evolve so incompatible changes require new versions of endpoints.
For some time old and new versions normally have to run in parallel until all clients are updated.

Services and their APIs could look like:
```
user-service:
/v1/users
/v2/users

company-service:
/v1/companies

project-service:
/v1/projects
/v1/tasks
/v1/tasks/descriptions
/v2/tasks/descriptions
```

### API versioning schemes

#### Individual endpoint versioning

Looking at the `project-service`'s endpoints in the past section you may have recognized that different endpoints 
of different entities have different Max-API versions.

```
project-service:
/v1/projects
...
/v2/tasks/descriptions
```

This is one possible way how to version APIs. From a backend developer perspective, this approach is pretty nice and 
simple as each endpoint is versioned individually on demand.

From a client perspective this is a totally different story. If data from different endpoints relate to each other,
changes have to be tracked, to figure out which endpoints remain compatible to each other. 
This can become a nightmare if you have lots of endpoints that change regularly.

#### Centralized managed versioning
The total opposite would be to maintain one max-api version centrally and all services use it for all endpoints.
If one endpoint requires an incompatible change, i.e. a new api version is required, then all other endpoints in the 
system required a new (useless) version as well (highlighted with a * below).

```
user-service:
/v1/users
/v2/users

company-service:
/v1/companies
/v2/companies*

project-service:
/v1/projects
/v2/projects*
/v1/tasks
/v2/tasks*
/v1/tasks/descriptions
/v2/tasks/descriptions
```

From a backend developer's perspective this is a nightmare. Going through all service of a whole system for each
change is not a good idea. It becomes even worse if different services are maintained by different teams 
(organization, responsibility, ...).
Of course - from a client perspective this is really nice if only one API version is required for a whole system.

#### Versioning by domain / service

A good compromise is to maintain only one API version for all APIs that belong to one business domain.

```
project-service:
/v1/projects
/v2/projects*
/v1/tasks
/v2/tasks*
/v1/tasks/descriptions
/v2/tasks/descriptions
```

This obviously also requires useless duplicate versions of endpoints (*), but it is limited to one domain.

## Technical implementation

In the examples above we implicitly assumed that APIs are versioned by URL. This is just one approach, another one 
could be HTTP-Header based API versioning where the API version information is transmitted in the HTTP-Headers. 
There are different ways how API versioning can be implemented - the challenges remain the same or are similar.

In the subsequent sections the URL based versioning with Spring Web MVC is shown.

### Manual API versioning

Since there's no API Versioning mechanism in Spring Web MVC built-in, you have to write the api versions into the URLs.

Source: [io.nvtc.webmvc.user.api.UserController](src/main/kotlin/io/nvtc/webmvc/user/api/UserController.kt)
```kotlin
@RestController
class UserController {

  @GetMapping("/v2/users", "/v3/users") fun getUser() = ...

  @GetMapping("/v1/users") fun getUserV1() = ...
}
```

### Automatic API versioning

As explained in the theory section above, it's not so ideal to have versions hardcoded in the endpoints as this
requires manual adjustments when a new version has to be introduced in a service.

Ideally we would only need to add an annotation to the rest controller / endpoint to specify until / from which API 
version requests are handled by the annotated code.

#### Multiple versions handled by one controller

Source: [io.nvtc.webmvc.project.api.ProjectController](src/main/kotlin/io/nvtc/webmvc/project/api/ProjectController.kt)
```kotlin
@ApiVersioned
@RestController
class ProjectController {

  @GetMapping("/projects") fun getProjects() = ...
}
```

#### Different versions are handled by different controllers

Source: [io.nvtc.webmvc.project.api.TaskController.kt](src/main/kotlin/io/nvtc/webmvc/project/api/TaskController.kt)
```kotlin
@ApiVersioned(UntilVersion = 1)
@RestController
@RequestMapping("/tasks")
class TaskControllerV1 {
    @GetMapping fun getTasks() = ...
}

@ApiVersioned(FromVersion = 2)
@RestController
@RequestMapping("/tasks")
class TaskController {
    @GetMapping fun getTasks() = ...
}
```

#### Different versions handled by one controller

Source: [io.nvtc.webmvc.project.api.TaskDescriptionController](src/main/kotlin/io/nvtc/webmvc/project/api/TaskDescriptionController.kt)
```kotlin
@RestController
class TaskDescriptionController {

    @ApiVersioned(UntilVersion = 1)
    @GetMapping("/tasks/descriptions")
    fun getDescriptionsV1() = ...

    @ApiVersioned(FromVersion = 2)
    @GetMapping("/tasks/descriptions")
    fun getDescriptions() = ...
}
```

#### Implementation
Spring Web MVC offers the possibility to customize the URL to endpoint mapping by registering a custom implementation
of `RequestMappingHandlerMapping`.
By default, spring takes the URl that is defined in a `@RequestMapping`/ `@GetMapping` / ... and maps the endpoint to
the URL.
To implement the "automated" API versioning as described above, the multiplication of mappings for the different API 
versions can be implemented as follows:

Source: [io.nvtc.webmvc.config.ApiVersioningRequestMappingHandlerMapping](src/main/kotlin/io/nvtc/webmvc/config/ApiVersioningRequestMappingHandlerMapping.kt)

```kotlin
class ApiVersioningRequestMappingHandlerMapping : RequestMappingHandlerMapping() {
  override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
    // get original mapping information  
    val superMapping = super.getMappingForMethod(method, handlerType) ?: return null

    // handle annotations defined on methods
    AnnotationUtils.findAnnotation(method, ApiVersioned::class.java)?.let {
      val requestCondition = getCustomMethodCondition(method)
      return getApiVersionInfo(method, it, requestCondition).combine(superMapping)
    }

    // handle annotations defined on classes
    AnnotationUtils.findAnnotation(handlerType, ApiVersioned::class.java)?.let {
      val requestCondition = getCustomTypeCondition(handlerType)
      return getApiVersionInfo(method, it, requestCondition).combine(superMapping)
    }

    // handle unversioned endpoints
    return super.getMappingForMethod(method, handlerType)
  }
    private fun getApiVersionInfo(
            method: Method,
            annotation: ApiVersioned,
            requestCondition: RequestCondition<*>?
    ): RequestMappingInfo {
        // multiply the mapping
    }

    companion object {
        const val VERSION_PREFIX = "/v"
        const val MIN_API_VERSION = 1
        const val MAX_API_VERSION = 3
    }
}
```

Introducing a new maximum-api-version is simple by increasing the `MAX_API_VERSION` number. All endpoints are then also 
available under the new API version.

The `ApiVersioningRequestMappingHandlerMapping` is registered in a `WebMvcRegistrations` bean.

Source: [io.nvtc.webmvc.config.WebMvcConfiguration](src/main/kotlin/io/nvtc/webmvc/config/WebMvcConfiguration.kt)
```kotlin
@Bean
fun webMvcRegistrations(): WebMvcRegistrations {
  return object : WebMvcRegistrations {
    override fun getRequestMappingHandlerMapping() = ApiVersioningRequestMappingHandlerMapping()
  }
}
```

## License
The source code to showcase the API versioning with Spring Web MVC in this repository is licensed under the 
MIT-License ([LICENSE](LICENSE) or [http://opensource.org/licenses/MIT]).