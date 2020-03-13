# kotlin-c-interop-type-provider

Type Provider that lets you import C-Headers inside of Kotlin Scripting

## Example

Use `@Include` with a .def or .h file and it will create stubs to call all the methods of that C Library directly from Kotlin

### Using the C Standard Library

```kotlin
@file:Include("math.h")

println("erf(π) = ${erf(M_PI)}")
```

### Using your own Code

If you wrote some code in C that you want to use in your Kotlin script, just reference the header file and it will be integrate.
For example you wrote a header file `foo.h`:

```h
int foo();
```

And an implementation file `foo.c`:

```c
#include "test.h"

int foo() {
    return 42;
}
```

Just include the header file:

```kotlin
@file:Include("foo.h")

println("foo() = ${foo()}") // 42
```

### Using a Def File

If you already have a .def file you can also include it.

For example if you have the following .def file `curl.def` that will import curl:

```
headers = curl/curl.h
headerFilter = curl/*
linkerOpts = -L/opt/local/lib -L/usr/local/opt/curl/lib -lcurl
``` 

Then you can include it:

```kotlin
@file:Include("curl.def")

val curl = curl_easy_init()
// do more stuff with this object
``` 

## Usage

coming soon

## How does it work?

This Script Definition relies on a custom version of the Kotlin Native [C Interop Tool](https://kotlinlang.org/docs/reference/native/c_interop.html). 
You can find it under `native/` 

The logic behind this is to:
1. Find or create a .def file for the included header
    1. In case of a local header file with an implementation, we look for the implementation and compile it using clang
    1. options for how to compile the implementation are available in `compilerOpts` and `linkerOpts`
1. Run the C Interop Tool with the `jvm` flavor. 
    1. The Kotlin Native Command Line tools do not allow to use the JVM as a flavor.
    1. However it is possible if you call the following function: [main](https://github.com/JetBrains/kotlin-native/blob/da0e56edea47622c751b06378e31b6587dc74887/Interop/StubGenerator/src/main/kotlin/org/jetbrains/kotlin/native/interop/gen/jvm/main.kt#L40)
    1. This is why we need the custom version of Kotlin Native
1. Update the classpath of the script to include Kotlin Native
1. Update the library search path for the JVM, so that it can find the generated stubs library
1. Import the generated kotlin stubs for the library

## Disclaimer

Due to the fact that the scripts are running on the JVM, this only supports interoperability with C. 
Objective-C is not supported yet inside the JVM yet
