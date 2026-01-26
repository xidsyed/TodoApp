## Code Snippets to instantiate Admin 
```kotlin
// before user created
return Ok(Unit)
// custom access token hook
userRepository.save(
	UserEntity(
		userId = jwt.uuid,
		email = jwt.claims.email,
		displayName = jwt.claims.userMetadata.fullName,
		profilePic = jwt.claims.userMetadata.avatarUrl,
		role = NewzroomRoleEntity.ADMIN,
	)
)
return Ok(jwt.run { copy(claims = claims.copy(appRole = NewzroomRole.ADMIN)) })
```

## Setup Secrets
```.yaml

// src/main/kotlin/resources/secrets.yaml
spring:
  r2dbc:
    username: ****
    password: ****

secret:
  webhook:
    sources:
      supabase: whsec_****
  management:
    SWAGGER:
      - username: ****
        password: ****

```