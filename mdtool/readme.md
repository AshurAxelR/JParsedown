# MD Tool

**MD** tool is a JParsedown-based command line tool for converting Markdown files into HTML pages.

## Download

[md.zip](https://github.com/AshurAxelR/JParsedown/releases/download/1.0.4/md.zip) (71.4KB)  
contains **md.jar** and templates.

## Usage

```
java -jar md.jar sourcefile [-o outputfile] [options]
```

Recursive mode:

```
java -jar md.jar -r sourcedir [-o outputdir] [options]
```

| option | description |
| :--- | :--- |
| **-o**&nbsp;path | Optional: output path or filename. If **-o** is directory or not specified, the output file name is derived from source file name by replacing the extension with `html` or template's extension. |
| **-t**&nbsp;filename | Optional: HTML template file name. If not specified, the output will contain only the body of HTML. |
| **-s**&nbsp;filename | Optional: CSS stylesheet file name. If not specified, no stylesheet is linked or embedded. |
| **-e** | Optional: embed stylesheet within a `<style>` tag. By default, the stylesheet is linked using `<link>` tag. When linking CSS, the **-s** parameter is used in `href` as is without any path conversion. |
| **-r** | Optional: recursive mode. Processes all MD files in the source directory and its subdirectories. |
| **-m** | Optional: check for modification. The Markdown is processed only if there is no output file or the output file is older than the source, stylesheet, or template. |
| **-u** | Optional: enable [MD links conversion](../readme.md#md-links-conversion). Replacement extension is `.html` or template's extension. |
| **--version** | Print JParsedown library version and exit. |
| **--help** | Print usage help and exit. |

## HTML Template Format

HTML template is an HTML file with `{{ variable }}` and `{{ 'text' }}` inserts. The latter is usually used to escape `{{` sequences, i.e. `{{ '{{' }}`.

> **Note:** The syntax looks like [Jinja 2](http://jinja.pocoo.org) but it is not.
> MDTool can only do straightforward replacement.

Available variables:

| variable | description |
| :--- | :--- |
| **body** | HTML body of the processed Markdown. Normally, this should appear between `<body>` and `</body>`. |
| **title** | Automatically detected HTML page title (see [Page Title Detection](../readme.md#page-title-detection)) without the `<title>` tag. |
| **style** | Stylesheet. Depending on the **-e** option, this is either a `<style>` tag or a `<link>`. Normally, this should appear in the `<head>` section. |

Basic HTML template:

```
<!DOCTYPE html>
<html>
<head>
<title>{{ title }}</title>
{{ style }}
</head>
<body>
{{ body }}
</body>
</html>
```
It is also recommended to use `<meta charset="UTF-8">` as the Markdown processing is done in UTF-8.

## Built-in Templates and Styles

MD tool comes with a set of templates and styles located in `templates` folder.

### Templates

| template | file | description |
| :--- | :--- | :--- |
| **basic** | [basic.html](templates/basic.html) | No layout. |
| **page** | [page.html](templates/page.html) | Two additional `div` containers around Markdown to help with page layout. |

> The templates do not include [highlight.js](https://highlightjs.org/), so there is no syntax highlighting.

### Styles

| style | file | supported templates | preview |
| :--- | :--- | :--- | ---: |
| **ghlike** | [md_ghlike.css](templates/md_ghlike.css) | all | [preview](https://AshurAxelR.github.io/JParsedown/mdtool/templates/preview/cheatsheet_ghlike.html) |
| **jdark** | [md_jdark.css](templates/md_jdark.css) | all | [preview](https://AshurAxelR.github.io/JParsedown/mdtool/templates/preview/cheatsheet_jdark.html) |
| **jlight** | [md_jlight.css](templates/md_jlight.css) | all | [preview](https://AshurAxelR.github.io/JParsedown/mdtool/templates/preview/cheatsheet_jlight.html) |

