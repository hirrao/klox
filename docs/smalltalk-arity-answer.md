# Why Smalltalk Doesn't Need Runtime Arity Checking

## Question
Our interpreter carefully checks that the number of arguments passed to a function matches the number of parameters it expects. Since this check is done at runtime on every call, it has a performance cost. Smalltalk implementations don't have that problem. Why not?

## Answer

Smalltalk implementations avoid the runtime arity checking overhead because **the method name itself encodes the parameter count**. In Smalltalk, the number of parameters is intrinsic to the method selector (name), making it impossible to call a method with the wrong number of arguments.

### How Smalltalk Method Names Work

In Smalltalk, method names (called "selectors") include colons that indicate parameter positions:

- **Zero parameters**: `size` (no colons)
- **One parameter**: `add:` (one colon)
- **Two parameters**: `at:put:` (two colons)
- **Three parameters**: `replaceFrom:to:with:` (three colons)

The number of colons **is** the arity. Each colon marks where an argument goes.

### Example

```smalltalk
"Method with 2 parameters"
dict at: 'key' put: 'value'

"The selector is: at:put:"
"It has 2 colons, so it expects exactly 2 arguments"
```

If you try to call this method with the wrong number of arguments:
- `dict at: 'key'` would look for a method named `at:` (1 parameter) - a different method!
- `dict at: 'key' put: 'value' with: 'extra'` would look for `at:put:with:` (3 parameters) - also a different method!

### Why This Eliminates Runtime Arity Checks

When a Smalltalk program calls a method:

1. The runtime looks up the method by its **complete selector** (including all colons)
2. If a method with that exact selector exists, it by definition has the correct arity
3. If it doesn't exist, you get a "method not found" error, not an "arity mismatch" error

The arity check happens **implicitly during method lookup**, not as a separate validation step. You can't accidentally call a 2-parameter method with 3 arguments - you'd be calling a completely different method that doesn't exist.

### Contrast with Lox

In our Lox interpreter:

1. Functions have names independent of their parameter count
2. You can define `function add(a, b)` and still try to call `add(1, 2, 3)`
3. We must check at runtime: `if (arguments.size != function.arity)` (see `Interpreter.kt`, lines 90-97)
4. This check happens on **every function call**, adding overhead

### Performance Impact

- **Lox**: Requires an explicit comparison on every call: `O(1)` overhead per call
- **Smalltalk**: Arity is validated during method lookup, which must happen anyway: `O(0)` additional overhead

The Smalltalk design makes arity mismatches a **static** property that's caught during method resolution, while Lox treats it as a **dynamic** property that must be validated at runtime.

## Related Code

The runtime arity check in our interpreter can be found in:
- `/src/main/kotlin/com/hirrao/klox/interpreter/Interpreter.kt` (lines 90-97)
- The `arity` property is defined in `/src/main/kotlin/com/hirrao/klox/interpreter/LoxCallable.kt`

## References

This is a design question from the "Crafting Interpreters" book by Robert Nystrom, which explores different implementation strategies for dynamic languages.
