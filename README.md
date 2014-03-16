#css-analyser

This tool is designed for analysis and safe refactoring of the CSS code.
Right now, it finds three different types of duplication in CSS files and safely refactores them.

I am developing this tool as an infrastructure for my research on CSS during my PhD studies.

##License

This project is licensed under the ["Apache License, Version 2.0"](https://github.com/crawljax/crawljax/blob/master/LICENSE).

##Usage

This tool supports three modes:

* **Crawl mode** In this mode, tool uses [Crawljax](https://github.com/crawljax/crawljax) 
to crawl web pages of the given url(s). Then it analyzes all the CSS files of the cawled web pages,
with respect to the collected DOM states. <br />
For using this mode, use `--mode:crawl --url:"http://to.be.analyzed" --outfolder:"path/to/analyzed/info/folder"`.
Tool will gather DOM states using Crawljax in the outfolder. It also creates a folder called css, in which 
all the CSS files are saved. 
<br />
It is also possible to use `--urlfile:"path/to/file"`, to provide a list of websites for analysis. Websites 
URLs must be given one per line in this file.

* **Folder mode** If a previous data from crawling is available, one may use
`--mode:folder --infolder:"path/to/crawled/data"` to avoid re-crawling of the web pages. Also, the parameter
`--foldersfile:"path/to/list/of/folders"` could be used to provide a file containing a list of paths of crawled 
web sites.

* **NODOM mode** This mode analyzes CSS files without using corresponding DOM states. One must provide a folder,
containing CSS files (with "css" extension) for analysis using `--infolder:"path/to/css/files/"`. Safe refactoring is 
not possible in this mode.

### FP-Growth minsup
Using `--minsup`, one may provide minimum support for FP-Growth, that is, the minimum number of selectors 
which have one or more duplicated declarations. The default value is 2.
