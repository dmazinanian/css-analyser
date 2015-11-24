# css-analyser

This tool is designed for analysis and safe refactoring of the CSS code.
Right now, it finds three different types of duplication in CSS files and safely refactores them.

I am developing this tool as an infrastructure for my research on CSS during my PhD studies.

## License

This project is licensed under the MIT License.

## Usage

## Building 
You will need Java 8 installed on your machine for running this tool.
We use [Gradle](http://gradle.org/) as build system.
After installing Gradle, run `gradle build` in the root of the project.
When the build is finished, the generated standalone jar files will be found in `build\distributions`,
inside the zip or tar archives (either of the archives may be used).
For convenience, Gradle generates scripts (inside the `bin` folder in the zip and tar archives) for running the tool
under windows (named `css-analyzer.bat`) or other operating systems (named `css-analyser`).

## Generating project files for Eclipse
You can run `gradle eclipse` to download the dependencies,
generate the Eclipse project files (including `classpath`, `.project`, etc).

### Clone refactoring
This tool supports three modes:

* **Crawl mode** In this mode, tool uses [Crawljax](https://github.com/crawljax/crawljax) 
to crawl web pages of the given url(s). Then it analyzes all the CSS files of the cawled web pages,
with respect to the collected DOM states. <br />
For using this mode, use `--mode:crawl --url:"http://to.be.analyzed" --outfolder:"path/to/analyzed/info/folder"`.
Tool will gather DOM states using Crawljax in the outfolder. It also creates a folder called css, in which 
all the CSS files are saved. 
<br />
It is also possible to use `--urlfile:"path/to/file"`, to provide a list of websites for analysis. Website 
URLs must be given one per line in this file.

* **Folder mode** If a previous data from crawling is available, one may use
`--mode:folder --infolder:"path/to/crawled/data"` to avoid re-crawling of the web pages. Also, the parameter
`--foldersfile:"path/to/list/of/folders"` could be used to provide a file containing a list of paths of crawled 
web sites.

* **NODOM mode** This mode analyzes CSS files without using corresponding DOM states. One must provide a folder,
containing CSS files (with "css" extension) for analysis using `--infolder:"path/to/css/files/"`. Safe refactoring is 
not possible in this mode.

#### FP-Growth minsup
Using `--minsup`, one may provide minimum support count for FP-Growth, that is, the minimum number of selectors 
which have one or more duplicated declarations. The default value is 2.

### Detecting Mixin migration opportunities
**Note: This feature is under development**

This mode allows finding declaration-level duplication and suggesting Mixins (currently, in Less syntax).
Use `--prep --infolder:"path/to/crawled/data" --outfolder:"path/to/output/folder"`, 
where `path/to/crawled/data` points to the previously-crawled data (using duplication-detector in the Crawl mode).
The migrated less files will be created in `path/to/output/folder`. 