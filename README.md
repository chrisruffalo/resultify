# Resultify: A Simple Result Handling Framework

## Overview
Resultify is a simple framework for taking the result that you got from some other code and
safely handling it without exceptions resulting in a more natural and readable way of 
handling errors that _can_ be handled.

### Goals
* To simplify the handling of exceptions
* Use plain java without additional third party libraries
* Provide result monads and reduce null handling boilerplate
* Provide similar methods to `java.lang.Optional` and related types

### Non-Goals
* To be a general-use error handling framework

## Quick Example
Here is a quick example, that is covered in more detail further in:
```java
import io.github.chrisruffalo.resultify.Result;

String input = "22";
int value = Result.of(input).map(Integer::parseInt).failsafe(0).get();
```
This will allow a provided input (`22`) to be parsed as an integer and
then, if there is a failure or no value is provided, a failsafe value
is provided. The final `get()` call will retrieve the value from the
`Result` type.

## Why Not Use...
Yes, there are other patterns for this. There are better libraries in Java for this. I wrote this
because I wanted something that worked the way I thought and that way has been very heavily influenced
by Mutiny, Go, and Java's own Optional. If you like this and the way I think then go with it. Otherwise
there are other libraries that may fit your mental model better. Ultimately, as discussed further in
this README, the most important thing is that whatever you choose reduces the cognitive overhead
for you (and your team).

## Philosophy
One of the more frustrating examples that I see regularly is parsing a value to
an integer. In the general case I feel like I end up doing something like this:
```java
String input = "0";
int value;
try {
    value = Integer.parseInt(input);
} catch (Exception ex) {
    value = 0;
}
```

This works just fine but I tend to find myself writing utility functions and
other wrappers for it to create a "safe" value. The main issue with that approach
isn't verbosity or complexity. It has more to do with readability and the
approach to what exceptions are _for_. In this case it stands in as a proxy for
"if parseable" which moves it into flow control.

Take a look at the following example:
```java
String input = "0";
int value = Result.of(input).map(Integer::parseInt).failsafe(0).get();
```

This accomplishes a few things and the first to notice is that it isn't actually 
that much different in verbosity. What it does do is lay out a direct line of
what we are doing without need for blocks. Given an input, a mapping function,
and a failsafe value we want the final result. 