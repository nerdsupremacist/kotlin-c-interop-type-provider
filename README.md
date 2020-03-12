# kotlin-c-interop-type-provider

Type Provider that lets you import C-Headers inside of Kotlin Scripting

## Example

Use `@Include` with a def file and it will create stubs to call all the methods of that C Library directly from Kotlin

```kotlin
@file:Include("math.def")

println("hello world")
println("erf(π) = ${erf(M_PI)}")
```

## Usage

coming soon
