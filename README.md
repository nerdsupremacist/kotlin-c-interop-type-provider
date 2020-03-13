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
