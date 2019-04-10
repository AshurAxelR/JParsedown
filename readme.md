# JParsedown

Lightweight Markdown Parser in Java: [library](#jparsedown-library) or [command line tool](mdtool/readme.md)

## JParsedown Library

**JParsedown** is a lightweight single-file library for converting Markdown to HTML format.
The library is translated from [Parsedown](https://github.com/erusev/parsedown) PHP library
(version 1.8.0-beta-5) and preserves its features:

* One file
* No dependencies
* [GitHub flavored](https://help.github.com/articles/github-flavored-markdown)
* [Fast](#performance)
* [MIT licence](LICENSE)

The library is compliant with **Java 7+**.

Additinoal features of JParsedown that are not (yet) available in the original Parsedown:

* Github-compatible [Header IDs](#header-ids)
* [Page title detection](#page-title-detection)


### Download

**Source file:** [JParsedown.java](src/com/xrbpowered/jparsedown/JParsedown.java)

**JAR file:** [jparsedown-1.0.2.jar](https://github.com/ashurrafiev/JParsedown/releases/download/1.0.2/jparsedown-1.0.2.jar) (50.4 KB)

### Usage

```java
JParsedown parsedown = new JParsedown();
System.out.println(parsedown.text("Hello _Parsedown_!")); // prints: <p>Hello <em>Parsedown</em>!</p>
```

You can also parse inline markdown only:

```java
System.out.println(parsedown.line("Hello _Parsedown_!")); // prints: Hello <em>Parsedown</em>!
```

### Security

See [Parsedown Security](https://github.com/erusev/parsedown#security) page.


### Header IDs

Github automatically generates anchor IDs for each header in Markdown file to make it
easier to reference individual sections and create the table of contents. JParsedown attempts to generate
the same IDs, so the itra-page links in rendered HTML page still work like on Github.

For example, `## Header IDs` creates the following HTML:

```
<h2 id="header-ids">Header IDs</h2>
```

and can be referenced as follows:


```
[Header IDs](#header-ids)
```

ID generation in JParsedown follows these rules:

1. The header text is converted to lower case.
1. All characters other than letters, numbers, or whitespaces are removed.
1. Whitespaces are replaced with dashes `-`.
1. ID is URL-encoded to handle Unicode letters.
1. Duplicate IDs have a dash and a number appended: `header-ids`, `header-ids-1`, `header-ids-2`, etc.


### Page Title Detection

JParsedown provides the `title` string available after calling `text()` method:

```java
JParsedown parsedown = new JParsedown();
parsedown.text("# My Title\n\nMore text...");
System.out.println(parsedown.title); // prints: My Title
```

The string contains the best candidate for HTML page title, which is the first highest level header.
For example, if the page has no level-1 header, but has several level-2 headers, the first of them
will be the title.

If the page does not contain any headers, `title` will be `null`.

> **Note:** The Markdown in the title is not stripped or processed.


### Performance

Benchmark results:

| test file | repeat | JParsedown | [Parsedown](https://github.com/erusev/parsedown) (PHP) | [flexmark-java](https://github.com/vsch/flexmark-java) |
| :--- | ---: | ---: | ---: | ---: |
| [cheatsheet.md](mdtool/cheatsheet.md) | &times;100 | **4.4 ms** per item | **5.5 ms** per item (&times;1.25) | **6.2 ms** per item (&times;1.41) |
| [cheatsheet.md](mdtool/cheatsheet.md) | &times;1000 | **2.4 ms** per item | **5.4 ms** per item (&times;2.25) | **2.4 ms** per item (&times;1.00) |

The benchmarking does not consider saving and loading times. Only `text()` function is measured.

> At the moment, JParsedown is not properly performance optimised.
> Speedup against the origial Parsedown is due to Java vs PHP performance difference.
> Also note how JIT really helps Java with large batches of work.

## MD Tool

**MD** tool is a JParsedown-based command line tool for converting Markdown files into HTML pages.

See [MD Tool Readme](mdtool/readme.md)
